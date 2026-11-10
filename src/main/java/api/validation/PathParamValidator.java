package api.validation;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PathParamValidator {

  private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{([^/{}]+)}");

  private PathParamValidator() {}

  public static void validate(String url, Map<String, ?> pathParams) {
    Set<String> expectedParams = new LinkedHashSet<>();

    Matcher matcher = PATH_PARAM_PATTERN.matcher(url);

    while (matcher.find()) {
      expectedParams.add(matcher.group(1));
    }

    if (!expectedParams.equals(pathParams.keySet())) {
      throw new IllegalArgumentException(
          "Expected path parameters: " + expectedParams + ", but received: " + pathParams.keySet());
    }
  }
}
