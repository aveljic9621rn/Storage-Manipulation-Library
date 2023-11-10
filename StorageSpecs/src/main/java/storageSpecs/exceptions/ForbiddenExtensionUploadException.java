package storageSpecs.exceptions;

public class ForbiddenExtensionUploadException extends RuntimeException {
    public ForbiddenExtensionUploadException() {
        super("You're not allowed to upload files with this extension");
    }
}
