package com.noloverme.npromo.commands;

import com.noloverme.npromo.NPromo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NCodeCommand implements CommandExecutor {

    private final NPromo plugin;

    public NCodeCommand(NPromo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            this.plugin.getChatUtil().sendMessage(sender, "must-be-player");
            return true;
        }

        if (args.length != 1) {
            this.plugin.getChatUtil().sendMessage(sender, "usage", "{usage}", "/ncode <code>");
            return true;
        }

        Player player = (Player) sender;
        String code = args[0];

        this.plugin.getCodeManager().activateCode(player, code);

        return true;
    }
}
