package ec.com.technoloqie.ai.tecaiws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.langchain4j.data.document.Document;
import ec.com.technoloqie.ai.tecaiws.service.RagAdvancedService;

@RestController
@RequestMapping("api/v1")
public class VectorController {
	
	@Autowired
	RagAdvancedService ragService;
	
	@GetMapping("/query")
    public Document getQueryResults(@RequestParam String query){
        return ragService.queryJSONVector(query,"","").get(0);
    }

}
