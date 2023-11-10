package storageSpecs;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import storageSpecs.criteria.ConfigurationItem;
import storageSpecs.criteria.FilterCriteria;
import storageSpecs.criteria.OrderCriteria;
import storageSpecs.criteria.SortCriteria;
import storageSpecs.exceptions.FileCountLimitException;
import storageSpecs.exceptions.FileCountLimitMultipleException;
import storageSpecs.exceptions.ForbiddenExtensionUploadException;

import storageSpecs.exceptions.MaxSizeLimitException;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import  static  storageSpecs.criteria.FilterCriteria.MODIFY_DATE;

public abstract class StorageSpecs {
    private String root;
    private Configuration config;

    /**
     * Check if a config.json file exists at the provided root
     *
     * @param root The path to the root of the storage
     * @return If the config.json file exists or not
     */
    public abstract boolean checkConfig(String root);

    /**
     * Create a new configuration with default values
     */
    public void createConfig() {
        this.config = new Configuration(16000000, new ArrayList<>());
        updateConfig();
    }

    /**
     * Create a new configuration with custom max size limit and Forbidden extensions
     *
     * @param maxSizeLimit     The maximum limit of bytes the storage can hold
     * @param ForbiddenExtensions The list of Forbidden extensions
     */
    public void createConfig(double maxSizeLimit, List<String> ForbiddenExtensions) {
        this.config = new Configuration(maxSizeLimit, ForbiddenExtensions);
        updateConfig();
    }

    /**
     * Get the current root position
     *
     * @return The current root position
     */
    public String getRoot() {
        return root;
    }

    /**
     * Set the current root position
     *
     * @param root The new root position
     */
    public void setRoot(String root) {
        this.root = root;
    }

    /**
     * Check if a provided root path leads to an existing directory
     *
     * @param root The path to the storage root
     * @return If the root exists
     */
    public abstract boolean checkRoot(String root);

    /**
     * Create a root folder if one doesn't exist
     *
     * @param root The path to use to create the root
     * @return Whether the action was successful
     */
    public abstract boolean createRoot(String root);

    /**
     * Get the maximum size limit of an uploaded file
     *
     * @return The limit in bytes
     */
    public double getMaxSizeLimit() {
        return (Double) readConfig(ConfigurationItem.MAX_SIZE_LIMIT);
    }

    /**
     * Get the list of Forbidden extensions
     *
     * @return An ArrayList of Forbidden extensions
     */
    public List<String> getForbiddenExtensions() {
        return (List<String>) readConfig(ConfigurationItem.FORBIDDEN_EXTENSIONS);
    }

    /**
     * Get the maximum amount of files that can be in a directory
     *
     * @return The amount of files as an integer
     */
    public HashMap getFileCountLimits() {
        return (HashMap) readConfig(ConfigurationItem.FILE_COUNT_LIMIT);
    }

    /**
     * Update the maximum amount of files that can be in a directory
     *
     * @param fileCountLimits Updated version of the list
     */
    public void updateFileCountLimits(HashMap fileCountLimits) {
        config.setFileCountLimits(fileCountLimits);
        updateConfig();
    }

    /**
     * Update the json file with new values
     */
    protected abstract void updateConfig();

    protected String setConfigJson() {
        JSONObject json = new JSONObject();
        json.put("max_size_limit", config.getMaxSizeLimit());
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(config.getForbiddenExtensions());
        json.put("Forbidden_extensions", jsonArray);
        json.put("file_count_limits", new JSONObject(config.getFileCountLimits()));
        return json.toString();
    }

    /**
     * Get a specific item from the config
     *
     * @param configItem The item of choice
     * @return The value of the item
     */
    protected abstract Object readConfig(ConfigurationItem configItem);

    /**
     * Get a specific part of config
     *
     * @param json       The json object of the config
     * @param configItem The item to search for
     * @return An object that can be converted to a usable value
     */
    protected Object getConfig(JSONObject json, ConfigurationItem configItem) {
        switch (configItem) {
            case FORBIDDEN_EXTENSIONS -> {
                return json.get("Forbidden_extensions");
            }
            case FILE_COUNT_LIMIT -> {
                return json.get("file_count_limits");
            }
            case MAX_SIZE_LIMIT -> {
                return json.get("max_size_limit");
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Set the config of the storage
     *
     * @param config The new config
     */
    public void setConfig(Configuration config) {
        this.config = config;
    }

    /**
     * Enter a specified directory
     *
     * @param name The name of the directory
     */
    public abstract boolean enterDirectory(String name);

    /**
     * Return to the previous directory
     */
    public abstract boolean returnBackFromDirectory();

    /**
     * Create a directory in the current directory
     *
     * @param name The name of the directory
     * @return If the action was successful or not
     * @throws FileAlreadyExistsException     If a directory with the same name already exists
     * @throws FileCountLimitException If the parent directory is full
     */
    public abstract boolean createDirectory(String name) throws FileAlreadyExistsException, FileCountLimitException;

    /**
     * Create a directory in the current directory with a file count limit
     *
     * @param name  The name of the directory
     * @param limit The limit of files and directories
     * @return If the action was successful or not
     * @throws FileAlreadyExistsException     If a directory with the same name already exists
     * @throws FileCountLimitException If the parent directory is full
     */
    public abstract boolean createDirectory(String name, int limit) throws FileAlreadyExistsException, FileCountLimitException;

    /**
     * Create several directories with names in range of given numbers in the current directory
     *
     * @param start The bottom number that the file is named with
     * @param end   The top number that the file is named with
     * @return If the action was successful or not
     * @throws FileAlreadyExistsException     If a directory with the same name already exists
     * @throws FileCountLimitException If the parent directory is full
     */
    public abstract boolean createDirectory(int start, int end) throws FileAlreadyExistsException, FileCountLimitException;

    /**
     * Create several directories with a prefix and names ending in range of numbers in the current directory
     *
     * @param name  The name prefix
     * @param start The bottom number
     * @param end   The top number
     * @return If the action was successful or not
     * @throws FileAlreadyExistsException     If a directory with the same name already exists
     * @throws FileCountLimitException If the parent directory is full
     */
    public abstract boolean createDirectory(String name, int start, int end) throws FileAlreadyExistsException, FileCountLimitException;

    /**
     * Move a given file to the storage
     *
     * @param file The path to the file that's being added to the current directory
     * @return If the action was successful or not
     * @throws FileAlreadyExistsException     If a file with the same name already exists
     * @throws FileCountLimitException If the parent directory is full
     * @throws MaxSizeLimitException  If the storage is full
     */
    public abstract boolean addFile(String file) throws FileAlreadyExistsException, FileCountLimitException, MaxSizeLimitException;

    /**
     * Delete a file or directory at a given path
     *
     * @param name The name of the file or directory
     * @return If the action was successful or not
     * @throws FileNotFoundException If a file with specified name doesn't exist
     */
    public abstract boolean deleteFileOrFolder(String name) throws FileNotFoundException;

    /**
     * Move a file from current directory to a new path
     *
     * @param name The name of the file to move
     * @param path The relative path where the file will be moved to
     * @return If the action was successful or not
     * @throws FileNotFoundException          If a file with specified name doesn't exist
     * @throws FileAlreadyExistsException     If a file with specified name already exists in the new location
     * @throws FileCountLimitException If the new location is full
     */
    public abstract boolean moveFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitException;

    /**
     * Download a file or directory from current directory
     *
     * @param name The name of the file to download
     * @param path The path to place the file in
     * @return If the action was successful or not
     * @throws FileNotFoundException      If a file with specified name doesn't exist
     * @throws FileAlreadyExistsException If a file with specified name already exists in the download location
     */
    public abstract boolean downloadFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException;

    /**
     * Rename a file or directory in the current directory
     *
     * @param name    The name of the file to rename
     * @param newName The new name
     * @return If the action was successful or not
     * @throws FileNotFoundException      If a file with specified name doesn't exist
     * @throws FileAlreadyExistsException If a file with the same new name already exists
     */
    public abstract boolean renameFileOrDirectory(String name, String newName) throws FileNotFoundException, FileAlreadyExistsException;

    /**
     * List all files with matching name in the current directory
     *
     * @param name The name of the file or directory
     */
    public abstract List<String> searchByName(String name);

    /**
     * List all files with matching extension in the current directory
     *
     * @param extension The extension to filter by
     */
    public abstract List<String> searchByExtension(String extension);

    /**
     * Search a file or directory by its last modified date being younger than provided date in the current directory
     *
     * @param date The maximum date the results can be old
     */
    public abstract List<String> searchByModifiedAfter(Date date);

    /**
     * List all files in the given directory
     *
     * @param root The directory to search in
     * @return List of results
     */
    public abstract List<String> searchAllFromRoot(String root);

    /**
     * List all files within directories of given directory
     *
     * @param root The directory to search in
     * @return List of results
     */
    public abstract List<String> searchAllFromRootWithoutRoot(String root);

    /**
     * List all files and sub-files in the current directory
     *
     * @return List of results
     */
    public abstract List<String> searchAll();

    /**
     * List all files which contain a substring in their name in the current directory
     *
     * @param substring The substring to search with
     * @return List of results
     */
    public abstract List<String> searchByPartOfName(String substring);

    public abstract List<MyFile> returnFileList(List<String> list);

    /**
     * Sort the list of results
     *
     * @param list      The results to sort
     * @param sortType  The type to sort by
     * @param orderType The order to sort by
     * @return Sorted list of results
     */
    public List<String> sortResults(List<String> list, SortCriteria sortType, OrderCriteria orderType) {
        List<MyFile> files = returnFileList(list);

        switch (sortType) {
            case NAME -> files.sort((o1, o2) -> {
                if (orderType == OrderCriteria.ASCENDING) {
                    return o1.getName().compareTo(o2.getName());
                } else {
                    return o2.getName().compareTo(o1.getName());
                }
            });
            case EXTENSION -> files.sort((o1, o2) -> {
                if (orderType == OrderCriteria.ASCENDING) {
                    return o1.getExtension().compareTo(o2.getExtension());
                } else {
                    return o2.getExtension().compareTo(o1.getExtension());
                }
            });
            case CREATION_DATE -> files.sort((o1, o2) -> {
                if (orderType == OrderCriteria.ASCENDING) {
                    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                } else {
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                }
            });
            case MODIFY_DATE -> files.sort((o1, o2) -> {
                if (orderType == OrderCriteria.ASCENDING) {
                    return o1.getLastModifiedAt().compareTo(o2.getLastModifiedAt());
                } else {
                    return o2.getLastModifiedAt().compareTo(o1.getLastModifiedAt());
                }
            });
        }
        List<String> result = new ArrayList<>();
        for (MyFile file : files) {
            result.add(file.getPath());
        }

        return result;
    }

    /**
     * Filter the list of results before printing it
     *
     * @param list        The results to filter
     * @param filterTypes The list of values to keep
     * @return Filtered list of results
     */
    public List<String> filterResults(List<String> list, List<FilterCriteria> filterTypes) {
        List<MyFile> files = returnFileList(list);

        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        List<String> result = new ArrayList<>();
        for (MyFile file : files) {
            if (Objects.equals(file.getPath(), getRoot() + "\\config.json")) {
                continue;
            }

            StringBuilder row = new StringBuilder();
            for (FilterCriteria filterType : filterTypes) {
                row.append(" ");
                switch (filterType) {
                    case NAME -> row.append(file.getName());
                    case EXTENSION -> row.append(file.getExtension());
                    case CREATION_DATE -> row.append(dateFormat.format(file.getCreatedAt()));
                    case MODIFY_DATE -> row.append(dateFormat.format(file.getLastModifiedAt()));
                }
            }
            result.add(row.toString().trim());
        }

        return result;
    }

    /**
     * Check if the file would go over the allowed limit
     *
     * @param root The directory that's being checked
     * @throws FileCountLimitException If the directory is full
     */
    protected abstract void checkFileCountLimit(String root) throws FileCountLimitException;

    /**
     * Check if the new files would go over the allowed limit
     *
     * @param root  The directory that's being checked
     * @param count The amount of new files being added
     * @throws FileCountLimitMultipleException If the directory would be full
     */
    protected abstract void checkMultipleFileCountLimit(String root, int count) throws FileCountLimitMultipleException;

    /**
     * Check if an extension is in the list of Forbidden extensions
     *
     * @param name The extension of the file
     * @throws ForbiddenExtensionUploadException If the extension is in the list of Forbidden extensions
     */
    protected void checkForbiddenExtension(String name) throws ForbiddenExtensionUploadException {
        if (getForbiddenExtensions().contains(name.toLowerCase().trim())) {
            throw new ForbiddenExtensionUploadException();
        }
    }

    /**
     * Check if the storage is full
     *
     * @param size The size of the file
     * @throws MaxSizeLimitException If the storage is full
     */
    protected abstract void checkMaxSizeLimit(double size) throws MaxSizeLimitException;
}