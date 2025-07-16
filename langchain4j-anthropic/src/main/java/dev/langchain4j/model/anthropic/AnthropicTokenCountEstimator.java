package dev.langchain4j.model.anthropic;

import static dev.langchain4j.internal.RetryUtils.withRetryMappingExceptions;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static dev.langchain4j.model.anthropic.InternalAnthropicHelper.createAnthropicRequest;
import static dev.langchain4j.model.anthropic.internal.api.AnthropicCacheType.NO_CACHE;
import static java.util.Collections.singletonList;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.anthropic.internal.api.AnthropicCreateMessageRequest;
import dev.langchain4j.model.anthropic.internal.api.AnthropicCreateMessageResponse;
import dev.langchain4j.model.anthropic.internal.client.AnthropicClient;
import dev.langchain4j.model.chat.request.ChatRequest;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

/**
 * This class can be used to estimate the count of tokens before calling Anthropic.
 * It uses the Anthropic API with maxTokens=0 to get accurate token counts for various content types.
 * <br>
 * Supports text, images, PDFs, tools, and other content types that Anthropic supports.
 */
public class AnthropicTokenCountEstimator implements TokenCountEstimator {

    private final AnthropicClient client;
    private final String modelName;
    private final Integer maxRetries;

    public AnthropicTokenCountEstimator(Builder builder) {
        this.client = AnthropicClient.builder()
                .httpClientBuilder(builder.httpClientBuilder)
                .baseUrl(getOrDefault(builder.baseUrl, "https://api.anthropic.com/v1/"))
                .apiKey(ensureNotBlank(builder.apiKey, "apiKey"))
                .version(getOrDefault(builder.version, "2023-06-01"))
                .beta(builder.beta)
                .timeout(builder.timeout)
                .logRequests(getOrDefault(builder.logRequests, false))
                .logResponses(getOrDefault(builder.logResponses, false))
                .build();
        this.modelName = ensureNotBlank(builder.modelName, "modelName");
        this.maxRetries = getOrDefault(builder.maxRetries, 2);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int estimateTokenCountInText(String text) {
        return estimateTokenCountInMessages(singletonList(UserMessage.from(text)));
    }

    @Override
    public int estimateTokenCountInMessage(ChatMessage message) {
        return estimateTokenCountInMessages(singletonList(message));
    }

    @Override
    public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
        List<ChatMessage> allMessages = new LinkedList<>();
        messages.forEach(allMessages::add);

        // Create a chat request with maxOutputTokens=0 to only count input tokens
        ChatRequest chatRequest = ChatRequest.builder()
                .modelName(modelName)
                .messages(allMessages)
                .maxOutputTokens(0) // Key: set to 0 to only count input tokens
                .build();

        // Create Anthropic request using existing helper
        AnthropicCreateMessageRequest anthropicRequest = createAnthropicRequest(
                chatRequest,
                null, // no thinking
                NO_CACHE,
                NO_CACHE,
                false // no streaming
        );

        return estimateTokenCount(anthropicRequest);
    }

    /**
     * Estimates the count of tokens in tool execution requests.
     *
     * @param toolExecutionRequests the tool execution requests.
     * @return the estimated count of tokens.
     */
    public int estimateTokenCountInToolExecutionRequests(Iterable<ToolExecutionRequest> toolExecutionRequests) {
        List<ToolExecutionRequest> allToolRequests = new LinkedList<>();
        toolExecutionRequests.forEach(allToolRequests::add);

        return estimateTokenCountInMessage(AiMessage.from(allToolRequests));
    }

    /**
     * Estimates the count of tokens in tool specifications.
     *
     * @param toolSpecifications the tool specifications.
     * @return the estimated count of tokens.
     */
    public int estimateTokenCountInToolSpecifications(Iterable<ToolSpecification> toolSpecifications) {
        List<ToolSpecification> allTools = new LinkedList<>();
        toolSpecifications.forEach(allTools::add);

        // Create a dummy message with tools to count tool specification tokens
        ChatRequest chatRequest = ChatRequest.builder()
                .modelName(modelName)
                .messages(singletonList(UserMessage.from("dummy"))) // dummy content
                .toolSpecifications(allTools)
                .maxOutputTokens(0)
                .build();

        AnthropicCreateMessageRequest anthropicRequest = createAnthropicRequest(
                chatRequest,
                null,
                NO_CACHE,
                NO_CACHE,
                false
        );

        // Subtract the tokens for the dummy content (approximately 1 token for "dummy")
        return estimateTokenCount(anthropicRequest) - 1;
    }

    private int estimateTokenCount(AnthropicCreateMessageRequest request) {
        AnthropicCreateMessageResponse response = withRetryMappingExceptions(
                () -> this.client.createMessage(request), this.maxRetries);
        
        if (response.usage == null || response.usage.inputTokens == null) {
            throw new RuntimeException("Failed to get token count from Anthropic API response");
        }
        
        return response.usage.inputTokens;
    }

    public static class Builder {

        private HttpClientBuilder httpClientBuilder;
        private String modelName;
        private String apiKey;
        private String baseUrl;
        private String version;
        private String beta;
        private Boolean logRequests;
        private Boolean logResponses;
        private Duration timeout;
        private Integer maxRetries;

        Builder() {}

        public Builder httpClientBuilder(HttpClientBuilder httpClientBuilder) {
            this.httpClientBuilder = httpClientBuilder;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder modelName(AnthropicChatModelName modelName) {
            this.modelName = modelName.toString();
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder beta(String beta) {
            this.beta = beta;
            return this;
        }

        public Builder logRequests(Boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        public Builder logResponses(Boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public AnthropicTokenCountEstimator build() {
            return new AnthropicTokenCountEstimator(this);
        }
    }
} 