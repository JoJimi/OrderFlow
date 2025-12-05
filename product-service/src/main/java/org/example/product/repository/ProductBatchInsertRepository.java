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
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductBatchInsertRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * JDBC Batch Insert로 대량 데이터 삽입
     * JPA보다 2-3배 빠름
     */
    public int[] batchInsert(List<Product> products) {
        String sql = """
            INSERT INTO products 
            (product_id, product_name, description, price, category, 
             is_deleted, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Product product = products.get(i);
                LocalDateTime now = LocalDateTime.now();

                ps.setString(1, product.getProductId());
                ps.setString(2, product.getProductName());
                ps.setString(3, product.getDescription());
                ps.setBigDecimal(4, product.getPrice());
                ps.setString(5, product.getCategory());
                ps.setBoolean(6, false);
                ps.setTimestamp(7, Timestamp.valueOf(now));
                ps.setTimestamp(8, Timestamp.valueOf(now));
            }

            @Override
            public int getBatchSize() {
                return products.size();
            }
        });
    }
}