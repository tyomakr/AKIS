package imageSigner.controller;

import imageSigner.MainApp;
import imageSigner.enums.textAlignSelector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.awt.*;

public class SignPanelController {

    //получение списка шрифтов
    private ObservableList<String> fontNames = FXCollections.observableArrayList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

    @FXML public AnchorPane signPanel;

    @FXML public TextArea textAreaSignature;

    @FXML public ComboBox<String> comboBoxFontName;
    @FXML public Spinner<Integer> spinnerFontSize;
    @FXML public Spinner<Integer> spinnerSignLineHeight;
    @FXML public ColorPicker colorPickerTextColor;
    @FXML public ColorPicker colorPickerSignBgColor;

    @FXML public ToggleGroup tgTypeSign;
    @FXML public ToggleGroup tgTextAlignment;

    @FXML public RadioButton radioButtonSSL;
    @FXML public RadioButton radioButtonBSL;
    @FXML public RadioButton radioButtonDOP;

    @FXML public Button buttonTextBold;
    @FXML public Button buttonTextItalic;
    @FXML public Button buttonTextUnderline;
    @FXML public ToggleButton toggleButtonTextAlignLeft;
    @FXML public ToggleButton toggleButtonTextAlignCenter;
    @FXML public ToggleButton toggleButtonTextAlignRight;
    @FXML public Button buttonPreview;


    //переменные для хранения начальной позиции окна для перетаскивания
    private double initX;
    private double initY;

    //даем контроллеру доступ к экземпляру mainApp
    private MainApp mainApp;

    //состояние кнопок
    private boolean isBtnTextBoldPressed = false;
    private boolean isBtnTextItalicPressed = false;
    private boolean isBtnTextUnderlinePressed = false;

    //переключатель выравнивания текста
    private textAlignSelector TAS = textAlignSelector.LEFT;


    public void initialize() {
        //listeners

        //когда кн.мыши нажата, сохраняем начальную позицию панели на экране
        signPanel.setOnMousePressed((MouseEvent me) -> {
            initX = me.getScreenX() - mainApp.getSpStage().getX();
            initY = me.getScreenY() - mainApp.getSpStage().getY();
        });
        //собственно само перетаскивание панели
        signPanel.setOnMouseDragged((MouseEvent me) -> {
            mainApp.getSpStage().setX(me.getScreenX() - initX);
            mainApp.getSpStage().setY(me.getScreenY() - initY);
        });


        //заполнение комбобокса со именами шрифтов
        comboBoxFontName.getItems().addAll(fontNames);
        comboBoxFontName.getSelectionModel().select("Verdana");


        //инициализация спиннера размер шрифта
        SpinnerValueFactory<Integer> svfFontSize = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 555, 60);
        spinnerFontSize.setValueFactory(svfFontSize);

        //инициализация спиннера высота линии подписи
        SpinnerValueFactory<Integer> svfSignLineHeight = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 555, 70);
        spinnerSignLineHeight.setValueFactory(svfSignLineHeight);

        //листенеры спиннеров для корректного рукописного ввода
        spinnerFontSize.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {return;}
            commitEditorText(spinnerFontSize);
        }));
        spinnerSignLineHeight.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {return;}
            commitEditorText(spinnerSignLineHeight);
        }));


        //инициализация colorPickers
        colorPickerTextColor.setValue(Color.WHITE);
        colorPickerSignBgColor.setValue(Color.valueOf("GRAY"));

    }



    /** buttons **/
    public void handleButtonPreview() {
        mainApp.getMvController().labelProcessStatus.setText("WORKING");
        //если подпись на однотонном фоне
        if (radioButtonSSL.isSelected()) {
            mainApp.getMvController().getIc().setSolidLineSignature();
        }
        //если подпись на замутненном фоне
        else if (radioButtonBSL.isSelected()) {
            mainApp.getMvController().getIc().setBlurLineSignature();
        }
        //если подпись без фона
        else if (radioButtonDOP.isSelected()) {
            mainApp.getMvController().getIc().setOnPhotoDirectlySignature();
        }
    }


    //text bold
    public void handleButtonTextBold() {
        isBtnTextBoldPressed = buttonsStyleSwitchPress(isBtnTextBoldPressed, buttonTextBold);
    }
    //text italic
    public void handleButtonTextItalic() {
        isBtnTextItalicPressed = buttonsStyleSwitchPress(isBtnTextItalicPressed, buttonTextItalic);
    }
    //text underline
    public void handleButtonTextUnderline() {
        isBtnTextUnderlinePressed = buttonsStyleSwitchPress(isBtnTextUnderlinePressed, buttonTextUnderline);
    }
    //text align left
    public void handleButtonTextAlignLeft() {
        TAS = textAlignSelector.LEFT;
    }
    //text align center
    public void handleButtonTextAlignCenter(){
        TAS = textAlignSelector.CENTER;
    }
    //text align right
    public void handleButtonTextAlignRight() {
        TAS = textAlignSelector.RIGHT;
    }





    //переключение состояния кнопок
    private boolean buttonsStyleSwitchPress(boolean isPressed, Button button) {
        isPressed = !isPressed;
        if (isPressed) {
            button.blendModeProperty().setValue(BlendMode.DIFFERENCE);
        } else {
            button.blendModeProperty().setValue(BlendMode.SRC_OVER);
        }
        return isPressed;
     }

    //применить рукописный ввод в спиннер (для корректной обработки)
    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }

    /** SETTERS AND GETTERS */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public boolean isBtnTextBoldPressed() {
        return isBtnTextBoldPressed;
    }

    public boolean isBtnTextItalicPressed() {
        return isBtnTextItalicPressed;
    }

    public boolean isBtnTextUnderlinePressed() {
        return isBtnTextUnderlinePressed;
    }

    public textAlignSelector getTAS() {
        return TAS;
    }
}



