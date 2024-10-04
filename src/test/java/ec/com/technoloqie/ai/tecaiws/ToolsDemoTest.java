package ec.com.technoloqie.ai.tecaiws;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import ec.com.technoloqie.ai.tecaiws.service.Assistant;

@SpringBootTest
@ActiveProfiles("local")
public class ToolsDemoTest {
	
	private static final Logger logger = LoggerFactory.getLogger(TecAiWsApplicationTests.class);
	
	@Autowired
	private HuggingFaceChatModel hfchatModel;	//modelo no soporta tools
	
	String apiKey = "demo";
	
	static String baseUrl() {
		return String.format("http://%s:%d", "localhost", 11434);
	}

	static class Calculadora {

	    @Tool("Calculates the length of a string")
	    int stringLength(String s) {
	        System.out.println("Called stringLength with s='" + s + "'");
	        return s.length();
	    }

	    @Tool("Calculates the sum of two numbers")
	    int add(int a, int b) {
	        System.out.println("Called add with a=" + a + ", b=" + b);
	        return a + b;
	    }

	    @Tool("Calculates the square root of a number")
	    double sqrt(int x) {
	        System.out.println("Called sqrt with x=" + x);
	        return Math.sqrt(x);
	    }
	}
	
	@Test
	public void functionCallingTest() {
		
		logger.info("inicio functionCallingTest.");
		try {
			String LLAMA2_MODEL_NAME = "qwen2:0.5b"; // "fllama2"
	
			// load model no soporta tools
			//ChatLanguageModel model = OllamaChatModel.builder().baseUrl(baseUrl()).temperature(0.1)
					//.timeout(Duration.ofMinutes(5)).modelName(LLAMA2_MODEL_NAME).build();
			/*AnthropicChatModel model = AnthropicChatModel.builder()
				    .apiKey(System.getenv("ANTHROPIC_API_KEY"))
				    .modelName("CLAUDE_3_5_SONNET_20240620")
				    .build();*/
			
			ChatLanguageModel model = OpenAiChatModel.builder()
			        .apiKey(apiKey)
			        .temperature(0.1)
			        .modelName("gpt-4o-mini")
			        .build();
			
			Assistant assistant = AiServices.builder(Assistant.class)
					.chatLanguageModel(model)
					.tools(new Calculadora())
					.chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build();
	
			String question = "What is the square root of the sum of the numbers of letters in the words \"hello\" and \"world\"?";
	
			String answer = assistant.chat(question);
	
			System.out.println(answer);
		}catch(Exception e) {
			logger.error("Error functionCallingTest.", e);
			}
		}
	
}
