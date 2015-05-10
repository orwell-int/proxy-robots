package orwell.proxy.config;

/**
 * Created by miludmann on 5/5/15.
 */
public class ConfigFactoryParameters {
    private final String filePath;
    private final EnumConfigFileType enumConfigFileType;

    public ConfigFactoryParameters(String filePath, EnumConfigFileType enumConfigFileType) {
        this.filePath = filePath;
        this.enumConfigFileType = enumConfigFileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public EnumConfigFileType getEnumConfigFileType() {
        return enumConfigFileType;
    }
}
