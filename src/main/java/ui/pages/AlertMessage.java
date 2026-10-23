package ui.pages;

import lombok.Getter;

@Getter
public enum AlertMessage {
    ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    USER_CREATED("✅ User created successfully!"),
    USER_CREATION_FAILED_INVALID_USERNAME("Username must be between 3 and 15 characters"),
    USERNAME_UPDATED("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY("Name must contain two words with letters only"),
    ACCOUNT_DEPOSITED("Successfully deposited $%s to account %s!"),
    DEPOSIT_AMOUNT_EXCEEDS_LIMIT("❌ Please deposit less or equal to 5000$."),
    DEPOSIT_AMOUNT_BLANK("❌ Please enter a valid amount."),
    NO_ACCOUNT_SELECTED("❌ Please select an account."),
    TRANSFER_SUCCESSFUL("✅ Successfully transferred $%s to account %s!"),
    NO_USER_FOUND_WITH_ACCOUNT_NUMBER("❌ No user found with this account number."),
    RECIPIENT_ACCOUNT_BLANK("❌ Please fill all fields and confirm."),
    TRANSFER_AMOUNT_EXCEEDS_LIMIT("❌ Error: Transfer amount cannot exceed 10000");

    private final String value;

    AlertMessage(String value) {
        this.value = value;
    }

    public String format(String amount, String accountNumber) {
        return String.format(value, amount, accountNumber);
    }
}
