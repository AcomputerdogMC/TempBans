package net.acomputerdog.tempbans;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.UUID;

/**
 * Tempbans command handler
 */
public class TempBansCommandHandler {
    /**
     * Plugin instance
     */
    private final PluginTempBans plugin;

    public TempBansCommandHandler(PluginTempBans plugin) {
        this.plugin = plugin;
    }

    public void onCommand(CommandSender sender, Command command, String[] args) {
        switch (command.getName()) {
            case "ban":
                onBan(sender, args);
                break;
            case "pardon":
                onPardon(sender, args);
                break;
            case "bantime":
                onBantime(sender, args);
                break;
            case "tbreload":
                onReload(sender);
                break;
            case "kick":
                onKick(sender, args);
                break;
            default:
                // this only happens if a command is registered in plugin.yml but not handled here
                sender.sendMessage(Messages.UNKNOWN_COMMAND_MESSAGE);
                plugin.getLogger().warning(Messages.getUnknownCommandLog(command));
                break;
        }
    }


    private void onBan(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tempans.ban")) {
            sender.sendMessage(Messages.NO_PERMISSIONS_MESSAGE);
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(Messages.INCORRECT_BAN_USAGE_MESSAGE);
            return;
        }

        // lookup player
        UUID uuid = plugin.getUUIDForPlayer(args[0]);
        if (uuid == null) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND_MESSAGE);
            return;
        }

        // read duration if present
        long expires = BanEntry.PERMANENT_BAN;
        try {
            if (args.length >= 2) {
                long delay = Long.parseLong(args[1]) * 60 * 1000; //delay in minutes
                expires = System.currentTimeMillis() + delay;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.INVALID_EXPIRATION_DATE_MESSAGE);
            return;
        }

        // read reason if present
        String reason = BanEntry.UNSPECIFIED_REASON;
        if (args.length >= 3) {
            StringBuilder builder = new StringBuilder();
            for (int idx = 2; idx < args.length; idx++) {
                if (idx != 2) {
                    builder.append(' ');
                }
                builder.append(args[idx]);
            }
            reason = builder.toString();
        }

        // apply the ban
        plugin.banPlayer(uuid, new Date(expires), reason, sender.getName());
        sender.sendMessage(Messages.PLAYER_BANNED_MESSAGE);
    }

    private void onPardon(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tempans.pardon")) {
            sender.sendMessage(Messages.NO_PERMISSIONS_MESSAGE);
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(Messages.PARDON_USAGE_MESSAGE);
            return;
        }

        // lookup player
        UUID uuid = plugin.getUUIDForPlayer(args[0]);
        if (uuid == null) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND_MESSAGE);
            return;
        }

        // pardon player
        plugin.pardonPlayer(uuid, sender.getName());
        sender.sendMessage(Messages.PLAYER_PARDONED_MESSAGE);
    }

    private void onBantime(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tempans.bantime")) {
            sender.sendMessage(Messages.NO_PERMISSIONS_MESSAGE);
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(Messages.BANTIME_USAGE_MESSAGE);
            return;
        }

        // lookup player
        UUID uuid = plugin.getUUIDForPlayer(args[0]);
        if (uuid == null) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND_MESSAGE);
            return;
        }

        // send ban info
        sender.sendMessage(Messages.getBanInfoMessage(plugin.getBanEntry(uuid)));
    }

    private void onReload(CommandSender sender) {
        if (!sender.hasPermission("tempans.reload")) {
            sender.sendMessage(Messages.NO_PERMISSIONS_MESSAGE);
            return;
        }

        plugin.onDisable();
        plugin.onEnable();
        sender.sendMessage(Messages.RELOADED_MESSAGE);
        plugin.getLogger().info(Messages.RELOADED_LOG);
    }

    private void onKick(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tempans.kick")) {
            sender.sendMessage(Messages.NO_PERMISSIONS_MESSAGE);
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(Messages.KICK_USAGE_MESSAGE);
            return;
        }

        // lookup player
        UUID uuid = plugin.getUUIDForPlayer(args[0]);
        if (uuid == null) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND_MESSAGE);
            return;
        }

        // combine remaining arguments into reason
        String reason = BanEntry.UNSPECIFIED_REASON;
        if (args.length >= 2) {
            StringBuilder builder = new StringBuilder();
            for (int idx = 1; idx < args.length; idx++) {
                if (idx != 1) {
                    builder.append(' ');
                }
                builder.append(args[idx]);
            }
            reason = builder.toString();
        }

        // kick player
        plugin.kickPlayer(uuid, Messages.getKickedMessage(reason), sender.getName());
        sender.sendMessage(Messages.KICK_MESSAGE);
    }
}
