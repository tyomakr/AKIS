package imageSigner.tools;

import imageSigner.containers.ImageContainer;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Листенеры для работы с масштабированием и смещением ImageContainer
 */
public class SceneGestures {
    private static final double MAX_SCALE = 10.0d;
    private static final double MIN_SCALE = 1d;

    private DragContext sceneDragContext = new DragContext();
    private ImageContainer ic;

    public SceneGestures(ImageContainer ic) {
        this.ic = ic;
    }


    private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            // правая кл.мыши - сдвиг IC
            if( !event.isSecondaryButtonDown())
                return;

            sceneDragContext.mouseAnchorX = event.getSceneX();
            sceneDragContext.mouseAnchorY = event.getSceneY();

            sceneDragContext.translateAnchorX = ic.getTranslateX();
            sceneDragContext.translateAnchorY = ic.getTranslateY();
        }

    };
    private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            // правая кл.мыши - сдвиг IC
            if( !event.isSecondaryButtonDown())
                return;

            ic.setTranslateX(sceneDragContext.translateAnchorX + event.getSceneX() - sceneDragContext.mouseAnchorX);
            ic.setTranslateY(sceneDragContext.translateAnchorY + event.getSceneY() - sceneDragContext.mouseAnchorY);

            event.consume();
        }
    };

    /** зум скроллом той точки, над которой находится курсор мыши */
    private EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {
        @Override
        public void handle(ScrollEvent event) {
            double delta = 1.2;
            double scale = ic.getScale();
            double oldScale = scale;

            if (event.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }

            scale = clamp( scale, MIN_SCALE, MAX_SCALE);

            double f = (scale / oldScale)-1;

            double dx = (event.getSceneX() - (ic.getBoundsInParent().getWidth()/2 + ic.getBoundsInParent().getMinX()));
            double dy = (event.getSceneY() - (ic.getBoundsInParent().getHeight()/2 + ic.getBoundsInParent().getMinY()));

            ic.setScale( scale);
            ic.setPivot(f*dx, f*dy);

            event.consume();

        }

    };

    private static double clamp(double value, double min, double max) {
        if( Double.compare(value, min) < 0)
            return min;
        if( Double.compare(value, max) > 0)
            return max;

        return value;
    }

    //GETTERS
    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {return onMousePressedEventHandler;}
    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
        return onMouseDraggedEventHandler;
    }
    public EventHandler<ScrollEvent> getOnScrollEventHandler() {
        return onScrollEventHandler;
    }

}


/**
 * Mouse drag context used for scene and nodes.
 */
class DragContext {

    double mouseAnchorX;
    double mouseAnchorY;

    double translateAnchorX;
    double translateAnchorY;

}