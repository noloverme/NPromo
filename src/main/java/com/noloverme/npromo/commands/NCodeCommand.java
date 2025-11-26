package com.noloverme.npromo.commands;

import com.noloverme.npromo.NPromo;
import com.noloverme.npromo.managers.CodeManager;
import com.noloverme.npromo.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NCodeCommand implements CommandExecutor {

    private final NPromo plugin;

    public NCodeCommand(NPromo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            ChatUtil.sendMessage(sender, "must-be-player");
            return true;
        }

        if (args.length != 1) {
            ChatUtil.sendMessage(sender, "usage", "{usage}", "/ncode <code>");
            return true;
        }

        Player player = (Player) sender;
        String code = args[0];
        CodeManager codeManager = plugin.getCodeManager();

        codeManager.activateCode(player, code);
        
        return true;
    }
}
