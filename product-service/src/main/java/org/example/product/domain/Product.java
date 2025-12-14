package org.example.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.shared.entity.BaseEntity;
import org.example.shared.type.product.CategoryType;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_is_deleted", columnList = "deleted"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Product extends BaseEntity implements Persistable<String> {

    @Id
    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private CategoryType category;

    @Override
    public String getId() {
        return productId;
    }

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }

    /**
     * 상품을 논리 삭제 처리합니다.
     */
    public void markAsDeleted() {
        this.setDeleted(true);
    }

    /**
     * 상품 정보를 업데이트합니다 (부분 수정 지원).
     */
    public void updateInfo(String productName, String description, BigDecimal price, CategoryType category) {
        if (productName != null && !productName.isBlank()) {
            this.productName = productName;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            this.price = price;
        }
        if (category != null) {
            this.category = category;
        }
    }
}