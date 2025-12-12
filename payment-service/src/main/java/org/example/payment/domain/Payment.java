package org.example.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.PaymentMethod;
import org.example.shared.type.PaymentStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_payment_status", columnList = "payment_status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Payment extends BaseEntity {

    @Id
    @Column(name = "payment_id", nullable = false, length = 50)
    private String paymentId;

    @Column(name = "order_id", nullable = false, length = 50)
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

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

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
     * 결제 완료 처리
     */
    public void complete(String transactionId) {
        this.paymentStatus = PaymentStatus.PAYMENT_COMPLETED;
        this.transactionId = transactionId;
    }

    /**
     * 결제 실패 처리
     */
    public void fail(String failureReason) {
        this.paymentStatus = PaymentStatus.PAYMENT_FAILED;
        this.failureReason = failureReason;
    }
}