package api.tests;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.AccountResponse;
import api.models.UserRequest;
import api.requests.steps.DataBaseSteps;
import common.annotations.APIVersion;
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

@APIVersion("with_database")
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

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());
        DaoAndModelAssertions.assertThat(account, accountDao).match();
    }
}
