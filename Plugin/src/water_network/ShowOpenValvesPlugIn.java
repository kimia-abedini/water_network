package water_network;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class ShowOpenValvesPlugIn extends AbstractPlugIn {

    public ShowOpenValvesPlugIn() throws SQLException {
        // No specific initialization needed for the constructor
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = FeatureInstaller.getInstance(context.getWorkbenchContext()); // OpenJUMP >= 2.0
        featureInstaller.addMainMenuPlugin(
                this,
                new String[]{"Water Network"},
                this.getName(),
                false,
                null,
                null
        );
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Database db = new Database();

        FeatureCollection[] valves = db.loadValves();
        context.getLayerManager().addLayer("Water Network", "Open Valves", valves[0]);
        context.getLayerManager().addLayer("Water Network", "Close Valves", valves[1]);


        return true;
    }

    @Override
    public String getName() {
        return "Show Valves in Manholes";
    }
}
