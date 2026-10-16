package api.testdata;

import generators.RandomModelGenerator;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static constants.messages.ValidationMessage.*;

public class ProfileValidationTestData {

    private static final String BLANK_NAME = " ";
    private static final String ONE_WORD_NAME =
            (String) RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class);

    private static final String THREE_WORDS_NAME =
            RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class)
                    + " "
                    + RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class)
                    + " "
                    + RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class);

    private static final String NAME_WITH_DIGITS =
            RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class)
                    + "123 "
                    + RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class);

    private static final String NAME_WITH_SPECIAL_CHAR =
            RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class)
                    + "@"
                    + RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class);

    private static final String NAME_WITH_DOUBLE_SPACE =
            RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class)
                    + "  "
                    + RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class);

    private static final String NAME_WITH_LEADING_SPACE =
            " " + RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class);

    private static final String NAME_WITH_TRAILING_SPACE =
            RandomModelGenerator.generateFromRegex("[A-Za-z]{3,10}", String.class) + " ";

    public static Stream<Arguments> invalidProfileNameField() {
        return Stream.of(
                Arguments.of(
                        BLANK_NAME,
                        NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE.getValue()
                ),
                // только одно слово
                Arguments.of(
                        ONE_WORD_NAME,
                        NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE.getValue()
                ),
                // три слова
                Arguments.of(
                        THREE_WORDS_NAME,
                        NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE.getValue()
                ),
                // c цифрами
                Arguments.of(
                        NAME_WITH_DIGITS,
                        NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE.getValue()
                ),
                // спецсимвол
                Arguments.of(
                        NAME_WITH_SPECIAL_CHAR,
                        NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE.getValue()
                ),
                // два пробела подряд
                Arguments.of(
                        NAME_WITH_DOUBLE_SPACE,
                        NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE.getValue()
                ),
                // пробел в начале
                Arguments.of(
                        NAME_WITH_LEADING_SPACE,
                        NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE.getValue()
                ),
                // пробел в конце
                Arguments.of(
                        NAME_WITH_TRAILING_SPACE,
                        NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE.getValue()
                )
        );

    }
}

