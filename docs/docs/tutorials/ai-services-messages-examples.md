---
sidebar_position: 7
---

# AI Services with @Messages Examples

This document provides comprehensive examples of using the `@Messages` annotation in AI Services for various real-world scenarios.

## Overview

The `@Messages` annotation enables dynamic message handling in AI Services, allowing you to pass pre-constructed chat message sequences directly to the underlying ChatModel. This is particularly useful for:

- Proxy services handling external conversation history
- Multi-turn conversations with complex message sequences
- RAG applications with dynamic context injection
- Programmatically constructed message sequences

## Basic Examples

### Simple Message List

```java
interface ChatAssistant {
    String chat(@Messages List<ChatMessage> messages);
}

// Usage
ChatModel model = OpenAiChatModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName(GPT_4_O_MINI)
    .build();

ChatAssistant assistant = AiServices.create(ChatAssistant.class, model);

List<ChatMessage> messages = List.of(
    SystemMessage.from("You are a helpful assistant"),
    UserMessage.from("Hello"),
    AiMessage.from("Hi there! How can I help you today?"),
    UserMessage.from("What's the weather like?")
);

String response = assistant.chat(messages);
```

### Array-based Messages

```java
interface ChatAssistant {
    String chat(@Messages ChatMessage... messages);
}

// Usage
ChatMessage[] messages = {
    SystemMessage.from("You are a helpful assistant"),
    UserMessage.from("Hello"),
    AiMessage.from("Hi there!"),
    UserMessage.from("What's the weather like?")
};

String response = assistant.chat(messages);
```

## Advanced Configuration Examples

### With Memory Integration

```java
interface MemoryAwareAssistant {
    @Messages(addToMemory = true)
    String chat(List<ChatMessage> messages);
}

// Messages will be automatically added to chat memory after processing
MemoryAwareAssistant assistant = AiServices.create(MemoryAwareAssistant.class, model);
```

### With System Message Integration

```java
interface SystemMessageAssistant {
    @SystemMessage("You are a helpful assistant specialized in weather information")
    @Messages(includeSystemMessage = true)
    String chat(List<ChatMessage> messages);
}

// The system message will be prepended to the provided messages
SystemMessageAssistant assistant = AiServices.create(SystemMessageAssistant.class, model);
```

### Disabling Order Validation

```java
interface FlexibleAssistant {
    @Messages(validateOrder = false)
    String chat(List<ChatMessage> messages);
}

// Allows messages in any order (useful for certain RAG scenarios)
FlexibleAssistant assistant = AiServices.create(FlexibleAssistant.class, model);
```

## Real-World Use Cases

### Proxy Service for External Chat History

```java
interface ProxyChatService {
    @Messages(addToMemory = true)
    String processExternalChat(List<ChatMessage> externalHistory);
}

// Usage in a proxy service
ProxyChatService proxy = AiServices.create(ProxyChatService.class, model);

// Convert external format to ChatMessage list
List<ChatMessage> externalMessages = convertFromExternalFormat(externalData);
String response = proxy.processExternalChat(externalMessages);
```

### Multi-turn Conversation with Context

```java
interface ConversationAssistant {
    @SystemMessage("You are a helpful assistant. Maintain context across messages.")
    @Messages(includeSystemMessage = true, addToMemory = true)
    String continueConversation(List<ChatMessage> conversationHistory);
}

// Usage
ConversationAssistant assistant = AiServices.create(ConversationAssistant.class, model);

List<ChatMessage> history = List.of(
    UserMessage.from("My name is Alice"),
    AiMessage.from("Nice to meet you, Alice!"),
    UserMessage.from("I like programming"),
    AiMessage.from("That's great! What programming languages do you know?"),
    UserMessage.from("Java and Python")
);

String response = assistant.continueConversation(history);
```

### RAG with Dynamic Context

```java
interface RAGAssistant {
    @Messages(validateOrder = false)
    String answerWithContext(List<ChatMessage> contextAndQuestion);
}

// Usage
RAGAssistant assistant = AiServices.create(RAGAssistant.class, model);

// Build context from retrieved documents
List<ChatMessage> contextAndQuestion = new ArrayList<>();
contextAndQuestion.add(SystemMessage.from("You are a helpful assistant. Use the following context to answer questions."));

// Add retrieved document content as context
for (Content document : retrievedDocuments) {
    contextAndQuestion.add(UserMessage.from("Context: " + document.text()));
}

// Add the actual question
contextAndQuestion.add(UserMessage.from("Question: " + userQuestion));

String answer = assistant.answerWithContext(contextAndQuestion);
```

### Mixed Annotations

```java
interface MixedAssistant {
    @SystemMessage("You are a helpful assistant")
    String chat(@Messages List<ChatMessage> history, @UserMessage String currentMessage);
}

// Usage
MixedAssistant assistant = AiServices.create(MixedAssistant.class, model);

List<ChatMessage> history = List.of(
    UserMessage.from("Hello"),
    AiMessage.from("Hi there!")
);

String response = assistant.chat(history, "How are you today?");
```

## Error Handling Examples

### Invalid Message Types

```java
interface RobustAssistant {
    @Messages(validateOrder = true)
    String chat(List<ChatMessage> messages);
}

// This will throw an exception if messages are not in proper order
try {
    List<ChatMessage> invalidMessages = List.of(
        AiMessage.from("This should not be first"),
        UserMessage.from("This should be first")
    );
    String response = assistant.chat(invalidMessages);
} catch (IllegalArgumentException e) {
    // Handle validation error
    System.err.println("Invalid message order: " + e.getMessage());
}
```

### Empty Message List

```java
interface Assistant {
    String chat(@Messages List<ChatMessage> messages);
}

// This will work but may not produce meaningful results
List<ChatMessage> emptyMessages = List.of();
String response = assistant.chat(emptyMessages);
```

## Performance Considerations

### Large Message Lists

```java
interface EfficientAssistant {
    @Messages(addToMemory = false) // Disable memory for large lists
    String chat(List<ChatMessage> messages);
}

// For large conversation histories, consider:
// 1. Disabling memory integration
// 2. Truncating history to recent messages
// 3. Using streaming for real-time responses
```

### Memory Management

```java
interface MemoryOptimizedAssistant {
    @Messages(addToMemory = true, validateOrder = false)
    String chat(List<ChatMessage> messages);
}

// Consider implementing custom memory management for very long conversations
```

## Best Practices

1. **Use `addToMemory = true`** when you want to maintain conversation state
2. **Use `includeSystemMessage = true`** when combining with `@SystemMessage`
3. **Use `validateOrder = false`** for RAG scenarios with dynamic context
4. **Handle large message lists** carefully to avoid token limits
5. **Consider streaming** for real-time applications
6. **Validate input** before passing to AI Service

## Integration with Other Features

### With Tools

```java
interface ToolEnabledAssistant {
    @Messages(addToMemory = true)
    String chat(List<ChatMessage> messages);
}

// Tools can still be used with @Messages
ToolEnabledAssistant assistant = AiServices.builder(ToolEnabledAssistant.class)
    .chatModel(model)
    .tools(List.of(new WeatherTool()))
    .build();
```

### With Structured Outputs

```java
interface StructuredAssistant {
    @Messages(addToMemory = true)
    Result<WeatherInfo> getWeatherInfo(List<ChatMessage> messages);
}

// Structured outputs work with @Messages
StructuredAssistant assistant = AiServices.create(StructuredAssistant.class, model);
Result<WeatherInfo> result = assistant.getWeatherInfo(messages);
```

This comprehensive guide should help you effectively use the `@Messages` annotation in your AI Services applications. 