package ec.com.technoloqie.ai.tecaiws.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class PromptService {
	
	private final ChatClient chatClient;
    private final PromptTemplate jokePromptTemplate;
	private final OpenAiChatModel chatModel;

    @Autowired
    public PromptService(ChatClient chatClient, PromptTemplate jokePromptTemplate, OpenAiChatModel chatModel) {
        this.chatClient = chatClient;
        this.jokePromptTemplate = jokePromptTemplate;
        this.chatModel = chatModel;
    }
    
    public Generation generateJoke(String adjective, String topic) {
    	PromptTemplate promptTemplate = new PromptTemplate("Tell me a {adjective} joke about {topic}");

    	Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));

    	return chatModel.call(prompt).getResult();
    }
    
    public String generate(String message) {
    	return chatModel.call(message);
    }
    
    public Flux<ChatResponse> generateStream(String message){
    	Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }
    
    public String simple(String userInput) {
    	return this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }
    
    public ChatResponse findPopularYouTubers(String genre) {
        String message = """
            Enumere 10 de los YouTubers mas populares en {genre} junto con sus cifras actuales de suscriptores. Si no sabe
        		la respuesta, simplemente diga "No se".
            """;
        
        return chatClient.prompt()
                .user(u -> u.text(message).param("genre",genre))
                .call() //..content() devuelve respuesta chat
                .chatResponse();
    }

}
