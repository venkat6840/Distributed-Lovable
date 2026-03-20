package com.coding.distributed_lovable.workspace_service.service.impl;

import com.coding.distributed_lovable.workspace_service.dto.deploy.DeployResponse;
import com.coding.distributed_lovable.workspace_service.service.DeploymentService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class KubernetesDeploymentServiceImpl implements DeploymentService {

  private final KubernetesClient kubernetesClient;
  private final StringRedisTemplate stringRedisTemplate;

  @Value("${app.preview.namespace}")
  private String namespace;

  @Value("${app.preview.domain}")
  private String baseDomain;

  @Value("${app.preview.proxy-port}")
  private String proxyPort;

  private static final String POOL_LABEL = "status";
  private static final String PROJECT_LABEL = "project-id";
  private static final String IDLE = "idle";
  private static final String BUSY = "busy";

  @Override
  public DeployResponse deploy(Long projectId) {
    String domain = "project-" + projectId + ".app.domain.com";
    Pod existingPod = findActivePod(projectId);
    if (existingPod != null) {
      // the below url works when reverse proxy is configured
      registerRoutes(domain, existingPod);
      return new DeployResponse("http://" + domain + ":" + proxyPort);
    }
    return claimAndStartNewPod(projectId, domain);
  }

  private DeployResponse claimAndStartNewPod(Long projectId, String domain) {
    Pod pod =
        kubernetesClient
            .pods()
            .inNamespace(namespace)
            .withLabel(POOL_LABEL, IDLE)
            .list()
            .getItems()
            .stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "No idle containers are available. Please scaleup the runner-pool"));
    String podName = pod.getMetadata().getName();
    log.info("claiming pod {} for project {}", podName, projectId);

    kubernetesClient
        .pods()
        .inNamespace(namespace)
        .withName(podName)
        .edit(
            p -> {
              p.getMetadata().getLabels().put(POOL_LABEL, BUSY);
              p.getMetadata().getLabels().put(PROJECT_LABEL, projectId.toString());
              return p;
            });

    try {
      /*
      Syncer commands
      */
      String initialSyncCmd =
          String.format("mc mirror --overwrite myminio/projects/%d/ /app", projectId);

      log.info("Starting initial sync for project {} in pod {}", projectId, podName);
      execCommand(podName, "syncer", "sh", "-c", initialSyncCmd);

      // Watch for any change in minio bucket folders, if yes try to overwrite in /app folder of
      // syncer container
      String watchCmd =
          String.format(
              "nohup mc mirror --overwrite --watch myminio/projects/%d/ /app/ > /app/sync.log 2>&1 &",
              projectId);
      execCommand(podName, "syncer", "sh", "-c", watchCmd);

      /*
       Runner commands
      */
      String startCmd =
          "npm install && nohup npm run dev -- --host 0.0.0.0 --port 5173 > /app/dev.log 2>&1 &";

      log.info("Starting dev server for project {}...", projectId);
      execCommand(podName, "runner", "sh", "-c", startCmd);

      registerRoutes(domain, pod);
      log.info("Deployment successful: http://{}:{}", domain, proxyPort);
      return new DeployResponse("http://" + domain + ":" + proxyPort);
    } catch (Exception e) {
      log.error("Deployment failed for project {}. Releasing pod {}.", projectId, podName, e);
      kubernetesClient.pods().inNamespace(namespace).withName(podName).delete();
      throw new RuntimeException("Failed to deploy the project with id: " + projectId);
    }
  }

  private void execCommand(String podName, String container, String... command) {
    log.debug("Exec in {}:{} -> {}", podName, container, String.join(" ", command));

    CompletableFuture<String> data = new CompletableFuture<>();
    try (ExecWatch ignored =
        kubernetesClient
            .pods()
            .inNamespace(namespace)
            .withName(podName)
            .inContainer(container)
            .writingOutput(new ByteArrayOutputStream())
            .writingError(new ByteArrayOutputStream())
            .usingListener(
                new ExecListener() {
                  @Override
                  public void onClose(int code, String reason) {
                    data.complete("Done");
                  }
                })
            .exec(command)) {

      // Wait briefly to ensure command fired (Fabric8 exec is async)
      // For long running background jobs (nohup), we don't wait for "Done"
      if (command[command.length - 1].trim().endsWith("&")) {
        Thread.sleep(500);
      } else {
        data.get(30, TimeUnit.SECONDS); // Block for synchronous setup commands (npm install)
      }

    } catch (Exception e) {
      log.error("Exec failed", e);
      throw new RuntimeException("Pod Execution Failed", e);
    }
  }

  private void registerRoutes(String domain, Pod pod) {

    String podIp = pod.getStatus().getPodIP();
    if (podIp == null) {
      throw new RuntimeException("Pod is running, but has no IP");
    }
    stringRedisTemplate.opsForValue().set("route:" + domain, podIp + ":5173", 6, TimeUnit.HOURS);
  }

  private Pod findActivePod(Long projectId) {
    return kubernetesClient
        .pods()
        .inNamespace(namespace)
        .withLabel(PROJECT_LABEL, projectId.toString())
        .withLabel(POOL_LABEL, BUSY) // only find active/busy ones
        .list()
        .getItems()
        .stream()
        .filter(pod -> pod.getStatus().getPhase().equals("Running"))
        .findFirst()
        .orElse(null);
  }
}
