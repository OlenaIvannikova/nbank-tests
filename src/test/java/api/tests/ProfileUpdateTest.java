package api.tests;

import api.constants.messages.SuccessMessage;
import api.models.ProfileRequest;
import api.models.ProfileResponse;
import api.models.UserRequest;
import api.models.comparison.ModelAssertions;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.generators.RandomModelGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ProfileUpdateTest extends BaseTest {

  @Test
  public void userCanUpdateProfileTest() {
    // create user
    UserRequest user = AdminSteps.createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    ProfileRequest profileRequest = RandomModelGenerator.generate(ProfileRequest.class);

    ProfileResponse profileResponse =
        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.requestReturnsOK())
            .update(profileRequest)
            .extract()
            .as(ProfileResponse.class);

    ModelAssertions.assertThatModels(profileRequest, profileResponse.getCustomer()).match();

    softly
        .assertThat(profileResponse.getMessage())
        .isEqualTo(SuccessMessage.PROFILE_UPDATED.getValue());

    softly
        .assertThat(userSteps.getProfile().getName())
        .as("name")
        .isEqualTo(profileRequest.getName());
  }

  @ParameterizedTest
  @MethodSource("api.testdata.ProfileValidationTestData#invalidProfileNameField")
  public void userCannotUpdateProfileWithInvalidNameFieldTest(
      String name, String expectedErrorMessage) {
    UserRequest user = AdminSteps.createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    ProfileRequest profileRequest = RandomModelGenerator.generate(ProfileRequest.class);
    profileRequest.setName(name);

    new CrudRequester(
            RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
            Endpoint.CUSTOMER_PROFILE_UPDATE,
            ResponseSpecs.requestReturnsBadRequest(expectedErrorMessage))
        .update(profileRequest);

    softly.assertThat(userSteps.getProfile().getName()).as("name").isNull();
  }
}
