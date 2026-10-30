package api.tests;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.requests.steps.DataBaseSteps;
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

        UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
        DaoAndModelAssertions.assertThat(userResponse, userDao).match();
    }

    @ParameterizedTest
    @MethodSource("api.testdata.UserValidationTestData#invalidUsers")
    public void adminCannotCreateUserWithInvalidDataTest(String username, String password, String role, String expectedErrorKey, List<String> expectedErrors) {

        UserRequest userRequest = RandomModelGenerator.generate(UserRequest.class);
        userRequest.setUsername(username);
        userRequest.setPassword(password);
        userRequest.setRole(role);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER_CREATION,
                ResponseSpecs.requestReturnsBadRequest(expectedErrorKey, expectedErrors))
                .post(userRequest);

        List<UserResponse> users = getAllUsers();
        assertThat(users)
                .extracting(UserResponse::getUsername)
                .doesNotContain(userRequest.getUsername());

        assertThat(DataBaseSteps.getUserByUsername(userRequest.getUsername())).isNull();
    }
}

