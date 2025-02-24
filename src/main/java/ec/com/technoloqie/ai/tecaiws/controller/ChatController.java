package ec.com.technoloqie.ai.tecaiws.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.com.technoloqie.ai.tecaiws.model.Author;
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
    	log.info("bromas {}",generation.getOutput().getText());
    	log.info("generation ---{}", generation.toString());
        return generation.getOutput();
    }
    
    @GetMapping("/youtube/popular")
    public AssistantMessage findPopularYoutuberByGenre(@RequestParam(value="genre", defaultValue="tecnologia") String genre) {
    	ChatResponse chat = promptService.findPopularYouTubers(genre);
    	return chat.getResult().getOutput();
    }
    
    @GetMapping("/dad-jokes")
    public Generation dadJokes() {
    	return this.promptService.jokes();
    }
    
    @GetMapping("/songs")
    public List<String> getSongsByArtist(@RequestParam(value="artist", defaultValue="Megadeth") String artist) {
    	return this.promptService.getSongsByArtist(artist);
    }
    
    //http://127.0.0.1:8080/chats/author/craig%20walls
    @GetMapping("/author/{author}")
    public Map<String, Object> getAuthorSocialLinks(@PathVariable String author){
    	return this.promptService.getAuthorSocialLinks(author);
    }
    
    //http://127.0.0.1:8080/chats/by-author?author=ken%20kousen
    @GetMapping("/by-author")
    public Author getBooksByAuthor(@RequestParam(value="author", defaultValue="J.R.R. Tolkien") String author) {
    	return this.promptService.getBooksByAuthor(author);
    }
    
    //http://127.0.0.1:8080/chats/olympic?stuffit=true
    @GetMapping("/olympic")
    public String getOlympicSports(@RequestParam(value="message", defaultValue="Que deportes se incluiran en los Juegos Olimpicos de Verano de 2024") String message, 
    		@RequestParam(value="stuffit", defaultValue="false") boolean stuffit ) {
    	return this.promptService.getOlympicSports(message, stuffit);
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
