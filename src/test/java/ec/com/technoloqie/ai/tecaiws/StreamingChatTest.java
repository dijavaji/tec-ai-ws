package ec.com.technoloqie.ai.tecaiws;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import ec.com.technoloqie.ai.tecaiws.service.AssistantServiceImpl;
import reactor.core.publisher.Flux;

@SpringBootTest
@ActiveProfiles("local")
public class StreamingChatTest {
	
	private static final Logger logger = LoggerFactory.getLogger(TecAiWsApplicationTests.class);
	
	@Autowired
	AssistantServiceImpl assistantService;
	
	@Test
	public void getOllamaGenericTest() {
		//String baseUrl = String.format("http://%s:%d", ollama.getHost(), ollama.getFirstMappedPort());
	      ChatLanguageModel model = OllamaChatModel.builder()
	              .baseUrl("http://localhost:11434")
	              .modelName("qwen2:0.5b")
	              .build();
	      String answer = model.generate("Lista todas las peliculas dirigidas por Quentin Tarantino");
	      System.out.println(answer);
	}
	
	
	@Test
	public void getOllamaChatTest() {
		logger.info("inicio getOllamaChatTest.");
		try {
			String chatSessionId = "chat-" + UUID.randomUUID();
			
			//Flux.fromArray("I'm sorry, my brain is not yet hooked up.".split("(?<-\\s)")).delayElements(Duration.ofMillis(200));
			
			//Flux<String> flux = assistantService.chat(100, "hola mi nombre es Cristina Smith");
			Flux<String> flux = assistantService.chat(100, "indicame cual es mi nombre?");
			//Flux<String> flux = assistantService.chat(100, "Puedes explicarme la politica de cancelacion por favor?");
			
			//Flux<String> flux = assistantService.chat(100, "Hey, can you tell me about the cancellation policy?");
			//Flux<String> flux = assistantService.chat(100, "Que requisitos se requiere para realizar la cancelacion");
			
			//Flux<String> flux = assistantService.chat(100, "Hi my name is Cristina Smith. My booking number is 123-456, Can you please cancel it.");
			//Flux<String> flux = assistantService.chat(100, "Hola mi nombre es Cristina Smith. Mi numero de reserva es 123-456, puedes ayudarme cancelandolo.");
			
			flux.subscribe(logger::info);
			
			
			Thread.sleep((long) 60000);
			logger.info("finaliza getOllamaChatTest.");
		}catch(Exception e) {
			logger.error("Error getOllamaChatTest.", e);
		}
		
	}

}
