package requests.steps;

import models.UserRequest;
import models.UserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ProfileSteps {

    public static UserResponse getProfile(UserRequest user) {
        return new ValidatedCrudRequester<UserResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_RETRIEVAL,
                ResponseSpecs.requestReturnsOK())
                .get();
    }
}
