package common.extensions;

import common.annotations.AdminSession;
import api.models.UserRequest;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

public class AdminSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Проверяем есть ли у теста аннотация AdminSession
        AdminSession annotation = context.getRequiredTestMethod().getAnnotation(AdminSession.class);
        if (annotation != null) { // если аннотация есть, то добавляем в local storage токен админа
            BasePage.authAsUser(UserRequest.getAdmin());
        }
    }
}
