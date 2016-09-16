package imageSigner.containers;

import imageSigner.MainApp;
import imageSigner.enums.textAlignSelector;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import org.marvinproject.image.blur.gaussianBlur.GaussianBlur;

import java.awt.image.BufferedImage;


public class ImageContainer extends StackPane {

    private MainApp mainApp;

    private Image image;
    private ImageView imageView;

    private Canvas canvas = new Canvas(0, 0);
    private GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

    private Text signText;


    //constructor
    public ImageContainer (String pathUrl) {

        image = new Image(pathUrl);
        imageView = new ImageView();

        //листенер для определения scrollPane без передачи на наго ссылки
        parentProperty().addListener(e -> {
            //init
            initCanvas();

            //получение ссылки на ScrollPane и масштабирование изображения в окне
            if (getParent() != null) {
                ScrollPane sp = (ScrollPane) this.getParent().getParent().getParent();
                if (sp != null) {
                    imageView.fitWidthProperty().bind(sp.widthProperty());
                    imageView.fitHeightProperty().bind(sp.heightProperty());
                }
                getChildren().add(imageView);
            }
        });

        //сохранение пропорций изображения
        imageView.setPreserveRatio(true);
        //применение стиля к контейнеру
        this.getStylesheets().add("/imageSigner/view/css/MWindow.css");
        this.getStyleClass().add("imageContainer");
    }


    //init
    private void initCanvas() {
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());
        graphicsContext.drawImage(image, 0, 0);
        paint();
    }
    // отрисовка изображения
    private void paint() {
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(new SnapshotParameters(), writableImage);
        imageView.setImage(writableImage);
    }


    // наложение подписи на однотонном фоне
    public void setSolidLineSignature() {

        double signLineSize = mainApp.getSpController().spinnerSignLineHeight.getValue();               //получаем значение высоты линии подписи

        initCanvas();
        prepareSignLine(signLineSize);
        processingSignData(signLineSize);

    }

    // наложение подписи на замутненном фоне
    public void setBlurLineSignature() {

        mainApp.getMvController().progressBar.setProgress(-1);


        double signLineSize = mainApp.getSpController().spinnerSignLineHeight.getValue();               //получаем значение высоты линии подписи

        initCanvas();

        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight() + signLineSize);

        WritableImage writableImage = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());

        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        //считывание всего фото и запись в WritableImage
        for (int readH = 0; readH < image.getHeight(); readH++) {
            for (int readW = 0; readW < image.getWidth(); readW++) {
                Color color = pixelReader.getColor(readW, readH);
                pixelWriter.setColor(readW, readH, color);
            }
        }

        //позиция записи пикселей (для заполнения снизу вверх)
        int writeVPos = (int) writableImage.getHeight();
        //скан только нижней части фото, равной высоте линии подписи, но до нее и запись содержимого на линию подписи
        for (int readH = (int)(image.getHeight() - signLineSize + 1); readH < image.getHeight(); readH++) {
            for (int readW = 0; readW < image.getWidth(); readW++) {
                Color color = pixelReader.getColor(readW, readH);       //считываем цвет пикселя с исходного изображения (сверху вниз)
                pixelWriter.setColor(readW, writeVPos - 1, color);      //записываем цвет пикселя в writableImage (снизу вверх)
            }
            writeVPos--;
        }

        //конвертация в buffImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

        //загружаем изображение в марвин
        MarvinImage mImage = new MarvinImage(bufferedImage);

        //создаем маску для участка блюра
        MarvinImageMask mask = new MarvinImageMask(
                mImage.getWidth(), mImage.getHeight(), 0, mImage.getHeight() - (int)signLineSize, mImage.getWidth(), (int)signLineSize);

        //применяем блюр
        GaussianBlur gaussianBlur = new GaussianBlur();
        gaussianBlur.load();
        gaussianBlur.setAttributes("radius", 15);
        gaussianBlur.process(mImage.clone(), mImage, mask);
        mImage.update();

        //конвертация в ImageFX
        Image resImage = SwingFXUtils.toFXImage(mImage.getBufferedImage(), writableImage);
        imageView.setImage(resImage);

        mainApp.getMvController().progressBar.setProgress(100);

    }


    // наложение подписи напрямую на фото (без подложки для подписи)
    public void setOnPhotoDirectlySignature() {

        double interval = mainApp.getSpController().spinnerFontSize.getValue() * 1.17;                  //вместо отсутствующего signLineSize (для расчета подчеркивания и расположения текста)

        initCanvas();
        processingSignData(interval);

    }


    private void processingSignData(double signSubstrate) {
        signText = new Text(mainApp.getSpController().textAreaSignature.getText());                    //получение текста

        prepareFontParams();

        double startHPos = applyTextAlignSettings();
        if (mainApp.getSpController().isBtnTextUnderlinePressed()) {applyUnderline(startHPos, signSubstrate);}

        graphicsContext.fillText(signText.getText(), startHPos, canvas.getHeight() - (signSubstrate /5));//располагаем текст
        paint();                                                                                        //отрисовка


    }
    // подготовка строки подписи
    private void prepareSignLine(double signLineSize) {

        //подготовка пространства для подписи
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight() + signLineSize);

        //Настройка цвета фона для подписи
        Color bgColor = mainApp.getSpController().colorPickerSignBgColor.getValue();
        graphicsContext.setFill(bgColor);
        graphicsContext.fillRect(0, image.getHeight(), canvas.getWidth(), signLineSize);
    }
    // подготовка шрифта и его параметров
    private void prepareFontParams() {

        //Настройка шрифта и цвета подписи
        String fontName = mainApp.getSpController().comboBoxFontName.getValue();
        int fontSize = mainApp.getSpController().spinnerFontSize.getValue();
        Color textColor = mainApp.getSpController().colorPickerTextColor.getValue();

        //Проверка на жирность шрифта)
        FontWeight fontWeight = FontWeight.NORMAL;
        if (mainApp.getSpController().isBtnTextBoldPressed()) {
            fontWeight = FontWeight.BOLD;
        }
        //проверка на наклон шрифта
        FontPosture fontPosture = FontPosture.REGULAR;
        if (mainApp.getSpController().isBtnTextItalicPressed()) {
            fontPosture = FontPosture.ITALIC;
        }

        //применение параметров
        Font font = Font.font(fontName, fontWeight, fontPosture, fontSize);

        graphicsContext.setFill(textColor);
        graphicsContext.setFont(font);
        //применение параметров шрифта к тексту
        signText.setFont(font);
    }
    // получение координаты начала текста в зависимости от режима выравнивания
    private double applyTextAlignSettings() {

        double startHPos = 0;                                                                           //координата начала текста
        double textWidth = getTextWidthPixels();                                                        //определяем ширину текста в пикселях

        if (mainApp.getSpController().getTAS() == textAlignSelector.LEFT) { startHPos = 0;}             //Если выравнивание текста слева

        else if (mainApp.getSpController().getTAS() == textAlignSelector.CENTER) {                      //Если выравнивание текста по центру
            startHPos = (canvas.getWidth() / 2) - (textWidth / 2);                                      //вычисляем новую стартовую точку текста
        }
        else if (mainApp.getSpController().getTAS() == textAlignSelector.RIGHT) {                       //Если выравнивание текста справа
            startHPos = canvas.getWidth() - textWidth;
        }

        return startHPos;
    }
    // если нужно текст подчеркнуть
    private void applyUnderline(double startHPos, double signLineSize) {

        double lineVPos = canvas.getHeight() - (signLineSize / 18);                                 //позиция линии подчеркивания
        graphicsContext.setStroke(mainApp.getSpController().colorPickerTextColor.getValue());       //цвет линии
        graphicsContext.setLineWidth(mainApp.getSpController().spinnerFontSize.getValue() / 12.5);  //толщина линии
        graphicsContext.strokeLine(startHPos, lineVPos, startHPos + getTextWidthPixels(), lineVPos);//координаты линии
    }

    //получение ширины текста в пикселях
    private double getTextWidthPixels() {
        return signText.getLayoutBounds().getWidth();
    }

    //SETTERS AND GETTERS
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public Image getImage() {return image;}
    public ImageView getImageView() {
        return imageView;
    }


}
