package ec.com.technoloqie.ai.tecaiws;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.langchain4j.data.document.Document;
import ec.com.technoloqie.ai.tecaiws.model.dto.ChatDto;
import ec.com.technoloqie.ai.tecaiws.model.dto.ChatNewsResponse;
import ec.com.technoloqie.ai.tecaiws.service.RagAdvancedService;

@SpringBootTest
@ActiveProfiles("local")
public class AdvancedRagTest {
	
	@Autowired
	private RagAdvancedService ragService;
	
	@Test
    public void htmlLoaderTest() {
		List<Document> documents = ragService.queryJSONVector("Fecha", "Esmeraldas","aguaje en playas");
		
		Assertions.assertNotNull(documents,"htmlLoaderTest");
    }
	
	@Test
	public void testFakeNews() {
		ChatDto chatDto = new ChatDto();
		chatDto.setText("Alcalde de Guayaquil roba combustible y lo vende en baldes al Peru.");
		ChatNewsResponse response = ragService.getFakeNewsLinks(chatDto );
		
		Assertions.assertNotNull(response,"htmlLoaderTest");
	}
	
	@Test
	public void assistantWebSearchTest() {
		
		String response = ragService.webSearch("I had an accident, should I pay extra?");
		
		Assertions.assertNotNull(response,"htmlLoaderTest");
	}
}
