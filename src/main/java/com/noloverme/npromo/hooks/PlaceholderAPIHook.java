package com.noloverme.npromo.hooks;

import com.noloverme.npromo.NPromo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final NPromo plugin;

    public PlaceholderAPIHook(NPromo plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ncodes";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Noloverme";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.startsWith("activations_")) {
            String code = params.substring("activations_".length());
            if (plugin.getCodes().isConfigurationSection(code)) {
                return String.valueOf(plugin.getDatabase().getCodeActivations(code));
            } else {
                return "0";
            }
        }
        return null;
    }
}
