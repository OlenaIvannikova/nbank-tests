package base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import common.configs.Property;
import common.extensions.AdminSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.UserSessionExtension;
import common.configs.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@ExtendWith({AdminSessionExtension.class, UserSessionExtension.class, BrowserMatchExtension.class})
public class BaseUITest extends BaseTest {

    @BeforeAll
    public static void setUpSelenoid() {
        Configuration.remote = Config.getProperty(Property.UI_REMOTE);
        Configuration.baseUrl = Config.getProperty(Property.UI_BASE_URL);
        Configuration.browser = Config.getProperty(Property.BROWSER);
        Configuration.browserSize = Config.getProperty(Property.BROWSER_SIZE);
        // запуск тестов в фоновом (headless) режиме
        Configuration.headless = true;
        Configuration.browserCapabilities
                .setCapability(
                        "selenoid:options",
                        Map.of(
                                "enableVNC", true, //Разрешить подключение к браузеру через VNC -> позволяет видеть браузер в режиме реального времени
                                "enableLog", true, // Сохранять лог браузерной сессии
                                "enableVideo", false
                        )
                );
    }

    @AfterEach
    public void closeWebDriver() {
        Selenide.closeWebDriver();
    }
}
