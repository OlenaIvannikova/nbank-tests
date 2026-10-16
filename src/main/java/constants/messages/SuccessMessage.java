package constants.messages;

public enum SuccessMessage {

    TRANSFER_SUCCESSFUL("Transfer successful"),
    PROFILE_UPDATED("Profile updated successfully"),
    USER_DELETED("User with ID %d deleted successfully."),
    ACCOUNT_DELETED("Account deleted successfully");


    private final String value;

    SuccessMessage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
