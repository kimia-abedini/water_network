package water_network;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import org.locationtech.jts.geom.Coordinate;

import javax.swing.*;
import java.sql.SQLException;

public class OpenValve extends AbstractPlugIn {

    public OpenValve() throws SQLException {
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = FeatureInstaller.getInstance(context.getWorkbenchContext()); // OpenJUMP >= 2.0
        featureInstaller.addMainMenuPlugin(
                this, //exe
                new String[]{"Water Network", "Valve"},
                this.getName(),
                false,
                null,
                null);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Database db = new Database();

        context.getWorkbenchFrame().setStatusMessage("Click a point on the map...");

        context.getLayerViewPanel().setCurrentCursorTool(new NClickTool(context.getWorkbenchContext(), 1) {
            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public void gestureFinished() throws Exception {
                Coordinate clickedPoint = this.getCoordinates().get(0);

                // Find the nearset valve, get the coordinates, and try to open or close
                // You can even try to multi colorize the valve to have better understanding, or manage in another way
                System.out.printf(String.valueOf(clickedPoint));

            }});

        db.close();
        return false;
    }

    @Override
    public String getName() {
        return "Valve Management";
    }
}