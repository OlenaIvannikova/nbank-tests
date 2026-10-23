package api.tests;

import common.generators.RandomModelGenerator;
import api.models.UserRequest;
import api.models.UserResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import base.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static api.requests.steps.AdminSteps.getAllUsers;


public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectDataTest() {

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
    public void adminCannotCreateUserWithInvalidDataTest(String username, String password, String role, String expectedErrorKey, List<String> expectedErrors) {

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

        List<UserResponse> users = getAllUsers();

        assertThat(users)
                .extracting(UserResponse::getUsername)
                .doesNotContain(userRequest.getUsername());
    }
}

