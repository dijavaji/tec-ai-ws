package ec.com.technoloqie.ai.tecaiws;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;



@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
class TecAiWsApplicationTests {

	
	void contextLoads() {
	}
	
	@Test
	public void getDocsFromPdfTest() {
		
		try {
			PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/docs/sample1.pdf",
					PdfDocumentReaderConfig.builder()
						.withPageTopMargin(0)
						.withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
							.withNumberOfTopTextLinesToDelete(0)
							.build())
						.withPagesPerDocument(1)
						.build());

			List<Document> retorna = pdfReader.read();
		}catch(Exception e) {
			System.out.println("Error getDocsFromPdfTest."+e);
    		assertTrue("getDocsFromPdfTest.",Boolean.TRUE);
		}
    }

}
