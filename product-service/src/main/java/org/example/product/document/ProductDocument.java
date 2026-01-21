package org.example.product.document;

import lombok.*;
import org.example.shared.type.product.CategoryType;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Elasticsearch에 저장될 Product 문서
 *
 * @Document: Elasticsearch 인덱스 설정
 *   - indexName: 인덱스 이름
 *   - createIndex: 자동 인덱스 생성 여부
 *
 * @Setting: 인덱스 설정 (샤드, 레플리카)
 * @Mapping: 필드 매핑 정보
 */
@Document(indexName = "products", createIndex = true)
@Setting(
        shards = 2,
        replicas = 0,
        refreshInterval = "1s"
)
@Mapping(mappingPath = "elastic/product-mapping.json")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class ProductDocument {

    /**
     * 상품 ID - Elasticsearch 문서 ID로 사용
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String productId;

    /**
     * 상품명 - 전문 검색(Full-Text Search)을 위해 Text로 설정
     * analyzer: standard (한글 검색을 원하면 nori 분석기 사용)
     */
    @Field(
            type = FieldType.Text,
            analyzer = "standard",
            searchAnalyzer = "standard"
    )
    private String productName;

    /**
     * 상품명 정확 일치 검색용 (검색 정확도 개선)
     */
    @Field(type = FieldType.Keyword)
    private String productNameKeyword;

    /**
     * 상품 설명 - 전문 검색
     */
    @Field(
            type = FieldType.Text,
            analyzer = "standard"
    )
    private String description;

    /**
     * 가격 - 범위 검색을 위해 Scaled Float로 설정
     */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal price;

    /**
     * 카테고리 - 필터링을 위해 Keyword로 설정
     */
    @Field(type = FieldType.Keyword)
    private String category;

    /**
     * 생성 시간 - 정렬 및 범위 검색
     */
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    /**
     * 삭제 여부 - 필터링
     */
    @Field(type = FieldType.Boolean)
    private Boolean deleted;

    /**
     * 검색 점수 (검색 결과 랭킹)
     * - 실시간 계산됨, 색인에는 저장되지 않음
     */
    @Field(type = FieldType.Float, store = false)
    private Float score;

    // 생성 메서드

    /**
     * Product 엔티티에서 ProductDocument로 변환
     */
    public static ProductDocument from(org.example.product.domain.Product product) {
        return ProductDocument.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productNameKeyword(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory().getCode())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .deleted(product.isDeleted())
                .build();
    }

    /**
     * ProductDocument를 ProductResponse로 변환
     */
    public org.example.product.dto.response.ProductResponse toResponse() {
        return new org.example.product.dto.response.ProductResponse(
                productId,
                productName,
                description,
                price,
                CategoryType.from(category),
                createdAt,
                updatedAt,
                deleted != null && deleted
        );
    }
}