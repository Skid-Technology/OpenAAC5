package me.konsolas.aac.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.PlayerAnalysisReport;
import me.konsolas.aac.analysis.RiskLevel;
import me.konsolas.aac.api.AACExemption;
import me.konsolas.aac.util.chat.ChatMessages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.StringUtil;

public class AACCommandExecutor implements TabExecutor {

    private final AAC plugin;

    private final ChatColor ACCENT_COLOR;

    private final Map<CommandSender, MitigationMode> alertModes = new WeakHashMap();

    private final Map spectateLocations = new WeakHashMap();

    private static final List SUBCOMMANDS = Arrays.asList("help", "status", "exempt", "spec", "spectate", "check", "reset", "debug", "reload");

    private static final List ALERT_MODES = Arrays.asList("normal", "reduced", "off");

    public AACCommandExecutor(AAC plugin) {
        this.plugin = plugin;
        String string = plugin.getConfig().getString("aac_prefix_colour");
        if (string != null && string.length() == 1 && "0123456789abcdef".contains(string)) {
            this.ACCENT_COLOR = ChatColor.getByChar(string.charAt(0));
        } else {
            plugin.getLogger().warning("Invalid colour code " + string + ", defaulting to 'a'");
            this.ACCENT_COLOR = ChatColor.GREEN;
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String text, String[] texts) {
        if (texts.length == 0) {
            BaseComponent baseComponent = this.getBaseComponent();
            baseComponent.addExtra("AAC " + AAC.class.getPackage().getImplementationVersion() + " - ");
            TextComponent textComponent = new TextComponent("https://www.spigotmc.org/resources/6442/");
            textComponent.setClickEvent(new ClickEvent(Action.OPEN_URL, "https://www.spigotmc.org/resources/6442/"));
            baseComponent.addExtra(textComponent);
            ChatMessages.send(sender, baseComponent);
            return false;
        } else {
            String text2 = texts[0].toLowerCase();
            switch(text2) {
                case "help":
                    if (sender.hasPermission("aac.status")) {
                        BaseComponent baseComponent2 = this.getBaseComponent();
                        baseComponent2.addExtra("/aac status - list online players");
                        ChatMessages.send(sender, baseComponent2);
                        BaseComponent baseComponent3 = this.getBaseComponent();
                        baseComponent3.addExtra("/aac status <player> - view latest analysis for <player>");
                        ChatMessages.send(sender, baseComponent3);
                    }
                    if (sender.hasPermission("aac.exempt")) {
                        BaseComponent baseComponent4 = this.getBaseComponent();
                        baseComponent4.addExtra("/aac exempt <player> - exempt/unexempt <player> from AAC checking");
                        ChatMessages.send(sender, baseComponent4);
                    }
                    if (sender.hasPermission("aac.spectate")) {
                        BaseComponent baseComponent5 = this.getBaseComponent();
                        baseComponent5.addExtra("/aac spectate <player> - switch modes and teleport to <player>");
                        ChatMessages.send(sender, baseComponent5);
                        BaseComponent baseComponent6 = this.getBaseComponent();
                        baseComponent6.addExtra("/aac spectate - teleport back after spectating a player");
                        ChatMessages.send(sender, baseComponent6);
                    }
                    if (sender.hasPermission("aac.sudo")) {
                        BaseComponent baseComponent7 = this.getBaseComponent();
                        baseComponent7.addExtra("/aac sudo <player> <command> - force <player> to execute <command>");
                        ChatMessages.send(sender, baseComponent7);
                    }
                    if (sender.hasPermission("aac.check")) {
                        BaseComponent baseComponent8 = this.getBaseComponent();
                        baseComponent8.addExtra("/aac check <player> - force AAC to analyse <player>");
                        ChatMessages.send(sender, baseComponent8);
                    }
                    if (sender.hasPermission("aac.reset")) {
                        BaseComponent baseComponent9 = this.getBaseComponent();
                        baseComponent9.addExtra("/aac reset <player> - clear AAC's database entries for <player>");
                        ChatMessages.send(sender, baseComponent9);
                    }
                    if (sender.hasPermission("aac.alert")) {
                        BaseComponent baseComponent10 = this.getBaseComponent();
                        baseComponent10.addExtra("/aac alerts <off/normal/reduced> - set your alert level (reduced = only severe alerts)");
                        ChatMessages.send(sender, baseComponent10);
                        BaseComponent baseComponent11 = this.getBaseComponent();
                        baseComponent11.addExtra("/aac alerts - check your current alert level");
                        ChatMessages.send(sender, baseComponent11);
                    }
                    if (sender.hasPermission("aac.debug")) {
                        BaseComponent baseComponent12 = this.getBaseComponent();
                        baseComponent12.addExtra("/aac debug [players...] - toggle your debug output for [players...]");
                        ChatMessages.send(sender, baseComponent12);
                    }
                    if (sender.hasPermission("aac.reload")) {
                        BaseComponent baseComponent13 = this.getBaseComponent();
                        baseComponent13.addExtra("/aac reload - reload AAC's checks, API hooks, and configuration file");
                        ChatMessages.send(sender, baseComponent13);
                    }
                    return true;
                case "status":
                    if (!sender.hasPermission("aac.status")) {
                        sender.sendMessage("/aac status missing permission node aac.status");
                        return false;
                    }
                    if (texts.length > 1) {
                        for (int index = 1; index < texts.length; index++) {
                            OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(texts[index]);
                            CompletableFuture<PlayerAnalysisReport> completableFuture;
                            if (this.plugin.getAnalysisManager().getReport(offlinePlayer) == null) {
                                completableFuture = this.plugin.getAnalysisManager().analyzeAsync(offlinePlayer);
                            } else {
                                completableFuture = CompletableFuture.completedFuture(this.plugin.getAnalysisManager().getReport(offlinePlayer));
                            }
                            completableFuture.whenComplete((result, result2) -> {
                                this.updateCommandSender(sender, offlinePlayer);
                                if (!offlinePlayer.isOnline() || !this.plugin.getPacketDispatcher().getExemptions((Player) offlinePlayer).isEmpty()) {
                                    TextComponent textComponent2 = new TextComponent(" This player is currently exempt from AAC checking");
                                    textComponent2.setColor(ChatColor.DARK_AQUA);
                                    ChatMessages.send(sender, textComponent2);
                                    if (offlinePlayer.isOnline()) {
                                        for (AACExemption aACExemption : this.plugin.getPacketDispatcher().getExemptions((Player) offlinePlayer)) {
                                            TextComponent textComponent3 = new TextComponent("  - ");
                                            TextComponent textComponent4 = new TextComponent(aACExemption.getReason());
                                            textComponent4.setColor(ChatColor.GRAY);
                                            textComponent3.addExtra(textComponent4);
                                            ChatMessages.send(sender, textComponent3);
                                        }
                                    } else {
                                        TextComponent textComponent5 = new TextComponent("  - ");
                                        TextComponent textComponent6 = new TextComponent("The player is currently offline");
                                        textComponent6.setColor(ChatColor.GRAY);
                                        textComponent5.addExtra(textComponent6);
                                        ChatMessages.send(sender, textComponent5);
                                    }
                                }
                                for (BaseComponent baseComponent16 : this.plugin.getAnalysisManager().getReport(offlinePlayer).toChatComponents()) {
                                    ChatMessages.send(sender, baseComponent16);
                                }
                            });
                        }
                    } else {
                        BaseComponent baseComponent17 = this.getBaseComponent();
                        ArrayList<Player> values = new ArrayList(Bukkit.getOnlinePlayers());
                        values.sort(Comparator.<Player>comparingDouble(result3 -> this.plugin.getAnalysisManager().getReport(result3).getScore()).reversed());
                        for (Player player : values) {
                            baseComponent17.addExtra(this.findBaseComponent(player));
                            baseComponent17.addExtra(" ");
                        }
                        ChatMessages.send(sender, baseComponent17);
                    }
                    return true;
                case "exempt":
                    if (!sender.hasPermission("aac.exempt")) {
                        sender.sendMessage("/aac exempt missing permission node aac.exempt");
                        return false;
                    } else if (texts.length <= 1) {
                        sender.sendMessage("/aac exempt <player> [players...] missing player argument");
                        return false;
                    } else {
                        for (int index2 = 1; index2 < texts.length; index2++) {
                            Player player2 = this.plugin.getServer().getPlayer(texts[index2]);
                            if (player2 == null) {
                                sender.sendMessage("/aac exempt unknown player " + texts[index2]);
                            } else {
                                BaseComponent baseComponent18 = this.getBaseComponent();
                                if (this.plugin.toggleExemption(player2)) {
                                    baseComponent18.addExtra("Added command exemption to ");
                                } else {
                                    baseComponent18.addExtra("Removed command exemption from ");
                                }
                                baseComponent18.addExtra(this.findBaseComponent(player2));
                                ChatMessages.send(sender, baseComponent18);
                            }
                        }
                        return true;
                    }
                case "spectate":
                case "spec":
                    if (!sender.hasPermission("aac.spectate")) {
                        sender.sendMessage("/aac spectate missing permission node aac.spectate");
                        return false;
                    } else if (!(sender instanceof Player)) {
                        sender.sendMessage("/aac spectate sender must be a player");
                        return false;
                    } else if (texts.length > 2) {
                        sender.sendMessage("/aac spectate <player> too many arguments");
                        return false;
                    } else {
                        Player player3 = (Player) sender;
                        if (texts.length >= 2) {
                            Player player4 = this.plugin.getServer().getPlayer(texts[1]);
                            if (player4 == null) {
                                sender.sendMessage("/aac spectate unknown player " + texts[1]);
                                return false;
                            }
                            for (String text3 : this.plugin.getConfig().getStringList("pre_spectate")) {
                                this.plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), this.plugin.resolvePlaceholders(player3, text3));
                            }
                            this.spectateLocations.putIfAbsent(player3, player3.getLocation());
                            player3.teleport(player4, TeleportCause.COMMAND);
                            BaseComponent baseComponent19 = this.getBaseComponent();
                            baseComponent19.addExtra("Teleported to ");
                            baseComponent19.addExtra(this.findBaseComponent(player4));
                            baseComponent19.addExtra(" - ");
                            baseComponent19.addExtra(this.findBaseComponent(player3));
                            ChatMessages.send(sender, baseComponent19);
                            return true;
                        } else {
                            Location location = (Location) this.spectateLocations.get(player3);
                            if (location == null) {
                                sender.sendMessage("/aac spectate missing location to go back to");
                                return false;
                            }
                            for (String text4 : this.plugin.getConfig().getStringList("post_spectate")) {
                                this.plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), this.plugin.resolvePlaceholders(player3, text4));
                            }
                            player3.teleport(location, TeleportCause.COMMAND);
                            this.spectateLocations.remove(player3);
                            BaseComponent baseComponent20 = this.getBaseComponent();
                            baseComponent20.addExtra("Teleported back");
                            ChatMessages.send(sender, baseComponent20);
                            return true;
                        }
                    }
                case "sudo":
                    if (!sender.hasPermission("aac.sudo")) {
                        sender.sendMessage("/aac sudo missing permission node aac.sudo");
                        return false;
                    } else if (texts.length <= 1) {
                        sender.sendMessage("/aac sudo <player> <command> [args...] missing player");
                        return false;
                    } else if (texts.length <= 2) {
                        sender.sendMessage("/aac sudo <player> <command> [args...] missing command");
                        return false;
                    } else {
                        Player player5 = this.plugin.getServer().getPlayer(texts[1]);
                        if (player5 == null) {
                            sender.sendMessage("/aac sudo unknown player " + texts[1]);
                            return false;
                        }
                        String text5 = Arrays.stream(texts).skip(2L).collect(Collectors.joining(" "));
                        BaseComponent baseComponent21 = this.getBaseComponent();
                        baseComponent21.addExtra("/" + text5);
                        ChatMessages.send(player5, baseComponent21);
                        BaseComponent baseComponent22 = this.getBaseComponent();
                        baseComponent22.addExtra(this.findBaseComponent(player5));
                        baseComponent22.addExtra(" - /" + text5);
                        ChatMessages.send(sender, baseComponent22);
                        this.plugin.getServer().dispatchCommand(player5, text5);
                        return true;
                    }
                case "reset":
                    if (!sender.hasPermission("aac.reset")) {
                        sender.sendMessage("/aac reset missing permission node aac.reset");
                        return false;
                    } else {
                        if (texts.length <= 1) {
                            sender.sendMessage("/aac reset <player> [players...] missing player argument");
                            return false;
                        }
                        for (int index3 = 1; index3 < texts.length; index3++) {
                            OfflinePlayer offlinePlayer2 = this.plugin.getServer().getOfflinePlayer(texts[index3]);
                            this.plugin.getDatabase().resetPlayerAsync(offlinePlayer2).thenCompose(result4 -> this.plugin.getAnalysisManager().analyzeAsync(offlinePlayer2).thenApply(result5 -> result4)).whenComplete((result, result2) -> {
                                BaseComponent baseComponent23 = this.getBaseComponent();
                                if (result2 == null) {
                                    TextComponent textComponent7 = new TextComponent(Integer.toString(result));
                                    textComponent7.setColor(ChatColor.GRAY);
                                    baseComponent23.addExtra("Cleared ");
                                    baseComponent23.addExtra(textComponent7);
                                    baseComponent23.addExtra(" entries for ");
                                    baseComponent23.addExtra(this.findBaseComponent(offlinePlayer2));
                                    baseComponent23.addExtra(" from database");
                                    ChatMessages.send(sender, baseComponent23);
                                } else {
                                    baseComponent23.addExtra("Failed to reset ");
                                    baseComponent23.addExtra(this.findBaseComponent(offlinePlayer2));
                                    result2.printStackTrace();
                                }
                            });
                        }
                        return true;
                    }
                case "check":
                    if (!sender.hasPermission("aac.check")) {
                        sender.sendMessage("/aac check missing permission node aac.check");
                        return false;
                    } else {
                        if (texts.length <= 1) {
                            sender.sendMessage("/aac check <player> [players...] missing player argument");
                            return false;
                        }
                        for (int index4 = 1; index4 < texts.length; index4++) {
                            OfflinePlayer offlinePlayer3 = this.plugin.getServer().getOfflinePlayer(texts[index4]);
                            this.plugin.getAnalysisManager().analyzeAsync(offlinePlayer3).whenComplete((result, result2) -> {
                                BaseComponent baseComponent23 = this.getBaseComponent();
                                if (result2 == null) {
                                    baseComponent23.addExtra("Report ready for ");
                                    baseComponent23.addExtra(this.findBaseComponent(offlinePlayer3));
                                    ChatMessages.send(sender, baseComponent23);
                                } else {
                                    baseComponent23.addExtra("Failed to generate report for ");
                                    baseComponent23.addExtra(this.findBaseComponent(offlinePlayer3));
                                    result2.printStackTrace();
                                }
                            });
                        }
                        return true;
                    }
                case "alerts":
                    if (!sender.hasPermission("aac.alerts")) {
                        sender.sendMessage("/aac alerts missing permission node aac.alerts");
                        return false;
                    } else if (texts.length > 2) {
                        sender.sendMessage("/aac alerts <off/normal/reduced> too many arguments");
                        return false;
                    } else {
                        if (texts.length == 1) {
                            BaseComponent baseComponent24 = this.getBaseComponent();
                            baseComponent24.addExtra("Your alert level is " + this.alertModes.getOrDefault(sender, MitigationMode.NORMAL));
                            ChatMessages.send(sender, baseComponent24);
                        } else {
                            String text6 = texts[1].toLowerCase();
                            switch(text6) {
                                case "off":
                                    this.alertModes.put(sender, MitigationMode.OFF);
                                    break;
                                case "normal":
                                    this.alertModes.remove(sender);
                                    break;
                                case "reduced":
                                    this.alertModes.put(sender, MitigationMode.REDUCED);
                                    break;
                                default:
                                    sender.sendMessage("/aac alerts <off/normal/reduced> invalid alert level");
                                    return false;
                            }
                            BaseComponent baseComponent25 = this.getBaseComponent();
                            baseComponent25.addExtra("Alert level updated to " + this.alertModes.getOrDefault(sender, MitigationMode.NORMAL));
                            ChatMessages.send(sender, baseComponent25);
                        }
                        return true;
                    }
                case "debug":
                    if (!sender.hasPermission("aac.debug")) {
                        sender.sendMessage("/aac debug missing permission node aac.debug");
                        return false;
                    }
                    if (texts.length == 1) {
                        this.plugin.getDebugManager().updateCommandSender(sender);
                    } else {
                        for (int index5 = 1; index5 < texts.length; index5++) {
                            OfflinePlayer offlinePlayer4 = this.plugin.getServer().getOfflinePlayer(texts[index5]);
                            this.plugin.getDebugManager().updateCommandSender(sender, offlinePlayer4);
                        }
                    }
                    BaseComponent baseComponent26 = this.getBaseComponent();
                    baseComponent26.addExtra("Currently debugging:");
                    if (this.plugin.getDebugManager().checkDebugManager(sender)) {
                        baseComponent26.addExtra(" ");
                        TextComponent textComponent8 = new TextComponent("[global]");
                        textComponent8.setColor(ChatColor.GRAY);
                        baseComponent26.addExtra(textComponent8);
                    } else if (!this.plugin.getDebugManager().findSet(sender).isEmpty()) {
                        for (OfflinePlayer offlinePlayer5 : this.plugin.getDebugManager().findSet(sender)) {
                            baseComponent26.addExtra(" ");
                            baseComponent26.addExtra(this.findBaseComponent(offlinePlayer5));
                        }
                    } else {
                        baseComponent26.addExtra(" ");
                        TextComponent textComponent9 = new TextComponent("[none]");
                        textComponent9.setColor(ChatColor.GRAY);
                        baseComponent26.addExtra(textComponent9);
                    }
                    ChatMessages.send(sender, baseComponent26);
                    return true;
                case "reload":
                    if (!sender.hasPermission("aac.reload")) {
                        sender.sendMessage("/aac reload missing permission node aac.reload");
                        return false;
                    }
                    this.plugin.getLogger().info("Reload requested by " + sender.getName());
                    Bukkit.getServer().getPluginManager().disablePlugin(this.plugin);
                    Bukkit.getServer().getPluginManager().enablePlugin(this.plugin);
                    BaseComponent baseComponent27 = this.getBaseComponent();
                    baseComponent27.addExtra("Reload complete");
                    ChatMessages.send(sender, baseComponent27);
                    return true;
                default:
                    return false;
            }
        }
    }

    public void updatePlayer(OfflinePlayer offlinePlayer) {
        RiskLevel riskLevel = this.plugin.getAnalysisManager().getReport(offlinePlayer).getRiskLevel();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("aac.alerts") && this.alertModes.getOrDefault(player, MitigationMode.NORMAL).checkMitigationMode(riskLevel)) {
                this.updateCommandSender(player, offlinePlayer);
            }
        }
        if (Bukkit.getConsoleSender().hasPermission("aac.alerts") && this.alertModes.getOrDefault(Bukkit.getConsoleSender(), MitigationMode.NORMAL).checkMitigationMode(riskLevel)) {
            ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
            this.updateCommandSender(consoleSender, offlinePlayer);
        }
    }

    private void updateCommandSender(CommandSender sender, OfflinePlayer offlinePlayer) {
        BaseComponent baseComponent = this.getBaseComponent();
        baseComponent.addExtra(this.findBaseComponent(offlinePlayer));
        if (offlinePlayer.isOnline()) {
            baseComponent.addExtra(" - ");
            Player player = (Player) offlinePlayer;
            baseComponent.addExtra(this.findBaseComponent(player, sender));
        }
        ChatMessages.send(sender, baseComponent);
    }

    private BaseComponent getBaseComponent() {
        TextComponent textComponent = new TextComponent(" ");
        TextComponent textComponent2 = new TextComponent("AAC ");
        textComponent2.setColor(this.ACCENT_COLOR);
        textComponent2.setBold(true);
        textComponent.addExtra(textComponent2);
        return textComponent;
    }

    private BaseComponent findBaseComponent(OfflinePlayer offlinePlayer) {
        TextComponent textComponent = new TextComponent(offlinePlayer.getName());
        textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/aac status " + offlinePlayer.getName()));
        textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { new TextComponent("/aac status " + offlinePlayer.getName()) }));
        textComponent.setBold(false);
        PlayerAnalysisReport report = this.plugin.getAnalysisManager().getReport(offlinePlayer);
        if (report != null) {
            report.getRiskLevel().updateBaseComponent(textComponent);
        } else {
            RiskLevel.NEUTRAL.updateBaseComponent(textComponent);
        }
        if (!offlinePlayer.isOnline() || !this.plugin.getPacketDispatcher().getExemptions((Player) offlinePlayer).isEmpty()) {
            RiskLevel.IGNORED.updateBaseComponent(textComponent);
        }
        return textComponent;
    }

    private BaseComponent findBaseComponent(Player player, CommandSender sender) {
        TextComponent textComponent = new TextComponent("Spectate");
        textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/aac spec " + player.getName()));
        textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { new TextComponent("/aac spec " + player.getName()) }));
        if (!(sender instanceof Player) || !sender.hasPermission("aac.spectate")) {
            textComponent.setStrikethrough(true);
        }
        return textComponent;
    }

    private BaseComponent createBackLink(CommandSender sender) {
        TextComponent textComponent = new TextComponent("Go Back");
        textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/aac spec"));
        textComponent.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { new TextComponent("/aac spec") }));
        if (!(sender instanceof Player) || !sender.hasPermission("aac.spectate")) {
            textComponent.setStrikethrough(true);
        }
        return textComponent;
    }

    public List onTabComplete(CommandSender sender, Command command, String text, String[] texts) {
        ArrayList values = new ArrayList();
        if (texts.length == 0) {
            return SUBCOMMANDS;
        } else if (texts.length == 1) {
            StringUtil.copyPartialMatches(texts[0], SUBCOMMANDS, values);
            return values;
        } else if ("alerts".equals(texts[0])) {
            StringUtil.copyPartialMatches(texts[0], ALERT_MODES, values);
            return values;
        } else {
            return null;
        }
    }
}
