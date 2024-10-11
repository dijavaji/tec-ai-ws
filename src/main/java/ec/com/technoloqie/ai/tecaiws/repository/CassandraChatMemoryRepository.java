package ec.com.technoloqie.ai.tecaiws.repository;

import static java.util.stream.Collectors.toList;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.dtsx.astra.sdk.cassio.CassIO;
import com.dtsx.astra.sdk.cassio.ClusteredRecord;
import com.dtsx.astra.sdk.cassio.ClusteredTable;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author dvasquez
 */
@Slf4j
public class CassandraChatMemoryRepository implements ChatMemoryStore{
	
	 /**
     * Default message store.
     */
    public static final String DEFAULT_TABLE_NAME = "message_store";
    
    /**
     * Message Table.
     */
    private final ClusteredTable messageTable;
    
    /**
     * Constructor for message store
     *
     * @param session      cassandra session
     */
    public CassandraChatMemoryRepository(CqlSession session) {
        this(session, DEFAULT_TABLE_NAME);
    }
    
    /**
     * Constructor for message store
     *
     * @param session      cassandra session
     * @param tableName    table name
     */
    public CassandraChatMemoryRepository(CqlSession session, String tableName) {
        messageTable = new ClusteredTable(session, session.getKeyspace().get().asInternal(), tableName);
    }

	
	@Override
	public List<ChatMessage> getMessages(Object memoryId) {
		   /*
         * RATIONAL:
         * In the cassandra table the order is explicitly put to DESC with
         * latest to come first (for long conversation for instance). Here we ask
         * for the full history. Instead of changing the multipurpose table
         * we reverse the list.
         */
		List<ChatMessage> latestFirstList = messageTable
                .findPartition(getMemoryId(memoryId))
                .stream()
                .map(this::toChatMessage)
                .collect(toList());
        Collections.reverse(latestFirstList);
        return latestFirstList;
	}

	@Override
	public void updateMessages(@NonNull Object memoryId, List<ChatMessage> messages) {
		  deleteMessages(memoryId);
	        messageTable.upsertPartition(messages.stream()
	                .map(record -> fromChatMessage(getMemoryId(memoryId), record))
	                .collect(toList()));
		
	}

	@Override
	public void deleteMessages(Object memoryId) {
		  messageTable.deletePartition(getMemoryId(memoryId));
		
	}
	
    private String getMemoryId(Object memoryId) {
        if (!(memoryId instanceof String)) {
            throw new IllegalArgumentException("memoryId must be a String");
        }
        return (String) memoryId;
    }
    
    /**
     * Unmarshalling Cassandra row as a Message with proper subtype.
     *
     * @param record cassandra record
     * @return chat message
     */
    private ChatMessage toChatMessage(@NonNull ClusteredRecord record) {
        try {
            return ChatMessageDeserializer.messageFromJson(record.getBody());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse message body", e);
        }
    }
    
    /**
     * Serialize the {@link ChatMessage} as a Cassandra Row.
     *
     * @param memoryId    chat session identifier
     * @param chatMessage chat message
     * @return cassandra row.
     */
    private ClusteredRecord fromChatMessage(@NonNull String memoryId, @NonNull ChatMessage chatMessage) {
        try {
            ClusteredRecord record = new ClusteredRecord();
            record.setRowId(Uuids.timeBased());
            record.setPartitionId(memoryId);
            record.setBody(ChatMessageSerializer.messageToJson(chatMessage));
            return record;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse message body", e);
        }
    }
    
    /**
     * Access the cassandra session for fined grained operation.
     *
     * @return
     *      current cassandra session
     */
    public CqlSession getCassandraSession() {
        return messageTable.getCqlSession();
    }

    /**
     * Create the table if not exist.
     */
    public void create() {
        messageTable.create();
    }

    /**
     * Delete the table.
     */
    public void delete() {
        messageTable.delete();
    }

    /**
     * Delete all rows.
     */
    public void clear() {
        messageTable.clear();
    }
    
    public static class Builder {
        public static Integer DEFAULT_PORT = 9042;
        private List<String> contactPoints;
        private String localDataCenter;
        private Integer port = DEFAULT_PORT;
        private String userName;
        private String password;
        protected String keyspace;
        protected String table = DEFAULT_TABLE_NAME;

        public CassandraChatMemoryRepository.Builder contactPoints(List<String> contactPoints) {
            this.contactPoints = contactPoints;
            return this;
        }

        public CassandraChatMemoryRepository.Builder localDataCenter(String localDataCenter) {
            this.localDataCenter = localDataCenter;
            return this;
        }

        public CassandraChatMemoryRepository.Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public CassandraChatMemoryRepository.Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public CassandraChatMemoryRepository.Builder password(String password) {
            this.password = password;
            return this;
        }

        public CassandraChatMemoryRepository.Builder keyspace(String keyspace) {
            this.keyspace = keyspace;
            return this;
        }

        public CassandraChatMemoryRepository.Builder table(String table) {
            this.table = table;
            return this;
        }

        public Builder() {
        }

        public CassandraChatMemoryRepository build() {
            CqlSessionBuilder builder = CqlSession.builder()
                    .withKeyspace(keyspace)
                    .withLocalDatacenter(localDataCenter);
            if (userName != null && password != null) {
                builder.withAuthCredentials(userName, password);
            }
            contactPoints.forEach(cp -> builder.addContactPoint(new InetSocketAddress(cp, port)));
            return new CassandraChatMemoryRepository(builder.build(), table);
        }
    }

    public static CassandraChatMemoryRepository.Builder builder() {
        return new CassandraChatMemoryRepository.Builder();
    }

    public static CassandraChatMemoryRepository.BuilderAstra builderAstra() {
        return new CassandraChatMemoryRepository.BuilderAstra();
    }

    public static class BuilderAstra {
        private String token;
        private UUID dbId;
        private String tableName = DEFAULT_TABLE_NAME;
        private String keyspaceName = "default_keyspace";
        private String dbRegion = "us-east1";
        private AstraEnvironment env = AstraEnvironment.PROD;

        public BuilderAstra token(String token) {
            this.token = token;
            return this;
        }

        public CassandraChatMemoryRepository.BuilderAstra databaseId(UUID dbId) {
            this.dbId = dbId;
            return this;
        }

        public CassandraChatMemoryRepository.BuilderAstra env(AstraEnvironment env) {
            this.env = env;
            return this;
        }

        public CassandraChatMemoryRepository.BuilderAstra databaseRegion(String dbRegion) {
            this.dbRegion = dbRegion;
            return this;
        }

        public CassandraChatMemoryRepository.BuilderAstra keyspace(String keyspaceName) {
            this.keyspaceName = keyspaceName;
            return this;
        }

        public CassandraChatMemoryRepository.BuilderAstra table(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public CassandraChatMemoryRepository build() {
        	
        	CqlSession cqlSession = null;
        	try {
        		log.info("costruyo sesion cassandra {} {} {} {} {} {}", token, dbId, dbRegion, keyspaceName, env, tableName);
            	cqlSession = CassIO.init(token, dbId, dbRegion, keyspaceName, env);
                log.info("sesion  {}",cqlSession);	
            }catch(Exception e) {
            	log.info("Error al construir sesion cassandra {}",e);
            }
            return new CassandraChatMemoryRepository(cqlSession, tableName);
        }
    }
    

}
