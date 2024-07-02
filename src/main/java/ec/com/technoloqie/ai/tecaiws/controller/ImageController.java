package ec.com.technoloqie.ai.tecaiws.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/images")
public class ImageController {
	
	/*private final ImageClient imageClient;
	
	public ImageController(ImageClient imageClient) {
		this.imageClient = imageClient;
	}
	
	@GetMapping
	public String getUrlImage(@RequestParam("text") String text) {
		
		return this.imageClient.call(new ImagePrompt(text)).getResult().getOutput().getUrl();
	}*/
}
