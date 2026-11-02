package me.konsolas.aac.analysis;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;

public enum RiskLevel {

    IGNORED(ChatColor.STRIKETHROUGH), NEUTRAL(ChatColor.GRAY), SUSPICIOUS(ChatColor.GOLD), HIGH(ChatColor.RED);

    private final ChatColor color;

    private RiskLevel(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public void updateBaseComponent(BaseComponent baseComponent) {
        switch(this) {
            case IGNORED:
                baseComponent.setStrikethrough(true);
                break;
            case NEUTRAL:
                baseComponent.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                break;
            case SUSPICIOUS:
                baseComponent.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                break;
            case HIGH:
                baseComponent.setColor(net.md_5.bungee.api.ChatColor.RED);
        }
    }
}
