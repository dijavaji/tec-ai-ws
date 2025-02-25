package ec.com.technoloqie.ai.tecaiws.config;

import static dev.langchain4j.model.huggingface.HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT;
import static java.time.Duration.ofSeconds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import ec.com.technoloqie.ai.tecaiws.service.Assistant;

@Configuration
public class LangChainConfiguration {
	
	@Value("${langchain4j.hugging-face.chat-model.api-key}")
    private String hfApiKey;
	
	private static String MODEL_NAME ="deepseek-r1:14b"; //"qwen2:0.5b";		//llama3:latest		all-minilm:latest	orca-mini
	
	private static String OLLAMA_HOST ="http://35.211.131.67:11434";
	/**
     * This chat memory will be used by an {@link Assistant}
     */
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
    
   @Bean
   HuggingFaceChatModel huggingFaceChatModel() {
    	return HuggingFaceChatModel.builder()
                .accessToken(hfApiKey)
                .modelId("deepseek-ai/DeepSeek-R1-Distill-Qwen-1.5B")		// mistralai/Mistral-7B-Instruct-v0.1 meta-llama/Meta-Llama-3.1-70B-Instruct  NousResearch/Hermes-3-Llama-3.1-8B  microsoft/Phi-3.5-mini-instruct  Qwen/Qwen2.5-72B-Instruct
                .timeout(ofSeconds(15))
                .temperature(0.1)
                .maxNewTokens(20)
                .waitForModel(true)
                .build();
    }
    
    @Bean
    HuggingFaceLanguageModel huggingFaceLanguageModel() {
    	 return HuggingFaceLanguageModel.builder()
                 .accessToken(hfApiKey)
                 .modelId(TII_UAE_FALCON_7B_INSTRUCT)
                 .timeout(ofSeconds(15))
                 .temperature(0.7)
                 .maxNewTokens(20)
                 .waitForModel(true)
                 .build();
    }
    
    @Bean
    StreamingChatLanguageModel streamingChatLanguageModel() {
    	return OllamaStreamingChatModel.builder()
    	.baseUrl(OLLAMA_HOST)	//.baseUrl("http://localhost:11434")
        .modelName(MODEL_NAME)
        .temperature(0.0)
        .build();
    	/*return OpenAiStreamingChatModel.builder()
    	        .apiKey(System.getenv("OPENAI_API_KEY"))
    	        .temperature(0.1)
		        .modelName("gpt-4o-mini")
    	        .build();*/
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
    
    @Bean
    EmbeddingModel embeddingModel() {
    	/*return OllamaEmbeddingModel.builder()
	            .baseUrl(OLLAMA_HOST)
	            .modelName(MODEL_NAME)
	            .build();*/
    	return new AllMiniLmL6V2EmbeddingModel();
    }
    
    /*@Bean
	EmbeddingModel embeddingModel() {
		return OllamaEmbeddingModel.builder()
	            .baseUrl(OLLAMA_HOST)
	            .modelName(MODEL_NAME)
	            .build();
	}*/
   /* @Bean
	ChatLanguageModel chatModel() {
    	
		return  OllamaChatModel.builder().baseUrl(OLLAMA_HOST)
				.modelName(MODEL_NAME)
				.temperature(0.1)
				//.timeout(Duration.ofSeconds(60))
				.build();
	}*/
    
    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
    	return new InMemoryEmbeddingStore<>();
    }
    
    
    /*@Bean deprecado uso ContentRetriever
    Retriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        int maxResults = 1;
        double minScore = 0.6;
        return EmbeddingStoreRetriever.from(embeddingStore, embeddingModel, maxResults, minScore);
    }*/
    
    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        // You will need to adjust these parameters to find the optimal setting, which will depend on two main factors:
        // - The nature of your data
        // - The embedding model you are using
        int maxResults = 1;
        double minScore = 0.6;

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }
    
    @Bean
    CommandLineRunner docToEmbedding(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, Tokenizer tokenizer, ResourceLoader loader) {
    	return args -> {
    		var resource = loader.getResource("classpath:miles-of-smiles-terms-of-use.txt");
    		var doc = FileSystemDocumentLoader.loadDocument(resource.getFile().toPath());
    		
    		var splitter = DocumentSplitters.recursive(100, 0, tokenizer);
    		
    		var ingestor = EmbeddingStoreIngestor.builder()
    				.embeddingModel(embeddingModel)
    				.embeddingStore(embeddingStore)
    				.documentSplitter(splitter)
    				.build();
    		ingestor.ingest(doc);
    	};
    }
    
}
