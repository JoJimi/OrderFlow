package org.example.order.client;

import lombok.extern.slf4j.Slf4j;
import org.example.order.dto.response.ProductInfoResponse;
import org.example.shared.exception.BusinessException;
import org.example.shared.exception.ErrorCode;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        return new ProductClient() {
            @Override
            public ProductInfoResponse getProduct(String productId) {
                log.error("Product Service 호출 실패 - productId: {}, cause: {}",
                        productId, cause.getMessage());
                throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                        "상품 서비스와 통신할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }
        };
    }
}