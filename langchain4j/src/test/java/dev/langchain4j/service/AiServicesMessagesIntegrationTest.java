package dev.langchain4j.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.TestChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServicesMessagesIntegrationTest {

    private final ChatModel chatModel = new TestChatModel();

    @Test
    void should_handle_basic_messages_list() {
        // Given
        interface ChatAssistant {
            String chat(@Messages List<ChatMessage> messages);
        }

        ChatAssistant assistant = AiServices.create(ChatAssistant.class, chatModel);

        List<ChatMessage> messages = List.of(
            SystemMessage.from("You are a helpful assistant"),
            UserMessage.from("Hello"),
            AiMessage.from("Hi there!"),
            UserMessage.from("How are you?")
        );

        // When
        String response = assistant.chat(messages);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_handle_array_based_messages() {
        // Given
        interface ChatAssistant {
            String chat(@Messages ChatMessage... messages);
        }

        ChatAssistant assistant = AiServices.create(ChatAssistant.class, chatModel);

        ChatMessage[] messages = {
            SystemMessage.from("You are a helpful assistant"),
            UserMessage.from("Hello"),
            AiMessage.from("Hi there!"),
            UserMessage.from("How are you?")
        };

        // When
        String response = assistant.chat(messages);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_handle_memory_integration() {
        // Given
        interface MemoryAwareAssistant {
            @Messages(addToMemory = true)
            String chat(List<ChatMessage> messages);
        }

        MemoryAwareAssistant assistant = AiServices.create(MemoryAwareAssistant.class, chatModel);

        List<ChatMessage> messages = List.of(
            UserMessage.from("My name is Alice"),
            AiMessage.from("Nice to meet you, Alice!"),
            UserMessage.from("I like programming")
        );

        // When
        String response = assistant.chat(messages);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_handle_system_message_integration() {
        // Given
        interface SystemMessageAssistant {
            @SystemMessage("You are a helpful assistant specialized in weather information")
            @Messages(includeSystemMessage = true)
            String chat(List<ChatMessage> messages);
        }

        SystemMessageAssistant assistant = AiServices.create(SystemMessageAssistant.class, chatModel);

        List<ChatMessage> messages = List.of(
            UserMessage.from("What's the weather like in Paris?"),
            AiMessage.from("I don't have real-time weather data, but I can tell you about Paris weather patterns."),
            UserMessage.from("Tell me about the climate")
        );

        // When
        String response = assistant.chat(messages);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_handle_flexible_order() {
        // Given
        interface FlexibleAssistant {
            @Messages(validateOrder = false)
            String chat(List<ChatMessage> messages);
        }

        FlexibleAssistant assistant = AiServices.create(FlexibleAssistant.class, chatModel);

        // Messages in non-standard order (useful for RAG scenarios)
        List<ChatMessage> messages = List.of(
            UserMessage.from("What is Java?"),
            SystemMessage.from("You are a helpful assistant. Use the following context to answer questions."),
            UserMessage.from("Context: Java is a programming language created by Sun Microsystems in 1995.")
        );

        // When
        String response = assistant.chat(messages);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_handle_mixed_annotations() {
        // Given
        interface MixedAssistant {
            @SystemMessage("You are a helpful assistant")
            String chat(@Messages List<ChatMessage> history, @UserMessage String currentMessage);
        }

        MixedAssistant assistant = AiServices.create(MixedAssistant.class, chatModel);

        List<ChatMessage> history = List.of(
            UserMessage.from("Hello"),
            AiMessage.from("Hi there!")
        );

        // When
        String response = assistant.chat(history, "How are you today?");

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_handle_rag_scenario() {
        // Given
        interface RAGAssistant {
            @Messages(validateOrder = false)
            String answerWithContext(List<ChatMessage> contextAndQuestion);
        }

        RAGAssistant assistant = AiServices.create(RAGAssistant.class, chatModel);

        // Simulate retrieved documents
        List<String> retrievedDocuments = List.of(
            "Java is a high-level, class-based, object-oriented programming language.",
            "Java was originally developed by James Gosling at Sun Microsystems.",
            "Java applications are typically compiled to bytecode that can run on any Java virtual machine."
        );

        // Build context from retrieved documents
        List<ChatMessage> contextAndQuestion = new ArrayList<>();
        contextAndQuestion.add(SystemMessage.from("You are a helpful assistant. Use the following context to answer questions."));

        // Add retrieved document content as context
        for (String document : retrievedDocuments) {
            contextAndQuestion.add(UserMessage.from("Context: " + document));
        }

        // Add the actual question
        contextAndQuestion.add(UserMessage.from("Question: Who developed Java?"));

        // When
        String answer = assistant.answerWithContext(contextAndQuestion);

        // Then
        assertThat(answer).isNotNull();
        assertThat(answer).isNotEmpty();
    }

    @Test
    void should_handle_proxy_service_scenario() {
        // Given
        interface ProxyChatService {
            @Messages(addToMemory = true)
            String processExternalChat(List<ChatMessage> externalHistory);
        }

        ProxyChatService proxy = AiServices.create(ProxyChatService.class, chatModel);

        // Simulate external chat history (e.g., from a database or API)
        List<ChatMessage> externalHistory = List.of(
            UserMessage.from("Hello"),
            AiMessage.from("Hi! How can I help you?"),
            UserMessage.from("I need help with Java"),
            AiMessage.from("I'd be happy to help with Java! What specific question do you have?"),
            UserMessage.from("How do I create a class?")
        );

        // When
        String response = proxy.processExternalChat(externalHistory);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_handle_multi_turn_conversation() {
        // Given
        interface ConversationAssistant {
            @SystemMessage("You are a helpful assistant. Maintain context across messages.")
            @Messages(includeSystemMessage = true, addToMemory = true)
            String continueConversation(List<ChatMessage> conversationHistory);
        }

        ConversationAssistant assistant = AiServices.create(ConversationAssistant.class, chatModel);

        List<ChatMessage> history = List.of(
            UserMessage.from("My name is Alice"),
            AiMessage.from("Nice to meet you, Alice!"),
            UserMessage.from("I like programming"),
            AiMessage.from("That's great! What programming languages do you know?"),
            UserMessage.from("Java and Python"),
            AiMessage.from("Excellent choices! Both are very popular languages."),
            UserMessage.from("Which one should I learn first?")
        );

        // When
        String response = assistant.continueConversation(history);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_validate_message_order_when_enabled() {
        // Given
        interface RobustAssistant {
            @Messages(validateOrder = true)
            String chat(List<ChatMessage> messages);
        }

        RobustAssistant assistant = AiServices.create(RobustAssistant.class, chatModel);

        // Invalid message order
        List<ChatMessage> invalidMessages = List.of(
            AiMessage.from("This should not be first"),
            UserMessage.from("This should be first")
        );

        // When & Then
        assertThatThrownBy(() -> assistant.chat(invalidMessages))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid message order");
    }

    @Test
    void should_handle_empty_message_list() {
        // Given
        interface Assistant {
            String chat(@Messages List<ChatMessage> messages);
        }

        Assistant assistant = AiServices.create(Assistant.class, chatModel);

        List<ChatMessage> emptyMessages = List.of();

        // When
        String response = assistant.chat(emptyMessages);

        // Then
        assertThat(response).isNotNull();
        // Note: Empty message list may not produce meaningful results
        // but should not throw an exception
    }

    @Test
    void should_handle_single_message() {
        // Given
        interface Assistant {
            String chat(@Messages List<ChatMessage> messages);
        }

        Assistant assistant = AiServices.create(Assistant.class, chatModel);

        List<ChatMessage> singleMessage = List.of(
            UserMessage.from("Hello")
        );

        // When
        String response = assistant.chat(singleMessage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }

    @Test
    void should_handle_large_message_list() {
        // Given
        interface EfficientAssistant {
            @Messages(addToMemory = false) // Disable memory for large lists
            String chat(List<ChatMessage> messages);
        }

        EfficientAssistant assistant = AiServices.create(EfficientAssistant.class, chatModel);

        // Create a large message list
        List<ChatMessage> largeMessageList = new ArrayList<>();
        largeMessageList.add(SystemMessage.from("You are a helpful assistant"));

        for (int i = 0; i < 10; i++) {
            largeMessageList.add(UserMessage.from("Message " + i));
            largeMessageList.add(AiMessage.from("Response " + i));
        }

        largeMessageList.add(UserMessage.from("Final question: What is the sum of all message numbers?"));

        // When
        String response = assistant.chat(largeMessageList);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }
} 