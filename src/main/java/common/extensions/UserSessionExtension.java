package common.extensions;

import api.models.UserRequest;
import api.requests.steps.AdminSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

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
