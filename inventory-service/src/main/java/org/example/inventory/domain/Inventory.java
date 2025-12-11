package org.example.inventory.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.exception.inventory.InsufficientStockException;
import org.example.shared.exception.inventory.NegativeStockNotAllowedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 재고 엔티티
 * Products 테이블과 1:1 연결 (한 상품당 하나의 재고 정보)
 */
@Entity
@Table(name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_product_id", columnNames = "product_id")
        },
        indexes = {
                @Index(name = "idx_product_id", columnList = "product_id"),
                @Index(name = "idx_last_updated", columnList = "last_updated")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Inventory extends BaseEntity {

    @Id
    @Column(name = "inventory_id", nullable = false, length = 50)
    private String inventoryId;

    @Column(name = "product_id", nullable = false, unique = true, length = 50)
    private String productId;

    @Column(name = "total_stock", nullable = false)
    @Builder.Default
    private Integer totalStock = 0;

    @Column(name = "reserved_stock", nullable = false)
    @Builder.Default
    private Integer reservedStock = 0;

    @Column(name = "available_stock", nullable = false)
    @Builder.Default
    private Integer availableStock = 0;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventoryLog> inventoryLogs = new ArrayList<>();

    /**
     * 재고 예약 (주문 생성 시)
     */
    public void reserveStock(int quantity) {
        if (quantity <= 0) {
            throw new NegativeStockNotAllowedException("예약 수량은 0보다 커야 합니다.");
        }
        if (this.availableStock < quantity) {
            throw new InsufficientStockException(
                    String.format("재고가 부족합니다. 요청: %d, 사용가능: %d", quantity, this.availableStock)
            );
        }
        this.reservedStock += quantity;
        this.availableStock -= quantity;
    }

    /**
     * 재고 차감 (결제 완료 시)
     */
    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new NegativeStockNotAllowedException("차감 수량은 0보다 커야 합니다.");
        }
        if (this.reservedStock < quantity) {
            throw new InsufficientStockException(
                    String.format("예약된 재고가 부족합니다. 요청: %d, 예약됨: %d", quantity, this.reservedStock)
            );
        }
        this.reservedStock -= quantity;
        this.totalStock -= quantity;
    }

    /**
     * 재고 복구 (결제 실패 시 보상 트랜잭션)
     */
    public void restoreStock(int quantity) {
        if (quantity <= 0) {
            throw new NegativeStockNotAllowedException("복구 수량은 0보다 커야 합니다.");
        }
        this.reservedStock -= quantity;
        this.availableStock += quantity;
    }

    /**
     * 재고 증가 (입고)
     */
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new NegativeStockNotAllowedException("증가 수량은 0보다 커야 합니다.");
        }
        this.totalStock += quantity;
        this.availableStock += quantity;
    }

    /**
     * 재고 일관성 검증
     */
    public boolean isConsistent() {
        return this.totalStock == (this.availableStock + this.reservedStock);
    }

    /**
     * 재고 로그 추가
     */
    public void addInventoryLog(InventoryLog log) {
        this.inventoryLogs.add(log);
        log.setInventory(this);
    }
}