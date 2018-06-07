package pm.gui;

import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseMotionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import static javafx.scene.paint.Color.WHITE;
import static javafx.scene.paint.Color.YELLOW;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.imageio.ImageIO;
import pm.data.DataManager;
import saf.ui.AppGUI;
import saf.AppTemplate;
import saf.components.AppWorkspaceComponent;
import static saf.settings.AppStartupConstants.FILE_PROTOCOL;

/**
 * This class serves as the workspace component for this application, providing
 * the user interface controls for editing work.
 *
 * @author Richard McKenna
 * @author Daniel Delgado
 * @version 1.0
 */
public class Workspace extends AppWorkspaceComponent {

    static final String CLASS_PMA_PANE = "pma_pane";
    static final String CLASS_PMA_CONTROLS = "pma_controls";
    static final String SUBHEADING_LABEL = "subheading_label";
    static final String CLASS_EDIT_TOOLBAR = "edit_toolbar";
    static final String COLOR_CHOOSER = "color_chooser_pane";

    // HERE'S THE APP
    AppTemplate app;

    // IT KNOWS THE GUI IT IS PLACED INSIDE
    AppGUI gui;

    BorderPane borderPane;
    VBox controls;
    HBox shapeButtons;
    ArrayList<Button> shapes;
    HBox layerButtons;
    ArrayList<Button> layers;
    VBox backgroundColor;
    VBox fillColor;
    VBox outlineColor;
    VBox thickness;
    HBox snapshot;
    Pane pane;
    boolean selectShape;
    boolean addRectangle;
    boolean addEllipse;
    Cursor cursor;
    ColorPicker bColor;
    ColorPicker fColor;
    ColorPicker oColor;
    Slider tSlider;
    Rectangle r;
    Ellipse l;
    ArrayList<Shape> shapeArray;
    Shape selected;
    Rectangle selRect;
    Ellipse selEllipse;
    Color savedStroke;
    String background;
    double offsetX;
    double offsetY;
    Button select;
    Button rect;
    Button ellipse;
    Button remove;
    Button back;
    Button forward;
    boolean load;

    /**
     * Constructor for initializing the workspace, note that this constructor
     * will fully setup the workspace user interface for use.
     *
     * @param initApp The application this workspace is part of.
     *
     * @throws IOException Thrown should there be an error loading application
     * data for setting up the user interface.
     */
    public Workspace(AppTemplate initApp) throws IOException {
        // KEEP THIS FOR LATER
        app = initApp;

        // KEEP THE GUI FOR LATER
        gui = app.getGUI();

        //Data Manager
        DataManager dataManager = (DataManager) app.getDataComponent();
        dataManager.setBackground("-fx-background-color: #f2f2f2ff;");
        dataManager.setShapeArray(shapeArray);

        workspace = new Pane();
        borderPane = new BorderPane();
        borderPane.prefHeightProperty().bind(workspace.heightProperty());
        borderPane.prefWidthProperty().bind(workspace.widthProperty());
        controls = new VBox();
        controls.setAlignment(Pos.TOP_CENTER);
        selectShape = false;
        addRectangle = false;
        addEllipse = false;
        shapeArray = new ArrayList<Shape>();
        selRect = new Rectangle();
        selEllipse = new Ellipse();

        //BUTTONS
        select = new Button();
        rect = new Button();
        ellipse = new Button();
        remove = new Button();
        back = new Button();
        forward = new Button();

        //FIRST ROW OF BUTTONS
        shapeButtons = new HBox();
        shapeButtons.setPrefHeight(60);
        shapeButtons.setPrefWidth(controls.getWidth());
        shapeButtons.setAlignment(Pos.CENTER);
        shapeButtons.setSpacing(7);
        shapes = new ArrayList();
        //SELECTION TOOL BUTTON
        Image selectIMG = new Image(FILE_PROTOCOL + "images/SelectionTool.png");
        select.setGraphic(new ImageView(selectIMG));
        if (shapeArray.isEmpty()) {
            select.setDisable(true);
        } else {
            select.setDisable(false);
        }
        shapes.add(select);

        select.setOnAction(e -> {
            workspace.setCursor(Cursor.DEFAULT);
            select.setDisable(true);
            rect.setDisable(false);
            ellipse.setDisable(false);
            back.setDisable(true);
            forward.setDisable(true);
            remove.setDisable(true);
            selectShape = true;
            addRectangle = false;
            addEllipse = false;
        });

        //REMOVE BUTTON
        Image removeIMG = new Image(FILE_PROTOCOL + "images/Remove.png");
        remove.setGraphic(new ImageView(removeIMG));
        shapes.add(remove);
        remove.setDisable(true);

        remove.setOnAction(e -> {
            workspace.setCursor(Cursor.DEFAULT);
            if (selected != null) {
                remove.setDisable(true);
                pane.getChildren().remove(selected);
                shapeArray.remove(selected);
                for (int i = 0; i < shapeArray.size() - 1; i++) {
                    if (shapeArray.get(i) == null) {
                        shapeArray.set(i, shapeArray.get(i + 1));
                        shapeArray.set(i + 1, null);
                    }
                }
                selected = null;
                if (shapeArray.size() == 0) {
                    select.setDisable(true);
                }
                back.setDisable(true);
                forward.setDisable(true);
                dataManager.setShapeArray(shapeArray);
                app.getGUI().updateToolbarControls(false);
            }
        });

        //RECTANGLE BUTTON
        Image rectIMG = new Image(FILE_PROTOCOL + "images/Rect.png");
        rect.setGraphic(new ImageView(rectIMG));
        rect.setDisable(false);
        shapes.add(rect);

        rect.setOnAction(e -> {
            workspace.setCursor(Cursor.CROSSHAIR);
            if (shapeArray.size() > 0) {
                select.setDisable(false);
            }
            rect.setDisable(true);
            ellipse.setDisable(false);
            back.setDisable(true);
            forward.setDisable(true);
            remove.setDisable(true);
            if (selected != null) {
                selected.setStroke(savedStroke);
                selected = null;
            }
            addRectangle = true;
            selectShape = false;
            addEllipse = false;
        });

        //ELLIPSE BUTTON
        Image ellipseIMG = new Image(FILE_PROTOCOL + "images/Ellipse.png");
        ellipse.setGraphic(new ImageView(ellipseIMG));
        ellipse.setDisable(false);
        shapes.add(ellipse);

        ellipse.setOnAction(e -> {
            workspace.setCursor(Cursor.CROSSHAIR);
            if (shapeArray.size() > 0) {
                select.setDisable(false);
            }
            rect.setDisable(false);
            ellipse.setDisable(true);
            back.setDisable(true);
            forward.setDisable(true);
            remove.setDisable(true);
            if (selected != null) {
                selected.setStroke(savedStroke);
                selected = null;
            }
            selectShape = false;
            addRectangle = false;
            addEllipse = true;
        });

        //ADD BUTTONS TO HBox
        for (Button b : shapes) {
            shapeButtons.getChildren().add(b);
        }

        //SECOND ROW OF BUTTONS
        layerButtons = new HBox();
        layerButtons.setPrefHeight(60);
        layerButtons.setPrefWidth(controls.getWidth());
        layerButtons.setAlignment(Pos.CENTER);
        layerButtons.setSpacing(7);
        //BACK BUTTON
        Image backIMG = new Image(FILE_PROTOCOL + "images/MoveToBack.png");
        back.setGraphic(new ImageView(backIMG));
        back.setPrefSize(100, 39);
        back.setDisable(false);
        layerButtons.getChildren().add(back);

        //FIX THIS!!! SELECTED SHAPE SHOULD BE MOVED TO THE BACK.
        back.setOnAction(e -> {
            selected.toBack();
            shapeArray.set(shapeArray.indexOf(selected), null);
            for (int i = shapeArray.size() - 1; i > 0; i--) {
                if (shapeArray.get(i) == null) {
                    shapeArray.set(i, shapeArray.get(i - 1));
                    shapeArray.set(i - 1, null);
                }
            }
            shapeArray.set(0, selected);
            dataManager.setShapeArray(shapeArray);
            forward.setDisable(false);
            back.setDisable(true);
            app.getGUI().updateToolbarControls(false);
        });

        //FORWARD BUTTON
        Image forwardIMG = new Image(FILE_PROTOCOL + "images/MoveToFront.png");
        forward.setGraphic(new ImageView(forwardIMG));
        forward.setPrefSize(100, 39);
        forward.setDisable(false);
        layerButtons.getChildren().add(forward);

        forward.setOnAction(e -> {
            pane.getChildren().remove(selected);
            pane.getChildren().add(selected);
            shapeArray.remove(selected);
            for (int i = 0; i < shapeArray.size() - 1; i++) {
                if (shapeArray.get(i) == null) {
                    shapeArray.set(i, shapeArray.get(i + 1));
                    shapeArray.set(i + 1, null);
                }
            }
            shapeArray.add(selected);
            dataManager.setShapeArray(shapeArray);
            forward.setDisable(true);
            back.setDisable(false);
            app.getGUI().updateToolbarControls(false);
        });

        //BACKGROUND COLOR PICKER
        backgroundColor = new VBox();
        backgroundColor.setPrefHeight(60);
        Label bLabel = new Label();
        bLabel.setText("Background Color");
        bLabel.setTextAlignment(TextAlignment.LEFT);
        backgroundColor.getChildren().add(bLabel);
        bColor = new ColorPicker();
        backgroundColor.getChildren().add(bColor);
        bColor.setOnAction(e -> {
            pane.setStyle("-fx-background-color: #" + bColor.getValue().toString().substring(2) + ";");
            background = "-fx-background-color: #" + bColor.getValue().toString().substring(2) + ";";
            dataManager.setBackground(background);
            app.getGUI().updateToolbarControls(false);
        });

        //FILL COLOR PICKER
        fillColor = new VBox();
        fillColor.setPrefHeight(60);
        Label fLabel = new Label();
        fLabel.setText("Fill Color");
        fLabel.setTextAlignment(TextAlignment.LEFT);
        fillColor.getChildren().add(fLabel);
        fColor = new ColorPicker();
        fillColor.getChildren().add(fColor);
        fColor.setOnAction(e -> {
            if (selected != null) {
                selected.setFill(fColor.getValue());
                app.getGUI().updateToolbarControls(false);
            }
        });

        //OUTLINE COLOR PICKER
        outlineColor = new VBox();
        outlineColor.setPrefHeight(60);
        Label oLabel = new Label();
        oLabel.setText("Outline Color");
        oLabel.setTextAlignment(TextAlignment.LEFT);
        outlineColor.getChildren().add(oLabel);
        oColor = new ColorPicker();
        outlineColor.getChildren().add(oColor);
        oColor.setOnAction(e -> {
            if (selected != null) {
                savedStroke = oColor.getValue();
                app.getGUI().updateToolbarControls(false);
            }
        });

        //OUTLINE THICKNESS SLIDER
        thickness = new VBox();
        thickness.setPrefHeight(60);
        Label tLabel = new Label();
        tLabel.setText("Outline Thickness");
        tLabel.setTextAlignment(TextAlignment.LEFT);
        thickness.getChildren().add(tLabel);
        tSlider = new Slider();
        tSlider.setMin(1);
        tSlider.setMax(50);
        thickness.getChildren().add(tSlider);
        tSlider.setOnMouseDragged(e -> {
            if (selected != null) {
                selected.setStrokeWidth(tSlider.getValue());
                app.getGUI().updateToolbarControls(false);
            }
        });

        //SNAPSHOT BUTTON
        snapshot = new HBox();
        snapshot.setPrefHeight(60);
        snapshot.setAlignment(Pos.CENTER);
        Button ss = new Button();
        Image ssIMG = new Image(FILE_PROTOCOL + "images/Snapshot.png");
        ss.setGraphic(new ImageView(ssIMG));
        snapshot.getChildren().add(ss);

        ss.setOnAction(e -> {
            if (selected != null) {
                selected.setStroke(savedStroke);
            }
            WritableImage shot = new WritableImage((int) pane.getWidth(), (int) pane.getHeight());
            pane.snapshot(null, shot);
            FileChooser ssChooser = new FileChooser();
            ssChooser.setTitle("Save Snapshot");
            FileChooser.ExtensionFilter pngExt = new ExtensionFilter("PNG Files (*.png)", "*.png");
            ssChooser.getExtensionFilters().add(pngExt);
            File file = ssChooser.showSaveDialog(app.getGUI().getWindow());
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(shot, null), "png", file);
            } catch (IOException ex) {
                Logger.getLogger(Workspace.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (selected != null) {
                selected.setStroke(YELLOW);
            }
        });

        controls.getChildren().add(shapeButtons);
        controls.getChildren().add(layerButtons);
        controls.getChildren().add(backgroundColor);
        controls.getChildren().add(fillColor);
        controls.getChildren().add(outlineColor);
        controls.getChildren().add(thickness);
        controls.getChildren().add(snapshot);
        controls.prefHeightProperty().bind(borderPane.heightProperty());
        pane = new Pane();
        pane.prefHeightProperty().bind(borderPane.heightProperty());
        borderPane.setLeft(controls);
        borderPane.setCenter(pane);
        workspace.getChildren().add(borderPane);

        //MOUSE EVENTS
        addMouseListener(new MouseAdapter() {
        });
        pane.setOnMouseClicked(e -> {
            if (selectShape) {
                boolean shapeSelected = false;
                for (Shape s : shapeArray) {
                    if (e.getTarget() == s) {
                        if (selected != null && savedStroke != null) {
                            selected.setStroke(savedStroke);
                        }
                        selected = s;
                        fColor.setValue((Color) s.getFill());
                        oColor.setValue((Color) s.getStroke());
                        tSlider.setValue(s.getStrokeWidth());
                        savedStroke = (Color) s.getStroke();
                        selected.setStroke(YELLOW);
                        back.setDisable(false);
                        forward.setDisable(false);
                        for (int i = 0; i < shapeArray.size(); i++) {
                            if (shapeArray.get(i) == selected && i == 0) {
                                back.setDisable(true);
                            }
                            if (shapeArray.get(i) == selected && i == (shapeArray.size() - 1)) {
                                forward.setDisable(true);
                            }
                        }
                        remove.setDisable(false);
                        shapeSelected = true;
                    }
                }
                if (!shapeSelected) {
                    if (selected != null) {
                        selected.setStroke(savedStroke);
                        selected = null;
                    }
                }
            }
        });
        pane.setOnMousePressed(e -> {
            if (selectShape) {
                boolean shapeSelected = false;
                for (Shape s : shapeArray) {
                    if (e.getTarget() == s) {
                        if (selected != null && savedStroke != null) {
                            selected.setStroke(savedStroke);
                        }
                        selected = s;
                        fColor.setValue((Color) s.getFill());
                        oColor.setValue((Color) s.getStroke());
                        tSlider.setValue(s.getStrokeWidth());
                        savedStroke = (Color) s.getStroke();
                        selected.setStroke(YELLOW);
                        back.setDisable(false);
                        forward.setDisable(false);
                        for (int i = 0; i < shapeArray.size(); i++) {
                            if (shapeArray.get(i) == selected && i == 0) {
                                back.setDisable(true);
                            }
                            if (shapeArray.get(i) == selected && i == (shapeArray.size() - 1)) {
                                forward.setDisable(true);
                            }
                        }
                        remove.setDisable(false);
                        shapeSelected = true;
                        if (e.getTarget() instanceof Rectangle) {
                            Rectangle rec = (Rectangle) selected;
                            offsetX = e.getX() - rec.getX();
                            offsetY = e.getY() - rec.getY();
                        } 
                        else if (e.getTarget() instanceof Ellipse) {
                            Ellipse ell = (Ellipse) selected;
                            offsetX = e.getX() - ell.getCenterX();
                            offsetY = e.getY() - ell.getCenterY();
                        }
                    }
                }
                if (!shapeSelected) {
                    if (selected != null) {
                        selected.setStroke(savedStroke);
                        selected = null;
                    }
                }
            } 
            else if (addRectangle) {
                r = new Rectangle();
                r.setX(e.getX());
                r.setY(e.getY());
                r.setFill(fColor.getValue());
                r.setStroke(oColor.getValue());
                r.setStrokeWidth(tSlider.getValue());
                pane.getChildren().add(r);
            } 
            else if (addEllipse) {
                l = new Ellipse();
                l.setCenterX(e.getX());
                l.setCenterY(e.getY());
                l.setFill(fColor.getValue());
                l.setStroke(oColor.getValue());
                l.setStrokeWidth(tSlider.getValue());
                pane.getChildren().add(l);
            }
        });
        pane.setOnMouseDragged(e -> {
            if (selectShape) {
                if (e.getTarget() == selected) {
                    workspace.setCursor(Cursor.MOVE);
                    if (selected instanceof Rectangle) {
                        selRect = (Rectangle) selected;
                        selEllipse = null;
                        selRect.setX(e.getX() - offsetX);
                        selRect.setY(e.getY() - offsetY);
                    } 
                    else if (selected instanceof Ellipse) {
                        selEllipse = (Ellipse) selected;
                        selRect = null;
                        selEllipse.setCenterX(e.getX() - offsetX);
                        selEllipse.setCenterY(e.getY() - offsetY);
                    }
                }
            } 
            else if (addRectangle) {
                r.setWidth(e.getX() - r.getX());
                r.setHeight(e.getY() - r.getY());
            } 
            else if (addEllipse) {
                l.setRadiusX(e.getX() - l.getCenterX());
                l.setRadiusY(e.getY() - l.getCenterY());
            }
        });
        pane.setOnMouseReleased(e -> {
            if (selectShape) {
                workspace.setCursor(Cursor.DEFAULT);
                app.getGUI().updateToolbarControls(false);
            }
            if (addRectangle) {
                if (r.getWidth() > 0 && r.getHeight() > 0) {
                    shapeArray.add(r);
                    dataManager.setShapeArray(shapeArray);
                    select.setDisable(false);
                    app.getGUI().updateToolbarControls(false);
                } 
                else {
                    pane.getChildren().remove(r);
                }
            } 
            else if (addEllipse) {
                if (l.getRadiusX() > 0 && l.getRadiusY() > 0) {
                    shapeArray.add(l);
                    dataManager.setShapeArray(shapeArray);
                    select.setDisable(false);
                    app.getGUI().updateToolbarControls(false);
                } 
                else {
                    pane.getChildren().remove(l);
                }
            }
        });
    }

    public ArrayList<Shape> getShapeArray() {
        return shapeArray;
    }

    /**
     * This function specifies the CSS style classes for all the UI components
     * known at the time the workspace is initially constructed. Note that the
     * tag editor controls are added and removed dynamicaly as the application
     * runs so they will have their style setup separately.
     */
    @Override
    public void initStyle() {
        // NOTE THAT EACH CLASS SHOULD CORRESPOND TO
        // A STYLE CLASS SPECIFIED IN THIS APPLICATION'S
        // CSS FILE
        //borderPane.getStyleClass().add(CLASS_MAX_PANE);
        controls.getStyleClass().add(CLASS_PMA_CONTROLS);
        shapeButtons.getStyleClass().add(CLASS_PMA_PANE);
        layerButtons.getStyleClass().add(CLASS_PMA_PANE);
        backgroundColor.getChildren().get(0).getStyleClass().add(COLOR_CHOOSER);
        backgroundColor.getStyleClass().add(CLASS_PMA_PANE);
        fillColor.getChildren().get(0).getStyleClass().add(COLOR_CHOOSER);
        fillColor.getStyleClass().add(CLASS_PMA_PANE);
        outlineColor.getChildren().get(0).getStyleClass().add(COLOR_CHOOSER);
        outlineColor.getStyleClass().add(CLASS_PMA_PANE);
        thickness.getChildren().get(0).getStyleClass().add(COLOR_CHOOSER);
        thickness.getStyleClass().add(CLASS_PMA_PANE);
        snapshot.getStyleClass().add(CLASS_PMA_PANE);
    }

    /**
     * This function reloads all the controls for editing tag attributes into
     * the workspace.
     */
    @Override
    public void reloadWorkspace() {
        if (!load) {
            bColor.setValue(WHITE);
            fColor.setValue(WHITE);
            oColor.setValue(WHITE);
            tSlider.setValue(tSlider.getMin());
            pane.getChildren().clear();
            pane.setStyle("-fx-background-color: #f2f2f2;");
            select.setDisable(true);
            remove.setDisable(true);
            rect.setDisable(false);
            ellipse.setDisable(false);
            back.setDisable(true);
            forward.setDisable(true);
        }
        load = false;
    }

    public void resetSelected() {
        if (selected != null)
            selected.setStroke(savedStroke);
    }
    
    public void reselect() {
        if (selected != null)
            selected.setStroke(YELLOW);
    }

    public void loadWork(ArrayList<Shape> shapes, String bg) {
        reloadWorkspace();
        pane.setStyle(bg);
        bColor.setValue(Color.valueOf(bg.substring(22, 29)));
        shapeArray = shapes;
        for (Shape s : shapeArray) {
            pane.getChildren().add(s);
        }
        if (shapeArray.size() > 0) {
            select.setDisable(false);
        }
        load = true;
    }
}
