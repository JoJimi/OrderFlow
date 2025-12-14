package org.example.inventory.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.inventory.InventoryAction;

@Entity
@Table(name = "inventory_logs", indexes = {
        @Index(name = "idx_inventory_id", columnList = "inventory_id"),
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class InventoryLog extends BaseEntity {

    @Id
    @Column(name = "log_id", nullable = false, length = 50)
    private String logId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_log_inventory"))
    private Inventory inventory;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private InventoryAction action;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "order_id", length = 50)
    private String orderId;
}