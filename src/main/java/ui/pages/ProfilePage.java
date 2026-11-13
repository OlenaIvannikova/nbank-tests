package ui.pages;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;

public class ProfilePage extends BasePage<ProfilePage> {

  private SelenideElement newUsernameInput =
      $(Selectors.byAttribute("placeholder", "Enter new name"));
  private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

  @Override
  public String url() {
    return "/edit-profile";
  }

  public ProfilePage editProfile(String newUsername) {
    RetryUtils.retry(
        () -> {
          newUsernameInput.clear();
          newUsernameInput.setValue(newUsername);
          return newUsernameInput.getValue();
        },
        newUsername::equals,
        5,
        2000);
    saveChangesButton.click();
    return this;
  }
}
