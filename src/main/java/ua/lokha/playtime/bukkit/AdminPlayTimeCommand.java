package ua.lokha.playtime.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminPlayTimeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(
                    "§e===============[BungeeAdminPlayTime]===============\n" +
                            "§3/adminplaytime reload §7- перезагрузить конфиг плагина");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadCustomConfig();
            sender.sendMessage("§aКонфиг перезагружен (На Spigot, если нужно перезагрузить на банге, пишите в консоли банги).");
            return true;
        }

        sender.sendMessage("§сАргумент команды не найден, используйте /adminplaytime help");
        return false;
    }
}
