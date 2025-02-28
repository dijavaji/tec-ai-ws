package ec.com.technoloqie.ai.tecaiws.service;

import dev.langchain4j.service.SystemMessage;

public interface AssistantWebSearch {
	
	/*@SystemMessage({
        "You are a web search support agent.",
        "If there is any event that has not happened yet",
        "You MUST create a web search request with user query and",
        "use the web search tool to search the web for organic web results.",
        "Include the source link in your final response."
	})*/
	String answer(String query);
}
