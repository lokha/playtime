package ua.lokha.playtime.bungee;


import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import ua.lokha.playtime.Dao;

public class PlayTimeCommand extends Command {

    public PlayTimeCommand() {
        super("playtime", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(Message.HELP.get());
            return;
        }

        Dao.async(dao -> {
            String username = sender.getName();
            if (args.length > 0) {
                username = args[0];
            }

            Dao.UserInfo info = dao.getInfo(username, Main.getInstance().getServers());
            String header = Message.HEADER.get(
                    "{name}", info.getUsername(),
                    "{total_playtime}", displayTime(info.calcSumSeconds()),
                    "{place}", String.valueOf(info.getPlace()));
            sender.sendMessage(header);
            for (String server : Main.getInstance().getServers()) {
                String perServer = Message.PER_SERVER.get(
                        "{server}", server,
                        "{playtime}", displayTime(info.getOnlineSeconds().getOrDefault(server, 0)));
                sender.sendMessage(perServer);
            }
        });
    }

    public static String displayTime(int onlineSeconds) {
        int minutes = onlineSeconds / 60;
        int hours = minutes / 60;

        if (hours == 0) {
            return minutes + " " + Message.MINUTES.get();
        }

        return hours + " " + Message.HOURS.get() + " " + (minutes - (hours * 60)) + " " + Message.MINUTES.get();
    }
}
