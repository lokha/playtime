package ua.lokha.playtime.bungee;


import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class AdminPlayTimeCommand extends Command {

    public AdminPlayTimeCommand() {
        super("adminplaytime", "command.adminplaytime");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(
                    "§e===============[BungeeAdminPlayTime]===============\n" +
                            "§3/adminplaytime reload §7- перезагрузить конфиг плагина");
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadCustomConfig();
            sender.sendMessage("§aКонфиг перезагружен.");
            return;
        }

        sender.sendMessage("§сАргумент команды не найден, используйте /adminplaytime help");
    }
}
