package ec.com.technoloqie.ai.tecaiws.config;

import static dev.langchain4j.model.huggingface.HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT;
import static java.time.Duration.ofSeconds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import ec.com.technoloqie.ai.tecaiws.service.Assistant;
import ec.com.technoloqie.ai.tecaiws.service.CustomerSupportAgent;

@Configuration
public class LangChainConfiguration {
	
	@Value("${langchain4j.hugging-face.chat-model.api-key}")
    private String hfApiKey;
	
	private static String MODEL_NAME="qwen2:0.5b";		//llama3:latest		all-minilm:latest	orca-mini
	
	/**
     * This chat memory will be used by an {@link Assistant}
     */
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
    
    @Bean
    HuggingFaceChatModel huggingFaceChatModel() {
    	HuggingFaceChatModel model = HuggingFaceChatModel.builder()
                .accessToken(hfApiKey)
                .modelId("microsoft/Phi-3.5-mini-instruct")		// meta-llama/Meta-Llama-3.1-70B-Instruct  NousResearch/Hermes-3-Llama-3.1-8B  microsoft/Phi-3.5-mini-instruct  Qwen/Qwen2.5-72B-Instruct
                .timeout(ofSeconds(15))
                .temperature(0.7)
                .maxNewTokens(20)
                .waitForModel(true)
                .build();
    	return model;
    }
    
    @Bean
    HuggingFaceLanguageModel huggingFaceLanguageModel() {
    	 HuggingFaceLanguageModel model = HuggingFaceLanguageModel.builder()
                 .accessToken(hfApiKey)
                 .modelId(TII_UAE_FALCON_7B_INSTRUCT)
                 .timeout(ofSeconds(15))
                 .temperature(0.7)
                 .maxNewTokens(20)
                 .waitForModel(true)
                 .build();
    	 return model;
    }
    
    @Bean
    StreamingChatLanguageModel streamingChatLanguageModel() {
    	return OllamaStreamingChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName(MODEL_NAME)
        .temperature(0.0)
        .build();
    }
    
    @Bean
    Tokenizer tokenizer() {
    	return null;
    }
    
    /*@Bean
    CustomerSupportAgent customerSupportAgent(StreamingChatLanguageModel streamingChatLanguageModel, Tokenizer tokenizer) {
    	return AiServices.builder(CustomerSupportAgent.class)
    			.streamingChatLanguageModel(streamingChatLanguageModel)
    			//.chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder().id(memoryId).maxTokens(500, tokenizer).build())
    			.chatMemory(MessageWindowChatMemory.withMaxMessages(20))
    			.build();
    }*/
    
}
