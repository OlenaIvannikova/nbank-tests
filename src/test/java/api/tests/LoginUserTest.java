package api.tests;

import api.models.LoginUserRequest;
import api.models.UserRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpHeaders;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.configs.Config;
import common.configs.Property;
import common.generators.RandomModelGenerator;
import common.helpers.StepLogger;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class LoginUserTest extends BaseTest {

  @Test
  public void adminCanGenerateAuthTokenTest() {

    LoginUserRequest loginUserRequest =
        LoginUserRequest.builder()
            .username(Config.getProperty(Property.ADMIN_LOGIN))
            .password(Config.getProperty(Property.ADMIN_PASSWORD))
            .build();

    StepLogger.log(
        "Verify that administrator can generate authentication token",
        () ->
            new CrudRequester(
                    RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK())
                .post(loginUserRequest));
  }

  @Test
  public void userCanGenerateAuthTokenTest() {

    // create user
    UserRequest userRequest = AdminSteps.createUser();
    LoginUserRequest loginUserRequest = RandomModelGenerator.generate(LoginUserRequest.class);
    loginUserRequest.setUsername(userRequest.getUsername());
    loginUserRequest.setPassword(userRequest.getPassword());

    // user generates auth token
    StepLogger.log(
        "Verify that user can generate authentication token",
        () ->
            new CrudRequester(
                    RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK())
                .post(loginUserRequest)
                .header(HttpHeaders.AUTHORIZATION, Matchers.notNullValue()));
  }
}
