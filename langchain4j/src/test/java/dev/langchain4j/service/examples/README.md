# @Messages Annotation Examples

This directory contains comprehensive examples demonstrating the usage of the `@Messages` annotation in LangChain4j AI Services.

## Overview

The `@Messages` annotation enables dynamic message handling in AI Services, allowing you to pass pre-constructed chat message sequences directly to the underlying ChatModel. This is particularly useful for:

- Proxy services handling external conversation history
- Multi-turn conversations with complex message sequences
- RAG applications with dynamic context injection
- Programmatically constructed message sequences

## Examples Included

### 1. BasicUsageExample
Demonstrates the simplest usage of `@Messages` with a `List<ChatMessage>` parameter.

```java
interface ChatAssistant {
    String chat(@Messages List<ChatMessage> messages);
}
```

### 2. ArrayBasedExample
Shows how to use `@Messages` with varargs (`ChatMessage...`).

```java
interface ChatAssistant {
    String chat(@Messages ChatMessage... messages);
}
```

### 3. MemoryIntegrationExample
Demonstrates memory integration with `addToMemory = true`.

```java
interface MemoryAwareAssistant {
    @Messages(addToMemory = true)
    String chat(List<ChatMessage> messages);
}
```

### 4. SystemMessageIntegrationExample
Shows how to combine `@Messages` with `@SystemMessage` annotation.

```java
interface SystemMessageAssistant {
    @SystemMessage("You are a helpful assistant specialized in weather information")
    @Messages(includeSystemMessage = true)
    String chat(List<ChatMessage> messages);
}
```

### 5. FlexibleOrderExample
Demonstrates disabling message order validation for flexible message sequences.

```java
interface FlexibleAssistant {
    @Messages(validateOrder = false)
    String chat(List<ChatMessage> messages);
}
```

### 6. MixedAnnotationsExample
Shows how to combine `@Messages` with other annotations like `@UserMessage`.

```java
interface MixedAssistant {
    @SystemMessage("You are a helpful assistant")
    String chat(@Messages List<ChatMessage> history, @UserMessage String currentMessage);
}
```

### 7. RAGExample
Demonstrates RAG (Retrieval-Augmented Generation) with dynamic context injection.

```java
interface RAGAssistant {
    @Messages(validateOrder = false)
    String answerWithContext(List<ChatMessage> contextAndQuestion);
}
```

### 8. ProxyServiceExample
Shows how to use `@Messages` in proxy services for external chat history.

```java
interface ProxyChatService {
    @Messages(addToMemory = true)
    String processExternalChat(List<ChatMessage> externalHistory);
}
```

### 9. MultiTurnConversationExample
Demonstrates multi-turn conversations with context maintenance.

```java
interface ConversationAssistant {
    @SystemMessage("You are a helpful assistant. Maintain context across messages.")
    @Messages(includeSystemMessage = true, addToMemory = true)
    String continueConversation(List<ChatMessage> conversationHistory);
}
```

### 10. ErrorHandlingExample
Shows error handling with message order validation.

```java
interface RobustAssistant {
    @Messages(validateOrder = true)
    String chat(List<ChatMessage> messages);
}
```

## Configuration Options

The `@Messages` annotation provides several configuration options:

- **`addToMemory`** (default: `false`): Whether to add messages to chat memory after processing
- **`validateOrder`** (default: `true`): Whether to validate message order and structure
- **`includeSystemMessage`** (default: `false`): Whether to include system messages from `@SystemMessage` annotation

## Usage

To run these examples, you need to:

1. Provide a `ChatModel` implementation
2. Call the appropriate `demonstrate()` method with your model

Example:
```java
ChatModel model = OpenAiChatModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName(GPT_4_O_MINI)
    .build();

MessagesAnnotationExamples.BasicUsageExample.demonstrate(model);
```

## Key Features

- **Dynamic Message Handling**: Pass pre-constructed message sequences
- **Memory Integration**: Optional chat memory integration
- **System Message Support**: Combine with `@SystemMessage` annotations
- **Order Validation**: Configurable message order validation
- **Flexible Usage**: Support for both `List<ChatMessage>` and `ChatMessage...`
- **Error Handling**: Built-in validation and error handling

## Best Practices

1. Use `addToMemory = true` when you want to maintain conversation state
2. Use `includeSystemMessage = true` when combining with `@SystemMessage`
3. Use `validateOrder = false` for RAG scenarios with dynamic context
4. Handle large message lists carefully to avoid token limits
5. Consider streaming for real-time applications
6. Validate input before passing to AI Service

## Related Documentation

- [AI Services Tutorial](../../../../../docs/docs/tutorials/ai-services.md)
- [@Messages Examples Documentation](../../../../../docs/docs/tutorials/ai-services-messages-examples.md)
- [Chat and Language Models Tutorial](../../../../../docs/docs/tutorials/chat-and-language-models.md) 