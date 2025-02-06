package ec.com.technoloqie.ai.tecaiws.model.dto;

import java.util.Collection;

public class ChatNewsResponse {
	
	private String response;
	
	private Collection <String> urlNews; 
	
	private String generationId;
	
	public Collection<String> getUrlNews() {
		return urlNews;
	}

	public void setUrlNews(Collection<String> urlNews) {
		this.urlNews = urlNews;
	}

	public String getGenerationId() {
		return generationId;
	}

	public void setGenerationId(String generationId) {
		this.generationId = generationId;
	}
	
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
