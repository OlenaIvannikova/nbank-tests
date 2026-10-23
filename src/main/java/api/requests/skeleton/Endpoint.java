package api.requests.skeleton;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
public enum Endpoint {

    // Accounts
    ACCOUNT_CREATION("/accounts", BaseModel.class, AccountResponse.class),
    TRANSFER("/accounts/transfer", TransferRequest.class, TransferResponse.class),
    DEPOSIT("/accounts/deposit", DepositRequest.class, AccountResponse.class),
    ACCOUNT_TRANSACTIONS("/accounts/{accountId}/transactions", BaseModel.class, Transaction.class),
    ACCOUNT_DELETION("/accounts", BaseModel.class, AccountDeletionResponse.class),

    // Admin
    ADMIN_USER_RETRIEVAL("/admin/users", BaseModel.class, UserResponse.class),
    ADMIN_USER_CREATION("/admin/users", UserRequest.class, UserResponse.class),
    ADMIN_USER_DELETION("/admin/users", BaseModel.class, BaseModel.class),

    // Authentication
    LOGIN("/auth/login", LoginUserRequest.class, LoginUserResponse.class),

    // Customer
    CUSTOMER_PROFILE_RETRIEVAL("/customer/profile", BaseModel.class, UserResponse.class),
    CUSTOMER_PROFILE_UPDATE("/customer/profile", ProfileRequest.class, ProfileResponse.class),
    CUSTOMER_ACCOUNTS("/customer/accounts", BaseModel.class, AccountResponse.class);

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;

    public List<String> getPathParamNames() {
        Matcher matcher = Pattern.compile("\\{([^{}]+)}")
                .matcher(url);

        List<String> paramNames = new ArrayList<>();

        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }

        return paramNames;
    }
}
