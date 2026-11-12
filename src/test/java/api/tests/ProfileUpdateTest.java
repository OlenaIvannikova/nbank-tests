package api.tests;

import api.constants.messages.SuccessMessage;
import api.models.ProfileRequest;
import api.models.ProfileResponse;
import api.models.UserRequest;
import api.models.UserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.generators.RandomModelGenerator;
import common.helpers.StepLogger;
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
        StepLogger.log(
            "Update user profile",
            () ->
                new CrudRequester(
                        RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                        Endpoint.CUSTOMER_PROFILE_UPDATE,
                        ResponseSpecs.requestReturnsOK())
                    .update(profileRequest)
                    .extract()
                    .as(ProfileResponse.class));

    StepLogger.log(
        "Verify updated profile data in response",
        () ->
            ModelAssertions.assertThatModels(profileRequest, profileResponse.getCustomer())
                .match());

    StepLogger.log(
        "Verify successful profile update message",
        () ->
            softly
                .assertThat(profileResponse.getMessage())
                .isEqualTo(SuccessMessage.PROFILE_UPDATED.getValue()));

    UserResponse updatedProfile =
        StepLogger.log("Get updated user profile", () -> userSteps.getProfile());

    StepLogger.log(
        "Verify that user profile was updated",
        () ->
            softly
                .assertThat(updatedProfile.getName())
                .as("name")
                .isEqualTo(profileRequest.getName()));
  }

  @ParameterizedTest
  @MethodSource("api.testdata.ProfileValidationTestData#invalidProfileNameField")
  public void userCannotUpdateProfileWithInvalidNameFieldTest(
      String name, String expectedErrorMessage) {
    UserRequest user = AdminSteps.createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    ProfileRequest profileRequest = RandomModelGenerator.generate(ProfileRequest.class);
    profileRequest.setName(name);

    StepLogger.log(
        "Verify that profile update is rejected for invalid name: " + name,
        () ->
            new CrudRequester(
                    RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                    Endpoint.CUSTOMER_PROFILE_UPDATE,
                    ResponseSpecs.requestReturnsBadRequest(expectedErrorMessage))
                .update(profileRequest));

    UserResponse profile =
        StepLogger.log("Get user profile after rejected update", userSteps::getProfile);

    StepLogger.log(
        "Verify that invalid name was not saved",
        () -> softly.assertThat(profile.getName()).as("name").isNull());
  }
}
