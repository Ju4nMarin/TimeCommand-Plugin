package me.evo_es.timecommand;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class VersionCommand implements CommandExecutor {
    private final Updater updater;

    public VersionCommand(Updater updater) {
        this.updater = updater;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for(Player player : Bukkit.getServer().getOnlinePlayers())
        {
            if(player.isOp())
            {
                player.sendMessage("HOLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            }else{
                player.sendMessage("No eres OP XD");

            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
            if (sender instanceof Player && !((Player) sender).isOp()) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }

            sender.sendMessage("Current plugin version: " + updater.getCurrentVersion());
            if (updater.checkForUpdates()) {
                sender.sendMessage("A new version (" + updater.getNewVersion() + ") is available!");
                sender.sendMessage("Download it from: " + updater.getResourceURL());
            } else {
                sender.sendMessage("Plugin is up to date.");
            }
            return true;
        }
        return false;
    }
}
