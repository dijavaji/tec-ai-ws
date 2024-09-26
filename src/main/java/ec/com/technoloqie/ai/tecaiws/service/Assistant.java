package ec.com.technoloqie.ai.tecaiws.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {
	
	//@SystemMessage("Eres un asistente de rap. Responde a los mensajes de los usuarios generando versos de rap.")
	String chat(String message);
	
	String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}