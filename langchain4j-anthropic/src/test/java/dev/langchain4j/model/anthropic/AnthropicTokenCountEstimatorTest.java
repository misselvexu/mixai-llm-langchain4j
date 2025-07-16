package dev.langchain4j.model.anthropic;

import static dev.langchain4j.data.message.AiMessage.aiMessage;
import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.anthropic.AnthropicChatModelName.CLAUDE_3_5_HAIKU_20241022;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class AnthropicTokenCountEstimatorTest {

    private static final String ANTHROPIC_API_KEY = System.getenv("ANTHROPIC_API_KEY");

    @Test
    void should_estimate_token_count_for_text() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        // when
        int count = tokenCountEstimator.estimateTokenCountInText("Hello world!");

        // then
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void should_estimate_token_count_for_a_message() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        // when
        int count = tokenCountEstimator.estimateTokenCountInMessage(userMessage("Hello World!"));

        // then
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void should_estimate_token_count_for_multiple_messages() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        List<ChatMessage> messages = Arrays.asList(
                systemMessage("You are a helpful assistant."),
                userMessage("What is the capital of France?"),
                aiMessage("The capital of France is Paris.")
        );

        // when
        int count = tokenCountEstimator.estimateTokenCountInMessages(messages);

        // then
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void should_estimate_token_count_for_tool_execution_requests() {
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
        int count = tokenCountEstimator.estimateTokenCountInToolExecutionRequests(toolRequests);

        // then
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void should_estimate_token_count_for_tool_specifications() {
        // given
        AnthropicTokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
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
        int count = tokenCountEstimator.estimateTokenCountInToolSpecifications(toolSpecifications);

        // then
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void should_estimate_token_count_for_large_text() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        String largeText = "This is a large text that contains many words. ".repeat(100);

        // when
        int count = tokenCountEstimator.estimateTokenCountInText(largeText);

        // then
        assertThat(count).isGreaterThan(0);
        assertThat(count).isGreaterThan(50); // Should be significantly more than a short text
    }

    @Test
    void should_estimate_token_count_for_empty_text() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        // when
        int count = tokenCountEstimator.estimateTokenCountInText("");

        // then
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    void should_estimate_token_count_for_system_message() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        // when
        int count = tokenCountEstimator.estimateTokenCountInMessage(
                systemMessage("You are a helpful assistant that provides accurate information."));

        // then
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void should_estimate_token_count_for_ai_message() {
        // given
        TokenCountEstimator tokenCountEstimator = AnthropicTokenCountEstimator.builder()
                .modelName(CLAUDE_3_5_HAIKU_20241022)
                .apiKey(ANTHROPIC_API_KEY)
                .logRequests(true)
                .logResponses(true)
                .build();

        // when
        int count = tokenCountEstimator.estimateTokenCountInMessage(
                aiMessage("I can help you with that question."));

        // then
        assertThat(count).isGreaterThan(0);
    }
} 