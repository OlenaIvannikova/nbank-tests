package api.tests;

import base.BaseTest;
import constants.messages.SuccessMessage;
import generators.RandomModelGenerator;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static requests.steps.ProfileSteps.getProfile;

public class ProfileUpdateTest extends BaseTest {

    @Test
    public void userCanUpdateProfileTest() {
        // create user
        UserRequest user = AdminSteps.createUser();

        ProfileRequest profileRequest = RandomModelGenerator.generate(ProfileRequest.class);

        ProfileResponse profileResponse = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.requestReturnsOK())
                .update(profileRequest)
                .extract()
                .as(ProfileResponse.class);

        softly.assertThat(profileResponse.getMessage())
                .isEqualTo(SuccessMessage.PROFILE_UPDATED.getValue());

        softly.assertThat(getProfile(user).getName())
                .as("name")
                .isEqualTo(profileRequest.getName());
    }

    @ParameterizedTest
    @MethodSource("api.testdata.ProfileValidationTestData#invalidProfileNameField")
    public void userCannotUpdateProfileWithInvalidNameFieldTest(String name, String expectedErrorMessage) {
        UserRequest user = AdminSteps.createUser();

        ProfileRequest profileRequest = ProfileRequest.builder()
                .name(name)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.requestReturnsBadRequest(expectedErrorMessage))
                .update(profileRequest);

        softly.assertThat(getProfile(user).getName())
                .as("name")
                .isNull();
    }
}
