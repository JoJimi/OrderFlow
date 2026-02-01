package org.example.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.payment.PaymentMethod;
import org.example.shared.type.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order_id", columnList = "order_id"),
        @Index(name = "idx_payment_user_id", columnList = "user_id"),
        @Index(name = "idx_payment_status", columnList = "payment_status"),
        @Index(name = "idx_payment_toss_payment_key", columnList = "toss_payment_key"),
        @Index(name = "idx_payment_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Payment extends BaseEntity {

    @Id
    @Column(name = "payment_id", nullable = false, length = 50)
    private String paymentId;

    @Column(name = "order_id", nullable = false, length = 50, unique = true)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PAYMENT_PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    // ===== Toss Payments 관련 필드 =====

    @Column(name = "toss_payment_key", length = 200)
    private String tossPaymentKey;

    @Column(name = "toss_order_id", length = 100)
    private String tossOrderId;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "card_company", length = 50)
    private String cardCompany;

    @Column(name = "card_number", length = 50)
    private String cardNumber;

    @Column(name = "installment_months")
    private Integer installmentMonths;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    // ===== 비즈니스 메서드 =====

    /**
     * 결제 상태 변경
     */
    public void updateStatus(PaymentStatus newStatus) {
        if (!this.paymentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("결제 상태를 %s에서 %s로 변경할 수 없습니다.",
                            this.paymentStatus, newStatus)
            );
        }
        this.paymentStatus = newStatus;
    }

    /**
     * Toss 결제 승인 완료 처리
     */
    public void completeWithToss(String paymentKey,
                                 String transactionId,
                                 OffsetDateTime approvedAt,
                                 String cardCompany,
                                 String cardNumber,
                                 Integer installmentMonths,
                                 String receiptUrl,
                                 PaymentMethod method) {
        this.paymentStatus = PaymentStatus.PAYMENT_COMPLETED;
        this.tossPaymentKey = paymentKey;
        this.transactionId = transactionId;
        this.approvedAt = approvedAt;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.installmentMonths = installmentMonths;
        this.receiptUrl = receiptUrl;
        this.paymentMethod = method;
    }

    /**
     * 결제 실패 처리
     */
    public void fail(String failureCode, String failureReason) {
        this.paymentStatus = PaymentStatus.PAYMENT_FAILED;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
    }

    /**
     * 결제 대기 상태인지 확인
     */
    public boolean isPending() {
        return this.paymentStatus == PaymentStatus.PAYMENT_PENDING;
    }

    /**
     * 결제 완료 상태인지 확인
     */
    public boolean isCompleted() {
        return this.paymentStatus == PaymentStatus.PAYMENT_COMPLETED;
    }
}