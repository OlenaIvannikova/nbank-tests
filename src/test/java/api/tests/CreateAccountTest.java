package api.tests;

import api.models.AccountResponse;
import api.models.UserRequest;
import common.utils.RetryUtils;
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
        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

        // user creates account
        AccountResponse account = ValidatedCrudRequester.<AccountResponse>authenticatedAsUser(
                user.getUsername(),
                user.getPassword(),
                Endpoint.ACCOUNT_CREATION,
                ResponseSpecs.entityWasCreated()
        ).post();

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там
        List<AccountResponse> userAccounts = RetryUtils.retry(
                userSteps::getAccounts,
                accounts -> accounts != null && accounts.stream()
                        .anyMatch(a -> a.getId().equals(account.getId())),
                5,
                1000
        );
        assertThat(userAccounts)
                .extracting(AccountResponse::getId)
                .contains(account.getId());
    }
}
