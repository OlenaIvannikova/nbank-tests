package ui;

import static api.requests.skeleton.Endpoint.ADMIN_USER_DELETION;
import static api.requests.steps.AdminSteps.getAllUsers;
import static api.testdata.UserValidationTestData.SHORT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.models.DeletionCredentials;
import api.models.UserRequest;
import api.models.UserResponse;
import base.BaseUITest;
import common.annotations.AdminSession;
import common.generators.RandomModelGenerator;
import common.generators.TestDataStorage;
import common.helpers.StepLogger;
import java.util.List;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanelPage;
import ui.pages.AlertMessage;

public class CreateUserTest extends BaseUITest {

  @Test
  @AdminSession
  public void adminCanCreateUserTest() {

    // Админ создает нового user и проверяем, что user отображается на UI
    UserRequest newUser = RandomModelGenerator.generate(UserRequest.class);
    StepLogger.log(
        "Admin creates a new user through UI",
        () ->
            new AdminPanelPage()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(AlertMessage.USER_CREATED.getValue()));

    // Проверяем создание пользователя через API
    UserResponse createdUser =
        StepLogger.log(
            "Verify that the created user exists through API",
            () -> {
              List<UserResponse> users = getAllUsers();
              return users.stream()
                  .filter(user -> user.getUsername().equals(newUser.getUsername()))
                  .findFirst()
                  .orElseThrow(() ->
                      new AssertionError(
                          "User was not found through API: " + newUser.getUsername()));
            });

    // Регистрируем пользователя для удаления в @AfterEach
    TestDataStorage.getStorage()
        .addCreatedEntity(ADMIN_USER_DELETION, createdUser, DeletionCredentials.admin());
  }

  @Test
  @AdminSession
  public void adminCannotCreateUserWithInvalidDataTest() {

    UserRequest newUser =
        StepLogger.log(
            "Generate test data with invalid username",
            () -> {
              UserRequest user = RandomModelGenerator.generate(UserRequest.class);
              user.setUsername(SHORT_USERNAME);
              return user;
            });

    StepLogger.log(
        "Admin cannot create a user with invalid username through UI",
        () ->
            assertTrue(
                new AdminPanelPage()
                        .open()
                        .createUser(newUser.getUsername(), newUser.getPassword())
                        .checkAlertMessageAndAccept(
                            AlertMessage.USER_CREATION_FAILED_INVALID_USERNAME.getValue())
                        .getAllUsers()
                        .stream()
                        .noneMatch(
                            userBage -> userBage.getUsername().equals(newUser.getUsername()))));

    // Проверяем, что user не создан на API
    StepLogger.log(
        "Verify that the user was not created through API",
        () -> {
          List<UserResponse> users = getAllUsers();
          assertThat(users)
              .extracting(UserResponse::getUsername)
              .doesNotContain(newUser.getUsername());
        });
  }
}
