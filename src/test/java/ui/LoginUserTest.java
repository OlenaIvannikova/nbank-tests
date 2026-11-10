package ui;

import api.models.UserRequest;
import api.requests.steps.AdminSteps;
import base.BaseUITest;
import com.codeborne.selenide.Condition;
import common.annotations.Browsers;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanelPage;
import ui.pages.LoginPage;
import ui.pages.UserDashboardPage;

public class LoginUserTest extends BaseUITest {

  @Test
  @Browsers({"chrome"})
  public void adminCanLoginWithCorrectDataTest() {
    UserRequest admin = new UserRequest().getAdmin();

    new LoginPage()
        .open()
        .login(admin.getUsername(), admin.getPassword())
        .getPage(AdminPanelPage.class)
        .getAdminPanelText()
        .shouldBe(Condition.visible);
  }

  @Test
  public void userCanLoginWithCorrectDataTest() {
    UserRequest user = AdminSteps.createUser();

    new LoginPage()
        .open()
        .login(user.getUsername(), user.getPassword())
        .getPage(UserDashboardPage.class)
        .getWelcomeText()
        .shouldBe(Condition.visible)
        .shouldHave(
            Condition.text(UserDashboardPage.WELCOME_PREFIX + UserDashboardPage.DEFAULT_NAME));
  }
}
