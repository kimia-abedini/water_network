package water_network;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

import java.sql.SQLException;


public class Plugin extends AbstractPlugIn {

    public Plugin() throws SQLException {
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
                null
        );
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Database db = new Database();

        FeatureCollection pozzetti = db.loadMap("pozzetti");
        context.getLayerManager().addLayer("Water Network", "pozzetti", pozzetti);

        FeatureCollection ru = db.loadMap("ru");
        context.getLayerManager().addLayer("Water Network", "ru", ru);

        FeatureCollection tratte = db.loadMap("tratte");
        context.getLayerManager().addLayer("Water Network", "tratte", tratte);

        db.close();
        return false;
    }

    @Override
    public String getName() {
        return "Load Data";
    }
}