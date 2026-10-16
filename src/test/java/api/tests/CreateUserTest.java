package api.tests;

import generators.RandomModelGenerator;
import models.UserRequest;
import models.UserResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import base.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static requests.steps.AdminSteps.getAllUsers;


public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectData() {

        UserRequest userRequest = RandomModelGenerator.generate(UserRequest.class);

        UserResponse userResponse = new ValidatedCrudRequester<UserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER_CREATION,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        ModelAssertions.assertThatModels(userRequest, userResponse).match();

        List<UserResponse> users = getAllUsers();

        assertThat(users)
                .extracting(UserResponse::getUsername)
                .contains(userRequest.getUsername());
    }

    @ParameterizedTest
    @MethodSource("api.testdata.UserValidationTestData#invalidUsers")
    public void adminCannotCreateUserWithInvalidData(String username, String password, String role, String expectedErrorKey, List<String> expectedErrors) {

        UserRequest userRequest = UserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER_CREATION,
                ResponseSpecs.requestReturnsBadRequest(expectedErrorKey, expectedErrors))
                .post(userRequest);
    }
}

