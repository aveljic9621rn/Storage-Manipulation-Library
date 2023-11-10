package gdrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import storageSpecs.MyFile;
import storageSpecs.StorageManager;
import storageSpecs.StorageSpecs;
import storageSpecs.criteria.ConfigurationItem;
import storageSpecs.exceptions.FileCountLimitException;
import storageSpecs.exceptions.FileCountLimitMultipleException;
import storageSpecs.exceptions.MaxSizeLimitException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GoogleDriveExample extends StorageSpecs {

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "My project";

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        StorageManager.register(new GoogleDriveExample());
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }



    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveExample.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {

        Drive service = getDriveService();

        FileList result = service.files().list()
                .setPageSize(8)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }


    }

    @Override
    public boolean checkConfig(String root) {
        FileList result = null;
        try {

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + "config.json" + "' AND '" +getRoot()+ "' in parents")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean checkRoot(String root) {
        FileList result = null;
        try {


            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            return false;
        } else {

            for (File file : files) {
                if (root.equals(file.getId())) {
                    setRoot(file.getId());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean createRoot(String root) {
        try {
            System.out.println("");
            File fileMetadata = new File();
            fileMetadata.setName(root);
            // fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = getDriveService().files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
            setRoot(file.getId());

            return true;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;    }

    @Override
    protected void updateConfig() {
        try {
            // File's metadata.
            String json = setConfigJson();
            File fileMetadata = new File();
            Path tempor = Files.createTempFile("config", ".json");
            Files.write(tempor, json.getBytes(StandardCharsets.UTF_8));
            fileMetadata.setName("config.json");
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/json");
            FileContent mediaContent = new FileContent("application/json", tempor.toFile());
            File file = getDriveService().files().create(fileMetadata, mediaContent).setFields("id").execute();
            System.out.println("File ID: " + file.getId());
            //file.getId();
        } catch (IOException e) {
            System.err.println("ERROR");

        }
    }

    @Override
    protected Object readConfig(ConfigurationItem configItem) {
        return null;
    }

    @Override
    public boolean enterDirectory(String name) {
        String dc = nameToId(name);
        if (!dc.isEmpty()) {
            setRoot(dc);
            return true;
        }
        return false;    }

    @Override
    public boolean returnBackFromDirectory() {
        return false;
    }

    @Override
    public boolean createDirectory(String name) throws FileAlreadyExistsException, FileCountLimitException {
        try {
            System.out.println("");
            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = getDriveService().files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());

        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean createDirectory(String name, int limit) throws FileAlreadyExistsException, FileCountLimitException {
        return false;
    }

    @Override
    public boolean createDirectory(int start, int end) throws FileAlreadyExistsException, FileCountLimitException {
        for (int b = start; b <= end; b++) {
            if (!createDirectory(Integer.toString(b))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean createDirectory(String name, int start, int end) throws FileAlreadyExistsException, FileCountLimitException {
        for (int b = start; b <= end; b++) {
            if (!createDirectory(name + b)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addFile(String file) throws FileAlreadyExistsException, FileCountLimitException, MaxSizeLimitException {
        try {

            File fileMetadata = new File();
            List<String> nizs = List.of(file.split("\\\\"));
            fileMetadata.setName(nizs.get(nizs.size() - 1));
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            java.io.File filePath = new java.io.File(file);
            FileContent mediaContent = new FileContent("application/octet-stream", filePath);
            File f = getDriveService().files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + f.getId());
            return true;

        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteFileOrFolder(String name) throws FileNotFoundException {
        try {
            System.out.println("");
            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setParents(Collections.singletonList(getRoot()));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            getDriveService().files().delete(nameToId(name))
                    .setFields("id")
                    .execute();

            return true;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean moveFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException, FileCountLimitException {
        File file = null;
        try {
            file = getDriveService().files().get(nameToId(name))
                    .setFields("parents")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder previousParents = new StringBuilder();
        for (String parent : file.getParents()) {
            previousParents.append(parent);
            previousParents.append(',');
        }
        // Move the file to the new folder
        try {
            file = getDriveService().files().update(nameToId(name), null)
                    .setAddParents(nameToId(path))
                    .setRemoveParents(previousParents.toString())
                    .setFields("id, parents")
                    .execute();
            System.out.println("Move successfully made.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;    }

    @Override
    public boolean downloadFileOrDirectory(String name, String path) throws FileNotFoundException, FileAlreadyExistsException {
        try {

            OutputStream outputStream = new ByteArrayOutputStream();

            getDriveService().files().get(nameToId(name)).executeMediaAndDownloadTo(outputStream);
            java.io.File f = new java.io.File(path+"\\"+name);
            FileWriter fileWriter = new FileWriter(f.getPath());
            fileWriter.write(String.valueOf(outputStream));
            fileWriter.close();
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;    }

    @Override
    public boolean renameFileOrDirectory(String name, String newName) throws FileNotFoundException, FileAlreadyExistsException {
        return false;
    }

    @Override
    public List<String> searchByName(String name) {
        FileList result = null;
        List<String> list = new ArrayList<>();
        try {
            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + name + "' AND '" +getRoot()+ "' in parents")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                list.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;    }

    @Override
    public List<String> searchByExtension(String extension) {
        FileList result = null;
        List<String> list= new ArrayList<>();
        try {

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("fullText contains '" + extension + "' AND '" +getRoot()+ "' in parents")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                list.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<String> searchByModifiedAfter(Date date) {
        FileList result = null;
        List<String> list = new ArrayList<>();
        try {

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("\tmodifiedTime > '" + date + "' AND '" +getRoot()+ "' in parents")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                list.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;    }

    @Override
    public List<String> searchAllFromRoot(String root) {
        return null;
    }

    @Override
    public List<String> searchAllFromRootWithoutRoot(String root) {
        return null;
    }

    @Override
    public List<String> searchAll() {
        FileList result = null;
        List<String> list = new ArrayList<>();
        try {

            result = getDriveService().files().list()
                    .setQ("'" +getRoot()+ "' in parents")
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                list.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<String> searchByPartOfName(String substring) {
        FileList result = null;
        List<String> list = new ArrayList<>();
        try {

            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("fullText contains '" + substring + "' AND '" +getRoot()+ "' in parents")
                    .execute();
            List<File> files = result.getFiles();
            for (File file : files) {
                list.add(file.getName() + " " + file.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<MyFile> returnFileList(List<String> list) {
        return null;
    }

    @Override
    protected void checkFileCountLimit(String root) throws FileCountLimitException {

    }

    @Override
    protected void checkMultipleFileCountLimit(String root, int count) throws FileCountLimitMultipleException {

    }

    @Override
    protected void checkMaxSizeLimit(double size) throws MaxSizeLimitException {

    }


    public String nameToId(String s) {
        FileList result = null;
        try {
            result = getDriveService().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("name = '" + s + "'")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && files.size() == 1) {
                return files.get(0).getId();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

