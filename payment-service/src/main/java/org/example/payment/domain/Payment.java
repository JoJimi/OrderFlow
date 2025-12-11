package org.example.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.PaymentMethod;
import org.example.shared.type.PaymentStatus;

import java.math.BigDecimal;

/**
 * 결제 엔티티
 * Orders 테이블과 1:1 연결 (한 주문당 하나의 결제)
 */
@Entity
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_order_id", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_status", columnList = "payment_status"),
                @Index(name = "idx_transaction_id", columnList = "transaction_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Payment extends BaseEntity {

    @Id
    @Column(name = "payment_id", nullable = false, length = 50)
    private String paymentId;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PAYMENT_PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * 결제 성공 처리
     */
    public void completePayment(String transactionId) {
        if (this.paymentStatus != PaymentStatus.PAYMENT_PENDING) {
            throw new IllegalStateException(
                    String.format("결제 상태가 %s일 때는 완료 처리할 수 없습니다.", this.paymentStatus)
            );
        }
        this.paymentStatus = PaymentStatus.PAYMENT_COMPLETED;
        this.transactionId = transactionId;
        this.failureReason = null;
    }

    /**
     * 결제 실패 처리
     */
    public void failPayment(String failureReason) {
        if (this.paymentStatus != PaymentStatus.PAYMENT_PENDING) {
            throw new IllegalStateException(
                    String.format("결제 상태가 %s일 때는 실패 처리할 수 없습니다.", this.paymentStatus)
            );
        }
        this.paymentStatus = PaymentStatus.PAYMENT_FAILED;
        this.failureReason = failureReason;
    }

    /**
     * 결제 금액 검증
     */
    public boolean isAmountValid(BigDecimal orderTotalPrice) {
        return this.amount.compareTo(orderTotalPrice) == 0;
    }
}