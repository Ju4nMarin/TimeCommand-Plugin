package me.evo_es.timecommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class TimeCommand extends JavaPlugin {

    List<ScheduledCommand> scheduledCommands = new ArrayList<>();
    private Updater updater;

    @Override
    public void onEnable() {

        String message1 = ChatColor.GOLD + "------------------------------ " +
                ChatColor.RESET + getDescription().getName() +
                ChatColor.GREEN + " Enabled" + ChatColor.GOLD + " ----------------------";
        getServer().getConsoleSender().sendMessage(message1);

        String message2 = ChatColor.YELLOW + "       Thanks for downloading my first plugin, " +
                ChatColor.GOLD + getDescription().getAuthors().get(0) +
                ChatColor.YELLOW + " :)       ";
        getServer().getConsoleSender().sendMessage(message2);

        String message3 = ChatColor.GOLD + "-------------------------------------------------------------------------";
        getServer().getConsoleSender().sendMessage(message3);


        updater = new Updater(this);
        updater.checkAndUpdate();

        getServer().getPluginManager().registerEvents(new JoinUpdaterMessage(updater), this);

        getCommand("tmc").setExecutor(new CommandTm(this, scheduledCommands));
        Bukkit.getScheduler().runTaskTimer(this, this::updateScheduledCommands, 0L, 20L);
    }

    @Override
    public void onDisable() {
        getLogger().info("§cPlugin disabled");
    }

    private void updateScheduledCommands() {
        Iterator<ScheduledCommand> iterator = scheduledCommands.iterator();
        while (iterator.hasNext()) {
            ScheduledCommand scheduledCommand = iterator.next();
            if (scheduledCommand.decrementTicksRemaining() <= 0) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), scheduledCommand.getCommand());
                iterator.remove();
            }
        }
    }
}

class CommandTm implements CommandExecutor {

    private final TimeCommand plugin;
    private final List<ScheduledCommand> scheduledCommands;

    public CommandTm(TimeCommand plugin, List<ScheduledCommand> scheduledCommands) {
        this.plugin = plugin;
        this.scheduledCommands = scheduledCommands;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length >= 3 && !args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("timecommand.command.tmc")) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            List<String> itemsBeforeStop = new ArrayList<>();

            List<String> stopValues = new ArrayList<>();
            stopValues.add("s");
            stopValues.add("m");
            stopValues.add("h");
            stopValues.add("d");
            stopValues.add("mm");
            stopValues.add("S");
            stopValues.add("M");
            stopValues.add("H");
            stopValues.add("D");
            stopValues.add("MM");


            for (String item : args) {
                if (stopValues.contains(item)) {
                    break;
                }

                itemsBeforeStop.add(item);
            }

            String comando = String.join(" ", itemsBeforeStop);
            long ticks;

            String unitemp = args[args.length - 2];
            String unidad = unitemp.toLowerCase();

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
                    sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Invalid time unit. The options are: (s, m, h, d, mm) seconds, minutes, days and mount");
                    return true;
            }

            ScheduledCommand scheduledCommand = new ScheduledCommand(comando, ticks);
            scheduledCommands.add(scheduledCommand);

            sender.sendMessage(ChatColor.AQUA + "[TMC]: " +
                    ChatColor.GOLD + "Command '" + ChatColor.YELLOW +  comando +  ChatColor.GOLD + "' executed in "
                    + ChatColor.YELLOW + cantidad + " " + ChatColor.GOLD + unidad + ".");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            int pagina;
            try {
                pagina = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Invalid page number.");
                return true;
            }

            int comandosPorPagina = 5;
            int totalPaginas = (int) Math.ceil((double) scheduledCommands.size() / comandosPorPagina);

            if (pagina <= 0 || pagina > totalPaginas) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Page number out of range.");
                return true;
            }

            int inicio = (pagina - 1) * comandosPorPagina;
            int fin = Math.min(inicio + comandosPorPagina, scheduledCommands.size());
            sender.sendMessage( " ");
            sender.sendMessage(ChatColor.YELLOW + "Scheduled Commands Page (" + ChatColor.GOLD + pagina + ChatColor.YELLOW + "/" + ChatColor.YELLOW +  totalPaginas + ChatColor.YELLOW +"):");
            sender.sendMessage( " ");
            if (inicio >= fin) {
                sender.sendMessage(ChatColor.YELLOW + "Empty page");
            } else {
                for (int i = inicio; i < fin; i++) {
                    ScheduledCommand scheduledCommand = scheduledCommands.get(i);
                    long remainingTicks = scheduledCommand.getTicksRemaining();
                    String timeRemaining = formatTime(remainingTicks / 20);

                    sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.GOLD + "' " + ChatColor.WHITE + scheduledCommand.getCommand() + ChatColor.GOLD + " '" +
                            ChatColor.GOLD + " Time Remaining: " + "(" + ChatColor.YELLOW + timeRemaining +  ChatColor.GOLD + ")");
                }
            }
            sender.sendMessage(" ");
        } else {
            sender.sendMessage(ChatColor.GOLD + "============ TimeCommand ==============");
            sender.sendMessage(" ");
            sender.sendMessage(ChatColor.GOLD + "1." + ChatColor.YELLOW + " tmc <command> <unit> <quantity>.");
            sender.sendMessage(ChatColor.GOLD + "2." + ChatColor.YELLOW + " tmc list <page>");
            sender.sendMessage(" ");
            sender.sendMessage(ChatColor.GOLD + "======================================");
        }

        return true;
    }

    private String formatTime(long timeInSeconds) {
        long days = timeInSeconds / 86400;
        long hours = (timeInSeconds % 86400) / 3600;
        long minutes = (timeInSeconds % 3600) / 60;
        long seconds = timeInSeconds % 60;
        return String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds);
    }
}

class ScheduledCommand {
    private String command;
    private long ticksRemaining;

    public ScheduledCommand(String command, long ticksRemaining) {
        this.command = command;
        this.ticksRemaining = ticksRemaining;
    }

    public long decrementTicksRemaining() {
        ticksRemaining -= 20;
        return ticksRemaining;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getTicksRemaining() {
        return ticksRemaining;
    }

    public void setTicksRemaining(long ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
    }
}
