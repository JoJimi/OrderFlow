package org.example.payment.dto.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Toss Payments API 응답 DTO
 * 결제 승인, 조회 등에서 반환되는 결제 정보
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String version,
        String paymentKey,
        String type,
        String orderId,
        String orderName,
        String mId,
        String currency,
        String method,
        Long totalAmount,
        Long balanceAmount,
        String status,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        Boolean useEscrow,
        String lastTransactionKey,
        Long suppliedAmount,
        Long vat,
        Boolean cultureExpense,
        Long taxFreeAmount,
        Integer taxExemptionAmount,
        List<Cancel> cancels,
        Boolean isPartialCancelable,
        Card card,
        VirtualAccount virtualAccount,
        Transfer transfer,
        MobilePhone mobilePhone,
        GiftCertificate giftCertificate,
        CashReceipt cashReceipt,
        CashReceipts cashReceipts,
        Discount discount,
        EasyPay easyPay,
        String country,
        Failure failure,
        Receipt receipt,
        Checkout checkout,
        String secret
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(
            Long amount,
            String issuerCode,
            String acquirerCode,
            String number,
            Integer installmentPlanMonths,
            String approveNo,
            Boolean useCardPoint,
            String cardType,
            String ownerType,
            String acquireStatus,
            Boolean isInterestFree,
            String interestPayer
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VirtualAccount(
            String accountType,
            String accountNumber,
            String bankCode,
            String customerName,
            OffsetDateTime dueDate,
            String refundStatus,
            Boolean expired,
            String settlementStatus,
            RefundReceiveAccount refundReceiveAccount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RefundReceiveAccount(
            String bankCode,
            String accountNumber,
            String holderName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Transfer(
            String bankCode,
            String settlementStatus
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MobilePhone(
            String customerMobilePhone,
            String settlementStatus,
            String receiptUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GiftCertificate(
            String approveNo,
            String settlementStatus
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CashReceipt(
            String type,
            String receiptKey,
            String issueNumber,
            String receiptUrl,
            Long amount,
            Long taxFreeAmount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CashReceipts(
            String receiptKey,
            String orderId,
            String orderName,
            String type,
            String issueNumber,
            String receiptUrl,
            String businessNumber,
            String transactionType,
            Long amount,
            Long taxFreeAmount,
            String issueStatus,
            Failure failure,
            String customerIdentityNumber,
            OffsetDateTime requestedAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancel(
            Long cancelAmount,
            String cancelReason,
            Long taxFreeAmount,
            Integer taxExemptionAmount,
            Long refundableAmount,
            Long easyPayDiscountAmount,
            OffsetDateTime canceledAt,
            String transactionKey,
            String receiptKey
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Discount(
            Long amount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EasyPay(
            String provider,
            Long amount,
            Long discountAmount
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Failure(
            String code,
            String message
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Receipt(
            String url
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Checkout(
            String url
    ) {}

    /**
     * 결제 성공 여부 확인
     */
    public boolean isSuccess() {
        return "DONE".equals(status);
    }

    /**
     * 결제 대기 중 여부 (가상계좌 입금 대기 등)
     */
    public boolean isWaitingForDeposit() {
        return "WAITING_FOR_DEPOSIT".equals(status);
    }

    /**
     * 결제 취소 여부
     */
    public boolean isCanceled() {
        return "CANCELED".equals(status);
    }
}