package api.tests;

import models.AccountResponse;
import models.UserRequest;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.ResponseSpecs;

import base.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static requests.steps.UserSteps.getAccounts;


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
        List<AccountResponse> userAccounts = getAccounts(user);

        assertThat(userAccounts)
                .extracting(AccountResponse::getId)
                .contains(account.getId());
    }
}
