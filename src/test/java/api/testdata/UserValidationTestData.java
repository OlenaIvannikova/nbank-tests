package api.testdata;

import common.generators.RandomModelGenerator;
import api.models.UserRole;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static api.constants.messages.ValidationMessage.*;

public class UserValidationTestData {

    private static final String VALID_PASSWORD = (String) RandomModelGenerator.generateFromRegex(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,20}$",
            String.class);
    private static final String USER_ROLE = UserRole.USER.name();
    private static final String USERNAME_FIELD = "username";

    private static final String BLANK_USERNAME = " ";
    public static final String SHORT_USERNAME = (String) RandomModelGenerator.generateFromRegex(
            "[A-Za-z]{1,2}", String.class);
    private static final String LONG_USERNAME = (String) RandomModelGenerator.generateFromRegex(
            "[A-Za-z]{16,20}", String.class);
    private static final String USERNAME_WITH_SPECIAL_CHAR = RandomModelGenerator.generateFromRegex(
            "[A-Za-z]{3,10}", String.class
    ) + "$";

    public static Stream<Arguments> invalidUsers() {
        return Stream.of(
                Arguments.of(
                        BLANK_USERNAME,
                        VALID_PASSWORD,
                        USER_ROLE,
                        USERNAME_FIELD,
                        List.of(
                                USERNAME_INVALID_CHARACTERS.getValue(),
                                USERNAME_INVALID_LENGTH.getValue(),
                                USERNAME_BLANK.getValue()
                        )
                ),
                Arguments.of(
                        SHORT_USERNAME,
                        VALID_PASSWORD,
                        USER_ROLE,
                        USERNAME_FIELD,
                        List.of(USERNAME_INVALID_LENGTH.getValue())
                ),
                Arguments.of(
                        LONG_USERNAME,
                        VALID_PASSWORD,
                        USER_ROLE,
                        USERNAME_FIELD,
                        List.of(USERNAME_INVALID_LENGTH.getValue())
                ),
                Arguments.of(
                        USERNAME_WITH_SPECIAL_CHAR,
                        VALID_PASSWORD,
                        USER_ROLE,
                        USERNAME_FIELD,
                        List.of(USERNAME_INVALID_CHARACTERS.getValue())
                )
        );
    }
}
