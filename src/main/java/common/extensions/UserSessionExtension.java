package common.extensions;

import common.annotations.UserSession;
import common.storage.SessionStorage;
import api.models.UserRequest;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import api.requests.steps.AdminSteps;
import ui.pages.BasePage;

import java.util.LinkedList;
import java.util.List;


public class UserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Проверяем есть ли у теста аннотация UserSession
        UserSession annotation = context.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            int userCount = annotation.value();
            SessionStorage.clear();

            // накапливаем созданных users
            List<UserRequest> users = new LinkedList<>();

            for (int i = 0; i < userCount; i++) {
                UserRequest user = AdminSteps.createUser();
                users.add(user);
            }

            SessionStorage.addUsers(users);

            int authAsUser = annotation.auth();
            // залогиниваемся с user-ом, номер которого передан в auth параметре UserSession аннотации
            BasePage.authAsUser(SessionStorage.getUser(authAsUser));
        }
    }
}
