package ec.com.technoloqie.ai.tecaiws.service;

import java.net.URI;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageAIServiceImpl {
	
	private final OpenAiImageModel openaiImageModel;
	
	private final OpenAiChatModel chatModel;
	
	@Value("${ec.com.technoloqie.ai.ollama.base-url}")
    private String BASE_URL;
	
	public ImageAIServiceImpl(OpenAiImageModel openAiImageModel, OpenAiChatModel chatModel) {
		this.openaiImageModel = openAiImageModel;
		this.chatModel  = chatModel;
	}
	
	public String getImageGeneration(String message) {
		
		ImageResponse response = openaiImageModel.call(
		        new ImagePrompt(message,
		        OpenAiImageOptions.builder()
		                //.quality("hd")
		                .N(1)
		                .height(1024)
		                .width(1024).build()));
		
		Image image = response.getResult().getOutput();
		
		return image.getUrl();
	}
	
	
	public String getmultimodalVisionUrl(String chat, String url) {
		//var imageResource = new ClassPathResource("/multimodal.test.png");
		var userMessage =  UserMessage.builder().text(chat)
				.media(new Media(MimeTypeUtils.IMAGE_PNG,
		                URI.create(url)))
				.build();
		ChatResponse response = chatModel.call(new Prompt(userMessage,
		        OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O.getValue()).build()));
		
		return response.getResult().getOutput().getText();
	}
	
	public String getImageModelResponse(String chat, String url) {
		
		 ChatLanguageModel model = OllamaChatModel.builder().baseUrl(BASE_URL)
					.modelName("gemma3:27b")	//	("qwen2:0.5b")			("deepseek-r1:14b")
					.temperature(0.1)
					//.timeout(Duration.ofSeconds(60))
					.build();
		 
		 dev.langchain4j.data.message.UserMessage userMessage = dev.langchain4j.data.message.UserMessage.from(
				 dev.langchain4j.data.message.TextContent.from(chat),
				 dev.langchain4j.data.message.ImageContent.from(url)
         );

         Response<AiMessage> response = model.generate(userMessage);
		
		return response.content().text();
	}

}
