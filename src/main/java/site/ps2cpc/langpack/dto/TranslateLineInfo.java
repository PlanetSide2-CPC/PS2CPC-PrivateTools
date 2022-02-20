package site.ps2cpc.langpack.dto;

public class TranslateLineInfo {
    private String key;
    private String type;
    private String content;
    private String originalLine;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOriginalLine() {
        return originalLine;
    }

    public void setOriginalLine(String originalLine) {
        this.originalLine = originalLine;
    }

    public TranslateLineInfo(String key, String type, String content) {
        this.key = key;
        this.type = type;
        this.content = content;
    }

    public TranslateLineInfo(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public TranslateLineInfo() {
    }

    @Override
    public String toString() {
        return "TranslateLineInfo{" +
                "key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", originalLine='" + originalLine + '\'' +
                '}';
    }
}
