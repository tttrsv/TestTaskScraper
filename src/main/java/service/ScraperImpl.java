package service;

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
import java.util.List;
import java.util.stream.Collectors;

import static utils.ScraperConstants.*;
import static utils.ScraperHelper.*;


public class ScraperImpl implements Scraper {


    @Override
    public List<DocumentFile> scrap() {
        Document document = getJsoupConnection(ScraperConstants.URL);

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
            getInfoByTitle(currentElement, documentFile, level);


            currentElement = currentElement.nextElementSibling();
        }

        return files;
    }

    private void getInfoByTitle(Element currentElement, DocumentFile file, Level parentLevel) throws IOException {

        Elements sections = getSections(currentElement);
        if (sections.size() == 0) {
            Elements rowsFromChapterTable = getElementsFromNextPage(getJsoupConnection(getLink(currentElement)));
            getInfoByChapterElement(rowsFromChapterTable, file, parentLevel);
        }
    }

    private void getInfoByChapterElement(Elements rowChapterElements, DocumentFile file, Level parentLevel) throws IOException {
        for (Element rowChapterElement : rowChapterElements) {
            Element cellChapterNumber = getCellContent(rowChapterElement, CELL_NUMBER_INDEX);
            Element cellChapterName = getCellContent(rowChapterElement, CELL_NAME_INDEX);
            String heading = cellChapterName.text();
            Level chapter = new Level();
            chapter.setHeading(heading);
            String path = parentLevel.getPath();
            chapter.setPath(path);
            chapter.appendPathName(heading);
            parentLevel.getChildLevel().add(chapter);

            Elements rowsFromSubchaptersTable = getElementsFromNextPage(getJsoupConnection(getLink(cellChapterNumber)));
            getInfoBySubchapter(rowsFromSubchaptersTable, file, chapter);


        }
    }

    private void getInfoBySubchapter(Elements rowSubchapterElements, DocumentFile file, Level chapter) throws IOException {
        for (Element rowSubchapterElement : rowSubchapterElements) {
            Element cellSubchapterNumber = getCellContent(rowSubchapterElement, CELL_NUMBER_INDEX);
            Element cellSubchapterName = getCellContent(rowSubchapterElement, CELL_NAME_INDEX);
            String heading = cellSubchapterName.text();
            Level subchapter = new Level();
            subchapter.setHeading(heading);
            String path = chapter.getPath();
            subchapter.setPath(path);
            subchapter.appendPathName(heading);

            chapter.getChildLevel().add(subchapter);

            Elements partElements = getElementsFromNextPage(getJsoupConnection(getLink(cellSubchapterNumber)));
            getInfoByPart(partElements, file, subchapter);
        }
    }

    private Elements getElementsFromNextPage(Document page){
        return getRowsFromSubTable(getSubTable(page));
    }


    private void getInfoByPart(Elements partElements, DocumentFile file, Level subchapter) throws IOException {
        for (Element partElement : partElements) {
            Element cellSubchapterName = getCellContent(partElement, CELL_NAME_INDEX);
            String heading = cellSubchapterName.text();
            Level part = new Level();
            part.setHeading(heading);
            String path = subchapter.getPath();
            part.setPath(path);
            part.appendPathName(heading);
            subchapter.getChildLevel().add(part);
            processSections(partElement, file, part);
        }
    }


    private void processSections(Element rowChapterElement, DocumentFile file, Level parentLevel) throws IOException {
        String chapterPageLink = getLink(rowChapterElement);
        Document tabPage = Jsoup.connect(chapterPageLink).get();
        Element tabElements = getTab(tabPage);

        List<Level> citationLevels = new ArrayList<>();

        Elements headers = getHeaders(tabElements);
        for (Element header : headers) {
            String heading = getHeadingText(header);
            String citation = getCitation(header);
            Element element = header.nextElementSibling();
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
                level.setPath(parentLevel.getPath());
                level.appendPathName(heading);
                level.setTextExist(Boolean.TRUE);
                citationLevels.add(level);
            }
        }
        parentLevel.setChildLevel(citationLevels);
    }

    private Document getJsoupConnection(String link) {
        try {
            return Jsoup.connect(link).get();
        } catch (IOException e) {
            System.out.println("Error while getting jsoup connection = " + e.getMessage());
        }
        return null;
    }

}

