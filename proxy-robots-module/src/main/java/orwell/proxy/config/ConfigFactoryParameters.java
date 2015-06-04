package orwell.proxy.config;

/**
 * Created by Michaël Ludmann on 5/5/15.
 */
public class ConfigFactoryParameters {
    private final String filePath;
    private final EnumConfigFileType enumConfigFileType;

    public ConfigFactoryParameters(final String filePath, final EnumConfigFileType enumConfigFileType) {
        this.filePath = filePath;
        this.enumConfigFileType = enumConfigFileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public EnumConfigFileType getEnumConfigFileType() {
        return enumConfigFileType;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ConfigFactoryParameters))
            return false;
        if (obj == this)
            return true;
        final ConfigFactoryParameters parameters = (ConfigFactoryParameters) obj;
        return this.enumConfigFileType == parameters.getEnumConfigFileType() &&
                this.filePath.equals(parameters.getFilePath());
    }
}
