package me.konsolas.aac.debug;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import me.konsolas.aac.AAC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugManager {

    private final AAC plugin;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final Map<UUID, Set<CommandSender>> debugSubscriptions = new ConcurrentHashMap();

    private CommandSender consoleSubscriber = null;

    public DebugManager(AAC plugin) {
        this.plugin = plugin;
    }

    public boolean isDebugging(Player player) {
        if (this.consoleSubscriber != null) {
            return true;
        } else {
            Set values = (Set) this.debugSubscriptions.get(player.getUniqueId());
            return values != null && !values.isEmpty();
        }
    }

    public void log(Player player, String text) {
        long now = System.currentTimeMillis();
        this.executor.execute(() -> this.writeLog(player, text, now));
    }

    private void writeLog(Player player, String text, long value) {
        Set<CommandSender> values = (Set) this.debugSubscriptions.get(player.getUniqueId());
        if (this.consoleSubscriber != null) {
            if (values == null) {
                values = Collections.singleton(this.consoleSubscriber);
            } else {
                values.add(this.consoleSubscriber);
            }
        }
        if (values != null) {
            String text2 = Long.toString(value);
            text = "" + ChatColor.DARK_GRAY + text2.substring(text2.length() - 4) + " " + ChatColor.DARK_GREEN + player.getName() + " " + ChatColor.GRAY + text;
            for (CommandSender sender : values) {
                sender.sendMessage(text);
            }
        }
    }

    public void updatePlayer(Player player, String text, Object[] results) {
        this.log(player, String.format(Locale.UK, text, results));
    }

    public void updateCommandSender(CommandSender sender) {
        if (this.consoleSubscriber == sender) {
            this.consoleSubscriber = null;
        } else {
            this.consoleSubscriber = sender;
        }
    }

    public boolean checkDebugManager(CommandSender sender) {
        return sender == this.consoleSubscriber;
    }

    public void updateCommandSender(CommandSender sender, OfflinePlayer offlinePlayer) {
        Set values = this.debugSubscriptions.computeIfAbsent(offlinePlayer.getUniqueId(), result -> new HashSet());
        if (values.remove(sender)) {
            if (values.isEmpty()) {
                this.debugSubscriptions.remove(offlinePlayer.getUniqueId());
            }
        } else {
            values.add(sender);
        }
    }

    public Set<OfflinePlayer> findSet(CommandSender sender) {
        HashSet values = new HashSet();
        for (Entry entry : (Iterable<Entry>) (Iterable<?>) (this.debugSubscriptions.entrySet())) {
            if (((Set) entry.getValue()).contains(sender)) {
                values.add(Bukkit.getOfflinePlayer((UUID) entry.getKey()));
            }
        }
        return values;
    }
}
