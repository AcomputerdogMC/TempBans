package net.acomputerdog.tempbans;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PluginTempBans extends JavaPlugin implements Listener {
    private static final long EXPIRATION_NEVER = 0L;

    private File bansFile;
    private Map<String, Long> banMap;
    private Map<String, String> reasonMap;
    private Date sharedDate;

    //don't reset during onEnable or onDisable
    private boolean reloading = false;

    @Override
    public void onEnable() {
        try {
            if (!getDataFolder().isDirectory()) {
                getDataFolder().mkdir();
            }
            bansFile = new File(getDataFolder(), "bans.lst");
            banMap = new HashMap<>();
            reasonMap = new HashMap<>();
            sharedDate = new Date();
            loadBans();
            if (!reloading) {
                getServer().getPluginManager().registerEvents(this, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        bansFile = null;
        banMap = null;
        reasonMap = null;
        sharedDate = null;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        String id = e.getPlayer().getUniqueId().toString().toLowerCase();
        if (banMap.containsKey(id)) {
            if (isBanned(id)) {
                e.disallow(PlayerLoginEvent.Result.KICK_BANNED, getBanMessage(id));
            } else {
                banMap.remove(id);
                reasonMap.remove(id);
                getLogger().info("Player [" + id + "]'s ban expired.");
                saveBans();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "ban" :
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
                sender.sendMessage(ChatColor.RED + "Unknown command!");
                getLogger().warning("Unknown command: " + command.getName());
                break;
        }
        return true;
    }

    private void onBan(CommandSender sender, String[] args) {
        if (sender.hasPermission("tempbans.ban")) {
            if (args.length >= 1) {
                String p = getPlayer(args[0]);
                if (p != null) {
                    try {
                        String reason = "unspecified";
                        long expires = EXPIRATION_NEVER;
                        if (args.length >= 2) {
                            long delay = Long.parseLong(args[1]) * 60 * 1000; //delay in minutes
                            expires = System.currentTimeMillis() + delay;
                        }
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
                        banPlayer(sender, p, expires, reason);
                        sender.sendMessage(ChatColor.YELLOW + "Player has been banned.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid expiration date!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "That player could not be found!  If they are offline, make sure you specify UUID instead of name.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Incorrect usage, use /ban <name | uuid> [duration] [reason]");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onPardon(CommandSender sender, String[] args) {
        if (sender.hasPermission("tempbans.pardon")) {
            if (args.length >= 1) {
                String p = getPlayer(args[0]);
                if (p != null) {
                    pardonPlayer(sender, p);
                    sender.sendMessage(ChatColor.YELLOW + "Player pardoned.");
                } else {
                    sender.sendMessage(ChatColor.RED + "That player could not be found!  If they are offline, make sure you specify UUID instead of name.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Incorrect usage, use /ban <name | uuid> [duration] [reason]");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onBantime(CommandSender sender, String[] args) {
        if (sender.hasPermission("tempbans.bantime")) {
            if (args.length >= 1) {
                String p = getPlayer(args[0]);
                if (p != null) {
                    sender.sendMessage(ChatColor.YELLOW + getBanInfo(p));
                } else {
                    sender.sendMessage(ChatColor.RED + "That player could not be found!  If they are offline, make sure you specify UUID instead of name.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Incorrect usage, use /ban <name | uuid> [duration] [reason]");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onReload(CommandSender sender) {
        reloading = true;
        onDisable();
        onEnable();
        reloading = false;
        sender.sendMessage(ChatColor.YELLOW + "TempBans reloaded.");
        getLogger().info("Reloaded.");
    }

    private void onKick(CommandSender sender, String[] args) {
        if (sender.hasPermission("tempbans.kick")) {
            if (args.length >= 1) {
                String p = getPlayer(args[0]);
                if (p != null) {
                    String reason = "unspecified";
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
                    kickPlayer(sender, p, "Kicked for: \"" + reason + "\"");
                    sender.sendMessage(ChatColor.YELLOW + "Player has been kicked.");
                } else {
                    sender.sendMessage(ChatColor.RED + "That player could not be found!  If they are offline, make sure you specify UUID instead of name.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Incorrect usage, use /kick <name | uuid> [reason]");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private String getPlayer(String id) {
        try {
            try {
                UUID.fromString(id); //make sure UUID is valid
                return id.toLowerCase();
            } catch (IllegalArgumentException e) {
                return null;
            }
        } catch (Exception e) {
            Player p = getServer().getPlayer(id);
            if (p != null) {
                return p.getUniqueId().toString().toLowerCase();
            } else {
                return null;
            }
        }
    }

    private void kickPlayer(CommandSender sender, String id, String reason) {
        Player p = getServer().getPlayer(UUID.fromString(id));
        if (p != null) {
            p.kickPlayer(reason);
        }
        getLogger().info("Player [" + id + "] is kicked by [" + sender.getName() + "] for [" + reason + "].");
    }

    private void banPlayer(CommandSender sender, String id, long endTime, String reason) {
        banMap.put(id, endTime);
        reasonMap.put(id, reason);
        kickPlayer(sender, id, reason);
        saveBans();
        getLogger().info("Player [" + id + "] is banned by [" + sender.getName() + "] until [" + formatTime(endTime) + "] for [" + reason +"].");
    }

    private void pardonPlayer(CommandSender sender, String id) {
        banMap.remove(id);
        reasonMap.remove(id);
        saveBans();
        getLogger().info("Player [" + id + "] is pardoned by [" + sender.getName() +"].");
    }

    private String getBanInfo(String id) {
        StringBuilder builder = new StringBuilder();
        Long end = banMap.get(id);
        if (end != null) {
            builder.append("Ban information for ").append(id).append(":\n");
            builder.append("Ban end: ").append(end == EXPIRATION_NEVER ? "never" : formatTime(end)).append('\n');
            builder.append("Ban reason: ").append(reasonMap.get(id));
        } else {
            builder.append("That player is not currently banned.");
        }
        return builder.toString();
    }

    private void loadBans() {
        if (bansFile.isFile()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(bansFile));
                while (reader.ready()) {
                    try {
                        String line = reader.readLine();
                        String[] parts = line.split("§");
                        if (parts.length >= 3) {
                            if (parts.length > 3) {
                                getLogger().warning("Malformed line: \"" + line + "\"");
                            }
                            String id = parts[0];
                            Long end = Long.parseLong(parts[1]);
                            String message = parts[2];
                            banMap.put(id, end);
                            reasonMap.put(id, message);
                        }
                    } catch (Exception e) {
                        getLogger().warning("Exception loading ban list entry: ");
                    }
                }
            } catch (IOException e) {
                getLogger().severe("Exception loading ban list!  Banned players may be able to access the server!");
                throw new RuntimeException("Exception loading ban list!", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored){}
                }
            }
        }
    }

    private void saveBans() {
        Writer writer = null;
        try {
            writer = new FileWriter(bansFile);
            for (String id : banMap.keySet()) {
                writer.write(id);
                writer.write("§");
                writer.write(String.valueOf(banMap.get(id)));
                writer.write("§");
                writer.write(reasonMap.get(id));
                writer.write("\n");
            }
        } catch (IOException e) {
            getLogger().severe("Exception saving bans!");
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored){}
            }
        }
    }

    private String getBanMessage(String id) {
        Long end = banMap.get(id);
        if (end != null) {
            return "You are banned until [" + (end == 0 ? "forever" : formatTime(end)) + "] for [" + reasonMap.get(id) + "].";
        } else {
            return "You are not banned.";
        }
    }

    private String formatTime(long time) {
        sharedDate.setTime(time);
        return sharedDate.toString();
    }

    private boolean isBanned(String id) {
        Long end = banMap.get(id);
        if (end != null) {
            if (end == EXPIRATION_NEVER || end > System.currentTimeMillis()) {
                return true;
            }
        }
        return false;
    }
}
