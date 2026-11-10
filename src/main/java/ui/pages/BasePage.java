package ui.pages;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

import api.models.UserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.*;
import common.configs.Config;
import common.configs.Property;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.elements.BaseElement;

public abstract class BasePage<T extends BasePage> {

  protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
  protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
  protected SelenideElement selectAccountBox = $(Selectors.byClassName("account-selector"));
  protected SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));

  public abstract String url();

  public T open() {
    // открывает страницу по URL и возвращает объект соответствующего типа
    return Selenide.open(url(), (Class<T>) this.getClass());
  }

  public <T extends BasePage> T getPage(Class<T> pageClass) {
    return Selenide.page(pageClass);
  }

  public T checkAlertMessageAndAccept(String alertMessage) {
    Alert alert = getWait().until(ExpectedConditions.alertIsPresent());
    assertThat(alert.getText()).contains(alertMessage);
    alert.accept();
    return (T) this;
  }

  protected WebDriverWait getWait() {
    return new WebDriverWait(
        WebDriverRunner.getWebDriver(),
        Duration.ofSeconds(Long.valueOf(Config.getProperty(Property.EXPLICIT_WAIT_SECONDS))));
  }

  public static void authAsUser(String username, String password) {
    Selenide.open("/login");
    String authHeader = RequestSpecs.getAuthHeader(username, password);
    executeJavaScript("localStorage.setItem('authToken', arguments[0]);", authHeader);
  }

  public static void authAsUser(UserRequest user) {
    authAsUser(user.getUsername(), user.getPassword());
  }

  // коллекция Selenide-элементов ElementsCollection -> List<T>
  protected <T extends BaseElement> List<T> generatePageElements(
      ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
    return elementsCollection.stream()
        .map(constructor) // Превращает каждый Selenide-элементов в BaseElement объект Java
        .toList();
  }
}
