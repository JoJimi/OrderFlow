package org.example.product.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.product.domain.Product;
import org.example.product.domain.QProduct;
import org.example.shared.type.product.CategoryType;
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
     */
    public Page<Product> findByDynamicFilter(
            CategoryType category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword,
            Pageable pageable
    ) {
        BooleanExpression condition = buildCondition(category, minPrice, maxPrice, keyword);
        List<OrderSpecifier<?>> orders = getOrderSpecifiers(pageable.getSort());

        List<Product> content = queryFactory
                .selectFrom(product)
                .where(condition)
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    /**
     * 동적 조건 빌더
     */
    private BooleanExpression buildCondition(
            CategoryType category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword
    ) {
        BooleanExpression condition = product.deleted.isFalse();

        if (category != null) {
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

        return condition;
    }

    /**
     * 가격 범위별 상품 개수 조회
     */
    public long countByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        Long count = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        product.deleted.isFalse(),
                        product.price.between(minPrice, maxPrice)
                )
                .fetchOne();

        return count != null ? count : 0;
    }

    /**
     * 카테고리별 평균 가격 조회
     */
    public BigDecimal getAveragePriceByCategory(CategoryType category) {
        Double avgPrice = queryFactory
                .select(product.price.avg())
                .from(product)
                .where(
                        product.deleted.isFalse(),
                        product.category.eq(category)
                )
                .fetchOne();

        return avgPrice != null ? BigDecimal.valueOf(avgPrice) : BigDecimal.ZERO;
    }

    /**
     * 가장 비싼 상품 N개 조회
     */
    public List<Product> findTopNExpensiveProducts(int limit) {
        return queryFactory
                .selectFrom(product)
                .where(product.deleted.isFalse())
                .orderBy(product.price.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 특정 가격 이상의 상품 목록 조회
     */
    public Page<Product> findProductsAbovePrice(BigDecimal minPrice, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(
                        product.deleted.isFalse(),
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
                        product.deleted.isFalse(),
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
            orders.add(product.createdAt.desc());
            return orders;
        }

        for (Sort.Order order : sort) {
            com.querydsl.core.types.Order direction = order.isAscending()
                    ? com.querydsl.core.types.Order.ASC
                    : com.querydsl.core.types.Order.DESC;

            OrderSpecifier<?> orderSpecifier = switch (order.getProperty()) {
                case "productName" -> new OrderSpecifier<>(direction, product.productName);
                case "price" -> new OrderSpecifier<>(direction, product.price);
                case "category" -> new OrderSpecifier<>(direction, product.category);
                case "createdAt" -> new OrderSpecifier<>(direction, product.createdAt);
                case "updatedAt" -> new OrderSpecifier<>(direction, product.updatedAt);
                default -> new OrderSpecifier<>(direction, product.createdAt);
            };

            orders.add(orderSpecifier);
        }

        return orders;
    }
}