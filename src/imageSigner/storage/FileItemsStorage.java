package imageSigner.storage;

import imageSigner.MainApp;
import imageSigner.containers.FileItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class FileItemsStorage {

    private static FileItemsStorage ourInstance = new FileItemsStorage();

    private FileItemsStorage() {
    }

    //доступ к экземпляру mainApp
    private MainApp mainApp;

    //список, где будут храниться файлы для работы
    private ObservableList<FileItem> fileItemsList = FXCollections.observableArrayList();


    public Label refreshLabelCounter(Label label) {
        label.setText(String.valueOf(fileItemsList.size()));
        return label;
    }


    /** Методы добавления файлов и/или папок */
    public void addFiles(boolean isAddFolderSubfolder, boolean isAddOnlyFiles) {


        /** Если добавляем файлы папками */
        if (!isAddOnlyFiles) {
            File dir = dirChooser();

            /** условие добавления подпапок */
            if (isAddFolderSubfolder) {
                scanSubFolders(dir);
            }
            /** условие добавления без подпапок */
            else if (!isAddFolderSubfolder) {
                try {
                    Files.walk(Paths.get(dir.getAbsolutePath()), 1)
                            .distinct().forEach(filePath -> additionFiles(filePath.toFile()));
                } catch (IOException | RuntimeException e) {
                    //TODO Позже допишу логгер в консоль
                    //LOGGER.error("ОШИБКА чтения :" + e.getMessage() + "\nФайл НЕ будет добавлен и обработан");
                }
            }
        }

        /** Если добавляем только отдельные файлы */
        else if (isAddOnlyFiles) {
            FileChooser fileChooser = new FileChooser();

            /** условие добавления только изображений */
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg");
            fileChooser.getExtensionFilters().add(extFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(mainApp.getPrimaryStage());

                if (files != null) fileItemsList.addAll(files.stream().filter(file -> !file.getName().startsWith(".") && file.isFile()).map(file -> new FileItem(file, file.getName(), file.getAbsolutePath())).collect(Collectors.toList()));
        }
    }

    //вспомогательный метод диалога выбора папки
    private File dirChooser() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.getInitialDirectory();
        File dir;
        dir = dirChooser.showDialog(mainApp.getPrimaryStage());

        return dir;
    }

    //вспомогательный метод для добавления содержимого папки
    private void additionFiles(File file) {

        //если имя файла не начинается с точки, все ок
        if (!file.getName().startsWith(".") && file.isFile()) {

            /** условие добавления только изображений jpeg*/
            if (       file.getName().endsWith(".jpg")
                    || file.getName().endsWith(".jpeg")
                    || file.getName().endsWith(".JPG")
                    || file.getName().endsWith(".JPEG")) {

                fileItemsList.add(new FileItem(file, file.getName(), file.getAbsolutePath()));
            }
        }
    }

    //вспомогательный метод для добавления содержимого папки и подпапок
    private void scanSubFolders(File dir) {

        try {
            Files.walk(Paths.get(dir.getAbsolutePath())).forEach(filePath -> {
                if (!Files.isDirectory(filePath) && !filePath.toString().contains("[AKIS_Originals]")) {
                    additionFiles(filePath.toFile());
                }
            });
        } catch (RuntimeException | IOException e) {
            //TODO Позже допишу логгер в консоль
            //LOGGER.error("ОШИБКА ЧТЕНИЯ ФАЙЛА/ПАПКИ - " + e.getMessage().substring(36));
        }
    }

    /** Сохранение файла как.. */
    public File saveFileAs(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save photo as..");
        FileChooser.ExtensionFilter extFilter =  new FileChooser.ExtensionFilter("Images (*.jpg)", "*.jpg");
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser.showSaveDialog(mainApp.getPrimaryStage().getScene().getWindow());
    }


    /** setters and getters */
    public static FileItemsStorage getInstance() {
        return ourInstance;
    }

    public ObservableList<FileItem> getFileItemsList() {
        return fileItemsList;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
