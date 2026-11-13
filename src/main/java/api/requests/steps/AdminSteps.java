package api.requests.steps;

import api.models.UserRequest;
import api.models.UserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.generators.RandomModelGenerator;
import common.helpers.StepLogger;
import java.util.List;

public class AdminSteps {

  public static UserRequest createUser() {
    UserRequest userRequest =
        StepLogger.log(
            "Generate random user data", () -> RandomModelGenerator.generate(UserRequest.class));

    return StepLogger.log(
        "Create user as administrator",
        () -> {
          new ValidatedCrudRequester<UserResponse>(
                  RequestSpecs.adminSpec(),
                  Endpoint.ADMIN_USER_CREATION,
                  ResponseSpecs.entityWasCreated())
              .post(userRequest);

          return userRequest;
        });
  }

  public static List<UserRequest> createTwoDifferentUsers() {
    UserRequest user1 = AdminSteps.createUser();
    UserRequest user2 = AdminSteps.createUser();

    return List.of(user1, user2);
  }

  public static List<UserResponse> getAllUsers() {
    return StepLogger.log(
        "Admin gets all users",
        () ->
            new ValidatedCrudRequester<UserResponse>(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_USER_RETRIEVAL,
                    ResponseSpecs.requestReturnsOK())
                .getAll(UserResponse[].class));
  }
}
