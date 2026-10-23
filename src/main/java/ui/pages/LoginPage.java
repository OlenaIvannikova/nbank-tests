package ui.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage<LoginPage> {

    private SelenideElement loginButton = $("button");

    @Override
    public String url() {
        return "/login";
    }

    public LoginPage login(String username, String password) {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        loginButton.click();
        return this;
    }
}
