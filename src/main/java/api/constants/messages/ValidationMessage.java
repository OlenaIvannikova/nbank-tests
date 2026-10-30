package api.constants.messages;

public enum ValidationMessage {

    // Username validation
    USERNAME_INVALID_CHARACTERS(
            "Username must contain only letters, digits, dashes, underscores, and dots"
    ),
    USERNAME_INVALID_LENGTH(
            "Username must be between 3 and 15 characters"
    ),
    USERNAME_BLANK(
            "Username cannot be blank"
    ),

    // Deposit validation
    DEPOSIT_AMOUNT_TOO_SMALL(
            "Invalid account or amount"
    ),
    DEPOSIT_AMOUNT_EXCEEDS_LIMIT("Deposit amount exceeds the 5000 limit"),

    // Transfer validation
    TRANSFER_AMOUNT_EXCEEDS_LIMIT("Transfer amount cannot exceed 10000"),
    TRANSFER_AMOUNT_TOO_SMALL("Invalid transfer: insufficient funds or invalid accounts"),

    // Profile validation
    NAME_MUST_CONTAIN_TWO_WORDS_SEPARATED_BY_SPACE(
            "Name must contain two words with letters only"
    );

    private final String value;

    ValidationMessage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}