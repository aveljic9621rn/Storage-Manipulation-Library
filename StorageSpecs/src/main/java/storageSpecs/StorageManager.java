package storageSpecs;

public class StorageManager {
    private static StorageSpecs storage = null;

    public static void register(StorageSpecs storage) {
        StorageManager.storage = storage;
    }

    public static StorageSpecs getStorage(String root) {
        storage.setRoot(root);
        return storage;
    }
}
