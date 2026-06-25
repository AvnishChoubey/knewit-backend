package com.knewit.backend.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.elasticsearch")
@Data
public class ElasticsearchConfig {
    @Value("${spring.elasticsearch.uris}")
    private String url;
    private String username;
    private String password;
    private String indexPrefix;
    private String syncStrategy;
    private String searchFailureMode;

    @Bean
    public RestClient restClient() {
        String hostUrl = url != null && !url.isBlank() ? url : "http://localhost:9200";
        return RestClient.builder(HttpHost.create(hostUrl)).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

