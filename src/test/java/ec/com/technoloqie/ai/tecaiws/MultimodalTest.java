package ec.com.technoloqie.ai.tecaiws;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ec.com.technoloqie.ai.tecaiws.service.ImageAIServiceImpl;
import lombok.extern.slf4j.Slf4j;


@SpringBootTest
@ActiveProfiles("local")
@Slf4j
public class MultimodalTest {
	
	@Autowired
	private ImageAIServiceImpl imageAIServiceImpl;
	
	@Test
	void getImageGenerationTest(){
		String response = this.imageAIServiceImpl.getImageGeneration("sexy girl young modern dress, have a color pink in the summer");
		log.info("url respuesta: {}", response);
		Assertions.assertNotNull(response,"getImageGenerationTest");
	}
	
	@Test
	void getImageVisionTest(){
		//"https://docs.spring.io/spring-ai/reference/_images/multimodal.test.png"
		String response = this.imageAIServiceImpl.getmultimodalVisionUrl("Explain what do you see on this picture?", "https://storage.googleapis.com/chatbot-doc_bucket/docKnowlege/Screenshot_2025-07-12_19-48-05.jpg");
		log.info("respuesta: {}", response);
		Assertions.assertNotNull(response,"getImageGenerationTest");
	}
}
