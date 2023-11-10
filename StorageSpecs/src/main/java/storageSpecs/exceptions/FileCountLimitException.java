package storageSpecs.exceptions;

public class FileCountLimitException extends RuntimeException {
    public FileCountLimitException() {
        super("You've reached the maximum amount of files that can be in a directory");
    }
}
