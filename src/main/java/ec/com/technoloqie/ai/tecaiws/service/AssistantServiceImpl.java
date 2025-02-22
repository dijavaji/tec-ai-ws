package ec.com.technoloqie.ai.tecaiws.service;

import org.springframework.stereotype.Service;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import ec.com.technoloqie.ai.tecaiws.repository.ChatMemoryStoreRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class AssistantServiceImpl {
	
	//private CustomerSupportAgent customerSupportAgent;
	
	private StreamingChatLanguageModel streamingChatLanguageModel;
	private ContentRetriever contentRetriever;
	private BookingTools bookingTools;
	
	private AssistantServiceImpl(StreamingChatLanguageModel streamingChatLanguageModel, ContentRetriever contentRetriever,
			BookingTools bookingTools) {
		//this.customerSupportAgent=customerSupportAgent;
		this.streamingChatLanguageModel = streamingChatLanguageModel;
		this.contentRetriever = contentRetriever;
		this.bookingTools = bookingTools;
	}
	
	public Flux<String> chat(Integer chatId, String question){
		
		ChatMemoryStoreRepository store = new ChatMemoryStoreRepository();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();
		
		CustomerSupportAgent customerSupportAgent = AiServices.builder(CustomerSupportAgent.class)
		.streamingChatLanguageModel(streamingChatLanguageModel)
		//.chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder().id(memoryId).maxTokens(500, tokenizer).build())
		.chatMemoryProvider(chatMemoryProvider)
		.contentRetriever(contentRetriever)
		//.retriever(null) deprecado
		//.tools(bookingTools) comentado para modelo hugging face java.lang.IllegalArgumentException: Tools are currently not supported by this model
		.build();
		Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer(); 
		
		customerSupportAgent.chat(chatId, question)
			.onNext(sink::tryEmitNext)
			.onComplete(e -> sink.tryEmitComplete())
			.onError(sink::tryEmitError)
			.start();
		
		return sink.asFlux();
	}
}
