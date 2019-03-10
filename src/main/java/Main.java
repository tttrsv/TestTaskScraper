import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import domain.DocumentFile;
import service.Scraper;
import service.ScraperImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Scraper scraper = new ScraperImpl();
        List<DocumentFile> scrap = scraper.scrap();
        System.out.println(scrap.size());

        for (DocumentFile documentFile : scrap) {
            File file = new File(documentFile.getFileName() + ".xml");
            mapper.writeValue(file, documentFile);
        }
    }
}