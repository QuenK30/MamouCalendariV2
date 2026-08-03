package fr.qmn.mamoucalendari.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AppConfig {

    private static final String mode;
    private static final String apiUrl;
    private static final String apiKey;
    private static final String apiUsername;
    private static final String apiPassword;
    private static final int    syncPushInterval;
    private static final int    syncPullInterval;
    private static final int    reminderInterval;

    static {
        Properties props = new Properties();

        // 1. Fichier override à côté du JAR (prod)
        try {
            Path loc = Paths.get(AppConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path dir = loc.toFile().isDirectory() ? loc : loc.getParent();
            Path override = dir.resolve("app.properties");
            if (override.toFile().exists()) {
                try (InputStream is = Files.newInputStream(override)) {
                    props.load(is);
                }
            }
        } catch (Exception ignored) {}

        // 2. Classpath (dev via resources, prod via JAR embarqué)
        if (props.isEmpty()) {
            try (InputStream is = AppConfig.class.getResourceAsStream("/fr/qmn/mamoucalendari/app.properties")) {
                if (is == null) throw new RuntimeException("app.properties introuvable dans le classpath");
                props.load(is);
            } catch (IOException e) {
                throw new RuntimeException("Impossible de charger app.properties", e);
            }
        }

        mode             = props.getProperty("repository.mode",                   "sqlite").trim();
        apiUrl           = props.getProperty("api.url",                           "").trim();
        apiKey           = props.getProperty("api.key",                           "").trim();
        apiUsername      = props.getProperty("api.username",                      "").trim();
        apiPassword      = props.getProperty("api.password",                      "").trim();
        syncPushInterval = Integer.parseInt(props.getProperty("sync.push.interval.seconds",        "30").trim());
        syncPullInterval = Integer.parseInt(props.getProperty("sync.pull.interval.seconds",       "300").trim());
        reminderInterval = Integer.parseInt(props.getProperty("reminder.check.interval.minutes",    "1").trim());

        if (!mode.equals("sqlite") && !mode.equals("remote") && !mode.equals("sync")) {
            throw new RuntimeException("repository.mode invalide : '" + mode + "' (valeurs acceptées : sqlite, remote, sync)");
        }

        boolean hasNetwork = mode.equals("remote") || mode.equals("sync");
        System.out.println("[AppConfig] mode=" + mode + (hasNetwork ? " url=" + apiUrl : ""));
    }

    public static String getMode()              { return mode; }
    public static String getApiUrl()            { return apiUrl; }
    public static String getApiKey()            { return apiKey; }
    public static String getApiUsername()       { return apiUsername; }
    public static String getApiPassword()       { return apiPassword; }
    public static int    getSyncPushInterval()  { return syncPushInterval; }
    public static int    getSyncPullInterval()  { return syncPullInterval; }
    public static int    getReminderInterval()  { return reminderInterval; }
}
