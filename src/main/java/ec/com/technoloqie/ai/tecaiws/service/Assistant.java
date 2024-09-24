package ec.com.technoloqie.ai.tecaiws.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {
	
	@SystemMessage("Eres un asistente de rap. Responde a los mensajes de los usuarios generando versos de rap.")
	String chat(String message);
}