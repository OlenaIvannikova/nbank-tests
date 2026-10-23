package api.tests;

import api.models.AccountResponse;
import api.models.UserRequest;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;

import base.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        // create user
        UserRequest user = AdminSteps.createUser();

        // user creates account
        AccountResponse account = ValidatedCrudRequester.<AccountResponse>authenticatedAsUser(
                user.getUsername(),
                user.getPassword(),
                Endpoint.ACCOUNT_CREATION,
                ResponseSpecs.entityWasCreated()
        ).post();

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там
        List<AccountResponse> userAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAccounts();

        assertThat(userAccounts)
                .extracting(AccountResponse::getId)
                .contains(account.getId());
    }
}
