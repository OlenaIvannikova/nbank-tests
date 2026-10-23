package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class ProfilePage extends BasePage<ProfilePage> {

    private SelenideElement newUsernameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public ProfilePage editProfile(String newUsername) {
        newUsernameInput.clear();
        newUsernameInput.sendKeys(newUsername);
        saveChangesButton.click();
        return this;
    }

}
