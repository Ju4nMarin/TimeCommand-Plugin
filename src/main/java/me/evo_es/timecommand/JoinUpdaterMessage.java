package me.evo_es.timecommand;

import org.bukkit.Bukkit;
import  org.bukkit.ChatColor;
import  org.bukkit.entity.Player;
import  org.bukkit.event.EventHandler;
import  org.bukkit.event.Listener;
import  org.bukkit.event.player.PlayerJoinEvent;


public class JoinUpdaterMessage  implements Listener{
    private final Updater updater;

    public JoinUpdaterMessage(Updater updater) {
        this.updater = updater;
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
            if(player.isOp())
            {
                player.sendMessage(ChatColor.GOLD  + "--------------- TimeCommand ----------------------- ");
                player.sendMessage("Current plugin version: " +  updater.getCurrentVersion());
                if (updater.checkForUpdates()) {
                    player.sendMessage(ChatColor.WHITE + "A new version (" + ChatColor.GREEN + updater.getNewVersion() + ChatColor.WHITE +") is available!");
                    player.sendMessage("Download it from: " + ChatColor.GREEN + updater.getResourceURL());

                } else {

                    player.sendMessage(ChatColor.GREEN + "Plugin is up to date.");
                }
                player.sendMessage("Plugin developed by " + ChatColor.YELLOW + "evo_es") ;
                player.sendMessage(ChatColor.GOLD + "--------------------------------------------------");
            }
    }
}
