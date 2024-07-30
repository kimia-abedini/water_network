package water_network;

import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;

import javax.swing.*;
import javax.xml.crypto.dsig.TransformException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import static water_network.AddressProcessor.getAddressFromCoordinates;
import static water_network.AddressProcessor.getSawazApiResponse;

public class WaterOutagePlugin extends AbstractPlugIn {

    public WaterOutagePlugin() throws SQLException {
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

        FeatureCollection pozzetti = db.loadMap("pozzetti");
        context.getLayerManager().addLayer("Water Network", "pozzetti", pozzetti);

        FeatureCollection ru = db.loadMap("ru");
        context.getLayerManager().addLayer("Water Network", "ru", ru);

        FeatureCollection tratte = db.loadMap("tratte");
        context.getLayerManager().addLayer("Water Network", "tratte", tratte);

//        String manholeId = "868";
//        Feature closedManhole = findManholeById(pozzetti, manholeId);
//
//        System.out.println(closedManhole);
//
//        Set<Feature> affectedHomes = findAffectedUserHomes(tratte, ru,
//                closedManhole
//        );
//        System.out.println(affectedHomes);


        MultiInputDialog mid = new MultiInputDialog(
                context.getWorkbenchFrame(),
                this.getName(), true
        );

        List<String> manholeIDs = new LinkedList<String>();
        for (Feature poz: pozzetti.getFeatures()) {
            manholeIDs.add(poz.getAttribute("id").toString());
        }

        String _manholeID = "Choose the ManHole ID to process";

        mid.addComboBox(_manholeID, manholeIDs.get(0), manholeIDs, "yes");
        mid.setVisible(true); // modal dialog
        if (!mid.wasOKPressed()) return false;

        String manholeId = mid.getText(_manholeID);
        Feature closedManhole = findManholeById(pozzetti, manholeId);

        Point centerPoint = findDisconnectedNodes(context, tratte, closedManhole, ru);
        System.out.println(centerPoint);
        centerPoint = transformCoordinate(centerPoint);
        System.out.println(centerPoint);
        try {
            String address = getAddressFromCoordinates(centerPoint.getY(), centerPoint.getX());
            System.out.println(address);
            String sawazResponse = getSawazApiResponse(address);
            System.out.println("Sawaz API Response:");
            System.out.println(sawazResponse); // Coppola,Costanza;Simoni,Carlo;Lorenzi,Sara
            JOptionPane.showMessageDialog(null, "Address: " + address + "\nResidences: " + sawazResponse, "Hint", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
    private Point transformCoordinate(Point sourcePoint) throws TransformException {
        GeometryFactory geometryFactory = new GeometryFactory();

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem sourceCRS = crsFactory.createFromName("EPSG:3003"); // Assuming WGS84
        CoordinateReferenceSystem targetCRS = crsFactory.createFromName("EPSG:4326"); // Adjust as necessary
        BasicCoordinateTransform transform = new BasicCoordinateTransform(sourceCRS, targetCRS);


        // Transform point coordinates if needed
        ProjCoordinate sourceCoord = new ProjCoordinate(sourcePoint.getX(), sourcePoint.getY());
        ProjCoordinate targetCoord = new ProjCoordinate();
        transform.transform(sourceCoord, targetCoord);
        return geometryFactory.createPoint(new Coordinate(targetCoord.x, targetCoord.y));
    }

    private Feature findManholeById(FeatureCollection manholes, String id) {
        for (Object obj : manholes.getFeatures()) {
            Feature feature = (Feature) obj;
            if (id.equals(feature.getAttribute("id"))) {
                return feature;
            }
        }
        return null;
    }

    private Point findDisconnectedNodes(PlugInContext context, FeatureCollection connections, Feature closedManhole, FeatureCollection homes) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Graph<Point, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (Feature connectionObject : connections.getFeatures()) {
            if (connectionObject.getID() == 1869 || connectionObject.getID() == 1957) continue;
            MultiLineString multiLineString = (MultiLineString) connectionObject.getGeometry();
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                LineString lineString = (LineString) multiLineString.getGeometryN(i);
                for (int j = 0; j < lineString.getNumPoints() - 1; j++) {
                    Coordinate coords1 = lineString.getPointN(j).getCoordinate();
                    Coordinate coords2 = lineString.getPointN(j + 1).getCoordinate();
                    Point p1 = geometryFactory.createPoint(new Coordinate(coords1.x, coords1.y));
                    Point p2 = geometryFactory.createPoint(new Coordinate(coords2.x, coords2.y));
                    graph.addVertex(p1);
                    graph.addVertex(p2);
                    graph.addEdge(p1, p2);
                }
            }
        }

        Graph<Point, DefaultEdge> graphCopy = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addGraph(graphCopy, graph);

        assert closedManhole != null;
        Point manholePoint = geometryFactory.createPoint(new Coordinate(closedManhole.getGeometry().getCoordinate().x, closedManhole.getGeometry().getCoordinate().y));

        graphCopy.removeVertex(manholePoint);

        ConnectivityInspector<Point, DefaultEdge> inspector = new ConnectivityInspector<>(graphCopy);
        List<Set<Point>> connectedComponents = inspector.connectedSets();

        int biggestConnectedArea = 0;
        Set<Point> allConnectedNodes = null;
        for (Set<Point> connectedComponent : connectedComponents) {
            int connectedCompsSize = connectedComponent.size();
            if (connectedCompsSize > biggestConnectedArea) {
                allConnectedNodes = connectedComponent;
                biggestConnectedArea = connectedComponent.size();
            }
        }

        // Find the disconnected nodes
        Set<Point> disconnectedNodes = new HashSet<>(graph.vertexSet());
        assert allConnectedNodes != null;
        disconnectedNodes.removeAll(allConnectedNodes);


        FeatureSchema fs = new FeatureSchema();
        FeatureCollection fc = new FeatureDataset(fs);
        fs.addAttribute("geom", AttributeType.GEOMETRY);

        Set<Point> disconnectedHomes = new HashSet<>();

        for (Point p : disconnectedNodes) {
            for (Feature homeObject : homes.getFeatures()) {
                Coordinate coord = p.getCoordinate();
                if (coord.x == homeObject.getGeometry().getCoordinate().x && coord.y == homeObject.getGeometry().getCoordinate().y) {
                    System.out.println(p.getNumPoints());

                    disconnectedHomes.add(p);

                    Feature f = new BasicFeature(fs);
                    f.setGeometry(p.getGeometryN(1));
                    fc.add(f);
                }
            }
        }
        context.getLayerManager().addLayer("Results", "Affected Area", fc);
        System.out.println(disconnectedHomes.size());

        return drawBoundary(context, disconnectedHomes);
    }

    private Point drawBoundary(PlugInContext context, Set<Point> disconnectedHomes){
        Envelope envelope = new Envelope();

        for (Point point : disconnectedHomes) {
            envelope.expandToInclude(point.getCoordinate());
        }

        // Buffer the bounding box by 5 meter
        GeometryFactory factory = new GeometryFactory();
        Polygon rectangle = (Polygon) factory.toGeometry(envelope).buffer(5.0);

        // Create a feature and add it to a feature collection
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute("Geometry", AttributeType.GEOMETRY);
        Feature feature = new BasicFeature(schema);
        feature.setGeometry(rectangle);

        FeatureCollection featureCollection = new FeatureDataset(schema);
        featureCollection.add(feature);

        // Add the feature collection to the layer manager and display it
        context.addLayer("Results", "Affected Area (Buffered)", featureCollection);

        return rectangle.getCentroid();
    }


    private Set<Feature> findAffectedUserHomes(FeatureCollection connections, FeatureCollection userHomes, Feature closedManhole) {
        Set<Feature> affectedHomes = new HashSet<>();

        // TODO: DRAW A CIRCLE OVER THAT,
        // TODO: CHECK THAT IS VALVE OR NOT ? WE CAN DO IT BEFORE
        //
        for (Object object: connections.getFeatures()){
            Feature connection = (Feature) object;
            for (Coordinate coord: connection.getGeometry().getCoordinates()){
                if (coord.x == closedManhole.getGeometry().getCoordinate().x) {
                    System.out.println(connection.getID());
                    for(Object homeObj: userHomes.getFeatures()){
                        Feature home = (Feature) homeObj;
                        for (Coordinate coord1: connection.getGeometry().getCoordinates()) {
                            if (coord1.x == home.getGeometry().getCoordinate().x) {
                                System.out.println(connection.getID());
                                System.out.println(home.getID());
                                affectedHomes.add(home);
                            }
                        }
                    }
                }
            }
        }

        return affectedHomes;

    }

    @Override
    public String getName() {
        return "Water Outage";
    }
}