package org.example.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.product.domain.Product;
import org.example.product.domain.QProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryDSL을 사용한 복잡한 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryQueryDsl {

    private final JPAQueryFactory queryFactory;
    private static final QProduct product = QProduct.product;

    /**
     * 동적 필터링 + 페이지네이션
     *
     * @param category  카테고리 (null이면 모든 카테고리)
     * @param minPrice  최소 가격 (null이면 제한 없음)
     * @param maxPrice  최대 가격 (null이면 제한 없음)
     * @param keyword   상품명 검색 키워드 (null이면 검색 안함)
     * @param pageable  페이지네이션 정보
     * @return 필터링된 상품 목록
     */
    public Page<Product> findByDynamicFilter(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword,
            Pageable pageable
    ) {
        // 동적 조건 생성
        BooleanExpression condition = product.isDeleted.isFalse();

        if (category != null && !category.isBlank()) {
            condition = condition.and(product.category.eq(category));
        }

        if (minPrice != null) {
            condition = condition.and(product.price.goe(minPrice));
        }

        if (maxPrice != null) {
            condition = condition.and(product.price.loe(maxPrice));
        }

        if (keyword != null && !keyword.isBlank()) {
            condition = condition.and(product.productName.containsIgnoreCase(keyword));
        }

        // 정렬 조건 생성
        List<OrderSpecifier<?>> orders = getOrderSpecifiers(pageable.getSort());

        // 쿼리 실행
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(condition)
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    /**
     * 가격 범위별 상품 개수 조회
     *
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @return 해당 범위의 상품 개수
     */
    public long countByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        Long count = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        product.isDeleted.isFalse(),
                        product.price.between(minPrice, maxPrice)
                )
                .fetchOne();

        return count != null ? count : 0;
    }

    /**
     * 카테고리별 평균 가격 조회
     *
     * @param category 카테고리
     * @return 평균 가격
     */
    public BigDecimal getAveragePriceByCategory(String category) {
        Double avgPrice = queryFactory
                .select(product.price.avg())
                .from(product)
                .where(
                        product.isDeleted.isFalse(),
                        product.category.eq(category)
                )
                .fetchOne();

        return avgPrice != null ? BigDecimal.valueOf(avgPrice) : BigDecimal.ZERO;
    }

    /**
     * 가장 비싼 상품 N개 조회
     *
     * @param limit 조회할 개수
     * @return 가장 비싼 상품 목록
     */
    public List<Product> findTopNExpensiveProducts(int limit) {
        return queryFactory
                .selectFrom(product)
                .where(product.isDeleted.isFalse())
                .orderBy(product.price.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 특정 가격 이상의 상품 목록 조회
     *
     * @param minPrice 최소 가격
     * @param pageable 페이지네이션
     * @return 상품 목록
     */
    public Page<Product> findProductsAbovePrice(BigDecimal minPrice, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(
                        product.isDeleted.isFalse(),
                        product.price.goe(minPrice)
                )
                .orderBy(product.price.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        product.isDeleted.isFalse(),
                        product.price.goe(minPrice)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    /**
     * Sort를 QueryDSL OrderSpecifier로 변환
     */
    private List<OrderSpecifier<?>> getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sort.isEmpty()) {
            // 기본 정렬: 생성일 역순
            orders.add(product.createdAt.desc());
            return orders;
        }

        for (Sort.Order order : sort) {
            com.querydsl.core.types.Order direction = order.isAscending()
                    ? com.querydsl.core.types.Order.ASC
                    : com.querydsl.core.types.Order.DESC;

            switch (order.getProperty()) {
                case "productName" -> orders.add(new OrderSpecifier<>(direction, product.productName));
                case "price" -> orders.add(new OrderSpecifier<>(direction, product.price));
                case "category" -> orders.add(new OrderSpecifier<>(direction, product.category));
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, product.createdAt));
                case "updatedAt" -> orders.add(new OrderSpecifier<>(direction, product.updatedAt));
                default -> orders.add(new OrderSpecifier<>(direction, product.createdAt));
            }
        }

        return orders;
    }
}