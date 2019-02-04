package net.acomputerdog.tempbans;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;

import java.util.UUID;

/**
 * Contains strings to use as chat messages and logs.  Fields and methods that end with "message" are intended
 * for use as chat messages, and those that end with "log" are intended for system logs.
 * <p>
 * Fields are constant strings, and methods will dynamically generate a formatted string based on provided data.
 */
public final class Messages {

    public static final String INVALID_EXPIRATION_DATE_MESSAGE = ChatColor.RED + "Invalid expiration date.";
    public static final String PLAYER_NOT_FOUND_MESSAGE = ChatColor.RED + "That player could not be found.  If they are offline, make sure you specify UUID instead of name.";
    public static final String INCORRECT_BAN_USAGE_MESSAGE = ChatColor.RED + "Incorrect usage, use /ban <name | uuid> [duration] [reason]";
    public static final String NO_PERMISSIONS_MESSAGE = ChatColor.RED + "You do not have permission.";
    public static final String PLAYER_BANNED_MESSAGE = ChatColor.YELLOW + "Player has been banned.";
    public static final String UNKNOWN_COMMAND_MESSAGE = ChatColor.RED + "Unknown command.  This is a bug, please report this to a server administrator.";
    public static final String PLAYER_PARDONED_MESSAGE = ChatColor.YELLOW + "Player pardoned.";
    public static final String PARDON_USAGE_MESSAGE = ChatColor.RED + "Incorrect usage, use /pardon <name | uuid>";
    public static final String BANTIME_USAGE_MESSAGE = ChatColor.RED + "Incorrect usage, use /bantime <name | uuid>";
    public static final String RELOADED_MESSAGE = ChatColor.YELLOW + "TempBans reloaded.";
    public static final String RELOADED_LOG = "Reloaded.";
    public static final String KICK_MESSAGE = ChatColor.YELLOW + "Player has been kicked.";
    public static final String KICK_USAGE_MESSAGE = ChatColor.RED + "Incorrect usage, use /kick <name | uuid> [reason]";
    public static final String LOAD_FAILED_LOG = "IO error loading ban list!  Banned players will be able to access the server!";
    public static final String STARTUP_EXCEPTION_LOG = "Unhandled while starting up";
    public static final String CREATE_FOLDER_FAILED_LOG = "Unable to create data directory!";
    public static final String BAN_EXPIRED_MESSAGE = ChatColor.YELLOW + "Your ban has expired.";
    public static final String BAN_FILE_MISSING_LOG = "Bans file does not exist";
    public static final String SAVE_FAILED_LOG = "Exception saving ban file, bans may be lost!";

    public static String getPardonLog(UUID id, String source) {
        return String.format("Player [%s] is pardoned by [%s].", id.toString(), source);
    }

    public static String getBanLog(BanEntry ban, String source) {
        return String.format("Player [%s] is banned by [%s] until [%s] for [%s].",
                ban.getPlayerUUID().toString(),
                source,
                ban.getEndTimeString(),
                ban.getMessage());
    }

    public static String getKickLog(UUID id, String reason, String source) {
        return String.format("Player [%s] is kicked by [%s] for [%s].", id.toString(), source, reason);
    }

    public static String getBannedMessage(BanEntry ban) {
        return String.format("You are banned until [%s] for [%s].", ban.getEndTimeString(), ban.getMessage());
    }

    public static String getBanExpiredLog(UUID id) {
        return String.format("Player [%s]'s ban expired.", id);
    }

    public static String getUnknownCommandLog(Command command) {
        return String.format("Unknown command: %s", command.getName());
    }

    public static String getKickedMessage(String reason) {
        return String.format("Kicked for: %s", reason);
    }

    public static String getBanInfoMessage(BanEntry ban) {
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.YELLOW);
        if (ban != null) {
            builder.append("Ban information for ").append(ban.getPlayerUUID().toString()).append(":\n");
            builder.append("Ban end: ").append(ban.getEndTimeString()).append('\n');
            builder.append("Ban reason: ").append(ban.getMessage());
        } else {
            builder.append("That player is not banned.");
        }
        return builder.toString();
    }

    public static String getLoadBanFailedLog(String line) {
        return String.format("Failed to load ban: %s", line);
    }

    private Messages() {
        // constant container class
    }
}
