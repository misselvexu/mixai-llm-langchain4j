package dev.langchain4j.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.langchain4j.data.message.AiMessage.aiMessage;
import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiServicesMessagesTest {

    @Test
    void should_support_messages_annotation_with_list() {
        // given
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse response = ChatResponse.builder()
                .aiMessage(aiMessage("Hello from AI"))
                .build();
        when(chatModel.chat(any())).thenReturn(response);

        ChatService chatService = AiServices.create(ChatService.class, chatModel);

        List<ChatMessage> messages = List.of(
                systemMessage("You are a helpful assistant"),
                userMessage("Hello"),
                aiMessage("Hi there!"),
                userMessage("How are you?")
        );

        // when
        String result = chatService.chat(messages);

        // then
        assertThat(result).isEqualTo("Hello from AI");
    }

    @Test
    void should_support_messages_annotation_with_array() {
        // given
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse response = ChatResponse.builder()
                .aiMessage(aiMessage("Hello from AI"))
                .build();
        when(chatModel.chat(any())).thenReturn(response);

        ChatService chatService = AiServices.create(ChatService.class, chatModel);

        ChatMessage[] messages = {
                systemMessage("You are a helpful assistant"),
                userMessage("Hello"),
                aiMessage("Hi there!"),
                userMessage("How are you?")
        };

        // when
        String result = chatService.chat(messages);

        // then
        assertThat(result).isEqualTo("Hello from AI");
    }

    interface ChatService {
        String chat(@Messages List<ChatMessage> messages);
        
        String chat(@Messages ChatMessage... messages);
    }
} 