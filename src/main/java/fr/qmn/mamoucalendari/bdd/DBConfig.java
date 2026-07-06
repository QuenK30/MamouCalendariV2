package fr.qmn.mamoucalendari.bdd;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DBConfig {

    public static final String URL;

    static {
        // Mode développement : CWD = racine projet, le dossier bdd/ existe
        Path devDir = Paths.get("src/main/resources/fr/qmn/mamoucalendari/bdd");
        if (devDir.toFile().isDirectory()) {
            URL = "jdbc:sqlite:" + devDir.resolve("UserRegistre.db").toAbsolutePath();
        } else {
            // Mode production : DB placée à côté du JAR
            String fallback = "jdbc:sqlite:UserRegistre.db";
            String computed = fallback;
            try {
                Path loc = Paths.get(DBConfig.class
                        .getProtectionDomain().getCodeSource().getLocation().toURI());
                Path dir = loc.toFile().isDirectory() ? loc : loc.getParent();
                computed = "jdbc:sqlite:" + dir.resolve("UserRegistre.db").toAbsolutePath();
            } catch (Exception ignored) { }
            URL = computed;
        }
        System.out.println("[DB] " + URL);
    }
}
