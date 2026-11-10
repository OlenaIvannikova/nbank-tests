package ui;

import static api.testdata.ProfileValidationTestData.ONE_WORD_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static ui.pages.AlertMessage.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY;
import static ui.pages.AlertMessage.USERNAME_UPDATED;

import api.models.ProfileRequest;
import base.BaseUITest;
import common.annotations.UserSession;
import common.generators.RandomModelGenerator;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.ProfilePage;

public class ProfileUpdateTest extends BaseUITest {

  @Test
  @UserSession
  public void userCanUpdateProfileTest() {
    ProfileRequest profileRequest = RandomModelGenerator.generate(ProfileRequest.class);

    new ProfilePage()
        .open()
        .editProfile(profileRequest.getName())
        .checkAlertMessageAndAccept(USERNAME_UPDATED.getValue());

    // Проверяем, что username отредактирован на API
    assertThat(SessionStorage.getSteps().getProfile().getName())
        .as("name")
        .isEqualTo(profileRequest.getName());
  }

  @Test
  @UserSession
  public void userCannotUpdateProfileWithInvalidDataTest() {

    ProfileRequest profileRequest = RandomModelGenerator.generate(ProfileRequest.class);
    profileRequest.setName(ONE_WORD_NAME);

    new ProfilePage()
        .open()
        .editProfile(profileRequest.getName())
        .checkAlertMessageAndAccept(NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getValue());

    // Проверяем, что username не отредактирован на API
    assertThat(SessionStorage.getSteps().getProfile().getName()).as("name").isNull();
  }
}
