package main.java.me.bytye.bytyeshop;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;

import java.util.*;

public class ShopListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        var config = Main.getPlugin(Main.class).getConfig();

        // MAIN MENU
        if (title.equals(Utils.color(config.getString("menu.title")))) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;

            for (String key : config.getConfigurationSection("categories").getKeys(false)) {

                var cat = config.getConfigurationSection("categories." + key);

                if (e.getCurrentItem().getType() == Material.valueOf(cat.getString("material"))) {
                    openCategory(p, key);
                }
            }
        }

        // SHOP HANDLER
        for (String shopKey : config.getConfigurationSection("shops").getKeys(false)) {

            String shopTitle = Utils.color(config.getString("shops." + shopKey + ".title"));

            if (!title.equals(shopTitle)) continue;

            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;

            var items = config.getConfigurationSection("shops." + shopKey + ".items");

            for (String itemKey : items.getKeys(false)) {

                var item = items.getConfigurationSection(itemKey);

                Material mat = Material.valueOf(item.getString("material"));

                if (e.getCurrentItem().getType() != mat) continue;

                double buy = item.getDouble("buy");
                double sell = item.getDouble("sell");

                handleTransaction(p, mat, buy, sell, e);
            }
        }
    }

    private void openCategory(Player p, String key) {

        var config = Main.getPlugin(Main.class).getConfig();

        String title = Utils.color(config.getString("shops." + key + ".title"));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Fill glass
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, Utils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null));
        }

        var items = config.getConfigurationSection("shops." + key + ".items");

        for (String itemKey : items.getKeys(false)) {

            var item = items.getConfigurationSection(itemKey);

            Material mat = Material.valueOf(item.getString("material"));
            int slot = item.getInt("slot");

            double buy = item.getDouble("buy");
            double sell = item.getDouble("sell");

            List<String> lore = List.of(
                    "&7Buy x1: &a$" + buy,
                    "&7Buy x16: &a$" + (buy * 16),
                    "&7Buy x64: &a$" + (buy * 64),
                    "",
                    "&7Sell x1: &c$" + sell,
                    "",
                    "&eLeft = Buy 1",
                    "&eShift+Left = Buy 16",
                    "&eShift+Right = Buy 64",
                    "&eRight = Sell 1"
            );

            inv.setItem(slot, Utils.createItem(mat, "&f" + itemKey, lore));
        }

        p.openInventory(inv);
    }

    private void handleTransaction(Player p, Material mat, double buy, double sell, InventoryClickEvent e) {

        var econ = Main.getEconomy();
        int amount = 1;

        if (e.isShiftClick() && e.isLeftClick()) amount = 16;
        if (e.isShiftClick() && e.isRightClick()) amount = 64;

        double totalBuy = buy * amount;

        // BUY
        if (e.isLeftClick()) {
            if (econ.getBalance(p) >= totalBuy) {
                econ.withdrawPlayer(p, totalBuy);
                p.getInventory().addItem(new ItemStack(mat, amount));
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                p.sendMessage("§aBought x" + amount + " for $" + totalBuy);
            } else {
                p.sendMessage("§cNot enough money!");
            }
        }

        // SELL
        if (e.isRightClick() && !e.isShiftClick()) {
            if (p.getInventory().contains(mat)) {
                p.getInventory().removeItem(new ItemStack(mat, 1));
                econ.depositPlayer(p, sell);
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                p.sendMessage("§aSold for $" + sell);
            }
        }
    }
                    }
