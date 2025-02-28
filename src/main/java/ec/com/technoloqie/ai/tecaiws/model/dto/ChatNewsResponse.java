package ec.com.technoloqie.ai.tecaiws.model.dto;

import java.util.Collection;

public class ChatNewsResponse {
	
	private String response;
	
	private Collection <String> urlNews; 
	
	private String newsName;
	
	private String constrast;
	
	private String clasification;
	
	private String analysis;
	
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

	public String getNewsName() {
		return newsName;
	}

	public void setNewsName(String newsName) {
		this.newsName = newsName;
	}

	public String getConstrast() {
		return constrast;
	}

	public void setConstrast(String constrast) {
		this.constrast = constrast;
	}

	public String getClasification() {
		return clasification;
	}

	public void setClasification(String clasification) {
		this.clasification = clasification;
	}

	public String getAnalysis() {
		return analysis;
	}

	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}

}
