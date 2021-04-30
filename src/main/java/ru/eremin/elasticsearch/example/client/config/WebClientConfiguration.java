package ru.eremin.elasticsearch.example.client.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "web-clients")
public class WebClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfiguration.class);
    public static final String ELASTIC_CLIENT = "elasticsearchWebClient";

    public Map<String, ClientProperties> getClients() {
        return clients;
    }

    public void setClients(final Map<String, ClientProperties> clients) {
        this.clients = clients;
    }

    private Map<String, ClientProperties> clients = new LinkedHashMap<>();

    @Bean(ELASTIC_CLIENT)
    public WebClient elasticSearchWebClient() throws SSLException {
        final ClientProperties properties = clients.get("elasticsearch");
        final SslContext sslContext = SslContextBuilder
                .forClient()
                .build();
        final HttpClient httpClient = HttpClient.create()
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeOutSeconds(), TimeUnit.SECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(properties.getWriteTimeOutSeconds(), TimeUnit.SECONDS));
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMillis())
                .secure(context -> context.sslContext(sslContext));

        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .baseUrl(properties.getHost())
                .filter(requestFilter())
                .filter(responseFilter())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private ExchangeFilterFunction requestFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.info("Request: {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction responseFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.info("Response: {}", response.statusCode());
            return Mono.just(response);
        });
    }

    @Getter
    @Setter
    public static class ClientProperties {
        private String host = "http://localhost:8080";
        private long readTimeOutSeconds = 20L;
        private long writeTimeOutSeconds = 20L;
        private int connectTimeoutMillis = 20;
    }
}
