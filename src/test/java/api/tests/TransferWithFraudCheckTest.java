package api.tests;

import static api.requests.steps.AdminSteps.createTwoDifferentUsers;
import static common.generators.RandomData.generateTransferAmount;

import api.models.AccountResponse;
import api.models.TransferRequest;
import api.models.TransferResponse;
import api.models.UserRequest;
import api.models.comparison.ModelAssertions;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.annotations.APIVersion;
import common.annotations.FraudCheckMock;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import common.generators.RandomModelGenerator;
import common.helpers.StepLogger;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@APIVersion("with_database")
@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

  @Test
  @Disabled
  @FraudCheckMock(
      status = "SUCCESS",
      decision = "APPROVED",
      riskScore = 0.2,
      reason = "Low risk transaction",
      requiresManualReview = false,
      additionalVerificationRequired = false)
  public void testTransferWithFraudCheck() {
    List<UserRequest> users =
        StepLogger.log("Create two different users", () -> createTwoDifferentUsers());
    UserRequest sender = users.get(0);

    UserSteps senderSteps = new UserSteps(sender.getUsername(), sender.getPassword());

    List<AccountResponse> accounts =
        StepLogger.log(
            "Create accounts with non-zero balances for both users",
            () -> UserSteps.createTwoAccountsWithNonZeroBalanceForDifferentUsers(users));

    AccountResponse senderAccount = accounts.get(0);
    AccountResponse receiverAccount = accounts.get(1);

    BigDecimal amount = generateTransferAmount(senderAccount.getBalance());
    TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
    transferRequest.setSenderAccountId(senderAccount.getId());
    transferRequest.setReceiverAccountId(receiverAccount.getId());
    transferRequest.setAmount(amount);

    TransferResponse transferResponse =
        StepLogger.log(
            "Execute transfer with fraud check",
            () ->
                senderSteps
                    .transferWithFraudCheck(transferRequest, ResponseSpecs.requestReturnsOK())
                    .as(TransferResponse.class));

    StepLogger.log(
        "Verify that transfer response is not null",
        () -> softly.assertThat(transferResponse).isNotNull());

    TransferResponse expectedResponse =
        TransferResponse.builder()
            .status("APPROVED")
            .message("Transfer approved and processed immediately")
            .amount(amount)
            .senderAccountId(senderAccount.getId())
            .receiverAccountId(receiverAccount.getId())
            .fraudRiskScore(0.2)
            .fraudReason("Low risk transaction")
            .requiresManualReview(false)
            .requiresVerification(false)
            .build();

    StepLogger.log(
        "Verify fraud check response details",
        () -> ModelAssertions.assertThatModels(expectedResponse, transferResponse).match());
  }
}
