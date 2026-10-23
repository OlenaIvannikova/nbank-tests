package ui;

import base.BaseUITest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import api.models.AccountResponse;
import org.junit.jupiter.api.Test;
import ui.pages.AlertMessage;
import ui.pages.UserDashboardPage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUITest {

    @Test
    @UserSession
    public void userCanCreateAccountTest() {

        new UserDashboardPage().open().createNewAccount();

        // Проверяем, что account создан на API
        List<AccountResponse> userAccounts = SessionStorage.getSteps().getAccounts();
        Long accountNumber = userAccounts.getFirst().getId();
        assertThat(userAccounts)
                .extracting(AccountResponse::getId)
                .contains(Long.valueOf(accountNumber));

        // Проверяем, что account создан на UI
        new UserDashboardPage().checkAlertMessageAndAccept(AlertMessage.ACCOUNT_CREATED.getValue() + userAccounts.getFirst().getAccountNumber());

    }
}
