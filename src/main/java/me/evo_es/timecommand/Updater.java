package me.evo_es.timecommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class Updater {
    private final int project = 112309;
    private URL checkURL;
    private String newVersion;
    private final String currentVersion;
    private final JavaPlugin plugin;
    int minRange = 1;
    int maxRange = 999999999;

    Random random = new Random();
    int randomInRange = random.nextInt(maxRange - minRange + 1) + minRange;

    public Updater(JavaPlugin paramJavaPlugin) {
        this.plugin = paramJavaPlugin;
        this.currentVersion = paramJavaPlugin.getDescription().getVersion();
        try {
            this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=112309&t="+randomInRange);
        } catch (MalformedURLException malformedURLException) {
        }
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    public String getCurrentVersion() {
        return this.currentVersion;
    }

    public String getNewVersion() {
        return this.newVersion;
    }

    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/112309";
    }

    public boolean checkForUpdates() {
        URLConnection uRLConnection = null;
        try {
            uRLConnection = this.checkURL.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.newVersion = (new BufferedReader(new InputStreamReader(uRLConnection.getInputStream()))).readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return !this.currentVersion.equals(this.newVersion);
    }

    public void checkAndNotifyUpdates() {
        if (checkForUpdates()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage("¡El plugin " + plugin.getName() + " se ha actualizado a la versión " + newVersion + "!");
                    player.sendMessage("Descarga la nueva versión desde: " + getResourceURL());
                }
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("El plugin " + plugin.getName() + " está actualizado en la versión " + currentVersion + ".");
        }
    }

    public void checkAndUpdate() {
        if (checkForUpdates()) {
            checkAndNotifyUpdates();
        }
    }
}
