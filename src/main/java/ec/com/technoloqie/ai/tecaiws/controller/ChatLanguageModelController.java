package ec.com.technoloqie.ai.tecaiws.controller;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;

/**
 * 
 * utilizado para ejemplo de https://docs.langchain4j.dev/integrations/language-models/hugging-face
 */
@RestController
@RequestMapping("/api/ai")
public class ChatLanguageModelController {
	
	private HuggingFaceChatModel chatLanguageModel;

    ChatLanguageModelController(HuggingFaceChatModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }
    
    //http://localhost:8080/api/ai/model?message=que%20eres
    @GetMapping("/model")
    public String model(@RequestParam(value = "message", defaultValue = "Oye hermano, ¿que haces?") String message) {
    	AiMessage aiMessage = chatLanguageModel.generate(
                systemMessage("Eres un buen amigo mio, a quien le gusta responder con chistes."),
                userMessage(message)
        ).content();
        return aiMessage.text();
    }
}
