package pm.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import pm.data.DataManager;
import saf.components.AppDataComponent;
import saf.components.AppFileComponent;
import pm.gui.Workspace;
import properties_manager.PropertiesManager;
import saf.ui.AppMessageDialogSingleton;

/**
 * This class serves as the file management component for this application,
 * providing all I/O services.
 *
 * @author Richard McKenna
 * @author Daniel Delgado
 * @version 1.0
 */
public class FileManager implements AppFileComponent {

    /**
     * This method is for saving user work, which in the case of this
     * application means the data that constitutes the page DOM.
     *
     * @param data The data management component for this application.
     *
     * @param filePath Path (including file name/extension) to where to save the
     * data to.
     *
     * @throws IOException Thrown should there be an error writing out data to
     * the file.
     */
    @Override
    public void saveData(AppDataComponent data, String filePath) throws IOException {
        DataManager dataManager = (DataManager) data;
        dataManager.resetSelected();
        ArrayList<Shape> shapes = dataManager.getShapeArray();
        String bg = dataManager.getBackground().substring(22, 30);
        JsonArrayBuilder jsonArrayShapes = Json.createArrayBuilder();
        for (Shape s : shapes) {
            jsonArrayShapes.add(saveShape(s));
        }
        JsonArray jsonShapes = jsonArrayShapes.build();
        JsonObject jsonMars = Json.createObjectBuilder().add("Background Color", bg).add("Shapes", jsonShapes).build();
        // AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        StringWriter sw = new StringWriter();
        JsonWriter jsonWriter = writerFactory.createWriter(sw);
        jsonWriter.writeObject(jsonMars);
        jsonWriter.close();

        // INIT THE WRITER
        OutputStream os = new FileOutputStream(filePath);
        JsonWriter jsonFileWriter = Json.createWriter(os);
        jsonFileWriter.writeObject(jsonMars);
        String prettyPrinted = sw.toString();
        PrintWriter pw = new PrintWriter(filePath);
        pw.write(prettyPrinted);
        pw.close();
        
        dataManager.reselect();
    }

    public JsonObject saveShape(Shape s) {
        JsonObject shape = null;
        if (s instanceof Rectangle) {
            Rectangle rect = (Rectangle) s;
            String fill = "#" + rect.getFill().toString().substring(2);
            String outline = "#" + rect.getStroke().toString().substring(2);
            JsonObject jsonRect = Json.createObjectBuilder()
                    .add("Shape Type", "Rectangle")
                    .add("X Location", "" + rect.getX() + "")
                    .add("Y Location", "" + rect.getY() + "")
                    .add("Width", "" + rect.getWidth() + "")
                    .add("Height", "" + rect.getHeight() + "")
                    .add("Fill Color", fill)
                    .add("Outline Color", outline)
                    .add("Outline Thickness", "" + rect.getStrokeWidth() + "")
                    .build();
            shape = jsonRect;
        }
        if (s instanceof Ellipse) {
            Ellipse ell = (Ellipse) s;
            String fill = "#" + ell.getFill().toString().substring(2);
            String outline = "#" + ell.getStroke().toString().substring(2);
            JsonObject jsonEll = Json.createObjectBuilder()
                    .add("Shape Type", "Ellipse")
                    .add("Center X Location", "" + ell.getCenterX() + "")
                    .add("Center Y Location", "" + ell.getCenterY() + "")
                    .add("X Radius", "" + ell.getRadiusX() + "")
                    .add("Y Radius", "" + ell.getRadiusY() + "")
                    .add("Fill Color", fill)
                    .add("Outline Color", outline)
                    .add("Outline Thickness", "" + ell.getStrokeWidth() + "")
                    .build();
            shape = jsonEll;
        }
        return shape;
    }

    /**
     * This method loads data from a JSON formatted file into the data
     * management component and then forces the updating of the workspace such
     * that the user may edit the data.
     *
     * @param data Data management component where we'll load the file into.
     *
     * @param filePath Path (including file name/extension) to where to load the
     * data from.
     *
     * @throws IOException Thrown should there be an error reading in data from
     * the file.
     */
    @Override
    public void loadData(AppDataComponent data, String filePath) throws IOException {
        File checkFile = new File(filePath);
        if (!(checkFile.exists())) {
            PropertiesManager props = PropertiesManager.getPropertiesManager();
            AppMessageDialogSingleton fileNotFound = AppMessageDialogSingleton.getSingleton();
            fileNotFound.show("Load Error", "File could not be found.");
        } 
        else {
            DataManager dataManager = (DataManager) data;
            JsonObject load = loadJSONFile(filePath);
            ArrayList<Shape> shapeArray = new ArrayList<Shape>();
            String background = "-fx-background-color: " + load.getString("Background Color").substring(0, 7) + ";";
            dataManager.setBackground(background);
            JsonArray jsonShapes = load.getJsonArray("Shapes");
            for (int i = 0; i < jsonShapes.size(); i++) {
                JsonObject shape = jsonShapes.getJsonObject(i);
                String type = shape.getString("Shape Type");
                if (type.equals("Rectangle")) {
                    Rectangle r = new Rectangle();
                    r.setX(Double.parseDouble(shape.getString("X Location")));
                    r.setY(Double.parseDouble(shape.getString("Y Location")));
                    r.setWidth(Double.parseDouble(shape.getString("Width")));
                    r.setHeight(Double.parseDouble(shape.getString("Height")));
                    String fill = shape.getString("Fill Color").substring(0, 7);
                    r.setFill(Color.valueOf(fill));
                    String outline = shape.getString("Outline Color").substring(0, 7);
                    r.setStroke(Color.valueOf(outline));
                    r.setStrokeWidth(Double.parseDouble(shape.getString("Outline Thickness")));
                    shapeArray.add(r);
                } else if (type.equals("Ellipse")) {
                    Ellipse e = new Ellipse();
                    e.setCenterX(Double.parseDouble(shape.getString("Center X Location")));
                    e.setCenterY(Double.parseDouble(shape.getString("Center Y Location")));
                    e.setRadiusX(Double.parseDouble(shape.getString("X Radius")));
                    e.setRadiusY(Double.parseDouble(shape.getString("Y Radius")));
                    String fill = shape.getString("Fill Color").substring(0, 7);
                    e.setFill(Color.valueOf(fill));
                    String outline = shape.getString("Outline Color").substring(0, 7);
                    e.setStroke(Color.valueOf(outline));
                    e.setStrokeWidth(Double.parseDouble(shape.getString("Outline Thickness")));
                    shapeArray.add(e);
                }
            }
            //CALL LOAD DATA
            dataManager.setShapeArray(shapeArray);
            dataManager.load();
        }
    }

    // HELPER METHOD FOR LOADING DATA FROM A JSON FORMAT
    private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
        InputStream is = new FileInputStream(jsonFilePath);
        JsonReader jsonReader = Json.createReader(is);
        JsonObject json = jsonReader.readObject();
        jsonReader.close();
        is.close();
        return json;
    }

    /**
     * This method exports the contents of the data manager to a Web page
     * including the html page, needed directories, and the CSS file.
     *
     * @param data The data management component.
     *
     * @param filePath Path (including file name/extension) to where to export
     * the page to.
     *
     * @throws IOException Thrown should there be an error writing out data to
     * the file.
     */
    @Override
    public void exportData(AppDataComponent data, String filePath) throws IOException {

    }

    /**
     * This method is provided to satisfy the compiler, but it is not used by
     * this application.
     */
    @Override
    public void importData(AppDataComponent data, String filePath) throws IOException {
        // NOTE THAT THE Web Page Maker APPLICATION MAKES
        // NO USE OF THIS METHOD SINCE IT NEVER IMPORTS
        // EXPORTED WEB PAGES
    }
}
