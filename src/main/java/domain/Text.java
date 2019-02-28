package domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "text")
public class Text {
    @JacksonXmlProperty(localName = "p")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Paragraph> paragraphs;
}
