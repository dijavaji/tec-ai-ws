package ec.com.technoloqie.ai.tecaiws.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
	
	@Value("${spring.ai.openai.api-key}")
    private String groqApiKey;
	
	/*@Bean //para version antigua 
    public OpenAiClient openAiClient() {
        return new OpenAiClient();
    }*/

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
	
	@Bean
    public OpenAiChatModel openAiClient() {
		//System.getenv("GROQ_API_KEY")
		var openAiApi = new OpenAiApi("https://api.groq.com/openai", groqApiKey );
		var openAiChatOptions = OpenAiChatOptions.builder()
				.model("llama3-70b-8192") // comentados deprecados.withModel("llama3-70b-8192")
				.temperature(0.4) //.withTemperature(0.4)
				.maxTokens(200)   //.withMaxTokens(200)
		        .build();
		return new OpenAiChatModel(openAiApi, openAiChatOptions);
    }

    @Bean
    public PromptTemplate jokePromptTemplate() {
        return new PromptTemplate("Tell me a {adjective} joke about {topic}");
    }
    
    
    /*@Bean
    public EmbeddingModel embeddingModel() {
    	var ollamaApi = new OllamaApi();
        return new OllamaEmbeddingModel(ollamaApi, OllamaOptions.builder()
    			.model(OllamaModel.MISTRAL.id())
                .build(), new ObservationRegistry(), null);

    }
    
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel){
        return null;
    }*/

}
