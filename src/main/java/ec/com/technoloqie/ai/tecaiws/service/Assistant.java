package ec.com.technoloqie.ai.tecaiws.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {
	
	//@SystemMessage("Eres un asistente de rap. Responde a los mensajes de los usuarios generando versos de rap.")
	String chat(String message);
	
	String chat(@MemoryId int memoryId, @UserMessage String userMessage);
	
	@SystemMessage("Eres un asistente para tareas de respuesta a preguntas. Si no sabes la respuesta, simplemente menciona que no la sabes.")
	String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}