package requests.steps;

import generators.RandomModelGenerator;
import models.UserRequest;
import models.UserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class AdminSteps {

    public static UserRequest createUser() {
        UserRequest userRequest =
                RandomModelGenerator.generate(UserRequest.class);

        new ValidatedCrudRequester<UserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER_CREATION,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        return userRequest;
    }

    public static List<UserRequest> createTwoDifferentUsers() {

        UserRequest user1 = AdminSteps.createUser();
        UserRequest user2 = AdminSteps.createUser();

        return List.of(user1, user2);
    }

    public static List<UserResponse> getAllUsers() {
        return new ValidatedCrudRequester<UserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER_RETRIEVAL,
                ResponseSpecs.requestReturnsOK())
                .getAll(UserResponse.class);
    }
}
