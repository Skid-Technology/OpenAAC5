package me.konsolas.aac.util.chat;

import me.konsolas.aac.protocol.ProtocolVersion;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatMessages {

    public static void send(CommandSender sender, BaseComponent baseComponent) {
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(baseComponent);
        } else {
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version = ProtocolVersion.V1_9;
            if (serverVersion.isAtLeast(version)) {
                sender.spigot().sendMessage(baseComponent);
            } else {
                sender.sendMessage(baseComponent.toLegacyText());
            }
        }
    }
}
