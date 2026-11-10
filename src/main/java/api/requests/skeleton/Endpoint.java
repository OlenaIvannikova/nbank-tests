package api.requests.skeleton;

import api.models.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {

  // Accounts
  ACCOUNT_CREATION("/accounts", BaseModel.class, AccountResponse.class),
  TRANSFER("/accounts/transfer", TransferRequest.class, TransferResponse.class),
  TRANSFER_WITH_FRAUD_CHECK(
      "/accounts/transfer-with-fraud-check", TransferRequest.class, TransferResponse.class),
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
    Matcher matcher = Pattern.compile("\\{([^{}]+)}").matcher(url);

    List<String> paramNames = new ArrayList<>();

    while (matcher.find()) {
      paramNames.add(matcher.group(1));
    }

    return paramNames;
  }

  /*
  Endpoint должен явно определять,
  создаёт ли он удаляемую сущность и каким endpoint она удаляется
  Т.к. POST /accounts/deposit и POST /accounts/transfer — это операции,
  а не создание самостоятельных ресурсов,
  которые можно удалить через DELETE на том же endpoint.
  */
  public Optional<Endpoint> getDeletionEndpoint() {
    return switch (this) {
      case ACCOUNT_CREATION -> Optional.of(ACCOUNT_DELETION);

      case ADMIN_USER_CREATION -> Optional.of(ADMIN_USER_DELETION);

      default -> Optional.empty();
    };
  }
}
