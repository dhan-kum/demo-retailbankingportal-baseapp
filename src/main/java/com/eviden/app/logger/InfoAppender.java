package com.eviden.app.logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import reactor.core.publisher.Mono;

public class InfoAppender extends AppenderBase<ILoggingEvent> {
    private String prefix;
    private static final String ELASTIC_SEARCH_API_HOST = "http://10.128.0.45:9200";
    private static final String ELASTIC_SEARCH_INDEX_NAME = "delivery-logs";
    private static final WebClient webClient = WebClient.create(ELASTIC_SEARCH_API_HOST);
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoAppender.class.getName());
    public static final DateTimeFormatter ISO_8601_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .withZone(ZoneId.systemDefault());

    @Override
    protected void append(final ILoggingEvent event) {
        final MapHolder<String, ILoggingEvent> holder = getHolder();

        if (prefix == null || "".equals(prefix)) {
            addError("Prefix is not set for MapAppender.");
            return;
        }
        holder.putEvent(getPrefix() + System.currentTimeMillis(), event);
        /*holder.putEvent("@timestamp",
                ISO_8601_FORMAT.format(Instant.ofEpochMilli(event.getTimeStamp())));*/
//        holder.putEvent("message", event);

        Map<String, Object> loggingEvent = new LinkedHashMap<>();
        loggingEvent.put("@timestamp",
                ISO_8601_FORMAT.format(Instant.ofEpochMilli(event.getTimeStamp())));
        JSONObject json=new JSONObject();
        json.put("level", event.getLevel());
        json.put("message",event.getFormattedMessage());      
       loggingEvent.put("message",json.toString());
       
      //System.out.println(event.getLevel());
        pushlog(loggingEvent);
    }

    private void pushlog( final Map<String, Object>   holder) {
        webClient.post()
                .uri("/{logIndex}/_doc", ELASTIC_SEARCH_INDEX_NAME)
                .bodyValue(holder)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(exception -> {
                    LOGGER.info("Unable to send log to elastic", exception);
                    return Mono.empty();
                })
                .subscribe();
    }

    public String getPrefix() {
        return String.valueOf(System.nanoTime());
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public MapHolder<String, ILoggingEvent> getHolder() {
        return MapHolder.create();
    }
}