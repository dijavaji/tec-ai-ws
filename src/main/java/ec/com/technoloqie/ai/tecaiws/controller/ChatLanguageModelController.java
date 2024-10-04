package ec.com.technoloqie.ai.tecaiws.controller;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.service.AiServices;
import ec.com.technoloqie.ai.tecaiws.service.Assistant;

/**
 * 
 * utilizado para ejemplo de https://docs.langchain4j.dev/integrations/language-models/hugging-face
 */
@RestController
@RequestMapping("/api/ai")
public class ChatLanguageModelController {
	
	private HuggingFaceChatModel hfchatModel;
	
	private ChatLanguageModel chatLanguageModel;
	
	private ChatMemory chatMemory;

    ChatLanguageModelController(HuggingFaceChatModel hfchatModel, ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        this.hfchatModel = hfchatModel;
        this.chatLanguageModel = chatLanguageModel;
        this.chatMemory = chatMemory;
    }
    
    //http://localhost:8080/api/ai/model?message=que%20eres
    @GetMapping("/model")
    public String model(@RequestParam(value = "message", defaultValue = "Oye hermano, ¿que haces?") String message) {
    	AiMessage aiMessage = hfchatModel.generate(
                systemMessage("Eres un buen amigo mio, a quien le gusta responder con chistes."),
                userMessage(message)
        ).content();
        return aiMessage.text();
    }
    
    
    /**
     * https://docs.langchain4j.dev/tutorials/chat-and-language-models
     * @return
     */
    @GetMapping("/multi-turn-conversation")
    public ResponseEntity<?> multiTurnConversation() {
    	UserMessage firstUserMessage = UserMessage.from("Hello, my name is Klaus");
    	AiMessage firstAiMessage = chatLanguageModel.generate(firstUserMessage).content(); // Hi Klaus, how can I help you?
    	UserMessage secondUserMessage = UserMessage.from("What is my name?");
    	AiMessage secondAiMessage = chatLanguageModel.generate(firstUserMessage, firstAiMessage, secondUserMessage).content(); // Klaus
        return ResponseEntity.ok(secondAiMessage.toString());
    }
    
    //http://127.0.0.1:8080/api/ai/assistant?message=Hola%20mi%20nombre%20es%20adam
    //http://127.0.0.1:8080/api/ai/assistant?message=quien%20soy%20yo
    //utilizado con chatMemory
    @GetMapping("/assistant")
    public String assistant(@RequestParam String message) {
    	//SystemMessage systemMessage = SystemMessage.from("Eres un asistente de rap. Responde a los mensajes de los usuarios generando versos de rap.");
    	 Assistant assistant = AiServices.builder(Assistant.class)
                 .chatLanguageModel(hfchatModel)
                 .chatMemory(chatMemory)
                 .build();
    	//UserMessage secondUserMessage = UserMessage.from(message);
    	return assistant.chat(message);
    }
    
    
}
