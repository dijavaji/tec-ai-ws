package ec.com.technoloqie.ai.tecaiws.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.com.technoloqie.ai.tecaiws.service.ImageAIServiceImpl;

@RestController
@RequestMapping("/images")
public class ImageController {
	
	private final ImageAIServiceImpl imageService;
	
	public ImageController(ImageAIServiceImpl imageService) {
		this.imageService = imageService;
	}
	
	@GetMapping("/image-gen")
	public String getUrlImage(@RequestParam("text") String text) {
		
		return this.imageService.getImageGeneration(text);
	}
}
