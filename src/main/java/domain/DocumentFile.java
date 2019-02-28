package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import utils.ScraperConstants;

import java.time.LocalDate;

@Data
@JsonPropertyOrder({"taskname", "workername", "date", "level"})
@JacksonXmlRootElement(localName = "document")
public class DocumentFile {


    @JacksonXmlProperty(localName = "taskname")
    private String taskName = ScraperConstants.TASK_NAME;

    @JacksonXmlProperty(localName = "workername")
    private String workerName = ScraperConstants.WORKER_NAME;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate date = LocalDate.now();

    private Level level;

    @JsonIgnore
    private String fileName;
}
