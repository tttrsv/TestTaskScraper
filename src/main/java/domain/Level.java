package domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JacksonXmlRootElement(localName = "level")
public class Level {

    @JacksonXmlProperty(isAttribute = true)
    private String path;

    @JacksonXmlProperty(isAttribute = true, localName = "text_exist")
    private Boolean textExist;

    @JacksonXmlProperty(localName = "level")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Level> childLevel = new ArrayList<>();

    private String heading;

    private String citation;

    private Text text;

    public Level returnLastLevel() {
        if (Objects.nonNull(childLevel) && childLevel.size() != 0) {
            Level level = this.childLevel.get(0);
            while (level != null) {
                List<Level> childLevel = level.getChildLevel();
                if (childLevel == null || childLevel.size() == 0) {
                    return level;
                }
                level = childLevel.get(0);
            }
            return level;
        }
        return null;
    }

    public Level returnLastHasText() {
        return childLevel.stream()
                .filter(level -> Boolean.TRUE.equals(level.textExist))
                .findFirst()
                .orElse(childLevel.get(childLevel.size() - 1));
    }

    public void appendPathName(String value) {
        String[] joinList = new String[]{getPath(), value};
        final String join = StringUtils.join(joinList, "/");
        setPath(join);
    }
}
