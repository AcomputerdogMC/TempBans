package net.acomputerdog.tempbans;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Data structure that records a ban for a player
 */
public class BanEntry {
    /**
     * Duration of time that indicates a ban that never expires
     */
    public static final long PERMANENT_BAN = 0L;

    public static final String UNSPECIFIED_REASON = "unspecified";

    private final UUID playerUUID;
    private final Date endTime;
    private final String message;

    public BanEntry(UUID playerUUID, Date endTime, String message) {
        this.playerUUID = playerUUID;
        this.endTime = endTime;
        this.message = message;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Date getEndTime() {
        return endTime;
    }

    public boolean isPermanent() {
        return endTime.getTime() == PERMANENT_BAN;
    }

    public String getEndTimeString() {
        if (isPermanent()) {
            return "forever";
        } else {
            return endTime.toString();
        }
    }

    public String getMessage() {
        return message;
    }

    public boolean isActive() {
        return isPermanent() || endTime.after(new Date());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BanEntry banEntry = (BanEntry) o;
        return endTime == banEntry.endTime &&
                Objects.equals(playerUUID, banEntry.playerUUID) &&
                Objects.equals(message, banEntry.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID, endTime, message);
    }

    @Override
    public String toString() {
        return playerUUID.toString().toLowerCase() + "§" + endTime.getTime() + "§" + message;
    }

    public static BanEntry parse(String line) {
        try {
            String[] parts = line.split("§");
            if (parts.length == 3) {
                String id = parts[0].toLowerCase();
                long end = Long.parseLong(parts[1]);
                String message = parts[2];

                return new BanEntry(UUID.fromString(id), new Date(end), message);
            } else {
                throw new IllegalArgumentException("Wrong number of sections");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed end time", e);
        }
    }
}
