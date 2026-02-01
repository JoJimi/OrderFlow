package org.example.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "toss.payments")
public class TossPaymentsProperties {

    private String secretKey;
    private String clientKey;
    private String baseUrl = "https://api.tosspayments.com";
    private int connectTimeout = 5000;
    private int readTimeout = 30000;

    /**
     * Base64 인코딩된 Authorization 헤더 값 생성
     * Toss API는 Secret Key를 Basic Auth로 전달
     */
    public String getAuthorizationHeader() {
        String credentials = secretKey + ":";
        return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}