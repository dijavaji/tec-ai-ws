package ec.com.technoloqie.ai.tecaiws;


import static com.dtsx.astra.sdk.utils.TestUtils.getAstraToken;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;


import static com.dtsx.astra.sdk.utils.TestUtils.TEST_REGION;
import com.dtsx.astra.sdk.AstraDBAdmin;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;

import ec.com.technoloqie.ai.tecaiws.repository.CassandraChatMemoryRepository;

/**
 * Test Cassandra Chat Memory Store with a Saas DB.
 */
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
public class CassandraChatMemoryStoreAstraIT extends CassandraChatMemoryStoreTestSupport {
	
	 static final String DB = "test_langchain4j";
	    static String token;
	    static UUID dbId;

	    @Override
	    void createDatabase() {
	        token = getAstraToken();
	        assertThat(token).isNotNull();
	        dbId = new AstraDBAdmin(token).createDatabase(DB, CloudProviderType.GCP, "us-east1");
	        assertThat(dbId).isNotNull();
	    }

	    @Override
	    CassandraChatMemoryRepository createChatMemoryStore() {
	        return CassandraChatMemoryRepository.builderAstra()
	                .token(getAstraToken())
	                .databaseId(dbId)
	                .databaseRegion(TEST_REGION)
	                .keyspace(KEYSPACE)
	                .build();
	    }

}
