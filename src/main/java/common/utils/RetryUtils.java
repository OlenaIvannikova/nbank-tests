package common.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Принимаем на вход общего ретрая: 1) что повторяем 2) условие выхода 3) максимальное количество
 * попыток 4) задержка между каждой попыткой
 */
public class RetryUtils {
  public static <T> T retry(
      Supplier<T> action, Predicate<T> condition, int maxAttempts, long delayMillis) {

    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be at least 1");
    }

    RuntimeException lastException = null;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        T result = action.get();

        if (condition.test(result)) {
          return result;
        }
      } catch (RuntimeException e) {
        lastException = e;
      }

      if (attempt < maxAttempts) {
        try {
          Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Retry interrupted", e);
        }
      }
    }

    throw new RuntimeException("Retry failed after " + maxAttempts + " attempts", lastException);
  }
}
