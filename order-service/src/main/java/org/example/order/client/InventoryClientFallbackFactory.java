package org.example.order.client;

import lombok.extern.slf4j.Slf4j;
import org.example.order.dto.response.InventoryInfoResponse;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClient() {
            @Override
            public InventoryInfoResponse getInventory(String productId) {
                log.error("Inventory Service 호출 실패 - productId: {}, cause: {}",
                        productId, cause.getMessage());
                throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                        "재고 서비스와 통신할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }
        };
    }
}