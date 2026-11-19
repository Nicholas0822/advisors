package com.nwhite.advisors.advisor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Component
public class SimpleLoggingAdvisor implements CallAdvisor, StreamAdvisor {
    public static final Function<@Nullable ChatClientRequest, String> DEFAULT_REQUEST_TO_STRING = (chatClientRequest) -> chatClientRequest != null ? chatClientRequest.toString() : "null";
    public static final Function<@Nullable ChatResponse, String> DEFAULT_RESPONSE_TO_STRING = (object) -> object != null ? JacksonUtils.getDefaultJsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object) : "null";
    private static final Log logger = LogFactory.getLog(SimpleLoggingAdvisor.class);
    private final Function<@Nullable ChatClientRequest, String> requestToString;
    private final Function<@Nullable ChatResponse, String> responseToString;
    private final int order;

    public SimpleLoggingAdvisor() {
        this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, 0);
    }

    public SimpleLoggingAdvisor(int order) {
        this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, order);
    }

    public SimpleLoggingAdvisor(@Nullable Function<@Nullable ChatClientRequest, String> requestToString, @Nullable Function<@Nullable ChatResponse, String> responseToString, int order) {
        this.requestToString = requestToString != null ? requestToString : DEFAULT_REQUEST_TO_STRING;
        this.responseToString = responseToString != null ? responseToString : DEFAULT_RESPONSE_TO_STRING;
        this.order = order;
    }

    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        this.logRequest(chatClientRequest);
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        this.logResponse(chatClientResponse);
        return chatClientResponse;
    }

    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        this.logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }

    protected void logRequest(ChatClientRequest request) {
        if (logger.isDebugEnabled()) {
            Log var10000 = logger;
            Object var10001 = this.requestToString.apply(request);
            var10000.debug("request: " + (String)var10001);
        }

    }

    protected void logResponse(ChatClientResponse chatClientResponse) {
        if (logger.isDebugEnabled()) {
            Log var10000 = logger;
            String var10001 = (String)this.responseToString.apply(chatClientResponse.chatResponse());
            var10000.debug("response: " + var10001);
        }

    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getOrder() {
        return this.order;
    }

    public String toString() {
        return SimpleLoggingAdvisor.class.getSimpleName();
    }

    public static SimpleLoggingAdvisor.Builder builder() {
        return new SimpleLoggingAdvisor.Builder();
    }

    public static final class Builder {
        private @Nullable Function<@Nullable ChatClientRequest, String> requestToString;
        private @Nullable Function<@Nullable ChatResponse, String> responseToString;
        private int order = 0;

        private Builder() {
        }

        public SimpleLoggingAdvisor.Builder requestToString(Function<@Nullable ChatClientRequest, String> requestToString) {
            this.requestToString = requestToString;
            return this;
        }

        public SimpleLoggingAdvisor.Builder responseToString(Function<@Nullable ChatResponse, String> responseToString) {
            this.responseToString = responseToString;
            return this;
        }

        public SimpleLoggingAdvisor.Builder order(int order) {
            this.order = order;
            return this;
        }

        public SimpleLoggingAdvisor build() {
            return new SimpleLoggingAdvisor(this.requestToString, this.responseToString, this.order);
        }
    }
}
