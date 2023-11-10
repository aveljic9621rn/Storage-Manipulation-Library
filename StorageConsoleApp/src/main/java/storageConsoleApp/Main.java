package storageConsoleApp;

import com.sun.jdi.InvalidTypeException;
import storageSpecs.StorageSpecs;
import storageSpecs.StorageManager;
import storageSpecs.criteria.FilterCriteria;
import storageSpecs.criteria.OrderCriteria;
import storageSpecs.criteria.SortCriteria;

import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static StorageSpecs createStorage(String[] args) throws Exception {
        String storageType = args[0];
        String path = args[1];
        String type;
        if (storageType.equalsIgnoreCase("google")) {
            type = "gdrive.GoogleDriveExample";
        } else if (storageType.equalsIgnoreCase("local")) {
            type = "localstorage.LocalStorage";
        } else {
            type = "invalid";
        }

        if (type.equals("invalid")) {
            throw new InvalidTypeException("Invalid storage type provided");
        }

        Class.forName(type);
        return StorageManager.getStorage(path);
    }

    public static StorageSpecs checkRoot(StorageSpecs storage) throws Exception {
        boolean validRoot = storage.checkRoot(storage.getRoot());
        if (validRoot) {
            return storage;
        }

        System.out.println("Provided root doesn't exist");
        if (!storage.createRoot(storage.getRoot())) {
            throw new Exception("Couldn't create the root, please try again with a different root");
        }

        System.out.println("Created the missing root successfully");
        return storage;
    }

    public static StorageSpecs checkConfig(StorageSpecs storage, Scanner input) {
        boolean configExists = storage.checkConfig(storage.getRoot());
        if (configExists) {
            System.out.println("Configuration already exists, starting the instance");
            return storage;
        }

        System.out.println("Configuration not found, would you like to create a new one or use a default one");
        System.out.println("1 - Create a new one");
        System.out.println("2 - Use a default one");
        String option = input.nextLine().trim();

        boolean task = false;
        while (!task) {
            switch (option) {
                case "1" -> {
                    System.out.println("Enter the max size the storage would be");
                    double maxSizeLimit = Double.parseDouble(input.nextLine());

                    System.out.println("Enter a list of Forbidden extensions separated by commas");
                    String[] extensions = input.nextLine().split(",");
                    List<String> ForbiddenExtensions = new ArrayList<>();
                    for (String extension : extensions) {
                        ForbiddenExtensions.add(extension.toLowerCase().trim());
                    }

                    storage.createConfig(maxSizeLimit, ForbiddenExtensions);
                    System.out.println("Created a new configuration");
                    task = true;
                }
                case "2" -> {
                    storage.createConfig();
                    System.out.println("Created a default configuration");
                    task = true;
                }
                default -> {
                    System.out.println("invalid option, valid options are 1 and 2");
                    option = input.nextLine().trim();
                }
            }
        }

        return storage;
    }

    public static String getCommand(String input) {
        String[] split = input.split(" ");
        if (split.length == 0) {
            System.out.println("Couldn't find a command");
            return null;
        }
        return split[0].trim().toLowerCase();
    }

    public static String[] getArguments(String input) {
        String[] split = input.split(" ");
        List<String> arguments = new ArrayList<>(Arrays.asList(split).subList(1, split.length));
        String[] args = new String[arguments.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = arguments.get(i);
        }

        return args;
    }

    public static void handleCommands(StorageSpecs storage, Scanner input) {
        String line = input.nextLine();
        String command = getCommand(line);
        while (!Objects.equals(command, "exit")) {
            if (command == null) {
                line = input.nextLine();
                command = getCommand(line);
                continue;
            }
            try {
                switch (command) {
                    case "cd" -> {
                        String[] args = getArguments(line);
                        if (args.length != 1) {
                            System.out.println("Expected 1 argument, got " + args.length);
                            break;
                        }

                        String path = args[0];
                        if (path.equalsIgnoreCase("..")) {
                            if (storage.returnBackFromDirectory()) {
                                System.out.println("Successfully returned to parent directory");
                            } else {
                                System.out.println("Unable to return to parent directory");
                            }
                        } else {
                            if (storage.enterDirectory(path)) {
                                System.out.println("Successfully entered " + path);
                            } else {
                                System.out.println("Unable to enter new directory");
                            }
                        }
                    }
                    case "create" -> {
                        System.out.println("What would you like to create?");
                        System.out.println("1 - A new directory with a name");
                        System.out.println("2 - A new directory with a name and limit of files that can be in it");
                        System.out.println("3 - A range of directories");
                        System.out.println("4 - A range of directories that start with a name");
                        String option = input.nextLine().trim();

                        boolean task = false;
                        while (!task) {
                            switch (option) {
                                case "1" -> {
                                    System.out.println("Enter the new directory's name");
                                    String name = input.nextLine();

                                    boolean success = storage.createDirectory(name);
                                    if (success) {
                                        System.out.println("Successfully created " + name);
                                    } else {
                                        System.out.println("Something went wrong when trying to create the new directory");
                                    }

                                    task = true;
                                }
                                case "2" -> {
                                    System.out.println("Enter the new directory's name");
                                    String name = input.nextLine();

                                    System.out.println("Enter the new directory's limit of files");
                                    int limit = Integer.parseInt(input.nextLine());

                                    boolean success = storage.createDirectory(name, limit);
                                    if (success) {
                                        System.out.println("Successfully created " + name + " with limit " + limit);
                                    } else {
                                        System.out.println("Something went wrong when trying to create the new directory");
                                    }

                                    task = true;
                                }
                                case "3" -> {
                                    System.out.println("Enter the bottom value of the range");
                                    int start = Integer.parseInt(input.nextLine());

                                    System.out.println("Enter the top value of the range");
                                    int end = Integer.parseInt(input.nextLine());

                                    boolean success = storage.createDirectory(start, end);
                                    if (success) {
                                        System.out.println("Successfully created the new directories");
                                    } else {
                                        System.out.println("Something went wrong when trying to create some of the directories");
                                    }

                                    task = true;
                                }
                                case "4" -> {
                                    System.out.println("Enter the prefix name for the directories");
                                    String name = input.nextLine();

                                    System.out.println("Enter the bottom value of the range");
                                    int start = Integer.parseInt(input.nextLine());

                                    System.out.println("Enter the top value of the range");
                                    int end = Integer.parseInt(input.nextLine());

                                    boolean success = storage.createDirectory(name, start, end);
                                    if (success) {
                                        System.out.println("Successfully created the new directories");
                                    } else {
                                        System.out.println("Something went wrong when trying to create some of the directories");
                                    }

                                    task = true;
                                }
                                default -> {
                                    System.out.println("You've entered an invalid option, valid options are 1, 2, 3, and 4");
                                    option = input.nextLine().trim();
                                }
                            }
                        }
                    }
                    case "delete" -> {
                        String[] args = getArguments(line);
                        if (args.length != 1) {
                            System.out.println("Expected 1 argument, got " + args.length);
                            break;
                        }

                        String name = args[0];
                        if (storage.deleteFileOrFolder(name)) {
                            System.out.println("Successfully deleted " + name);
                        } else {
                            System.out.println("Unable to delete the file or directory");
                        }
                    }
                    case "upload" -> {
                        String[] args = getArguments(line);
                        if (args.length != 1) {
                            System.out.println("Expected 1 argument, got " + args.length);
                            break;
                        }

                        String name = args[0];
                        if (storage.addFile(name)) {
                            System.out.println("Successfully uploaded " + name);
                        } else {
                            System.out.println("Unable to add the file or directory");
                        }
                    }
                    case "download" -> {
                        String[] args = getArguments(line);
                        if (args.length != 2) {
                            System.out.println("Expected 2 arguments, got " + args.length);
                            break;
                        }

                        String name = args[0];
                        String path = args[1];
                        if (storage.downloadFileOrDirectory(name, path)) {
                            System.out.println("Successfully downloaded " + name + " to " + path);
                        } else {
                            System.out.println("Unable to download the file or directory");
                        }
                    }
                    case "move" -> {
                        String[] args = getArguments(line);
                        if (args.length != 2) {
                            System.out.println("Expected 2 arguments, got " + args.length);
                            break;
                        }

                        String name = args[0];
                        String path = args[1];
                        if (storage.moveFileOrDirectory(name, path)) {
                            System.out.println("Successfully moved " + name + " to " + path);
                        } else {
                            System.out.println("Unable to move the file or directory");
                        }
                    }

                    case "rename" -> {
                        String[] args = getArguments(line);
                        if (args.length != 2) {
                            System.out.println("Expected 2 arguments, got " + args.length);
                            break;
                        }
                        String oldName = args[0];
                        String newName = args[1];
                        if (storage.renameFileOrDirectory(oldName, newName)) {
                            System.out.println("Successfully renamed " + oldName + " to " + newName);
                        } else {
                            System.out.println("Unable to rename the file or directory");

                        }

                    }
                    case "search" -> {
                        List<String> result = new ArrayList<>();
                        System.out.println("What would you like to search by?");
                        System.out.println("1 - Name of file or directory in current directory");
                        System.out.println("2 - Extension of file in current directory");
                        System.out.println("3 - Last modified date in current directory");
                        System.out.println("4 - All files in current directory");
                        System.out.println("5 - All files from directories of current directory");
                        System.out.println("6 - All files and sub-files in current directory");
                        System.out.println("7 - Substring matching any name of files or directories in current directory");
                        String option = input.nextLine().trim();

                        boolean task = false;
                        while (!task) {
                            switch (option) {
                                case "1" -> {
                                    System.out.println("Enter the name to search with");
                                    String name = input.nextLine();
                                    result.addAll(storage.searchByName(name));
                                    task = true;
                                }
                                case "2" -> {
                                    System.out.println("Enter the extension to search with");
                                    String extension = input.nextLine();
                                    result.addAll(storage.searchByExtension(extension));
                                    task = true;
                                }
                                case "3" -> {
                                    System.out.println("Enter the date to search newer files with");
                                    Date date = new SimpleDateFormat("dd/MM/yyyy").parse(input.nextLine());
                                    result.addAll(storage.searchByModifiedAfter(date));
                                    task = true;
                                }
                                case "4" -> {
                                    result.addAll(storage.searchAllFromRoot(storage.getRoot()));
                                    task = true;
                                }
                                case "5" -> {
                                    result.addAll(storage.searchAllFromRootWithoutRoot(storage.getRoot()));
                                    task = true;
                                }
                                case "6" -> {
                                    result = storage.searchAllFromRoot(storage.getRoot());
                                    result.addAll(storage.searchAllFromRootWithoutRoot(storage.getRoot()));
                                    task = true;
                                }
                                case "7" -> {
                                    System.out.println("Enter the substring to search with");
                                    String substring = input.nextLine();
                                    result.addAll(storage.searchByPartOfName(substring));
                                    task = true;
                                }
                                default -> {
                                    System.out.println("You've entered an invalid option, valid options are 1, 2, 3, 4, 5, 6 and 7");
                                    option = input.nextLine().trim();
                                }
                            }
                        }

                        if (result.size() == 0) {
                            System.out.println("No results found");
                            break;
                        }

                        if (result.size() > 1) {
                            System.out.println("Would you like to sort your results");
                            System.out.println("1 - Yes");
                            System.out.println("2 - No");
                            String sorting = input.nextLine();

                            task = false;
                            while (!task) {
                                switch (sorting) {
                                    case "1" -> {
                                        System.out.println("Please choose what to sort by");
                                        System.out.println("1 - Name");
                                        System.out.println("2 - Extension");
                                        System.out.println("3 - Creation date");
                                        System.out.println("4 - Last modified date");
                                        String sortingOption = input.nextLine();
                                        SortCriteria sortType = SortCriteria.NAME;

                                        boolean subTask = false;
                                        while (!subTask) {
                                            switch (sortingOption) {
                                                case "1" -> subTask = true;
                                                case "2" -> {
                                                    sortType = SortCriteria.EXTENSION;
                                                    subTask = true;
                                                }
                                                case "3" -> {
                                                    sortType = SortCriteria.CREATION_DATE;
                                                    subTask = true;
                                                }
                                                case "4" -> {
                                                    sortType = SortCriteria.MODIFY_DATE;
                                                    subTask = true;
                                                }
                                                default -> {
                                                    System.out.println("You've entered an invalid option, valid options are 1, 2, 3 and 4");
                                                    sortingOption = input.nextLine();
                                                }
                                            }
                                        }


                                        System.out.println("Please choose what order to sort in");
                                        System.out.println("1 - Ascending");
                                        System.out.println("2 - Descending");
                                        String orderOption = input.nextLine();
                                        OrderCriteria orderType = OrderCriteria.ASCENDING;

                                        subTask = false;
                                        while (!subTask) {
                                            switch (orderOption) {
                                                case "1" -> subTask = true;
                                                case "2" -> {
                                                    orderType = OrderCriteria.DESCENDING;
                                                    subTask = true;
                                                }
                                                default -> {
                                                    System.out.println("You've entered an invalid option, valid options are 1 and 2");
                                                    orderOption = input.nextLine();
                                                }
                                            }
                                        }

                                        System.out.println("aloha");
                                        result = storage.sortResults(result, sortType, orderType);
                                        task = true;
                                    }
                                    case "2" -> task = true;
                                    default -> {
                                        System.out.println("You've entered an invalid option, valid options are 1 and 2");
                                        sorting = input.nextLine();
                                    }
                                }
                            }
                        }

                        System.out.println("Would you like to filter your results");
                        System.out.println("1 - Yes");
                        System.out.println("2 - No");
                        String filtering = input.nextLine();

                        task = false;
                        while (!task) {
                            switch (filtering) {
                                case "1" -> {
                                    System.out.println("Please input one or more filtering option");
                                    System.out.println("1 - File name");
                                    System.out.println("2 - File extension");
                                    System.out.println("3 - File creation date");
                                    System.out.println("4 - File's last modified date");
                                    String[] filteringOptions = input.nextLine().split(" ");
                                    List<FilterCriteria> filterTypes = new ArrayList<>();
                                    for (String filterOption : filteringOptions) {
                                        switch (filterOption) {
                                            case "1" -> {
                                                if (!filterTypes.contains(FilterCriteria.NAME)) {
                                                    filterTypes.add(FilterCriteria.NAME);
                                                }
                                            }
                                            case "2" -> {
                                                if (!filterTypes.contains(FilterCriteria.EXTENSION)) {
                                                    filterTypes.add(FilterCriteria.EXTENSION);
                                                }
                                            }
                                            case "3" -> {
                                                if (!filterTypes.contains(FilterCriteria.CREATION_DATE)) {
                                                    filterTypes.add(FilterCriteria.CREATION_DATE);
                                                }
                                            }
                                            case "4" -> {
                                                if (!filterTypes.contains(FilterCriteria.MODIFY_DATE)) {
                                                    filterTypes.add(FilterCriteria.MODIFY_DATE);
                                                }
                                            }
                                            default -> System.out.println(filterOption + " was an invalid option, skipping");
                                        }
                                    }
                                    result = storage.filterResults(result, filterTypes);
                                    task = true;
                                }
                                case "2" -> task = true;
                                default -> {
                                    System.out.println("You've entered an invalid option, valid options are 1 and 2");
                                    filtering = input.nextLine();
                                }
                            }
                        }

                        for (String found : result) {
                           System.out.println(found);
                        }
                    }
                    case "help" -> {
                        System.out.println("Here's the list of all available commands:");
                        System.out.println("cd <dir>                 - Enters a subdirectory if it exists");
                        System.out.println("cd ..                    - Returns to parent directory");
                        System.out.println("create                   - Displays a menu for creating a new directory");
                        System.out.println("delete <file name>       - Deletes a file from current directory if it exists");
                        System.out.println("upload <path>            - Uploads a file from specified path to storage");
                        System.out.println("move <name> <path>       - Moves a specified file to a new location");
                        System.out.println("rename <name> <new name> - Renames a file or directory");
                        System.out.println("download <file name> <path> - Download a file or directory");
                        System.out.println("search                   - Displays a menu for searching through the storage");

                    }
                    default -> System.out.println("Invalid command, please see \"help\" for a list of valid commands");
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            line = input.nextLine();
            command = getCommand(line);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("Invalid amount of arguments, expected 2, got " + args.length);
        }

        System.out.println("Welcome to storage");

        Scanner input = new Scanner(System.in);
        StorageSpecs storage = createStorage(args);

        storage = checkRoot(storage);
        storage = checkConfig(storage, input);

        System.out.println("Type a command or \"exit\" to close the program. See \"help\" for a full list of commands");
        handleCommands(storage, input);
    }
}