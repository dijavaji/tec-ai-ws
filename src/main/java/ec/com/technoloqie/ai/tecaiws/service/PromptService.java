package ec.com.technoloqie.ai.tecaiws.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import ec.com.technoloqie.ai.tecaiws.model.Author;
import reactor.core.publisher.Flux;

@Service
public class PromptService {
	
	private final ChatClient chatClient;
    private final PromptTemplate jokePromptTemplate;
	private final OpenAiChatModel chatModel;
	@Value("classpath:/prompts/youtube.st")
	private Resource ytPromptResource;
	
	@Value("classpath:/prompts/olympic-sports.st")
	private Resource olympicResource;
	
	@Value("classpath:/docs/olympic-data.txt")
	private Resource docsToStuffResource;

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
        /*String message = """
            Enumere 10 de los YouTubers mas populares en {genre} junto con sus cifras actuales de suscriptores. Si no sabe
        		la respuesta, simplemente diga "No se".
            """;*/
        
        return chatClient.prompt()
                .user(u -> u.text(ytPromptResource).param("genre",genre))
                .call() //..content() devuelve respuesta chat
                .chatResponse();
    }
    
    public Generation jokes() {
    	//PromptTemplate promptTemplate = new PromptTemplate("Tell me a {adjective} joke about {topic}");
    	var system = new SystemMessage("Tu funcion principal es contar chistes de papa. Si alguien te pide cualquier otro tipo de chiste, dile que solo sabes contar chistes de papa.");
    	var user = new UserMessage("Cuentame un chiste serio sobre el universo.");
    	Prompt prompt = new Prompt(List.of(system, user));
    	return chatModel.call(prompt).getResult(); //.getOutput().getContent()
    }

	public List<String> getSongsByArtist(String artist) {
		ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());

		String format = listOutputConverter.getFormat();
		var template = """
	            Por favor, dame una lista de las 10 mejores canciones del artista {artist}. Si no sabes
	        		la respuesta, simplemente diga "No se". {format}
	            """;
		
		PromptTemplate promptTemplate = new PromptTemplate(template, Map.of("artist", artist, "format", format));
		Prompt prompt = promptTemplate.create();
		Generation generation = this.chatModel.call(prompt).getResult();

		List<String> list = listOutputConverter.convert(generation.getOutput().getContent());
		return list;
	}

	public Map<String, Object> getAuthorSocialLinks(String author) {
		MapOutputConverter mapOutputConverter = new MapOutputConverter();

		String format = mapOutputConverter.getFormat();
		String template = """
		        Generar una lista de enlaces para el autor {author}. Incluir el nombre del autor como clave y los enlaces a redes sociales como objeto.
		        Si no sabes la respuesta, simplemente diga "No se". {format}
		        """;

		Prompt prompt = new PromptTemplate(template,
		        Map.of("author", author, "format", format)).create();

		Generation generation = chatModel.call(prompt).getResult();

		Map<String, Object> result = mapOutputConverter.convert(generation.getOutput().getContent());
		return result;
	}

	public Author getBooksByAuthor(String author) {
		BeanOutputConverter<Author> beanOutputConverter =  new BeanOutputConverter<>(Author.class);
		String format = beanOutputConverter.getFormat();
		
		String template = """
				Genera una lista de libros escritos por el autor {author}. Si no estas seguro de que un libro pertenece a este autor, no lo incluyas.
				{format}
			        """;
		Generation generation = chatModel.call(new PromptTemplate(template, Map.of("author", author, "format", format)).create()).getResult();

		Author authorBooks = beanOutputConverter.convert(generation.getOutput().getContent());
		return authorBooks;
	}

	public String getOlympicSports(String message, boolean stuffit) {
		PromptTemplate promptTemplate = new PromptTemplate(olympicResource);
    	Map <String, Object> map = new HashMap<>();
    	map.put("question", message);
		if(stuffit) {
			map.put("context", docsToStuffResource);
    	}else {
    		map.put("context", "");
    	}
    	Prompt prompt = promptTemplate.create(map);
    	return chatModel.call(prompt).getResult().getOutput().getContent();
	}

}
