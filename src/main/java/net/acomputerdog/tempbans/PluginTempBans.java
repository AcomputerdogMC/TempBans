package net.acomputerdog.tempbans;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Plugin main class
 */
public class PluginTempBans extends JavaPlugin implements Listener {

    /**
     * File that stores record of bans
     */
    private File bansFile;

    /**
     * Map of UUIDs to ban durations
     * TODO database
     */
    private Map<UUID, BanEntry> banMap;

    /**
     * Command handler class
     */
    private TempBansCommandHandler commandHandler;

    @Override
    public void onEnable() {
        try {
            // create data directory
            if (!getDataFolder().isDirectory() && getDataFolder().mkdir()) {
                getLogger().warning(Messages.CREATE_FOLDER_FAILED_LOG);
            }
            bansFile = new File(getDataFolder(), "bans.lst");
            banMap = new HashMap<>();
            commandHandler = new TempBansCommandHandler(this);

            loadBans();
            getServer().getPluginManager().registerEvents(this, this);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, Messages.LOAD_FAILED_LOG, e);
            getServer().getPluginManager().disablePlugin(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, Messages.STARTUP_EXCEPTION_LOG, e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        bansFile = null;
        banMap = null;
        commandHandler = null;

        HandlerList.unregisterAll((JavaPlugin) this);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        BanEntry ban = banMap.get(uuid);
        if (ban != null) {
            if (ban.isActive()) {
                e.disallow(PlayerLoginEvent.Result.KICK_BANNED, Messages.getBannedMessage(ban));
            } else {
                banMap.remove(ban.getPlayerUUID());
                getLogger().info(() -> Messages.getBanExpiredLog(uuid));
                e.getPlayer().sendMessage(Messages.BAN_EXPIRED_MESSAGE);
                saveBans();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        commandHandler.onCommand(sender, command, args);
        return true;
    }

    /**
     * Gets the UUID of a player name or UUID.
     * <p>
     * If a name is given, then the server will be queried to look up
     * the UUID for that player.
     * <p>
     * If a UUID is given, then it is converted into an object.
     * <p>
     * If a player could not be found or the UUID is invalid, then null is
     * returned
     *
     * @param id The player name or UUID string
     * @return return a UUID object
     */
    public UUID getUUIDForPlayer(String id) {
        Player p = getServer().getPlayer(id);
        if (p != null) {
            return p.getUniqueId();
        } else {
            try {
                return UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public void kickPlayer(UUID id, String reason, String source) {
        Player p = getServer().getPlayer(id);
        if (p != null) {
            p.kickPlayer(reason);
        }
        getLogger().info(() -> Messages.getKickLog(id, reason, source));
    }

    public void banPlayer(UUID id, Date endTime, String reason, String source) {
        BanEntry ban = new BanEntry(id, endTime, reason);
        banMap.put(id, ban);

        kickPlayer(id, reason, source);
        saveBans();
        getLogger().info(() -> Messages.getBanLog(ban, source));
    }

    public void pardonPlayer(UUID id, String source) {
        banMap.remove(id);
        saveBans();
        getLogger().info(() -> Messages.getPardonLog(id, source));
    }

    private void loadBans() throws IOException {
        if (bansFile.isFile()) {
            try (Stream<String> lines = Files.lines(bansFile.toPath())) {
                lines.forEach(this::loadBanEntry);
            }
        } else {
            getLogger().warning(Messages.BAN_FILE_MISSING_LOG);
        }
    }

    private void loadBanEntry(String line) {
        try {
            BanEntry ban = BanEntry.parse(line);
            banMap.put(ban.getPlayerUUID(), ban);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.SEVERE, Messages.getLoadBanFailedLog(line), e);
        }
    }

    private void saveBans() {
        try (Stream<String> stream = banMap.values().stream().map(Object::toString)) {
            Files.write(bansFile.toPath(), (Iterable<String>) stream::iterator);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, Messages.SAVE_FAILED_LOG, e);
        }
    }

    public BanEntry getBanEntry(UUID uuid) {
        return banMap.get(uuid);
    }
}
