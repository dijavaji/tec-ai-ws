package ec.com.technoloqie.ai.tecaiws.config;

import static dev.langchain4j.model.huggingface.HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT;
import static java.time.Duration.ofSeconds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceLanguageModel;
import ec.com.technoloqie.ai.tecaiws.service.Assistant;

@Configuration
public class LangChainConfiguration {
	
	@Value("${langchain4j.hugging-face.chat-model.api-key}")
    private String hfApiKey;
	
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
    
}
