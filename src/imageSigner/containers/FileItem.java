package imageSigner.containers;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class FileItem {

    private File currentFile;
    private File backupFile;

    private final StringProperty filename;
    private final StringProperty filePath;


    public FileItem(File file, String filename, String filePath) {
        this.currentFile = file;
        this.filename = new SimpleStringProperty(file.getName());
        this.filePath = new SimpleStringProperty(file.getAbsolutePath());
    }


    /** setters and getters */
    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public File getBackupFile() {
        return backupFile;
    }

    public void setBackupFile(File backupFile) {
        this.backupFile = backupFile;
    }

    public String getFilename() {
        return filename.get();
    }

    public StringProperty filenameProperty() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename.set(filename);
    }

    public String getFilePath() {
        return filePath.get();
    }

    public StringProperty filePathProperty() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath.set(filePath);
    }

}
