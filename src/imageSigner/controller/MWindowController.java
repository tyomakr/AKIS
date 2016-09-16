package imageSigner.controller;

import imageSigner.MainApp;
import imageSigner.containers.FileItem;
import imageSigner.containers.ImageContainer;
import imageSigner.storage.FileItemsStorage;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.ResourceBundle;

import static imageSigner.MainApp.RESOURCE_PATH;

public class MWindowController {

    private ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "common", Locale.ENGLISH);

    //объявляем поля и элементы из FXML
    @FXML public BorderPane borderPaneMain;
    @FXML public ScrollPane scrollPanePreview;

    @FXML public Button buttonSelectFiles;
    @FXML public Button buttonPrevFile;
    @FXML public Button buttonNextFile;
    @FXML public Button buttonShowSignPanel;
    @FXML public Button buttonZoomUp;
    @FXML public Button buttonZoomDown;
    @FXML public Button buttonZoomFit;
    @FXML public Button buttonSave;
    @FXML public Button buttonSaveAs;
    @FXML public Button buttonResetChanges;

    @FXML public Label labelSelectedQtyValue;
    @FXML public Label labelFooterCounter;
    @FXML public Label labelProcessStatus;
    @FXML public Label labelFooterFileName;
    @FXML public Label labelFooterFileSize;
    @FXML public Label labelFooterFileResolution;
    @FXML public Label labelFooterFilePath;     //к этому полю привязана логика переключения next/prev и отслеживание изменения этого поля



    //даем контроллеру доступ к экземпляру mainApp
    private MainApp mainApp;

    //создаем лист для обработки файлов
    private ObservableList<FileItem> fileItemsList = FileItemsStorage.getInstance().getFileItemsList();

    //переменная для хранения индекса выбранного файла
    private int currentFileIndex;

    //контейнер для превью
    private ImageContainer ic;


    /** init */
    public void initialize() {
        //отслеживание изменения кол-ва выбранных файлов
        fileItemsList.addListener((ListChangeListener<FileItem>) c -> refreshCounter());

        //отслеживание предпросмотра текущего файла (изменение текстового поля с именем файла)
        labelFooterFilePath.textProperty().addListener(
                ((observable, oldValue, newValue) -> showPhotoPreview()));

        refreshCounter();
        paintEmptyBackground();
        setStatusReady();

        buttonShowSignPanel.setDisable(true);
    }


    //обновление счетчика
    private void refreshCounter() {
        FileItemsStorage.getInstance().refreshLabelCounter(labelSelectedQtyValue);
    }

    //выбор и открытие файла, выбранного в окне SelectFilesWindow (или первого в списке)
    public void selectFileFromSelectFilesWindow() {
        if (fileItemsList.size() != 0) {
            try {
                currentFileIndex = mainApp.getFwController().tableView.getSelectionModel().getSelectedIndex();
                showFooterPhotoData();
            }
            catch (ArrayIndexOutOfBoundsException e) {
                currentFileIndex = 0;
                showFooterPhotoData();
            }
        }
    }

    //заполнение данных о фото в футере окна
    private void showFooterPhotoData() {
        FileItem fi = fileItemsList.get(currentFileIndex);

        labelFooterCounter.setText((currentFileIndex + 1) + "/" + fileItemsList.size());
        labelFooterFileName.setText(fi.getFilename());
        double fileSize = new BigDecimal((double)fi.getCurrentFile().length() / 1024 / 1024).setScale(2, RoundingMode.UP).doubleValue();
        labelFooterFileSize.setText(fileSize + res.getString("mb"));
        labelFooterFileResolution.setText(ic.getImage().getWidth() + "x" + ic.getImage().getHeight());
        labelFooterFilePath.setText(fi.getFilePath());
    }

    //Показ превью фото
    private void showPhotoPreview(){
        if (fileItemsList.size() != 0) {
            ic = new ImageContainer(getCurrentImagePath());
            scrollPanePreview.setContent(ic);
            showFooterPhotoData();

            enableEditing();
        }
    }

    //разрешение редактирования
    private void enableEditing() {

        //активируем кнопку показа панели подписи
        buttonShowSignPanel.setDisable(false);

        //передаем в imageContainer ссылку на mainApp для удобста передачи в него параметров подписи
        ic.setMainApp(this.mainApp);
    }


    //кнопки
    public void handleButtonSelectFiles(){
        mainApp.showFilesWindow();
    }

    public void handleButtonPrevFile() {
        if (currentFileIndex > 0) {
            currentFileIndex --;
            labelFooterFilePath.setText(fileItemsList.get(currentFileIndex).getFilePath());
            setStatusReady();
        }
    }

    public void handleButtonNextFile() {
        if (currentFileIndex < fileItemsList.size() - 1) {
            currentFileIndex++;
            labelFooterFilePath.setText(fileItemsList.get(currentFileIndex).getFilePath());
            setStatusReady();
        }
    }

    public void handleButtonShowSignPanel() {
        mainApp.showSignPanel();
    }

    public void handleButtonZoomIn() {}

    public void handleButtonSave() {
        saveChangesInFile(fileItemsList.get(currentFileIndex).getFilePath());
    }

    public void handleButtonSaveAs() {
        File file = FileItemsStorage.getInstance().saveFileAs();
        if (file != null) {
            saveChangesInFile(file.getAbsolutePath());
        }
    }

    //сохранить изменения в файл
    private void saveChangesInFile(String filePath) {

            //получаем текущее изображение
            Image savingImage = mainApp.getMvController().getIc().getImageView().getImage();
            BufferedImage image = SwingFXUtils.fromFXImage(savingImage, null);

            //конвертация
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    newImage.setRGB(x, y, image.getRGB(x, y));
                }
            }

            //установка качества
            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(1f);

            //сохранение
            try {
                final ImageWriter iWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                iWriter.setOutput(new FileImageOutputStream(new File(filePath)));
                iWriter.write(null, new IIOImage(newImage, null, null), jpegParams);
                iWriter.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }

            labelProcessStatus.setText(res.getString("imgSave"));
    }

    //отрисовка пустого фона при старте программы
    private void paintEmptyBackground() {
        ic = new ImageContainer("/imageSigner/view/images/emptyBg.jpg");
        scrollPanePreview.setContent(ic);
    }

    //статус готов
    private void setStatusReady() {
        labelProcessStatus.setText(res.getString("ready"));
    }

    /** SETTERS AND GETTERS */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }


    private String getCurrentImagePath(){
        return new File(fileItemsList.get(currentFileIndex).getFilePath()).toURI().toString();
    }

    ImageContainer getIc() {
        return ic;
    }
}
