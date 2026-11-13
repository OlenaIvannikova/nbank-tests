package ui;

import static org.assertj.core.api.Assertions.assertThat;

import api.models.AccountResponse;
import api.requests.steps.UserSteps;
import base.BaseUITest;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.helpers.StepLogger;
import common.storage.SessionStorage;
import common.utils.RetryUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import ui.pages.AlertMessage;
import ui.pages.UserDashboardPage;

@APIVersion("with_database")
public class CreateAccountTest extends BaseUITest {

  @Test
  @UserSession
  public void userCanCreateAccountTest() {

    UserSteps userSteps = SessionStorage.getSteps();

    // Запоминаем ID аккаунтов до создания
    Set<Long> existingAccountIds =
        userSteps.getAccounts().stream().map(AccountResponse::getId).collect(Collectors.toSet());

    // Создаём аккаунт через UI
    String alertText =
        StepLogger.log(
            "User creates a new account through UI",
            () -> new UserDashboardPage().open().createNewAccount().checkAlertMessageAndAccept());

    // Ждём появления нового аккаунта в API
    List<AccountResponse> userAccounts =
        StepLogger.log(
            "Wait until the new account appears in API",
            () ->
                RetryUtils.retry(
                    userSteps::getAccounts,
                    accounts ->
                        accounts != null
                            && accounts.stream()
                                .anyMatch(a -> !existingAccountIds.contains(a.getId())),
                    5,
                    1000));

    // Получаем именно новый аккаунт через API
    AccountResponse createdAccount =
        StepLogger.log(
            "Get the newly created account from API",
            () ->
                userAccounts.stream()
                    .filter(a -> !existingAccountIds.contains(a.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("New account was not found in API")));

    // Проверяем, что новый аккаунт действительно появился в API
    StepLogger.log(
        "Verify that the new account has a unique ID",
        () -> assertThat(createdAccount.getId()).isNotIn(existingAccountIds));

    // Проверяем на UI
    StepLogger.log(
        "Verify account creation success message on UI",
        () ->
            assertThat(alertText)
                .isEqualTo(
                    AlertMessage.ACCOUNT_CREATED.getValue() + createdAccount.getAccountNumber()));
  }
}
