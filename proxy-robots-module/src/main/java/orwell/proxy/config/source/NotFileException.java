package orwell.proxy.config.source;

import java.nio.file.FileSystemException;

/**
 * Created by parapampa on 14/06/15.
 */
public class NotFileException extends FileSystemException {

    public NotFileException(String file) {
        super(file);
    }
}
