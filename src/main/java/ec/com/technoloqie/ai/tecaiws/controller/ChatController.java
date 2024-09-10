package ec.com.technoloqie.ai.tecaiws.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.com.technoloqie.ai.tecaiws.service.PromptService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chats")
public class ChatController {
	
	private static final Logger log = LoggerFactory.getLogger(ChatController.class);
	
	@Autowired
	private PromptService promptService;
	
	
    //http://localhost:8080/chats/ai/generate?message=que%20eres
    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.promptService.generate(message));
    }
    
    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return this.promptService.generateStream(message);
    }
    
    //http://localhost:8080/chats/joke?adjective=funny&topic=cows
    @GetMapping("/joke")
    public AssistantMessage getJoke(@RequestParam String adjective, @RequestParam String topic) {
    	Generation generation = promptService.generateJoke(adjective, topic);
    	log.info("bromas {}",generation.getOutput().getContent());
    	log.info("generation ---{}", generation.toString());
        return generation.getOutput();
    }
    
    @GetMapping("/youtube/popular")
    public AssistantMessage findPopularYoutuberByGenre(@RequestParam(value="genre", defaultValue="tecnologia") String genre) {
    	ChatResponse chat = promptService.findPopularYouTubers(genre);
    	return chat.getResult().getOutput();
    }
  //comentado version anterior <spring-ai.version>0.8.1</spring-ai.version>
  	//private final ChatClient chatClient;
  	/*
  	 * public ChatController(ChatClient chatClient) { this.chatClient = chatClient;
  	 * }
  	 */
  	/*
  	 * @GetMapping public String chat(@RequestParam String text){ //return
  	 * chatClient.call(text); }
  	 */
    
}
