package me.evo_es.timecommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
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
                String[] commands = scheduledCommand.getCommand().split("&&");
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.trim());
                }
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
        if (!sender.hasPermission("timecommand.command.tmc")) {
            sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length >= 3 && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("remove")) {
            ArrayList<String> stopValues = new ArrayList<>(Arrays.asList("s", "m", "h", "d", "mm"));
            ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));

            int unitIndex = -1;
            for (int i = argList.size() - 2; i >= 0; i--) {
                if (stopValues.contains(argList.get(i).toLowerCase())) {
                    unitIndex = i;
                    break;
                }
            }

            if (unitIndex == -1 || unitIndex + 1 >= argList.size()) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Invalid time unit. The options are: " +
                        ChatColor.GOLD + "(s, m, h, d, mm) " + ChatColor.YELLOW + "seconds, minutes, hours, days, and months.");
                return true;
            }

            String unidad = argList.get(unitIndex).toLowerCase();

            if (!stopValues.contains(unidad)) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Invalid time unit. The options are: " +
                        ChatColor.GOLD + "(s, m, h, d, mm) " + ChatColor.YELLOW + "seconds, minutes, hours, days, and months.");
                return true;
            }

            String comando = String.join(" ", argList.subList(0, unitIndex));
            int cantidad;
            try {
                cantidad = Integer.parseInt(argList.get(unitIndex + 1));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "The amount must be an integer.");
                return true;
            }

            long ticks;
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
                    sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Invalid time unit. The options are: " +
                            ChatColor.GOLD + "(s, m, h, d, mm) " + ChatColor.YELLOW + "seconds, minutes, hours, days, and months.");
                    return true;
            }

            if (comando.trim().toLowerCase().startsWith("tmc")) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.RED + "You cannot schedule nested /tmc commands.");
                return true;
            }

            ScheduledCommand scheduledCommand = new ScheduledCommand(comando, ticks);
            scheduledCommands.add(scheduledCommand);

            sender.sendMessage(ChatColor.AQUA + "[TMC]: " +
                    ChatColor.GOLD + "Command '" + ChatColor.YELLOW + comando + ChatColor.GOLD + "' scheduled to execute in " +
                    ChatColor.YELLOW + ticks / 20 + ChatColor.GOLD + " seconds.");
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
            int pagina = 1;

            if (args.length == 2) {
                try {
                    pagina = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Invalid page number.");
                    return true;
                }
            }

            if (scheduledCommands.isEmpty()) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "No commands scheduled.");
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
            sender.sendMessage(" ");
            sender.sendMessage(ChatColor.YELLOW + "Scheduled Commands Page (" + ChatColor.GOLD + pagina + ChatColor.YELLOW + "/" + ChatColor.GOLD + totalPaginas + ChatColor.YELLOW + "):");
            sender.sendMessage(" ");
            if (inicio >= fin) {
                sender.sendMessage(ChatColor.YELLOW + "Empty page");
            } else {
                for (int i = inicio; i < fin; i++) {
                    ScheduledCommand scheduledCommand = scheduledCommands.get(i);
                    long remainingTicks = scheduledCommand.getTicksRemaining();
                    String timeRemaining = formatTime(remainingTicks / 20);

                    sender.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + (i + 1) + ChatColor.DARK_AQUA + "] " +
                            ChatColor.GOLD + "' " + ChatColor.WHITE + scheduledCommand.getCommand() + ChatColor.GOLD + " '" +
                            ChatColor.GOLD + " Time Remaining: " + "(" + ChatColor.YELLOW + timeRemaining + ChatColor.GOLD + ")");
                }
            }
            sender.sendMessage(" ");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            int index;

            try {
                index = Integer.parseInt(args[1]) - 1; // Convert 1-based index to 0-based index
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Invalid index. Check the list index with the " +
                        ChatColor.GOLD + "/tmc list" + ChatColor.YELLOW + " command.");
                return true;
            }

            if (index < 0 || index >= scheduledCommands.size()) {
                sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.YELLOW + "Index out of range.");
                return true;
            }

            ScheduledCommand removedCommand = scheduledCommands.remove(index);
            sender.sendMessage(ChatColor.AQUA + "[TMC]: " + ChatColor.GOLD + "Command '" + ChatColor.YELLOW + removedCommand.getCommand() + ChatColor.GOLD + "' removed from the schedule.");
        } else {
            sender.sendMessage(ChatColor.GOLD + "============ TimeCommand ==============");
            sender.sendMessage(" ");
            sender.sendMessage(ChatColor.GOLD + "1." + ChatColor.YELLOW + " /tmc <command> <unit> <quantity>");
            sender.sendMessage(ChatColor.GOLD + "2." + ChatColor.YELLOW + " /tmc list <page>");
            sender.sendMessage(ChatColor.GOLD + "3." + ChatColor.YELLOW + " /tmc remove <index>");
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
    private final String command;
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

    public long getTicksRemaining() {
        return ticksRemaining;
    }
}
