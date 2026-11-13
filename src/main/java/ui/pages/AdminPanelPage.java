package ui.pages;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.*;
import common.helpers.StepLogger;
import common.utils.RetryUtils;
import java.util.List;
import lombok.Getter;
import ui.elements.UserBage;

@Getter
public class AdminPanelPage extends BasePage<AdminPanelPage> {

  private SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));
  private SelenideElement addUserButton = $(Selectors.byText("Add User"));
  private final SelenideElement allUsersHeading = $(Selectors.byText("All Users"));

  @Override
  public String url() {
    return "/admin";
  }

  public AdminPanelPage createUser(String username, String password) {
    usernameInput.sendKeys(username);
    passwordInput.sendKeys(password);
    addUserButton.click();
    return this;
  }

  public List<UserBage> getAllUsers() {
    return StepLogger.log(
        "Get all users from Dashboard",
        () -> {
          ElementsCollection elementsCollection = allUsersHeading.parent().findAll("li");
          return generatePageElements(elementsCollection, UserBage::new);
        });
  }

  public UserBage findUserByUsername(String username) {
    return RetryUtils.retry(
        () ->
            getAllUsers().stream()
                .filter(it -> it.getUsername().equals(username))
                .findAny()
                .orElse(null),
        result -> result != null,
        3,
        1000);
  }
}
