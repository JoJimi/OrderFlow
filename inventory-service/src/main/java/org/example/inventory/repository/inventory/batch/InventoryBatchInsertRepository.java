package org.example.inventory.repository.inventory.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventory.domain.Inventory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JDBC Batch Insert를 사용한 고성능 재고 대량 삽입
 * JPA보다 2-3배 빠른 성능 제공
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class InventoryBatchInsertRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * JDBC Batch Insert로 대량 재고 데이터 삽입
     *
     * @param inventories 저장할 재고 목록
     * @return 각 배치 작업의 영향받은 행 수 배열
     */
    public int[] batchInsert(List<Inventory> inventories) {
        if (inventories == null || inventories.isEmpty()) {
            return new int[0];
        }

        String sql = """
            INSERT INTO inventory 
            (inventory_id, product_id, total_stock, reserved_stock, available_stock,
             deleted, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Inventory inventory = inventories.get(i);
                LocalDateTime now = LocalDateTime.now();

                ps.setString(1, inventory.getInventoryId());
                ps.setString(2, inventory.getProductId());
                ps.setInt(3, inventory.getTotalStock());
                ps.setInt(4, inventory.getReservedStock());
                ps.setInt(5, inventory.getAvailableStock());
                ps.setBoolean(6, false);
                ps.setTimestamp(7, Timestamp.valueOf(now));
                ps.setTimestamp(8, Timestamp.valueOf(now));
            }

            @Override
            public int getBatchSize() {
                return inventories.size();
            }
        });

        log.debug("JDBC Batch Insert 완료 - {} 개 재고 저장", inventories.size());
        return results;
    }

    /**
     * 배치 크기를 지정한 대량 삽입
     * 메모리 효율성을 위해 큰 데이터셋을 작은 배치로 나누어 처리
     *
     * @param inventories 저장할 재고 목록
     * @param batchSize 배치 크기
     */
    public void batchInsertWithSize(List<Inventory> inventories, int batchSize) {
        if (inventories == null || inventories.isEmpty()) {
            return;
        }

        int totalSize = inventories.size();
        int processedCount = 0;

        for (int i = 0; i < totalSize; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalSize);
            List<Inventory> batch = inventories.subList(i, endIndex);

            batchInsert(batch);

            processedCount += batch.size();

            if ((i / batchSize + 1) % 10 == 0) {
                log.debug("JDBC Batch Insert 진행 중 - {} / {} ({} %)",
                        processedCount, totalSize, (processedCount * 100 / totalSize));
            }
        }

        log.info("JDBC Batch Insert 완료 - 총 {} 개 재고 저장", totalSize);
    }
}
