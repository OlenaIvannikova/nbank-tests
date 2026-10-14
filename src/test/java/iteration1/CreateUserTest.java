package iteration1;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectData() {

        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of(
                        " ",
                        "Password33!",
                        "USER",
                        "username",
                        List.of(
                                "Username must contain only letters, digits, dashes, underscores, and dots",
                                "Username must be between 3 and 15 characters",
                                "Username cannot be blank"
                        )),
                Arguments.of(
                        "ab",
                        "Password33!",
                        "USER",
                        "username",
                        List.of(
                                "Username must be between 3 and 15 characters"
                        )),
                Arguments.of(
                        "abcdefghmknlopqws",
                        "Password33!",
                        "USER",
                        "username",
                        List.of(
                                "Username must be between 3 and 15 characters"
                        )),
                Arguments.of(
                        "abcdopqws$",
                        "Password33!",
                        "USER",
                        "username",
                        List.of(
                                "Username must contain only letters, digits, dashes, underscores, and dots"
                        ))

        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCannotCreateUserWithInvalidData(String username, String password, String role, String errorKey, List<String> errors) {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest(errorKey, errors))
                .post(userRequest);
    }
}

