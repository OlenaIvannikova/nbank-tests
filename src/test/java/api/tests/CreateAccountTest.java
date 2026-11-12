package api.tests;

import static org.assertj.core.api.Assertions.assertThat;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.AccountResponse;
import api.models.UserRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.annotations.APIVersion;
import common.helpers.StepLogger;
import common.utils.RetryUtils;
import java.util.List;
import org.junit.jupiter.api.Test;

@APIVersion("with_database")
public class CreateAccountTest extends BaseTest {

  @Test
  public void userCanCreateAccountTest() {
    // create user
    UserRequest user = AdminSteps.createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    AccountResponse account =
        StepLogger.log(
            "Create account for user",
            () ->
                ValidatedCrudRequester.<AccountResponse>authenticatedAsUser(
                        user.getUsername(),
                        user.getPassword(),
                        Endpoint.ACCOUNT_CREATION,
                        ResponseSpecs.entityWasCreated())
                    .post());

    // запросить все аккаунты пользователя и проверить, что наш аккаунт там
    List<AccountResponse> userAccounts =
        RetryUtils.retry(
            userSteps::getAccounts,
            accounts ->
                accounts != null
                    && accounts.stream().anyMatch(a -> a.getId().equals(account.getId())),
            5,
            1000);
    StepLogger.log(
        "Verify account id in user's account list",
        () ->
            assertThat(userAccounts).extracting(AccountResponse::getId).contains(account.getId()));

    AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());

    StepLogger.log(
        "Verify API account matches database account",
        () -> DaoAndModelAssertions.assertThat(account, accountDao).match());
  }
}
