package base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
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
        Configuration.remote = Config.getProperty("uiRemote");
        Configuration.baseUrl = Config.getProperty("uiBaseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browserSize");
        Configuration.browserCapabilities
                .setCapability(
                        "selenoid:options",
                        Map.of(
                                "enableVNC", true, //Разрешить подключение к браузеру через VNC -> позволяет видеть браузер в режиме реального времени
                                "enableLog", true, // Сохранять лог браузерной сессии
                                "enableVideo", true
                        )
                );
    }

    @AfterEach
    public void closeWebDriver() {
        Selenide.closeWebDriver();
    }

}
