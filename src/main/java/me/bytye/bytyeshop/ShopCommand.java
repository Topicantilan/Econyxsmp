package main.java.me.bytye.bytyeshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) return true;

        var config = Main.getPlugin(Main.class).getConfig();

        Inventory inv = Bukkit.createInventory(null,
                config.getInt("menu.size"),
                Utils.color(config.getString("menu.title")));

        for (String key : config.getConfigurationSection("categories").getKeys(false)) {

            var cat = config.getConfigurationSection("categories." + key);

            Material mat = Material.valueOf(cat.getString("material"));
            int slot = cat.getInt("slot");

            inv.setItem(slot, Utils.createItem(mat, cat.getString("name"), null));
        }

        p.openInventory(inv);
        return true;
    }
    }
