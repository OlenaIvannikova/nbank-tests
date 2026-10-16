package requests.steps;

import generators.RandomModelGenerator;
import models.AccountResponse;
import models.DepositRequest;
import models.UserRequest;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class UserSteps {

    public static AccountResponse createAccount(UserRequest userRequest) {
        return ValidatedCrudRequester.<AccountResponse>authenticatedAsUser(
                userRequest.getUsername(),
                userRequest.getPassword(),
                Endpoint.ACCOUNT_CREATION,
                ResponseSpecs.entityWasCreated()
        ).post(null);
    }

    private static AccountResponse createAccountWithNonZeroBalance(UserRequest user) {
        AccountResponse account = createAccount(user);

        DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
        deposit.setId(account.getId());
        depositMoneyIntoAccount(user, deposit);

        return getAccountById(user, account.getId());
    }

    public static List<AccountResponse> createTwoAccountsWithNonZeroBalanceForUser(
            UserRequest user) {

        return List.of(
                createAccountWithNonZeroBalance(user),
                createAccountWithNonZeroBalance(user)
        );
    }


    public static List<AccountResponse> createTwoAccountsWithNonZeroBalanceForDifferentUsers(
            List<UserRequest> users) {

        return List.of(
                createAccountWithNonZeroBalance(users.get(0)),
                createAccountWithNonZeroBalance(users.get(1))
        );
    }

    private static void depositMoneyIntoAccount(UserRequest user, DepositRequest deposit) {
        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(deposit);
    }

    public static AccountResponse getAccountById(UserRequest user, Long accountId) {
        return new ValidatedCrudRequester<AccountResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .getAll(AccountResponse.class)
                .stream()
                .filter(account -> account.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Account not found: " + accountId));
    }

    public static List<AccountResponse> getAccounts(UserRequest user) {
        return new ValidatedCrudRequester<AccountResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .getAll(AccountResponse.class);
    }

}
