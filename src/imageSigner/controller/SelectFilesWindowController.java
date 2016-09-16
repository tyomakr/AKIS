package imageSigner.controller;

import imageSigner.containers.FileItem;
import imageSigner.storage.FileItemsStorage;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;


public class SelectFilesWindowController {

    //создаем лист для обработки файлов
    private ObservableList<FileItem> fileItemsList = FileItemsStorage.getInstance().getFileItemsList();

    //для метода добавления
    private boolean isAddFolderSubfolder;
    private boolean isAddOnlyFiles;


    //объявляем поля и элементы из FXML
    @FXML public BorderPane bp;

    @FXML public Button buttonAddDirSub;
    @FXML public Button buttonAddDir;
    @FXML public Button buttonAddFile;
    @FXML public Button buttonRemoveFile;
    @FXML public Button buttonMoveFileUP;
    @FXML public Button buttonMoveFileDOWN;
    @FXML public Button buttonClearAll;

    @FXML public TableView<FileItem> tableView;
    @FXML private TableColumn<FileItem, String> tableColumnFileName;
    @FXML private TableColumn<FileItem, String> tableColumnFilePath;

    @FXML public Label labelQuantityFiles;


    //initialize
    public void initialize() {
        fillTable();
    }

    //обновление счетчика файлов
    private void refreshQtyCounter() {
        FileItemsStorage.getInstance().refreshLabelCounter(labelQuantityFiles);
    }

    //заполнение таблицы текущими данными из FileItemsStorage
    private void fillTable(){
        tableColumnFileName.setCellValueFactory(cellData -> cellData.getValue().filenameProperty());
        tableColumnFilePath.setCellValueFactory(cellData -> cellData.getValue().filePathProperty());

        tableView.setItems(fileItemsList);

        refreshQtyCounter();
    }


    /** Методы кнопок */
    //добавление содержимого папки и подпапок в таблицу
    public void handleButtonAddFolderWithSubDirs() {
        isAddFolderSubfolder = true;
        isAddOnlyFiles = false;
        //передаем параметры этого пункта в метод добавления
        FileItemsStorage.getInstance().addFiles(isAddFolderSubfolder, isAddOnlyFiles);
        //заполняем таблицу
        fillTable();
    }

    //запись содержимого папки в таблицу
    public void handleButtonAddFolder() {
        isAddFolderSubfolder = false;
        isAddOnlyFiles = false;
        //передаем параметры этого пункта в метод добавления
        FileItemsStorage.getInstance().addFiles(isAddFolderSubfolder, isAddOnlyFiles);
        //заполняем таблицу
        fillTable();
    }

    //добавление только выбранных файлов в таблицу
    public void handleButtonAddFile() {
        isAddOnlyFiles = true;
        //передаем параметры этого пункта в метод добавления
        FileItemsStorage.getInstance().addFiles(isAddFolderSubfolder, isAddOnlyFiles);
        //заполняем таблицу
        fillTable();
    }

    //удаление файла из таблицы
    public void handleButtonRemoveFile() {

        int row = tableView.getSelectionModel().getSelectedIndex();
        if (row >= 0) {
            fileItemsList.remove(row);
        }
        fillTable();
    }

    //поднять файл в списке
    public void handleButtonMoveFileUp() {

        int row = tableView.getSelectionModel().getSelectedIndex();
        //проверяем, если элемент не первый в списке
        if (row > 0) {

            FileItem fileItem1 = fileItemsList.get(row);
            FileItem fileItem2 = fileItemsList.get(row - 1);

            fileItemsList.set(row - 1, fileItem1);
            fileItemsList.set(row, fileItem2);
        }
    }

    //опустить файл в списке
    public void handleButtonMoveFileDown() {

        int row = tableView.getSelectionModel().getSelectedIndex();
        //проверяем, если элемент не последний в списке
        if (row != fileItemsList.size() - 1) {
            FileItem fileItem1 = fileItemsList.get(row);
            FileItem fileItem2 = fileItemsList.get(row + 1);

            fileItemsList.set(row + 1, fileItem1);
            fileItemsList.set(row, fileItem2);
        }
    }

    //очистка списка файлов
    public void handleButtonClearAll() {
        fileItemsList.clear();
        fillTable();
    }

}
