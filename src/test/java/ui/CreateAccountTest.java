package ui;

import static org.assertj.core.api.Assertions.assertThat;

import api.models.AccountResponse;
import api.requests.steps.UserSteps;
import base.BaseUITest;
import common.annotations.APIVersion;
import common.annotations.UserSession;
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
        new UserDashboardPage().open().createNewAccount().checkAlertMessageAndAccept();

    // Ждём появления нового аккаунта в API
    List<AccountResponse> userAccounts =
        RetryUtils.retry(
            userSteps::getAccounts,
            accounts ->
                accounts != null
                    && accounts.stream().anyMatch(a -> !existingAccountIds.contains(a.getId())),
            5,
            1000);

    // Получаем именно новый аккаунт через API
    AccountResponse createdAccount =
        userAccounts.stream()
            .filter(a -> !existingAccountIds.contains(a.getId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("New account was not found in API"));

    // Проверяем, что новый аккаунт действительно появился в API
    assertThat(createdAccount.getId()).isNotIn(existingAccountIds);

    // Проверяем на UI
    assertThat(alertText)
        .isEqualTo(AlertMessage.ACCOUNT_CREATED.getValue() + createdAccount.getAccountNumber());
  }
}
