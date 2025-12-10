package org.example.product.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.product.domain.Product;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JDBC Batch Insert를 사용한 고성능 대량 삽입
 * JPA보다 2-3배 빠른 성능 제공
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductBatchInsertRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * JDBC Batch Insert로 대량 데이터 삽입
     *
     * @param products 저장할 상품 목록
     * @return 각 배치 작업의 영향받은 행 수 배열
     */
    public int[] batchInsert(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return new int[0];
        }

        String sql = """
            INSERT INTO products 
            (product_id, product_name, description, price, category, 
             is_deleted, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Product product = products.get(i);
                LocalDateTime now = LocalDateTime.now();

                ps.setString(1, product.getProductId());
                ps.setString(2, product.getProductName());
                ps.setString(3, product.getDescription());
                ps.setBigDecimal(4, product.getPrice());
                ps.setString(5, product.getCategory().getCode());
                ps.setBoolean(6, false);
                ps.setTimestamp(7, Timestamp.valueOf(now));
                ps.setTimestamp(8, Timestamp.valueOf(now));
            }

            @Override
            public int getBatchSize() {
                return products.size();
            }
        });

        log.debug("JDBC Batch Insert 완료 - {} 개 상품 저장", products.size());
        return results;
    }

    /**
     * 배치 크기를 지정한 대량 삽입
     * 메모리 효율성을 위해 큰 데이터셋을 작은 배치로 나누어 처리
     *
     * @param products 저장할 상품 목록
     * @param batchSize 배치 크기
     */
    public void batchInsertWithSize(List<Product> products, int batchSize) {
        if (products == null || products.isEmpty()) {
            return;
        }

        int totalSize = products.size();
        int processedCount = 0;

        for (int i = 0; i < totalSize; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalSize);
            List<Product> batch = products.subList(i, endIndex);

            batchInsert(batch);

            processedCount += batch.size();

            if ((i / batchSize + 1) % 10 == 0) {
                log.debug("JDBC Batch Insert 진행 중 - {} / {} ({} %)",
                        processedCount, totalSize, (processedCount * 100 / totalSize));
            }
        }

        log.info("JDBC Batch Insert 완료 - 총 {} 개 상품 저장", totalSize);
    }
}