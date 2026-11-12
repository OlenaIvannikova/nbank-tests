package api.requests.steps;

import api.models.*;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.generators.RandomModelGenerator;
import common.helpers.StepLogger;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import java.util.Map;

public class UserSteps {

  private String username;
  private String password;

  public UserSteps(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public AccountResponse createAccount() {
    return ValidatedCrudRequester.<AccountResponse>authenticatedAsUser(
            username, password, Endpoint.ACCOUNT_CREATION, ResponseSpecs.entityWasCreated())
        .post();
  }

  public AccountResponse createAccountWithNonZeroBalance() {
    AccountResponse account = createAccount();

    DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
    deposit.setId(account.getId());
    depositMoney(deposit, ResponseSpecs.requestReturnsOK());

    return getAccountById(account.getId());
  }

  public List<AccountResponse> createTwoAccountsWithNonZeroBalanceForUser() {
    return List.of(createAccountWithNonZeroBalance(), createAccountWithNonZeroBalance());
  }

  public static List<AccountResponse> createTwoAccountsWithNonZeroBalanceForDifferentUsers(
      List<UserRequest> users) {

    return List.of(
        new UserSteps(users.get(0).getUsername(), users.get(0).getPassword())
            .createAccountWithNonZeroBalance(),
        new UserSteps(users.get(1).getUsername(), users.get(1).getPassword())
            .createAccountWithNonZeroBalance());
  }

  public Response depositMoney(DepositRequest deposit, ResponseSpecification responseSpec) {

    return new CrudRequester(
            RequestSpecs.authAsUser(username, password), Endpoint.DEPOSIT, responseSpec)
        .post(deposit)
        .extract()
        .response();
  }

  public Response transferMoney(
      TransferRequest transferRequest, ResponseSpecification responseSpec) {
    return new CrudRequester(
            RequestSpecs.authAsUser(username, password), Endpoint.TRANSFER, responseSpec)
        .post(transferRequest)
        .extract()
        .response();
  }

  public Response transferWithFraudCheck(
      TransferRequest transferRequest, ResponseSpecification responseSpec) {

    return new CrudRequester(
            RequestSpecs.authAsUser(username, password),
            Endpoint.TRANSFER_WITH_FRAUD_CHECK,
            ResponseSpecs.requestReturnsOK())
        .post(transferRequest)
        .extract()
        .response();
  }

  public AccountResponse getAccountById(Long accountId) {
    return StepLogger.log(
        "User gets account by account id " + accountId,
        () ->
            new ValidatedCrudRequester<AccountResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.CUSTOMER_ACCOUNTS,
                    ResponseSpecs.requestReturnsOK())
                .getAll(AccountResponse[].class).stream()
                    .filter(account -> account.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(
                        () -> new IllegalStateException("Account not found: " + accountId)));
  }

  public List<AccountResponse> getAccounts() {
    return StepLogger.log(
        "Get all user accounts",
        () ->
            new ValidatedCrudRequester<AccountResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.CUSTOMER_ACCOUNTS,
                    ResponseSpecs.requestReturnsOK())
                .getAll(AccountResponse[].class));
  }

  public List<Transaction> getAccountTransactions(Long accountId) {
    return StepLogger.log(
        "Get all account transactions",
        () -> {
          Endpoint endpoint = Endpoint.ACCOUNT_TRANSACTIONS;
          String paramName = endpoint.getPathParamNames().getFirst();

          return new ValidatedCrudRequester<Transaction>(
                  RequestSpecs.authAsUser(username, password),
                  endpoint,
                  ResponseSpecs.requestReturnsOK())
              .getAllByPathParams(Map.of(paramName, accountId));
        });
  }

  public UserResponse getProfile() {
    return StepLogger.log(
        "Get updated user profile",
        () ->
            new ValidatedCrudRequester<UserResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.CUSTOMER_PROFILE_RETRIEVAL,
                    ResponseSpecs.requestReturnsOK())
                .get());
  }
}
