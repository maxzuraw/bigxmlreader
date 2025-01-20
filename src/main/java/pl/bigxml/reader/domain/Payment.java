package pl.bigxml.reader.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
public class Payment {

    private Long id;
    private Long inputFileChunkId;
    private Integer indexInChunk;
    private LocalDate dateOfArchiving;
    private BigDecimal amount;
    private String currency;
    private LocalDate deliveredExecutionDate;
    private BigDecimal inputSettlementInfoAmount;
    private String inputSettlementInfoCurrency;
    private String transactionId;
    private String instructionIdentificationExternal;
    private String clearingReferenceNumberOfTransaction;
    private String originalTransactionId;
    private String clearingSystemReference;
    private String uniqueEndToEndTransactionReference;
    private String originalUniqueEndToEndTransactionReference;
    private String endToEndIdentificationOfOriginalTransaction;
    private String inputFileFileIdentification;
    private String inputOrderId;
    private String inputOrderOrderType;
    private String outputOrderId;
    private String inputFileId;
    private String outputFileId;
    private String outputFileFormatGroupName;
    private String submitterIban;
    private String submitterBic;
    private String submitterAccountInNationalFormat;
    private String receiverIban;
    private String receiverBic;
    private String receiverAccountInNationalFormat;
    private String embargoResult;
    private String paymentPurpose;
    private String paymentInputOrderLogicalFormat;
    private String paymentOutputOrderLogicalFormat;
    private String rejectReturnReasonCode;
    private String chargeBearer;
    private String remittanceInfoUnstructured;
    private String manualInterventionManualApprovalAction;
    private String rejectDetected;
}
