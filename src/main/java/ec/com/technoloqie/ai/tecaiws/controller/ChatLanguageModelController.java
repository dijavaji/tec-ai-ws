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
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;

/**
 * 
 * utilizado para ejemplo de https://docs.langchain4j.dev/integrations/language-models/hugging-face
 */
@RestController
@RequestMapping("/api/ai")
public class ChatLanguageModelController {
	
	private HuggingFaceChatModel hfchatModel;
	
	private ChatLanguageModel chatLanguageModel;

    ChatLanguageModelController(HuggingFaceChatModel hfchatModel, ChatLanguageModel chatLanguageModel) {
        this.hfchatModel = hfchatModel;
        this.chatLanguageModel = chatLanguageModel;
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
}
