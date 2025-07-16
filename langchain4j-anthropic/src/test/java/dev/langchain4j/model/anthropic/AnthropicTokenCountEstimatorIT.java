package dev.langchain4j.model.anthropic;

import static dev.langchain4j.data.message.AiMessage.aiMessage;
import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.anthropic.AnthropicChatModelName.CLAUDE_3_5_HAIKU_20241022;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class AnthropicTokenCountEstimatorIT {

    private static final String ANTHROPIC_API_KEY = System.getenv("ANTHROPIC_API_KEY");

    @Test
    void should_estimate_token_count_accurately_for_simple_text() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        AnthropicChatModel model = AnthropicChatModel.builder()
                .apiKey(ANTHROPIC_API_KEY)
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .maxTokens(1) // minimal output to save tokens
                .logRequests(true)
                .logResponses(true)
                .build();

        String text = "Hello, how are you today?";

        // when
        int estimatedTokens = tokenCountEstimator.estimateTokenCountInText(text);
        var response = model.chat(userMessage(text));
        int actualInputTokens = response.tokenUsage().inputTokenCount();

        // then
        assertThat(estimatedTokens).isEqualTo(actualInputTokens);
    }

    @Test
    void should_estimate_token_count_accurately_for_messages() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        AnthropicChatModel model = AnthropicChatModel.builder()
                .apiKey(ANTHROPIC_API_KEY)
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .maxTokens(1)
                .logRequests(true)
                .logResponses(true)
                .build();

        List<ChatMessage> messages = Arrays.asList(
                systemMessage("You are a helpful assistant."),
                userMessage("What is 2+2?"),
                aiMessage("2+2 equals 4.")
        );

        // when
        int estimatedTokens = tokenCountEstimator.estimateTokenCountInMessages(messages);
        var response = model.chat(messages);
        int actualInputTokens = response.tokenUsage().inputTokenCount();

        // then
        assertThat(estimatedTokens).isEqualTo(actualInputTokens);
    }

    @Test
    void should_estimate_token_count_accurately_for_tools() {
        // given
        AnthropicTokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        AnthropicChatModel model = AnthropicChatModel.builder()
                .apiKey(ANTHROPIC_API_KEY)
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .maxTokens(1)
                .logRequests(true)
                .logResponses(true)
                .build();

        ToolSpecification calculator = ToolSpecification.builder()
                .name("calculator")
                .description("Returns a sum of two numbers")
                .parameters(JsonObjectSchema.builder()
                        .addIntegerProperty("first")
                        .addIntegerProperty("second")
                        .required("first", "second")
                        .build())
                .build();

        List<ToolSpecification> toolSpecifications = Arrays.asList(calculator);

        // when
        int estimatedTokens = tokenCountEstimator.estimateTokenCountInToolSpecifications(toolSpecifications);
        
        // Create a request with tools to get actual token count
        ChatRequest request = ChatRequest.builder()
                .messages(userMessage("Calculate 2+2"))
                .toolSpecifications(toolSpecifications)
                .build();
        var response = model.chat(request);
        int actualInputTokens = response.tokenUsage().inputTokenCount();

        // then
        // The estimated tokens should be close to the actual tokens (within 10% tolerance)
        // We subtract some tokens for the user message content
        int expectedTokens = actualInputTokens - tokenCountEstimator.estimateTokenCountInText("Calculate 2+2");
        assertThat(estimatedTokens).isCloseTo(expectedTokens, withPercentage(10));
    }

    @Test
    void should_estimate_token_count_accurately_for_tool_execution_requests() {
        // given
        AnthropicTokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        List<ToolExecutionRequest> toolRequests = Arrays.asList(
                ToolExecutionRequest.builder()
                        .id("1")
                        .name("calculator")
                        .arguments("{\"first\": 2, \"second\": 3}")
                        .build()
        );

        // when
        int estimatedTokens = tokenCountEstimator.estimateTokenCountInToolExecutionRequests(toolRequests);

        // then
        assertThat(estimatedTokens).isGreaterThan(0);
        // Note: We can't easily test this against actual API response since tool execution requests
        // are typically part of the AI response, not input
    }

    @Test
    void should_estimate_token_count_for_large_content() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        AnthropicChatModel model = AnthropicChatModel.builder()
                .apiKey(ANTHROPIC_API_KEY)
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .maxTokens(1)
                .logRequests(true)
                .logResponses(true)
                .build();

        String largeText = "This is a large text that contains many words. ".repeat(50);

        // when
        int estimatedTokens = tokenCountEstimator.estimateTokenCountInText(largeText);
        var response = model.chat(userMessage(largeText));
        int actualInputTokens = response.tokenUsage().inputTokenCount();

        // then
        assertThat(estimatedTokens).isEqualTo(actualInputTokens);
        assertThat(estimatedTokens).isGreaterThan(100); // Should be a significant number
    }

    @Test
    void should_estimate_token_count_for_different_model_names() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName("claude-3-5-sonnet-20241022")
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        AnthropicChatModel model = AnthropicChatModel.builder()
                .apiKey(ANTHROPIC_API_KEY)
                .modelName("claude-3-5-sonnet-20241022")
                .maxTokens(1)
                .logRequests(true)
                .logResponses(true)
                .build();

        String text = "Hello world!";

        // when
        int estimatedTokens = tokenCountEstimator.estimateTokenCountInText(text);
        var response = model.chat(userMessage(text));
        int actualInputTokens = response.tokenUsage().inputTokenCount();

        // then
        assertThat(estimatedTokens).isEqualTo(actualInputTokens);
    }

    @Disabled("This test is expensive and should be run manually")
    @Test
    void should_estimate_token_count_for_very_large_content() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        String veryLargeText = "This is a very large text that contains many words. ".repeat(500);

        // when
        int estimatedTokens = tokenCountEstimator.estimateTokenCountInText(veryLargeText);

        // then
        assertThat(estimatedTokens).isGreaterThan(1000);
        assertThat(estimatedTokens).isLessThan(10000); // Should not be unreasonably large
    }
} 