package ec.com.technoloqie.ai.tecaiws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.com.technoloqie.ai.tecaiws.model.dto.ChatDto;
import ec.com.technoloqie.ai.tecaiws.service.RagAdvancedService;

@RestController
@RequestMapping("api/v1/chat")
@CrossOrigin(origins = {"http://localhost:3008"})
public class CustomAIChatbotController {
	
	@Autowired
	private RagAdvancedService promptService;
	
	@PostMapping("/fake-news")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> getFakeNews(@RequestBody ChatDto chat) {
		return ResponseEntity.ok(this.promptService.getFakeNewsLinks(chat)); 
	}

}
