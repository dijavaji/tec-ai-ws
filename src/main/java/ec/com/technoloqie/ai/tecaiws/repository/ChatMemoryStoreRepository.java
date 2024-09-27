package ec.com.technoloqie.ai.tecaiws.repository;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;
import static org.mapdb.Serializer.INTEGER;
import static org.mapdb.Serializer.STRING;

import java.util.List;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

public class ChatMemoryStoreRepository implements ChatMemoryStore{
	
	 private final DB db = DBMaker.fileDB("multi-user-chat-memory.db").transactionEnable().make();
     private final Map<Integer, String> map = db.hashMap("messages", INTEGER, STRING).createOrOpen();

     @Override
     public List<ChatMessage> getMessages(Object memoryId) {
         String json = map.get((int) memoryId);
         return messagesFromJson(json);
     }

     @Override
     public void updateMessages(Object memoryId, List<ChatMessage> messages) {
         String json = messagesToJson(messages);
         map.put((int) memoryId, json);
         db.commit();
     }

     @Override
     public void deleteMessages(Object memoryId) {
         map.remove((int) memoryId);
         db.commit();
     }


}
