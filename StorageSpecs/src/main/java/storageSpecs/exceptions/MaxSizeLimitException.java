package storageSpecs.exceptions;

public class MaxSizeLimitException extends RuntimeException {
    public MaxSizeLimitException() {
        super("The file you're trying to move is larger than the maximum size limit");
    }
}
