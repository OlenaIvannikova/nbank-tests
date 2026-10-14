package iteration1;

import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;


public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        // create user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // user creates account
        new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);
    }
}
