package com.HealthAdvanced.healthAdvanced.HEADServiceRepository.Service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.transaction.Transactional;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class HEADServiceGeneric implements IHEADServiceGeneric {
    private String headUrlBase = "https://maps.googleapis.com";
    private Integer milliSeconds = 5000;

    @Override
    public void changeBaseUrl(String url) {
        headUrlBase = url;
    }
    @Override
    public WebClient webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, milliSeconds)
                .responseTimeout(Duration.ofMillis(milliSeconds))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(milliSeconds, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(milliSeconds, TimeUnit.MILLISECONDS)));

        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(headUrlBase)
                .build();
        return client;
    }
}
