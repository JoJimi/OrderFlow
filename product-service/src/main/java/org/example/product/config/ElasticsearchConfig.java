package org.example.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

/**
 * Spring Data Elasticsearch 설정
 */
@Configuration
@EnableElasticsearchRepositories(
        basePackages = "org.example.product.repository.elasticsearch"
)
@Slf4j
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.rest.uris:http://localhost:9200}")
    private String elasticsearchUrl;

    @Override
    public ClientConfiguration clientConfiguration() {
        log.info("Elasticsearch 설정 - URL: {}", elasticsearchUrl);

        return ClientConfiguration.builder()
                .connectedTo(parseHostAndPort(elasticsearchUrl))
                .withConnectTimeout(Duration.ofSeconds(5))
                .withSocketTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * URL을 "host:port" 형식으로 파싱
     * 예: http://localhost:9200 → localhost:9200
     */
    private String parseHostAndPort(String url) {
        if (url == null || url.isEmpty()) {
            return "localhost:9200";
        }
        // http:// 또는 https:// 제거
        return url.replaceAll("^https?://", "");
    }
}