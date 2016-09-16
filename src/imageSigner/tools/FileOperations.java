package imageSigner.tools;


import imageSigner.containers.FileItem;
import imageSigner.storage.FileItemsStorage;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.ResourceBundle;

import static imageSigner.MainApp.RESOURCE_PATH;

public class FileOperations {

    //создать папку
    public static void makeDir (String path) {
        File file = new File(path);
        file.mkdirs();
    }

    //Копирование файла
    public static void copyFile (File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }

    public static void backupOriginal(int currentFileIndex) {

        ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "common", Locale.ENGLISH);

        ObservableList<FileItem> fileItemsList = FileItemsStorage.getInstance().getFileItemsList();

        /** ДЕЛАЕМ РЕЗЕРВНУЮ КОПИЮ ИСХОДНОГО ФАЙЛа **/
        //берем оригинал файла
        File source = fileItemsList.get(currentFileIndex).getCurrentFile();
        //Создаем папку для оригиналов
        String originalFolder = fileItemsList.get(currentFileIndex).getCurrentFile().getParent() + "\\" + res.getString("backupFolderName");
        FileOperations.makeDir(originalFolder);
        //Указываем полный путь назначения
        File destination = new File(originalFolder + "\\[ORIGINAL]_" + fileItemsList.get(currentFileIndex).getFilename());
        //Копируем оригинал в созданную папку
        try {
            FileOperations.copyFile(source, destination);
        } catch (FileAlreadyExistsException e) {
            /** ЕСЛИ ФАЙЛ КОПИРОВАЛСЯ И НАХОДИТСЯ В ПАПКЕ ОРИГИНАЛОВ, ТО ПРОПУСКАЕМ **/
        } catch (IOException e) {
            e.printStackTrace();
        }
        //записываем полный путь назначения в fileItem
        fileItemsList.get(currentFileIndex).setBackupFile(destination);
    }

}
