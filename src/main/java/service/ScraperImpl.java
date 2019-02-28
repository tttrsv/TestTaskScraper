package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import domain.DocumentFile;
import domain.Level;
import domain.Paragraph;
import domain.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.ScraperConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static utils.ScraperConstants.*;
import static utils.ScraperHelper.*;


public class ScraperImpl implements Scraper {


    @Override
    public List<DocumentFile> scrap() {
        Document document = getJsoupConnection();

        Element mainTable = getMainTable(document);
        List<DocumentFile> files = new ArrayList<>();
        try {
            files = iterateOverTable(mainTable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    private List<DocumentFile> iterateOverTable(Element mainTable) throws IOException {
        List<DocumentFile> files = new ArrayList<>();

        Element currentElement = mainTable.children().first();

        while (!currentElement.children()
                .get(HEADER_TITLE_INDEX)
                .text()
                .equals(LAST_ELEMENT_HEADER_TITLE)) {
            DocumentFile documentFile = new DocumentFile();
            String cellName = getCellContent(currentElement, HEADER_TITLE_INDEX).text();
            String cellContent = getCellContent(currentElement, HEADER_CONTENT_INDEX).text();
            documentFile.setFileName(cellName);

            Level level = new Level();
            level.setHeading(cellContent);
            level.setPath(cellContent);

            documentFile.setLevel(level);
            files.add(documentFile);
            getInfoByTitle(currentElement, documentFile);


            currentElement = currentElement.nextElementSibling();
        }

        return files;
    }

    private void getInfoByTitle(Element currentElement, DocumentFile file) throws IOException {

        final Elements sections = getSections(currentElement);
        if (sections.size() == 0) {
            String chapterPageLink = getLink(currentElement);
            Document chapterPage = Jsoup.connect(chapterPageLink).get();
            Element chaptersTable = getSubTable(chapterPage);
            Elements rowChapterElements = getRowFromSubTable(chaptersTable);
            getInfoByChapterElement(rowChapterElements, file);
        }
    }

    private void getInfoByChapterElement(Elements rowChapterElements, DocumentFile file) throws IOException {
        for (Element rowChapterElement : rowChapterElements) {

            Element cellChapterNumber = getCellContent(rowChapterElement, CELL_NUMBER_INDEX);
            Element cellChapterName = getCellContent(rowChapterElement, CELL_NAME_INDEX);
            String heading = cellChapterName.text();
            Level childLevel = new Level();
            childLevel.setHeading(heading);
            Level lastLevel = file.getLevel().returnLastLevel();
            if (lastLevel == null) {
                childLevel.setPath(file.getLevel().getPath());
                childLevel.appendPathName(heading);
                file.getLevel().getChildLevel().add(childLevel);
            } else {
                String path = lastLevel.getPath();
                childLevel.setPath(path);
                childLevel.appendPathName(heading);
                lastLevel.setChildLevel(Collections.singletonList(childLevel));
            }

            String chapterPageLink = getLink(cellChapterNumber);
            Document chapterPage = Jsoup.connect(chapterPageLink).get();
            if (isTabExist(chapterPage)) {
                processSections(rowChapterElement, file);
            } else {

                Element chaptersTable = getSubTable(chapterPage);
                if (chaptersTable == null) {
                    return;
                }
                Elements rowSubChapterElements = getRowFromSubTable(chaptersTable);

                getInfoByChapterElement(rowSubChapterElements, file);
            }

        }
    }

    private void processSections(Element rowChapterElement, DocumentFile file) throws IOException {
        Level lastLevel = file.getLevel().returnLastHasText();
        String chapterPageLink = getLink(rowChapterElement);
        Document tabPage = Jsoup.connect(chapterPageLink).get();
        Element tabElements = getTab(tabPage);

        List<Level> citationLevels = new ArrayList<>();

        Elements headers = getHeaders(tabElements);
        for (Element header : headers) {
            String heading = getHeadingText(header);
            String citation = getCitation(header);
            final Element element = header.nextElementSibling();
            if (element.hasClass("doc-block")) {
                List<Paragraph> paragraphs = getParagraphs(element)
                        .stream()
                        .map(Paragraph::new)
                        .collect(Collectors.toList());

                Text text = new Text();
                text.setParagraphs(paragraphs);
                Level level = Level.builder()
                        .heading(heading)
                        .citation(citation)
                        .text(text)
                        .build();
                level.setPath(lastLevel.getPath());
                level.appendPathName(heading);
                level.setTextExist(Boolean.TRUE);
                citationLevels.add(level);
            }
        }
        lastLevel.returnLastLevel().setChildLevel(citationLevels);
    }

    private List<Paragraph> processParagraphs(Elements sections) {

        List<Paragraph> list = new ArrayList<>();

        for (Element section : sections) {
            List<String> paragraphs = getParagraphs(section);
            final List<Paragraph> collect = paragraphs.stream().map(Paragraph::new).collect(Collectors.toList());
            list.addAll(collect);
        }
        return list;

    }


    private Document getJsoupConnection() {
        try {
            return Jsoup.connect(ScraperConstants.URL).get();
        } catch (IOException e) {
            System.out.println("Error while get jsoup connection = " + e.getMessage());
        }
        return null;
    }

}
