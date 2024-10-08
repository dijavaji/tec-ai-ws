package ec.com.technoloqie.ai.tecaiws;

import static com.dtsx.astra.sdk.utils.TestUtils.TEST_REGION;
import static com.dtsx.astra.sdk.utils.TestUtils.getAstraToken;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import ec.com.technoloqie.ai.tecaiws.repository.CassandraChatMemoryRepository;
import ec.com.technoloqie.ai.tecaiws.repository.ChatMemoryStoreRepository;
import ec.com.technoloqie.ai.tecaiws.service.Assistant;



//@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
class TecAiWsApplicationTests {
	
	private static final Logger logger = LoggerFactory.getLogger(TecAiWsApplicationTests.class);
	
	@Autowired
	private HuggingFaceChatModel hfchatModel;
	@Autowired
	private ChatLanguageModel chatLanguageModel;
	@Autowired
	private ChatMemory chatMemory;
	
	void contextLoads() {
	}
	
	@Test
	public void getDocsFromPdfTest() {
		
		try {
			PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/docs/sample1.pdf",
					PdfDocumentReaderConfig.builder()
						.withPageTopMargin(0)
						.withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
							.withNumberOfTopTextLinesToDelete(0)
							.build())
						.withPagesPerDocument(1)
						.build());

			List<Document> retorna = pdfReader.read();
		}catch(Exception e) {
			logger.error("Error getDocsFromPdfTest."+e);
    		//assertTrue("getDocsFromPdfTest.",Boolean.TRUE);
		}
    }
	
	
	/**
	 * Prueba de c&#243;mo utilizar ConversationalChain con ChatMemory para recordar el contexto de la conversaci&#243;n.
	 * @author dvasquez
	 * @see https://www.sivalabs.in/generative-ai-conversations-using-langchain4j-chat-memory/
	 */
	@Test
	public void serviceWithMemoryTest() {
		
		ConversationalChain chain = ConversationalChain.builder()
                .chatLanguageModel(hfchatModel)
                .chatMemory(chatMemory)
                .build();
		String answer = chain.execute("What are all the movies directed by Quentin Tarantino?");
		logger.info(answer); // Pulp Fiction, Kill Bill, etc.

		answer = chain.execute("How old is he?");
		logger.info(answer); // Quentin Tarantino was born on March 27, 1963, so he is currently 58 years old.
		
		//You can refine the question by providing more context to get the exact age.
		answer = chain.execute("How old is he as of "+ LocalDate.now() + "?");
		logger.info(answer); //As of February 21, 2024, Quentin Tarantino would be 60 years old.
	}
	
	@Test
	public void usingPromptTemplateTest() {
		ConversationalChain chain = ConversationalChain.builder()
		        .chatLanguageModel(hfchatModel)
		        .chatMemory(chatMemory)
		        .build();
		String answer = chain.execute("What are all the movies directed by Quentin Tarantino?");
		logger.info(answer); // Pulp Fiction, Kill Bill, etc.

		Prompt prompt = PromptTemplate
                .from("How old is {{name}} as of {{current_date}}?")
                .apply(Map.of("name","Quentin Tarantino"));
		//comentado no se pasa por que variables especiales {{current_date}}, {{current_time}}, and {{current_date_time}} se rellenan automaticamente con LocalDate.now(), LocalTime.now(), and LocalDateTime.now() 
		//PromptTemplate.from("How old is he as of {{current_date}}?").apply(Map.of());
		answer = chain.execute(prompt.text());
		logger.info(answer); //As of February 21, 2024, Quentin Tarantino would be 60 years old.
		//Assert.assertThat(null).containsIgnoringCase("Washington");
	}
	
	
	//No se me ocurre ningun caso de uso en el que sea necesario agregar mensajes manualmente a ChatMemory en lugar de usar ConversationalChain, pero existe esa opcion.
	//Segun los JavaDocs de ConversationalChain, se recomienda usar AiServices en su lugar, ya que es mas potente
	//https://github.com/langchain4j/langchain4j-examples/blob/main/other-examples/src/main/java/ChatMemoryExamples.java
	//Chat memory
	@Test
	public void manuallyAddMessagesChatMemoryTest() {
		//ChatLanguageModel model = OpenAiChatModel.withApiKey(openAiKey);
		//ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);

		chatMemory.add(UserMessage.userMessage("What are all the movies directed by Quentin Tarantino?"));
		AiMessage answer = hfchatModel.generate(chatMemory.messages()).content();
		logger.info(answer.text()); // Pulp Fiction, Kill Bill, etc.
		chatMemory.add(answer);

		chatMemory.add(UserMessage.userMessage("How old is he?"));
		AiMessage answer2 = hfchatModel.generate(chatMemory.messages()).content();
		logger.info(answer2.text()); // Quentin Tarantino was born on March 27, 1963, so he is currently 58 years old.
		chatMemory.add(answer2);
	}
	
	
	//Separate chat memory for each user
	//https://github.com/langchain4j/langchain4j-examples/blob/main/other-examples/src/main/java/ServiceWithMemoryForEachUserExample.java
	@Test
	public void serviceWithMemoryForEachUserTest() {
		Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(hfchatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

		logger.info(assistant.chat(1, "Hello, my name is Klaus"));
        // Hi Klaus! How can I assist you today?

		logger.info(assistant.chat(2, "Hello, my name is Francine"));
        // Hello Francine! How can I assist you today?

		logger.info(assistant.chat(1, "What is my name?"));
        // Your name is Klaus.

		logger.info(assistant.chat(2, "What is my name?"));
        // Your name is Francine.
	}
	
	//Persistent chat memory for each user
	//https://github.com/langchain4j/langchain4j-examples/blob/main/other-examples/src/main/java/ServiceWithPersistentMemoryForEachUserExample.java
	@Test
	public void serviceWithPersistentMemoryForEachUserTest() {
		ChatMemoryStoreRepository store = new ChatMemoryStoreRepository();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();
		
		//ChatLanguageModel model = OpenAiChatModel.builder().apiKey(ApiKeys.OPENAI_API_KEY) .modelName(GPT_4_O_MINI) .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(hfchatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();

        logger.info(assistant.chat(1, "Hello, my name is Klaus"));
        logger.info(assistant.chat(2, "Hi, my name is Francine"));

        // Now, comment out the two lines above, uncomment the two lines below, and run again.

        // logger.info(assistant.chat(1, "What is my name?"));
        // logger.info(assistant.chat(2, "What is my name?"));
		
	}
	
	//utilizado para recuperar datos de MapDB
	@Test
	public void selectMapDbfile() {
		
		DB db = null;
        try {
            db = DBMaker.fileDB(new File("multi-user-chat-memory.db")).closeOnJvmShutdown().make();
            for (final Entry<String, Object> entry : db.getAll().entrySet()) {
                final String name = entry.getKey();
                final Object value = entry.getValue();
                if (value instanceof Map) {
                    inspectMap(name, (Map<?, ?>) value);
                } else {
                    System.err.println(String.format("Unexpected type (%s) for '%s'.", value.getClass(), name));
                }
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private static <K, V> void inspectMap(final String name, final Map<K, V> map) {
        System.out.println(name);
        for (final Entry<K, V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            System.out.println(String.format("    %s = %s [%s, %s]", key, value, key.getClass(), value.getClass()));
        }
    }
    /**
     * @author dvasquez
     */
    @Test
    public void shouldInsertItemsCassandraDbTest() {
    	
    	String chatSessionId = "chat-" + UUID.randomUUID();
    	
    	try {
    		//ChatMemory chatMemory = MessageWindowChatMemory.builder().chatMemoryStore(cassChatMemoryStore).maxMessages(100)
			//	.id(chatSessionId).build();
    		CassandraChatMemoryRepository cassChatMemoryStore = createChatMemoryStore();
    	logger.info("sesion de cassandra {}",cassChatMemoryStore);
		 ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
	                .id(memoryId)
	                .maxMessages(10)
	                .chatMemoryStore(cassChatMemoryStore)
	                .build();
		
		Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(hfchatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();

		logger.info(assistant.chat("1", "Hola, mi nombre es Juan"));

		logger.info(assistant.chat("2", "Hello, mi nombre es Francine"));

		logger.info(assistant.chat("1", "Cual es mi nombre?"));

		logger.info(assistant.chat("2", "Cual es mi nombre?"));
    		
    	}catch(Exception e) {
			logger.error("Error shouldInsertItemsCassandraDbTest. {}", e);
    		//assertTrue("getDocsFromPdfTest.",Boolean.TRUE);
		}
    	
    }
    
    protected final String KEYSPACE = "default_keyspace";
    
    private CassandraChatMemoryRepository createChatMemoryStore() {
        return CassandraChatMemoryRepository.builderAstra()
                .token(getAstraToken())
                .databaseId(UUID.fromString("759393ee-048b-4abb-8baf-d94428418ab0"))
                .databaseRegion(TEST_REGION)
                .keyspace(KEYSPACE)
                .build();
    }
}
