package utils;


import lombok.experimental.UtilityClass;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class ScraperHelper {

    public static Element getMainTable(Document document) {
        Objects.requireNonNull(document, "Document should be not null");
        return document.getElementById("titles-table").
                getElementsByTag("tbody").first();
    }


    public static String getLink(Element element) {
        return element.select("a").first().attr("href");
    }

    public static Element getCellContent(Element row, int cellIndex) {
        return row.children().get(cellIndex);
    }

    public static String getCitation(Element header) {
        return "Citation " + header.text().substring(0, header.text().indexOf(' '));
    }

    public static List<String> getParagraphs(Element section) {
        List<String> paragraphsList = new ArrayList<>();
        Elements paragraphs = section.getElementsByTag("p");
        for (Element paragraph : paragraphs) {
            Elements font = paragraph.getElementsByTag("font");
            Element first = font.first();
            if (first != null) {
                String text = first.text();
                paragraphsList.add(text);
            }
        }
        return paragraphsList;
    }

    public static Elements getSections(Element tab) {
        return tab.getElementsByTag("section");
    }

    public static Element getTab(Document document) {
        return document.getElementsByClass("tabs-panel").first();
    }

    public boolean isTabExist(Document document) {
        return !document.getElementsByClass("tabs-panel").isEmpty();
    }

    public static String getHeadingText(Element header) {
        return header.text().substring(header.text().indexOf(' ') + 1);
    }

    public static Elements getHeaders(Element block) {
        return block.getElementsByTag("h1");
    }

    public static Element getSubTable(Document document) {
        Elements unstriped = document.getElementsByClass("unstriped");
        if (unstriped != null
                && unstriped.size() != 0) {
            return unstriped
                    .first().getElementsByTag("tbody").first();
        }
        return null;
    }

    public static Elements getRowsFromSubTable(Element table) {
        ArrayList<Element> rowsList = new ArrayList<>();
        Elements allRows = table.children();
        for (Element row: allRows) {
            if(row.hasClass("odd-parent")){
                rowsList.add(row);
            }

        }
        return new Elements(rowsList);
    }
}
