package common.extensions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

  private static final Logger log = LoggerFactory.getLogger(TimingExtension.class);

  private final Map<String, Long> startTimes = new ConcurrentHashMap<>();

  @Override
  public void beforeTestExecution(ExtensionContext extensionContext) {

    String testId = extensionContext.getUniqueId();
    long startTime = System.currentTimeMillis();

    startTimes.put(testId, startTime);

    log.info(
        "Thread {}: Test started: {} at {}",
        Thread.currentThread().getName(),
        extensionContext.getDisplayName(),
        java.time.Instant.ofEpochMilli(startTime));
  }

  @Override
  public void afterTestExecution(ExtensionContext extensionContext) {

    String testId = extensionContext.getUniqueId();
    Long startTime = startTimes.remove(testId);

    if (startTime == null) {
      log.warn("Start time not found for test: {}", testId);
      return;
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    log.info(
        "Thread {}: Test finished: {} at {}; duration={} ms",
        Thread.currentThread().getName(),
        extensionContext.getDisplayName(),
        java.time.Instant.ofEpochMilli(endTime),
        duration);
  }
}
