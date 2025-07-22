package com.savant.labs.activity.tracker.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.savant.labs.activity.tracker.constants.GitHubApiConstants.DEFAULT_CONNECT_TIMEOUT_MS;
import static com.savant.labs.activity.tracker.constants.GitHubApiConstants.DEFAULT_READ_TIMEOUT_MS;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(GitHubConfig config) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT_MS)
                .responseTimeout(Duration.ofMillis(DEFAULT_READ_TIMEOUT_MS))
                .doOnConnected(conn -> conn.addHandlerLast(
                        new ReadTimeoutHandler(DEFAULT_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .defaultHeader("User-Agent", "GitHub-Activity-Connector/1.0")
                .build();
    }
}