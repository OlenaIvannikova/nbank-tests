package api.constants.messages;

public enum ErrorMessage {

    UNAUTHORIZED_ACCOUNT_ACCESS("Unauthorized access to account"),
    ACCOUNT_ACCESS_FORBIDDEN("You do not have permission to access this account"),
    INVALID_TRANSFER("Invalid transfer: insufficient funds or invalid accounts");

    private final String value;

    ErrorMessage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}