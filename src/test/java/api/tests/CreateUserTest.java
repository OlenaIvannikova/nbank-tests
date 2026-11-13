package api.tests;

import static api.requests.steps.AdminSteps.getAllUsers;
import static org.assertj.core.api.Assertions.assertThat;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.UserRequest;
import api.models.UserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.DataBaseSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.generators.RandomModelGenerator;
import common.helpers.StepLogger;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CreateUserTest extends BaseTest {

  @Test
  public void adminCanCreateUserWithCorrectDataTest() {

    UserRequest userRequest = RandomModelGenerator.generate(UserRequest.class);

    UserResponse userResponse =
        StepLogger.log(
            "Create user as administrator",
            () ->
                new ValidatedCrudRequester<UserResponse>(
                        RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER_CREATION,
                        ResponseSpecs.entityWasCreated())
                    .post(userRequest));

    StepLogger.log(
        "Verify created user data",
        () -> ModelAssertions.assertThatModels(userRequest, userResponse).match());

    List<UserResponse> users = StepLogger.log("Get all users", () -> getAllUsers());

    StepLogger.log(
        "Verify user appears in users list",
        () ->
            assertThat(users)
                .extracting(UserResponse::getUsername)
                .contains(userRequest.getUsername()));

    UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
    StepLogger.log(
        "Verify API user matches database user",
        () -> DaoAndModelAssertions.assertThat(userResponse, userDao).match());
  }

  @ParameterizedTest
  @MethodSource("api.testdata.UserValidationTestData#invalidUsers")
  public void adminCannotCreateUserWithInvalidDataTest(
      String username,
      String password,
      String role,
      String expectedErrorKey,
      List<String> expectedErrors) {

    UserRequest userRequest =
        StepLogger.log(
            "Prepare user data with invalid parameters",
            () -> {
              UserRequest request = RandomModelGenerator.generate(UserRequest.class);
              request.setUsername(username);
              request.setPassword(password);
              request.setRole(role);
              return request;
            });

    StepLogger.log(
        "Verify administrator cannot create user with invalid data",
        () ->
            new CrudRequester(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_USER_CREATION,
                    ResponseSpecs.requestReturnsBadRequest(expectedErrorKey, expectedErrors))
                .post(userRequest));

    List<UserResponse> users = StepLogger.log("Get all users", () -> getAllUsers());

    StepLogger.log(
        "Verify user does not appear in users list",
        () ->
            assertThat(users)
                .extracting(UserResponse::getUsername)
                .doesNotContain(userRequest.getUsername()));

    UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());

    StepLogger.log("Verify user was not saved in database", () -> assertThat(userDao).isNull());
  }
}
