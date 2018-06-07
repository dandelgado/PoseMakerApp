package pm.data;

import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import pm.gui.Workspace;
import saf.components.AppDataComponent;
import saf.AppTemplate;

/**
 * This class serves as the data management component for this application.
 *
 * @author Richard McKenna
 * @author Daniel Delgado
 * @version 1.0
 */
public class DataManager implements AppDataComponent {
    // THIS IS A SHARED REFERENCE TO THE APPLICATION
    AppTemplate app;
    
    ArrayList<Shape> shapeArray;
    String background;
    
    public ArrayList<Shape> getShapeArray() {
        return shapeArray;
    }
    
    public void setShapeArray(ArrayList<Shape> s) {
        shapeArray = s;
    }
    
    public String getBackground() {
        return background;
    }
    
    public void setBackground(String bg) {
        background = bg;
    }

    public void load() {
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        workspace.loadWork(shapeArray, background);
    }
    
    public void resetSelected() {
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        workspace.resetSelected();
    }
    
    public void reselect() {
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
        workspace.reselect();
    }
    
    /**
     * THis constructor creates the data manager and sets up the
     *
     *
     * @param initApp The application within which this data manager is serving.
     */
    public DataManager(AppTemplate initApp) throws Exception {
	// KEEP THE APP FOR LATER
	app = initApp;
    }
    
    /**
     * This function clears out the HTML tree and reloads it with the minimal
     * tags, like html, head, and body such that the user can begin editing a
     * page.
     */
    @Override
    public void reset() {
    }
}
