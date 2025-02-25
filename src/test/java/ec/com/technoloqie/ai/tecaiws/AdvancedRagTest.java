package ec.com.technoloqie.ai.tecaiws;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.langchain4j.data.document.Document;
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
}
