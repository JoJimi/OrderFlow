package org.example.shipping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.exception.shipping.ShippingNotFoundException;
import org.example.shipping.domain.Shipment;
import org.example.shipping.dto.request.ShippingStartRequest;
import org.example.shipping.dto.response.ShipmentResponse;
import org.example.shipping.repository.ShipmentRepository;
import org.example.shared.dto.PaymentEvent;
import org.example.shared.type.ShippingStatus;
import org.example.shared.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final ShippingEventProducer eventProducer;

    /**
     * 배송 준비 (PaymentCompleted 이벤트 수신 시 호출)
     */
    @Transactional
    public void prepareShipping(PaymentEvent event) {
        log.info("배송 준비 시작 - orderId: {}, paymentId: {}",
                event.orderId(), event.paymentId());

        // 이미 배송 정보가 존재하는지 확인
        if (shipmentRepository.existsByOrderId(event.orderId())) {
            log.warn("이미 배송 정보가 존재합니다 - orderId: {}", event.orderId());
            return;
        }

        // 배송 ID 생성
        String shipmentId = IdGenerator.generateShipmentId();

        // 배송 엔티티 생성 (초기 상태: SHIPPING_PREPARING)
        Shipment shipment = Shipment.builder()
                .shipmentId(shipmentId)
                .orderId(event.orderId())
                .shippingStatus(ShippingStatus.SHIPPING_PREPARING)
                .build();

        // DB 저장
        shipmentRepository.save(shipment);

        // ShippingPreparing 이벤트 발행
        eventProducer.publishShippingPreparingEvent(
                shipmentId,
                event.orderId(),
                event.userId()
        );

        log.info("배송 준비 완료 - shipmentId: {}, orderId: {}", shipmentId, event.orderId());
    }

    /**
     * 배송 시작
     */
    @Transactional
    public ShipmentResponse startShipping(String shipmentId, ShippingStartRequest request) {
        log.info("배송 시작 - shipmentId: {}", shipmentId);

        // 배송 정보 조회
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(ShippingNotFoundException::new);

        // 배송 상태 검증
        if (shipment.getShippingStatus() != ShippingStatus.SHIPPING_PREPARING) {
            throw new IllegalStateException(
                    String.format("배송 상태가 %s일 때는 시작할 수 없습니다.", shipment.getShippingStatus())
            );
        }

        // 배송 시작
        shipment.startShipping(
                request.trackingNumber(),
                request.carrier(),
                request.estimatedDeliveryDate()
        );

        shipmentRepository.save(shipment);

        // ShippingStarted 이벤트 발행
        eventProducer.publishShippingStartedEvent(shipment);

        log.info("배송 시작 완료 - shipmentId: {}, trackingNumber: {}",
                shipmentId, request.trackingNumber());

        return ShipmentResponse.from(shipment);
    }

    /**
     * 배송 완료
     */
    @Transactional
    public ShipmentResponse completeShipping(String shipmentId) {
        log.info("배송 완료 처리 - shipmentId: {}", shipmentId);

        // 배송 정보 조회
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(ShippingNotFoundException::new);

        // 배송 상태 검증
        if (shipment.getShippingStatus() != ShippingStatus.SHIPPING_STARTED) {
            throw new IllegalStateException(
                    String.format("배송 상태가 %s일 때는 완료할 수 없습니다.", shipment.getShippingStatus())
            );
        }

        // 배송 완료
        shipment.completeShipping();

        shipmentRepository.save(shipment);

        // ShippingCompleted 이벤트 발행
        eventProducer.publishShippingCompletedEvent(shipment);

        log.info("배송 완료 처리 완료 - shipmentId: {}", shipmentId);

        return ShipmentResponse.from(shipment);
    }

    /**
     * 배송 조회 (shipmentId)
     */
    @Transactional(readOnly = true)
    public ShipmentResponse getShipment(String shipmentId) {
        log.info("배송 조회 - shipmentId: {}", shipmentId);

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(ShippingNotFoundException::new);

        return ShipmentResponse.from(shipment);
    }

    /**
     * 배송 조회 (orderId)
     */
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderId(String orderId) {
        log.info("주문별 배송 조회 - orderId: {}", orderId);

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(ShippingNotFoundException::new);

        return ShipmentResponse.from(shipment);
    }
}