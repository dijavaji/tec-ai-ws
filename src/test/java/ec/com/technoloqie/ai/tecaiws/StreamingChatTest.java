package ec.com.technoloqie.ai.tecaiws;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ec.com.technoloqie.ai.tecaiws.service.AssistantServiceImpl;
import reactor.core.publisher.Flux;

@SpringBootTest
@ActiveProfiles("local")
public class StreamingChatTest {
	
	private static final Logger logger = LoggerFactory.getLogger(TecAiWsApplicationTests.class);
	
	@Autowired
	AssistantServiceImpl assistantService;
	
	
	@Test
	public void getOllamaChatTest() {
		logger.info("getOllamaChatTest.");
		try {
			String chatSessionId = "chat-" + UUID.randomUUID();
			Flux<String> flux = assistantService.chat(1, "hola como estas");
			flux.subscribe(logger::info);
			logger.info("finaliza getOllamaChatTest.");
		}catch(Exception e) {
			logger.error("Error getOllamaChatTest.", e);
		}
		
	}

}
