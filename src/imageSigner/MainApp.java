package imageSigner;

import imageSigner.controller.SelectFilesWindowController;
import imageSigner.controller.MWindowController;
import imageSigner.controller.SignPanelController;
import imageSigner.storage.FileItemsStorage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainApp extends Application {

    private Stage primaryStage;

    private MWindowController mvController;
    private SelectFilesWindowController fwController;
    private SignPanelController spController;

    private Stage sfStage = new Stage(StageStyle.TRANSPARENT);
    private Stage spStage = new Stage(StageStyle.TRANSPARENT);

    public static final String RESOURCE_PATH = "imageSigner.resources.";
    private ResourceBundle rbMain = ResourceBundle.getBundle(RESOURCE_PATH + "mainWindow", Locale.ENGLISH);
    private ResourceBundle rbSFW = ResourceBundle.getBundle(RESOURCE_PATH + "selectFilesWindow", Locale.ENGLISH);
    private ResourceBundle rbSPW = ResourceBundle.getBundle(RESOURCE_PATH + "signPanel", Locale.ENGLISH);
    @Override
    public void start(Stage primaryStage) {

        Locale.setDefault(Locale.ENGLISH);

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(rbMain.getString("window.title"));

        //Передача экземпляра mainApp для диалогов выбора файла
        FileItemsStorage.getInstance().setMainApp(this);

        initMainWindow();

        //для корректного показа\скрытия доп.окон
        sfStage.initOwner(primaryStage);
        spStage.initOwner(primaryStage);

    }

    //инициализация главного окна
    private void initMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(rbMain);
            loader.setLocation(MainApp.class.getResource("view/MWindowView.fxml"));
            BorderPane mWindow = loader.load();

            Scene scene = new Scene(mWindow);
            primaryStage.setScene(scene);
            primaryStage.show();

            mvController = loader.getController();
            mvController.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //инициализация окна выбора/выбранных файлов
    public void showFilesWindow() {

        if (sfStage.isShowing()) {sfStage.hide();}

        else if(!sfStage.isShowing()) {

            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setResources(rbSFW);
                loader.setLocation(MainApp.class.getResource("view/SelectFilesWindowView.fxml"));
                BorderPane filesWindow = loader.load();

                sfStage.setResizable(false);

                Scene scene = new Scene(filesWindow);
                sfStage.setScene(scene);

                fwController = loader.getController();

                //скрытие по щелчку, вне области этого окна
                primaryStage.getScene().setOnMouseClicked((MouseEvent me) -> sfStage.hide());

                sfStage.showAndWait();

                mvController.selectFileFromSelectFilesWindow();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //инициализация панели инструментов
    public void showSignPanel() {


        if (spStage.isShowing()) {spStage.hide();}

        else if(!spStage.isShowing()) {

            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setResources(rbSPW);
                loader.setLocation(MainApp.class.getResource("view/SignPanelView.fxml"));
                AnchorPane signPanel = loader.load();

                spStage.setOpacity(0.95);

                Scene spScene = new Scene(signPanel);
                spStage.setScene(spScene);

                spController = loader.getController();
                spController.setMainApp(this);
                spStage.setResizable(false);
                spStage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {
        launch(args);
    }


    /** setters and getters */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Stage getSpStage() {return spStage;}

    public MWindowController getMvController() {
        return mvController;
    }

    public SelectFilesWindowController getFwController() {
        return fwController;
    }

    public SignPanelController getSpController() { return spController; }
}
