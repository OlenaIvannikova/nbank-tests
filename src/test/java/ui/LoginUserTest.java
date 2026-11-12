package ui;

import api.models.UserRequest;
import api.requests.steps.AdminSteps;
import base.BaseUITest;
import com.codeborne.selenide.Condition;
import common.annotations.Browsers;
import common.helpers.StepLogger;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanelPage;
import ui.pages.LoginPage;
import ui.pages.UserDashboardPage;

public class LoginUserTest extends BaseUITest {

  @Test
  @Browsers({"chrome"})
  public void adminCanLoginWithCorrectDataTest() {
    UserRequest admin = new UserRequest().getAdmin();

    StepLogger.log(
        "Admin logs in with correct credentials and admin panel is displayed",
        () ->
            new LoginPage()
                .open()
                .login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanelPage.class)
                .getAdminPanelText()
                .shouldBe(Condition.visible));
  }

  @Test
  public void userCanLoginWithCorrectDataTest() {
    UserRequest user = StepLogger.log("Create a new user", () -> AdminSteps.createUser());

    StepLogger.log(
        "User logs in and welcome message is displayed with the default username",
        () ->
            new LoginPage()
                .open()
                .login(user.getUsername(), user.getPassword())
                .getPage(UserDashboardPage.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldHave(
                    Condition.text(
                        UserDashboardPage.WELCOME_PREFIX + UserDashboardPage.DEFAULT_NAME)));
  }
}
