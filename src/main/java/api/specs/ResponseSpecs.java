package api.specs;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import api.constants.messages.ErrorMessage;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import org.apache.http.HttpStatus;

// Готовые шаблоны для проверки ответов (ResponseSpecification)
public class ResponseSpecs {

  private ResponseSpecs() {}

  private static ResponseSpecBuilder defaultResponseBuilder() {
    return new ResponseSpecBuilder();
  }

  public static ResponseSpecification entityWasCreated() {
    return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_CREATED).build();
  }

  public static ResponseSpecification requestReturnsOK() {
    return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).build();
  }

  public static ResponseSpecification requestReturnsBadRequest(
      String errorKey, List<String> errorMessages) {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
        .expectBody(errorKey, containsInAnyOrder(errorMessages.toArray()))
        .build();
  }

  public static ResponseSpecification requestReturnsBadRequest(String errorMessage) {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
        .expectBody(equalTo(errorMessage))
        .build();
  }

  public static ResponseSpecification accountAccessForbidden() {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_FORBIDDEN)
        .expectBody(equalTo(ErrorMessage.UNAUTHORIZED_ACCOUNT_ACCESS.getValue()))
        .build();
  }

  public static ResponseSpecification accountAccessDenied() {
    return defaultResponseBuilder()
        .expectStatusCode(HttpStatus.SC_FORBIDDEN)
        .expectBody(equalTo(ErrorMessage.ACCOUNT_ACCESS_FORBIDDEN.getValue()))
        .build();
  }
}
