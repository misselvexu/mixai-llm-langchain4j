package dev.langchain4j.service.examples;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Messages;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;

import java.util.ArrayList;
import java.util.List;

/**
 * Examples demonstrating the usage of @Messages annotation in AI Services.
 * 
 * This class provides practical examples of how to use the @Messages annotation
 * for various scenarios including dynamic message handling, proxy services,
 * multi-turn conversations, and RAG applications.
 */
public class MessagesAnnotationExamples {

    /**
     * Example 1: Basic usage with List<ChatMessage>
     */
    public static class BasicUsageExample {
        
        interface ChatAssistant {
            String chat(@Messages List<ChatMessage> messages);
        }
        
        public static void demonstrate(ChatModel model) {
            ChatAssistant assistant = AiServices.create(ChatAssistant.class, model);
            
            List<ChatMessage> messages = List.of(
                SystemMessage.from("You are a helpful assistant"),
                UserMessage.from("Hello"),
                AiMessage.from("Hi there! How can I help you today?"),
                UserMessage.from("What's the weather like?")
            );
            
            String response = assistant.chat(messages);
            System.out.println("Basic usage response: " + response);
        }
    }
    
    /**
     * Example 2: Array-based messages
     */
    public static class ArrayBasedExample {
        
        interface ChatAssistant {
            String chat(@Messages ChatMessage... messages);
        }
        
        public static void demonstrate(ChatModel model) {
            ChatAssistant assistant = AiServices.create(ChatAssistant.class, model);
            
            ChatMessage[] messages = {
                SystemMessage.from("You are a helpful assistant"),
                UserMessage.from("Hello"),
                AiMessage.from("Hi there!"),
                UserMessage.from("What's the weather like?")
            };
            
            String response = assistant.chat(messages);
            System.out.println("Array-based response: " + response);
        }
    }
    
    /**
     * Example 3: With memory integration
     */
    public static class MemoryIntegrationExample {
        
        interface MemoryAwareAssistant {
            @Messages(addToMemory = true)
            String chat(List<ChatMessage> messages);
        }
        
        public static void demonstrate(ChatModel model) {
            MemoryAwareAssistant assistant = AiServices.create(MemoryAwareAssistant.class, model);
            
            List<ChatMessage> messages = List.of(
                UserMessage.from("My name is Alice"),
                AiMessage.from("Nice to meet you, Alice!"),
                UserMessage.from("I like programming")
            );
            
            String response = assistant.chat(messages);
            System.out.println("Memory integration response: " + response);
        }
    }
    
    /**
     * Example 4: With system message integration
     */
    public static class SystemMessageIntegrationExample {
        
        interface SystemMessageAssistant {
            @SystemMessage("You are a helpful assistant specialized in weather information")
            @Messages(includeSystemMessage = true)
            String chat(List<ChatMessage> messages);
        }
        
        public static void demonstrate(ChatModel model) {
            SystemMessageAssistant assistant = AiServices.create(SystemMessageAssistant.class, model);
            
            List<ChatMessage> messages = List.of(
                UserMessage.from("What's the weather like in Paris?"),
                AiMessage.from("I don't have real-time weather data, but I can tell you about Paris weather patterns."),
                UserMessage.from("Tell me about the climate")
            );
            
            String response = assistant.chat(messages);
            System.out.println("System message integration response: " + response);
        }
    }
    
    /**
     * Example 5: Disabling order validation
     */
    public static class FlexibleOrderExample {
        
        interface FlexibleAssistant {
            @Messages(validateOrder = false)
            String chat(List<ChatMessage> messages);
        }
        
        public static void demonstrate(ChatModel model) {
            FlexibleAssistant assistant = AiServices.create(FlexibleAssistant.class, model);
            
            // Messages in non-standard order (useful for RAG scenarios)
            List<ChatMessage> messages = List.of(
                UserMessage.from("What is Java?"),
                SystemMessage.from("You are a helpful assistant. Use the following context to answer questions."),
                UserMessage.from("Context: Java is a programming language created by Sun Microsystems in 1995.")
            );
            
            String response = assistant.chat(messages);
            System.out.println("Flexible order response: " + response);
        }
    }
    
    /**
     * Example 6: Mixed annotations
     */
    public static class MixedAnnotationsExample {
        
        interface MixedAssistant {
            @SystemMessage("You are a helpful assistant")
            String chat(@Messages List<ChatMessage> history, @UserMessage String currentMessage);
        }
        
        public static void demonstrate(ChatModel model) {
            MixedAssistant assistant = AiServices.create(MixedAssistant.class, model);
            
            List<ChatMessage> history = List.of(
                UserMessage.from("Hello"),
                AiMessage.from("Hi there!")
            );
            
            String response = assistant.chat(history, "How are you today?");
            System.out.println("Mixed annotations response: " + response);
        }
    }
    
    /**
     * Example 7: RAG with dynamic context
     */
    public static class RAGExample {
        
        interface RAGAssistant {
            @Messages(validateOrder = false)
            String answerWithContext(List<ChatMessage> contextAndQuestion);
        }
        
        public static void demonstrate(ChatModel model) {
            RAGAssistant assistant = AiServices.create(RAGAssistant.class, model);
            
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
            
            String answer = assistant.answerWithContext(contextAndQuestion);
            System.out.println("RAG response: " + answer);
        }
    }
    
    /**
     * Example 8: Proxy service for external chat history
     */
    public static class ProxyServiceExample {
        
        interface ProxyChatService {
            @Messages(addToMemory = true)
            String processExternalChat(List<ChatMessage> externalHistory);
        }
        
        public static void demonstrate(ChatModel model) {
            ProxyChatService proxy = AiServices.create(ProxyChatService.class, model);
            
            // Simulate external chat history (e.g., from a database or API)
            List<ChatMessage> externalHistory = List.of(
                UserMessage.from("Hello"),
                AiMessage.from("Hi! How can I help you?"),
                UserMessage.from("I need help with Java"),
                AiMessage.from("I'd be happy to help with Java! What specific question do you have?"),
                UserMessage.from("How do I create a class?")
            );
            
            String response = proxy.processExternalChat(externalHistory);
            System.out.println("Proxy service response: " + response);
        }
    }
    
    /**
     * Example 9: Multi-turn conversation with context
     */
    public static class MultiTurnConversationExample {
        
        interface ConversationAssistant {
            @SystemMessage("You are a helpful assistant. Maintain context across messages.")
            @Messages(includeSystemMessage = true, addToMemory = true)
            String continueConversation(List<ChatMessage> conversationHistory);
        }
        
        public static void demonstrate(ChatModel model) {
            ConversationAssistant assistant = AiServices.create(ConversationAssistant.class, model);
            
            List<ChatMessage> history = List.of(
                UserMessage.from("My name is Alice"),
                AiMessage.from("Nice to meet you, Alice!"),
                UserMessage.from("I like programming"),
                AiMessage.from("That's great! What programming languages do you know?"),
                UserMessage.from("Java and Python"),
                AiMessage.from("Excellent choices! Both are very popular languages."),
                UserMessage.from("Which one should I learn first?")
            );
            
            String response = assistant.continueConversation(history);
            System.out.println("Multi-turn conversation response: " + response);
        }
    }
    
    /**
     * Example 10: Error handling with validation
     */
    public static class ErrorHandlingExample {
        
        interface RobustAssistant {
            @Messages(validateOrder = true)
            String chat(List<ChatMessage> messages);
        }
        
        public static void demonstrate(ChatModel model) {
            RobustAssistant assistant = AiServices.create(RobustAssistant.class, model);
            
            try {
                // This will throw an exception if messages are not in proper order
                List<ChatMessage> invalidMessages = List.of(
                    AiMessage.from("This should not be first"),
                    UserMessage.from("This should be first")
                );
                String response = assistant.chat(invalidMessages);
                System.out.println("Response: " + response);
            } catch (IllegalArgumentException e) {
                System.err.println("Validation error caught: " + e.getMessage());
            }
        }
    }
    
    /**
     * Main method to run all examples
     */
    public static void main(String[] args) {
        // Note: This is a demonstration class. In a real application,
        // you would need to provide an actual ChatModel implementation.
        
        System.out.println("Messages Annotation Examples");
        System.out.println("============================");
        System.out.println();
        System.out.println("This class demonstrates various usage patterns for the @Messages annotation.");
        System.out.println("To run these examples, you need to provide a ChatModel implementation.");
        System.out.println();
        System.out.println("Example interfaces and usage patterns:");
        System.out.println("1. BasicUsageExample - Simple List<ChatMessage> usage");
        System.out.println("2. ArrayBasedExample - Array-based messages");
        System.out.println("3. MemoryIntegrationExample - With memory integration");
        System.out.println("4. SystemMessageIntegrationExample - With system message integration");
        System.out.println("5. FlexibleOrderExample - Disabling order validation");
        System.out.println("6. MixedAnnotationsExample - Combining with other annotations");
        System.out.println("7. RAGExample - RAG with dynamic context");
        System.out.println("8. ProxyServiceExample - Proxy service for external chat history");
        System.out.println("9. MultiTurnConversationExample - Multi-turn conversation with context");
        System.out.println("10. ErrorHandlingExample - Error handling with validation");
    }
} 