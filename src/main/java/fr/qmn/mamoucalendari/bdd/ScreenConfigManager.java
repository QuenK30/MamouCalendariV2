package fr.qmn.mamoucalendari.bdd;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.*;

public class ScreenConfigManager {

    public static boolean isConfigured() {
        try (Connection c = DriverManager.getConnection(DBConfig.URL);
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM SCREEN_CONFIG")) {
            return r.next() && r.getInt(1) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Renvoie [screen_visual, screen_calendar, screen_ocr], défaut [0,0,0]. */
    public static int[] getConfig() {
        try (Connection c = DriverManager.getConnection(DBConfig.URL);
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(
                 "SELECT screen_visual, screen_calendar, screen_ocr FROM SCREEN_CONFIG WHERE ID=1")) {
            if (r.next()) return new int[]{r.getInt(1), r.getInt(2), r.getInt(3)};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{0, 0, 0};
    }

    public static void saveConfig(int visual, int calendar, int ocr) {
        String sql = "INSERT OR REPLACE INTO SCREEN_CONFIG" +
                     "(ID,screen_visual,screen_calendar,screen_ocr) VALUES(1,?,?,?)";
        try (Connection c = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, visual);
            p.setInt(2, calendar);
            p.setInt(3, ocr);
            p.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Positionne un Stage UNDECORATED sur l'écran d'index screenIndex.
     * Si l'index dépasse les écrans disponibles, utilise l'écran 0.
     */
    public static void applyScreen(Stage stage, int screenIndex) {
        var screens = Screen.getScreens();
        int idx = Math.max(0, Math.min(screenIndex, screens.size() - 1));
        Rectangle2D b = screens.get(idx).getVisualBounds();
        stage.setX(b.getMinX());
        stage.setY(b.getMinY());
        stage.setWidth(b.getWidth());
        stage.setHeight(b.getHeight());
    }

    private static final double DESIGN_W = 1920.0;
    private static final double DESIGN_H = 1080.0;

    /**
     * Positionne le Stage sur l'écran cible et applique un Scale sur la racine
     * pour que le layout 1920×1080 remplisse la résolution réelle de l'écran.
     */
    public static void applyScreen(Stage stage, Parent root, int screenIndex) {
        var screens = Screen.getScreens();
        int idx = Math.max(0, Math.min(screenIndex, screens.size() - 1));
        Rectangle2D b = screens.get(idx).getVisualBounds();
        double sx = b.getWidth()  / DESIGN_W;
        double sy = b.getHeight() / DESIGN_H;
        root.getTransforms().setAll(new Scale(sx, sy, 0, 0));
        stage.setX(b.getMinX());
        stage.setY(b.getMinY());
        stage.setWidth(b.getWidth());
        stage.setHeight(b.getHeight());
    }
}
