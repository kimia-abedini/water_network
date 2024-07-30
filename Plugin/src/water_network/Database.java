package water_network;

import com.vividsolutions.jump.feature.*;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

import java.sql.*;


public class Database {
    private static final String connection = "jdbc:postgresql://localhost:5432/newgisdb";

    private Connection conn;

    public Database() {
        try {
            conn = DriverManager.getConnection(connection, "postgres", "postgres");
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public FeatureCollection loadMap(String tableName) {
        FeatureSchema fs = new FeatureSchema();
        fs.addAttribute("id", AttributeType.STRING);
        fs.addAttribute("geom", AttributeType.GEOMETRY);

        FeatureCollection fc = new FeatureDataset(fs);
        GeometryFactory gf = new GeometryFactory();
        WKTReader wkt = new WKTReader(gf);

        String query = "SELECT ST_ASTEXT(geom) as geometry, * FROM " + "\"" + tableName + "\"";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Feature f = new BasicFeature(fs);

                f.setAttribute("id", rs.getString("id"));
                f.setGeometry(wkt.read(rs.getString("geometry")));

                fc.add(f);
            }
        } catch (SQLException e) {
            System.out.println(e);
        } catch (org.locationtech.jts.io.ParseException e) {
            throw new RuntimeException(e);
        }


        return fc;

    }

    public FeatureCollection[] loadValves() {
        FeatureSchema fs = new FeatureSchema();

        fs.addAttribute("id", AttributeType.INTEGER);
        fs.addAttribute("geom", AttributeType.GEOMETRY);

        FeatureCollection fc_open = new FeatureDataset(fs);
        FeatureCollection fc_close = new FeatureDataset(fs);

        GeometryFactory gf = new GeometryFactory();
        WKTReader wkt = new WKTReader(gf);

        String query = "SELECT ST_ASTEXT(geom) as geometry, p.id, j.valve_state  FROM pozzetti p inner join junction_manhole_elements j on j.pozzetti_id = p.id where element_type ='Valvola'";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Feature f = new BasicFeature(fs);

                f.setAttribute("id", rs.getInt("id"));
                f.setGeometry(wkt.read(rs.getString("geometry")));

                if(rs.getString("valve_state").equals("open")) {
                    fc_open.add(f);
                } else {
                    fc_close.add(f);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (org.locationtech.jts.io.ParseException e) {
            throw new RuntimeException(e);
        }

        FeatureCollection[] feCo = new FeatureCollection[2];
        feCo[0] = fc_open;
        feCo[1] = fc_close;

        return feCo;
    }



    public Boolean changeValveState(Integer valveID, String valveState){
        String query = "UPDATE junction_manhole_elements SET valve_state = ? WHERE pozzetti_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1,valveState.contains("open") ? "close" : "open");
            pstmt.setInt(2, valveID);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }



    public void updateValveState(String valveId, String newState) {
        String query = "UPDATE junction_manhole_elements SET valve_state = ? WHERE pozzetti_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newState);
            pstmt.setString(2, valveId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);

        }
    }






    public void close() {
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }
}

