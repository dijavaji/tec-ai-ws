package ec.com.technoloqie.ai.tecaiws.service;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
//import static dev.langchain4j.internal.Utils.randomUUID;
import static java.util.stream.Collectors.joining;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import ec.com.technoloqie.ai.tecaiws.commons.exception.TecAIWsException;
//import langchain4j.community.web.search.searxng.SearXNGWebSearchEngine
import ec.com.technoloqie.ai.tecaiws.model.dto.ChatDto;
import ec.com.technoloqie.ai.tecaiws.model.dto.ChatNewsResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RagAdvancedService {
	
	private EmbeddingStore<TextSegment> embeddingStore;
	private EmbeddingModel embeddingModel;
	@Value("classpath:/prompts/prompt-template-fake.st")
	private Resource promptTemplateResource;
	
	private final OpenAiChatModel chatModel;
	
	private final ChatMemory chatMemory;
	
	@Value("${ec.com.technoloqie.ai.ollama.base-url}")
    private String BASE_URL;
	
	public RagAdvancedService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel,
			OpenAiChatModel chatModel, ChatMemory chatMemory) {
		super();
		this.embeddingStore = embeddingStore;
		this.embeddingModel = embeddingModel;
		this.chatModel = chatModel;
		this.chatMemory = chatMemory;
	}

	//private ChatLanguageModel model;
	
	public List<Document> queryJSONVector(String query, String acercaDe, String informacionSobre) {
        //EmbeddingStore<TextSegment> embeddingStore;

        //embeddingStore = new InMemoryEmbeddingStore<>();
        //embeddingStore = chromaEmbeddingStore();

        DocumentSplitter splitter = DocumentSplitters.recursive(600, 0);
        ChatLanguageModel model = OllamaChatModel.builder().baseUrl(BASE_URL)
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
                    Tell me about {{name}}?
                    
                    Use the information to answer the question:
                    {{information}}
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
	        } catch (Exception e) {
	        	log.error("Error al cargar archivo {}", e);
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
		Collection <String> urlNews = Arrays.asList(
				"https://www.eluniverso.com/noticias/politica/caso-triple-a-aquiles-alvarez-municipio-de-guayaquil-alcaldia-fiscalia-general-gobierno-daniel-noboa-denuncias-combustibles-nota/", 
				"https://www.ecuavisa.com/noticias/politica/aquiles-alvarez-jose-gasca-confrontan-caso-triple-a-XI8864176", 
				"https://www.eltelegrafo.com.ec/noticias/guayaquil/189/caso-triple-a-fiscalia-solicita-vinculacion-del-alcalde-de-guayaquil-aquiles-alvarez"
				);
		
		try {
			
			Collection <Document> docs= urlNews.stream().map(urlString ->{
				Document document =null;
				try {
					URL url = new URL(urlString); //URL("https://openlab.ec/mediahack");
					//Document document = loadDocument(toPath("/siva.txt"), new TextDocumentParser());
					Document htmlDocument = UrlDocumentLoader.load(url, new TextDocumentParser());
					//HtmlTextExtractor transformer = new HtmlTextExtractor(null, null, true);
					// Document document = transformer.transform(htmlDocument);
					HtmlToTextDocumentTransformer transformer = new HtmlToTextDocumentTransformer();
					document = transformer.transform(htmlDocument);
				}catch(Exception e) {
					
				}
				return document; 
			}).toList();
			
			//StringBuilder docs = new StringBuilder();
			//log.info("documento tomado {}", document.text());
			
			String format = mapOutputConverter.getFormat();

			//org.springframework.ai.chat.prompt.Prompt prompt = new org.springframework.ai.chat.prompt.PromptTemplate(promptTemplateResource,
			        //Map.of("input", chat.getText(), "documents", docs, "format", format)).create();
			org.springframework.ai.chat.prompt.PromptTemplate promptTemplate = new org.springframework.ai.chat.prompt.PromptTemplate(promptTemplateResource);

			org.springframework.ai.chat.prompt.Prompt prompt = promptTemplate.create(Map.of("input", chat.getText(), "documents", docs, "format", format));

			
			Generation generation = chatModel.call(prompt).getResult();
			
			
			Map<String, Object> result = mapOutputConverter.convert(generation.getOutput().getText());
			
			log.info("respuesta ai {}", generation.getOutput().getText());
			chatNewsResponse.setResponse(result.get("contraste").toString());
			chatNewsResponse.setNewsName(result.get("nombreNoticia").toString());
			chatNewsResponse.setConstrast(result.get("contraste").toString());
			chatNewsResponse.setClasification(result.get("clasificacion").toString().toLowerCase());
			chatNewsResponse.setAnalysis(result.get("analisis").toString());
			chatNewsResponse.setGenerationId(UUID.randomUUID().toString());
			//ObjectMapper objectMapper = new ObjectMapper();
			
			chatNewsResponse.setUrlNews(urlNews);
			
			
		}catch(Exception e) {
			log.error("Error al momento de consultar las noticias falsas", e);
		}
		return chatNewsResponse;
	}
	
	
	public String webSearch(String searchTerms) {
		
		 //embeddingStore.removeAll();
		
		try {
			//final String searchTerms = "What is Artificial Intelligence?";
	        WebSearchRequest request1 = WebSearchRequest.builder().searchTerms(searchTerms).build();
	        
	        final Map<String, Object> additionalParams3 = new HashMap<>();
	        additionalParams3.put("engines", "yahoo");
	        WebSearchRequest request3 = WebSearchRequest.builder()
	                .searchTerms(searchTerms)
	                .additionalParams(additionalParams3)
	                .build();
	        
	        //EmbeddingStore<TextSegment> embeddingStore = embed(toPath("classpath:miles-of-smiles-terms-of-use.txt"), embeddingModel);
	        
	        ContentRetriever embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
	                .embeddingStore(embeddingStore)
	                .embeddingModel(embeddingModel)
	                .maxResults(2)
	                .minScore(0.6)
	                .build();
	        
	        // Let's create our web search content retriever.
	        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
	               .apiKey(System.getenv("TAVILY_API_KEY")) // get a free key: https://app.tavily.com/sign-in
	               .build();
	        
	        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
	                .webSearchEngine(webSearchEngine)
	                .maxResults(3)
	                .build();
	        // Let's create a query router that will route each query to both retrievers.
	        QueryRouter queryRouter = new DefaultQueryRouter(embeddingStoreContentRetriever, webSearchContentRetriever);

	        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
	                .queryRouter(queryRouter)
	                .build();
	        
	        ChatLanguageModel model = OllamaChatModel.builder().baseUrl(BASE_URL)
					.modelName("deepseek-r1:14b")
					.temperature(0.1)
					//.timeout(Duration.ofSeconds(60))
					.build();  
	        		
	        		/*dev.langchain4j.model.openai.OpenAiChatModel.builder()
	                .apiKey("")
	                .modelName("")
	                .build();*/
	        	
	        
	        AssistantWebSearch assistant = AiServices.builder(AssistantWebSearch.class)
	        .chatLanguageModel(model)
	        .retrievalAugmentor(retrievalAugmentor)
	        .chatMemory(chatMemory)		//.chatMemory(MessageWindowChatMemory.withMaxMessages(10))
	        .build();
	        
	        String answer = assistant.answer(searchTerms);
	        log.info(answer);
	        return answer;
		}catch(Exception e) {
			log.error("Error en la busqueda websearch", e);
			throw new TecAIWsException("Error en la busqueda websearch",e);
		}
		
	}
	
	
	 private static EmbeddingStore<TextSegment> embed(Path documentPath, EmbeddingModel embeddingModel) {
	        DocumentParser documentParser = new TextDocumentParser();
	        Document document = loadDocument(documentPath, documentParser);

	        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
	        List<TextSegment> segments = splitter.split(document);

	        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

	        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
	        embeddingStore.addAll(embeddings, segments);
	        return embeddingStore;
	    }
	 
	 /**
	  * Este ejemplo demuestra como utilizar las API LangChain4j de bajo nivel para implementar RAG.
	  * @param question
	  * @return
	  */
	 public String lowLevelNaiveRAG(String question) {
		 
		// Load the document that includes the information you'd like to "chat" about with the model.
	        DocumentParser documentParser = new TextDocumentParser();
	        Document document = loadDocument("/home/diego/workspaceSts4/tec-ai-ws/src/main/resources/miles-of-smiles-terms-of-use.txt", documentParser);

	        // Split document into segments 100 tokens each
	        DocumentSplitter splitter = DocumentSplitters.recursive(300,0);
	        List<TextSegment> segments = splitter.split(document);

	        // Embed segments (convert them into vectors that represent the meaning) using embedding model
	        //EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
	        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

	        // Store embeddings into embedding store for further search / retrieval
	        //EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
	        embeddingStore.addAll(embeddings, segments);

	        // Specify the question you want to ask the model
	        //String question = "Who is Charlie?";

	        // Embed the question
	        Embedding questionEmbedding = embeddingModel.embed(question).content();

	        // Find relevant embeddings in embedding store by semantic similarity
	        // You can play with parameters below to find a sweet spot for your specific use case
	        int maxResults = 3;
	        double minScore = 0.5;
	        List<EmbeddingMatch<TextSegment>> relevantEmbeddings
	                = embeddingStore.findRelevant(questionEmbedding, maxResults, minScore);

	        // Create a prompt for the model that includes question and relevant embeddings
	        PromptTemplate promptTemplate = PromptTemplate.from(
	                "Answer the following question to the best of your ability:\n"
	                        + "\n"
	                        + "Question:\n"
	                        + "{{question}}\n"
	                        + "\n"
	                        + "Base your answer on the following information:\n"
	                        + "{{information}}");

	        String information = relevantEmbeddings.stream()
	                .map(match -> match.embedded().text())
	                .collect(joining("\n\n"));

	        Map<String, Object> variables = new HashMap<>();
	        variables.put("question", question);
	        variables.put("information", information);

	        Prompt prompt = promptTemplate.apply(variables);

	        // Send the prompt to the OpenAI chat model
	        /*ChatLanguageModel chatModel = OpenAiChatModel.builder()
	                .apiKey(OPENAI_API_KEY)
	                .modelName(GPT_4_O_MINI)
	                .timeout(Duration.ofSeconds(60))
	                .build();
	                AiMessage aiMessage = chatModel.chat(prompt.toUserMessage()).aiMessage();*/
	        ChatLanguageModel model = OllamaChatModel.builder().baseUrl(BASE_URL)
					.modelName("deepseek-r1:14b")
					.temperature(0.1)
					//.timeout(Duration.ofSeconds(60))
					.build(); 
	        AiMessage aiMessage = model.chat(prompt.toUserMessage()).aiMessage();

	        // See an answer from the model
	        String answer = aiMessage.text();
	        System.out.println(answer); // Charlie is a cheerful carrot living in VeggieVille...
	        
		 return answer;
	 }
	 
	 public String advancedRAGwithQueryCompression(String documentPath) {
		 Document document = loadDocument(toPath(documentPath), new TextDocumentParser());
		 String pregunta = "pregunta relacionada al documento";

	        //EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

	        //EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

	        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
	                .documentSplitter(DocumentSplitters.recursive(300, 0))
	                .embeddingModel(embeddingModel)
	                .embeddingStore(embeddingStore)
	                .build();

	        ingestor.ingest(document);

	        ChatLanguageModel chatLanguageModel = OllamaChatModel.builder().baseUrl(BASE_URL)
					.modelName("deepseek-r1:14b")
					.temperature(0.1)
					//.timeout(Duration.ofSeconds(60))
					.build(); 

	        // We will create a CompressingQueryTransformer, which is responsible for compressing
	        // the user's query and the preceding conversation into a single, stand-alone query.
	        // This should significantly improve the quality of the retrieval process.
	        QueryTransformer queryTransformer = new CompressingQueryTransformer(chatLanguageModel);

	        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
	                .embeddingStore(embeddingStore)
	                .embeddingModel(embeddingModel)
	                .maxResults(2)
	                .minScore(0.6)
	                .build();

	        // The RetrievalAugmentor serves as the entry point into the RAG flow in LangChain4j.
	        // It can be configured to customize the RAG behavior according to your requirements.
	        // In subsequent examples, we will explore more customizations.
	        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
	                .queryTransformer(queryTransformer)
	                .contentRetriever(contentRetriever)
	                .build();

	        return AiServices.builder(Assistant.class)
	                .chatLanguageModel(chatLanguageModel)
	                .retrievalAugmentor(retrievalAugmentor)
	                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
	                .build().chat(pregunta);
		 //return null;
	 }
}
