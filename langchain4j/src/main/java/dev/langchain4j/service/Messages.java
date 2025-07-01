package dev.langchain4j.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a dynamic list of chat messages to be used for AI service invocation.
 * This annotation allows passing a list of {@link dev.langchain4j.data.message.ChatMessage} objects
 * directly to the AI service, bypassing the template-based message construction.
 * <br>
 * This is particularly useful for:
 * <ul>
 *     <li>Proxy services that receive dynamic conversation history</li>
 *     <li>Multi-turn conversations with complex message sequences</li>
 *     <li>Scenarios where messages are programmatically constructed</li>
 *     <li>RAG applications with dynamic context injection</li>
 * </ul>
 * <br>
 * Example usage:
 * <pre>
 * interface ChatService {
 *     String chat(@Messages List&lt;ChatMessage&gt; messages);
 *     
 *     // With system message and dynamic messages
 *     @SystemMessage("You are a helpful assistant")
 *     String chat(@Messages List&lt;ChatMessage&gt; history, @UserMessage String currentMessage);
 * }
 * </pre>
 * <br>
 * When using @Messages:
 * <ul>
 *     <li>Template variables from @UserMessage and @SystemMessage are ignored for @Messages parameters</li>
 *     <li>Messages are sent directly to the underlying ChatModel</li>
 *     <li>ChatMemory integration can be controlled via addToMemory()</li>
 *     <li>Message order validation can be enabled/disabled</li>
 * </ul>
 * 
 * @see UserMessage
 * @see SystemMessage
 * @see dev.langchain4j.data.message.ChatMessage
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Messages {

    /**
     * Whether to add the messages to the chat memory after processing.
     * Default is false to avoid duplicate entries when messages are already in memory.
     * 
     * @return true if messages should be added to memory, false otherwise
     */
    boolean addToMemory() default false;

    /**
     * Whether to validate the order and structure of messages.
     * When true, ensures messages follow proper conversation flow.
     * Default is true for safety.
     * 
     * @return true if message validation should be performed, false otherwise
     */
    boolean validateOrder() default true;

    /**
     * Whether to include system messages from the @SystemMessage annotation
     * when processing the dynamic message list.
     * Default is true to maintain consistency with existing behavior.
     * 
     * @return true if system messages should be included, false otherwise
     */
    boolean includeSystemMessage() default true;
} 