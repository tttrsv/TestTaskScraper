import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import domain.DocumentFile;
import domain.Level;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import service.Scraper;
import service.ScraperImpl;
import utils.ScraperConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static utils.ScraperConstants.HEADER_CONTENT_INDEX;
import static utils.ScraperConstants.HEADER_TITLE_INDEX;

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