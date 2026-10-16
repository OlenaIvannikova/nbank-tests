package requests.steps;

import models.AccountResponse;
import models.TransferRequest;
import models.TransferResponse;
import models.UserRequest;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.math.BigDecimal;

public class TransferSteps {

    public static TransferRequest createTransferRequest(
            AccountResponse sender,
            AccountResponse receiver,
            BigDecimal amount) {

        return TransferRequest.builder()
                .senderAccountId(sender.getId())
                .receiverAccountId(receiver.getId())
                .amount(amount)
                .build();
    }

    public static TransferResponse executeTransfer(
            UserRequest user,
            TransferRequest request) {

        return new CrudRequester(
                RequestSpecs.authAsUser(
                        user.getUsername(),
                        user.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(request)
                .extract()
                .as(TransferResponse.class);
    }
}

