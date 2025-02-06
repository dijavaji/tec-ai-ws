package ec.com.technoloqie.ai.tecaiws.service;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import ec.com.technoloqie.ai.tecaiws.model.dto.ChatDto;
import ec.com.technoloqie.ai.tecaiws.model.dto.ChatNewsResponse;
import lombok.extern.slf4j.Slf4j;


//import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
//import static dev.langchain4j.internal.Utils.randomUUID;

@Service
@Slf4j
public class RagAdvancedService {
	
	private EmbeddingStore<TextSegment> embeddingStore;
	private EmbeddingModel embeddingModel;
	
	private final OpenAiChatModel chatModel;
	
	public RagAdvancedService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel,
			OpenAiChatModel chatModel) {
		super();
		this.embeddingStore = embeddingStore;
		this.embeddingModel = embeddingModel;
		this.chatModel = chatModel;
	}

	//private ChatLanguageModel model;
	
	public List<Document> queryJSONVector(String query, String acercaDe, String informacionSobre) {
        //EmbeddingStore<TextSegment> embeddingStore;

        //embeddingStore = new InMemoryEmbeddingStore<>();
        //embeddingStore = chromaEmbeddingStore();

        DocumentSplitter splitter = DocumentSplitters.recursive(600, 0);
        ChatLanguageModel model = OllamaChatModel.builder().baseUrl("http://35.211.131.67:11434")
		.modelName("deepseek-r1:14b")	//("qwen2:0.5b")			//("deepseek-r1:14b")
		.temperature(0.1)
		//.timeout(Duration.ofSeconds(60))
		.build();
        embeddingStore.removeAll();	//utilizado por que carga el bean con data al inicio de la app
		try {
			
			URL url = new URL("https://www.metroecuador.com.ec/noticias/2025/02/11/olas-predominantes-y-fase-de-aguaje-en-playas-del-ecuador-advierte-el-inocar/"); //URL("https://openlab.ec/mediahack");
			//Document document = loadDocument(toPath("/siva.txt"), new TextDocumentParser());
			Document htmlDocument = UrlDocumentLoader.load(url, new TextDocumentParser());
			//HtmlTextExtractor transformer = new HtmlTextExtractor(null, null, true);
			// Document document = transformer.transform(htmlDocument);
			HtmlToTextDocumentTransformer transformer = new HtmlToTextDocumentTransformer();
			Document document = transformer.transform(htmlDocument);
			
			EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
			            .documentSplitter(splitter)
			            .embeddingModel(embeddingModel)
			            .embeddingStore(embeddingStore)
			            .build();
			    ingestor.ingest(document);
			    
		    //Querying LLMs with data from EmbeddingStore
		    Embedding queryEmbedding = embeddingModel.embed(query).content();
	        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
	        EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

	        String information = embeddingMatch.embedded().text();
	        System.out.println("Relevant Information:\n"+information);
	        
	        Prompt prompt = PromptTemplate.from("""
                    """)
            .apply(Map.of("name", acercaDe, "information", information));	//MediaHack
	        String answer = model.generate(prompt.toUserMessage()).content().text();
	        System.out.println("Answer:\n"+answer);
	        
	        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
	                .embeddingStore(embeddingStore)
	                .embeddingModel(embeddingModel)
	                .maxResults(1)
	                //.minScore(0.5)
	                .build();
	        
	        ConferenceDataExtractor extractor = AiServices.builder(ConferenceDataExtractor.class)
	                        .chatLanguageModel(model)
	                        .contentRetriever(contentRetriever)
	                        .build();
	        Conference schedule = extractor.getInfoAbout(informacionSobre);
	        System.out.println("Conference:\n"+schedule);
	        

		} catch (Exception e) {
			log.error("Error al momento de busqueda html vectorial",e);
			//throw new Exception();
		}

		return null;
	}
	
	 private static Path toPath(String fileName) {
	        try {
	            URL fileUrl = RagAdvancedService.class.getResource(fileName);
	            return Paths.get(fileUrl.toURI());
	        } catch (URISyntaxException e) {
	            throw new RuntimeException(e);
	        }
	    }
	 
	 interface ConferenceDataExtractor {
         @UserMessage("Get information about {{it}} before {{current_date}}")
         Conference getInfoAbout(String name);
     }

     record Conference(
     		String name,
     		LocalDate startHour,
     		LocalDate finishHour) {
     }

	public ChatNewsResponse getFakeNewsLinks(ChatDto chat) {
		ChatNewsResponse chatNewsResponse = new ChatNewsResponse();
		
		MapOutputConverter mapOutputConverter = new MapOutputConverter();
		
		try {
			
			URL url = new URL("https://openlab.ec/mediahack"); //URL("https://openlab.ec/mediahack");
			//Document document = loadDocument(toPath("/siva.txt"), new TextDocumentParser());
			Document htmlDocument = UrlDocumentLoader.load(url, new TextDocumentParser());
			//HtmlTextExtractor transformer = new HtmlTextExtractor(null, null, true);
			// Document document = transformer.transform(htmlDocument);
			HtmlToTextDocumentTransformer transformer = new HtmlToTextDocumentTransformer();
			Document document = transformer.transform(htmlDocument);
			
			log.info("documento tomado {}", document.text());
			
			String format = mapOutputConverter.getFormat();
			String template = "";

			org.springframework.ai.chat.prompt.Prompt prompt = new org.springframework.ai.chat.prompt.PromptTemplate(template,
			        Map.of("input", chat.getText(), "documents", document.text())).create();

			Generation generation = chatModel.call(prompt).getResult();
			
			chatNewsResponse.setResponse(generation.getOutput().getText());
			//Map<String, Object> result = mapOutputConverter.convert(generation.getOutput().getText());
			log.info("respuesta ai {}", chatNewsResponse.getResponse());
		}catch(Exception e) {
			log.error("Error al momento de consultar las noticias falsas", e);
		}
		return chatNewsResponse;
	}
}
