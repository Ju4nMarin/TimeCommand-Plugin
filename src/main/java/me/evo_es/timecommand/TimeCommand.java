package me.evo_es.timecommand;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;

public final class TimeCommand extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        String message1 = ChatColor.GOLD + "------------------------------ " +
                ChatColor.RESET + getDescription().getName() +
                ChatColor.GOLD + " Enabled ------------------------------";
        getServer().getConsoleSender().sendMessage(message1);

        String message2 = ChatColor.YELLOW + "       Thanks for downloading my first plugin, " +
                ChatColor.GOLD + getDescription().getAuthors().get(0) +
                ChatColor.YELLOW + " :)       ";
        getServer().getConsoleSender().sendMessage(message2);

        String message3 = ChatColor.GOLD + "-------------------------------------------------------------------------";
        getServer().getConsoleSender().sendMessage(message3);

        getCommand("tmc").setExecutor(new CommandTm(this));
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("§cPlugin disabled");
    }
}

class CommandTm implements CommandExecutor {

    private final TimeCommand plugin;

    public CommandTm(TimeCommand plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("timecommand.command.tmc")) {
            sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }


        if (args.length < 3) {
            sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.GOLD + "Command: " + ChatColor.YELLOW + "/tmc <command> <unit> <quantity>.");
            return true;
        }

        List<String> itemsBeforeStop = new ArrayList<>();

        List<String> stopValues = new ArrayList<>();
        stopValues.add("s");
        stopValues.add("m");
        stopValues.add("h");
        stopValues.add("d");
        stopValues.add("mm");

        for (String item : args) {
            if (stopValues.contains(item)) {
                break;
            }

            itemsBeforeStop.add(item);
        }


        String comando = String.join(" ", itemsBeforeStop);
        long ticks;

        String unidad = args[args.length - 2];
        int cantidad;

        try {
            cantidad = Integer.parseInt(args[args.length - 1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "The amount must be an integer.");
            return true;
        }

        switch (unidad) {
            case "s":
                ticks = cantidad * 20L;
                break;
            case "m":
                ticks = cantidad * 1200L;
                break;
            case "h":
                ticks = cantidad * 72000L;
                break;
            case "d":
                ticks = cantidad * 1728000L;
                break;
            case "mm":
                ticks = cantidad * 51840000L;
                break;
            default:
                sender.sendMessage(ChatColor.AQUA + "[TMC]: "+ChatColor.YELLOW + "Invalid time unit. The options are: (s, m, h, d, mm) seconds, minutes, days and mount");
                return true;
        }

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand(sender, comando);
            }
        }, ticks);

        sender.sendMessage(ChatColor.AQUA + "[TMC]: " +
                ChatColor.GOLD + "Command '" + ChatColor.YELLOW +  comando +  ChatColor.GOLD + "' executed in "
                + ChatColor.YELLOW + cantidad + " " + ChatColor.GOLD + unidad + ".");

        return true;
    }
}
