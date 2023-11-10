package storageSpecs.exceptions;

public class FileCountLimitMultipleException extends RuntimeException {
    public FileCountLimitMultipleException() {
        super("You've reached the maximum amount of files that can be in a directory");
    }
}
