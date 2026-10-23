package api.tests;

import api.models.LoginUserRequest;
import api.models.UserRequest;
import common.configs.Config;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpHeaders;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import base.BaseTest;


public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {

        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username(Config.getProperty("admin.username"))
                .password(Config.getProperty("admin.password"))
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(loginUserRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {

        // create user
        UserRequest userRequest = AdminSteps.createUser();

        // user generates auth token
        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .header(HttpHeaders.AUTHORIZATION, Matchers.notNullValue());
    }
}
