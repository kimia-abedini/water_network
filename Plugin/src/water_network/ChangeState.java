package water_network;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ChangeState extends AbstractPlugIn {
    private FeatureCollection openValves;
    private FeatureCollection closedValves;

    public ChangeState() throws SQLException {
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = FeatureInstaller.getInstance(context.getWorkbenchContext()); // OpenJUMP >= 2.0
        featureInstaller.addMainMenuPlugin(
                this, //exe
                new String[]{"Water Network"},
                this.getName(),
                false,
                null,
                null);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Database db = new Database();
        LayerManager layerManager = context.getLayerManager();

        if ( layerManager.getLayer("Open Valves") == null) {
            JOptionPane.showMessageDialog(null, "You should Load Valves at First :|", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        openValves = layerManager.getLayer("Open Valves").getFeatureCollectionWrapper();
        closedValves = layerManager.getLayer("Close Valves").getFeatureCollectionWrapper();

        JOptionPane.showMessageDialog(null, "Click a point on the map...", "Hint", JOptionPane.INFORMATION_MESSAGE);
        context.getLayerViewPanel().setCurrentCursorTool(new NClickTool(context.getWorkbenchContext(), 1) {
            @Override
            public Icon getIcon() {
                return null;
            }

            private Feature findNearestValve(Coordinate clickPoint) {
                Feature nearestValve = null;
                double minDistance = Double.MAX_VALUE;

                GeometryFactory gf = new GeometryFactory();
                Point clickLocation = gf.createPoint(clickPoint);

                for (FeatureCollection fc : new FeatureCollection[]{openValves, closedValves}) {
                    for (Feature feature : fc.getFeatures()) {
                        Point point = (Point) feature.getGeometry();
                        double distance = clickLocation.distance(point);
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestValve = feature;
                        }
                    }
                }

                return nearestValve;
            }

            @Override
            public void gestureFinished() throws Exception {
                Coordinate clickedPoint = this.getCoordinates().get(0);
                Feature nearestValve = findNearestValve(clickedPoint);

                // Calculate the distance to the nearest valve and print its state
                if (nearestValve != null) {
                    String valveState = openValves.getFeatures().contains(nearestValve) ? "open" : "close";
                    double distance = clickedPoint.distance(nearestValve.getGeometry().getCoordinate());

                    Integer n_id = (Integer) nearestValve.getAttribute("id");
                    System.out.println("Distance to nearest valve: " + distance + " meters");
                    System.out.println("Nearest valve state: " + valveState);
                    System.out.println(n_id);

                    Boolean is_ok = db.changeValveState(n_id, valveState);

                    if (is_ok) {
                        LayerManager layerManager = context.getLayerManager();

                        context.getLayerManager().remove(layerManager.getLayer("Open Valves"));
                        context.getLayerManager().remove(layerManager.getLayer("Close Valves"));

                        FeatureCollection[] valves = db.loadValves();

                        Layer openLayer = new Layer("Open Valves", Color.green, valves[0], layerManager);
                        layerManager.addLayer("Water Network", openLayer);

                        Layer closeLayer = new Layer("Close Valves", Color.red, valves[1], layerManager);
                        layerManager.addLayer("Water Network", closeLayer);
                    }
                    System.out.println("Valve state changed, is_ok: " + is_ok);
                } else {
                    System.out.println("No valves found.");
                }
            }
        });

//        db.close();
        return false;
    }

    @Override
    public String getName() {
        return "Change Valve State";
    }
}
