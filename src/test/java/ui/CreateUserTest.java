package ui;

import base.BaseUITest;
import common.annotations.AdminSession;
import common.generators.RandomModelGenerator;
import api.models.UserRequest;
import api.models.UserResponse;
import org.junit.jupiter.api.Test;
import ui.elements.UserBage;
import ui.pages.AdminPanelPage;
import ui.pages.AlertMessage;

import java.util.List;

import static api.testdata.UserValidationTestData.SHORT_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static api.requests.steps.AdminSteps.getAllUsers;

public class CreateUserTest extends BaseUITest {

    @Test
    @AdminSession
    public void adminCanCreateUserTest() {

        // Админ создает нового user и проверяем, что user отображается на UI
        UserRequest newUser = RandomModelGenerator.generate(UserRequest.class);
        UserBage newUserBage = new AdminPanelPage()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(AlertMessage.USER_CREATED.getValue())
                .findUserByUsername(newUser.getUsername());

        assertThat(newUserBage)
                .as("UserBage should exist on Dashboard after user creation")
                .isNotNull();

        // Проверяем, что user создан на API
        List<UserResponse> users = getAllUsers();
        assertThat(users)
                .extracting(UserResponse::getUsername)
                .contains(newUser.getUsername());
    }

    @Test
    @AdminSession
    public void adminCannotCreateUserWithInvalidDataTest() {

        UserRequest newUser = RandomModelGenerator.generate(UserRequest.class);
        newUser.setUsername(SHORT_USERNAME);

        assertTrue(new AdminPanelPage()
                .open()
                .createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(AlertMessage.USER_CREATION_FAILED_INVALID_USERNAME.getValue())
                .getAllUsers()
                .stream()
                .noneMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        // Проверяем, что user не создан на API
        List<UserResponse> users = getAllUsers();
        assertThat(users)
                .extracting(UserResponse::getUsername)
                .doesNotContain(newUser.getUsername());
    }

}
