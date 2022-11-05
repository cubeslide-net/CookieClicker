package net.cubeslide.cookieclicker.listeners;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.cubeslide.cookieclicker.CookieClicker;
import net.cubeslide.cookieclicker.utils.Database;
import net.cubeslide.cookieclicker.utils.ItemBuilder;
import net.cubeslide.cookieclicker.utils.NameFetcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListeners implements Listener {

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    final CookieClicker cookieClicker = CookieClicker.getInstance();
    final Configuration configuration = cookieClicker.getConfig();
    final Player player = event.getPlayer();

    new BukkitRunnable() {
      @Override
      public void run() {
        String name = configuration.getString("CookieItem.name").replace("&", "§");
        player.getInventory().setItem(configuration.getInt("CookieItem.slot"), ItemBuilder.buildItem(Material.COOKIE, 1, name,
            Arrays.asList("", "§7Lets farm some Cookies!")));
      }
    }.runTaskLaterAsynchronously(CookieClicker.getInstance(), 20L);
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR
        && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    final Player player = event.getPlayer();

    if (player.getInventory().getItemInMainHand() == null
        || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
      return;
    }

    if(player.getInventory().getItemInMainHand().getItemMeta() == null) return;

    final ItemStack currentItem = player.getInventory().getItemInMainHand();
    final Database database = CookieClicker.getInstance().getDatabase();
    final UUID uuid = player.getUniqueId();
    String name = CookieClicker.getInstance().getConfig().getString("CookieItem.name").replace("&", "§");

    if (currentItem.getItemMeta().getDisplayName()
        .equalsIgnoreCase(name)) {

      if(!database.doesPlayerExistByUUID(uuid)) {
        database.createNewUser(uuid);
      }


      Inventory inventory = Bukkit.createInventory(player, 3 * 9, name);

      inventory.setItem(11, ItemBuilder.buildItem(Material.GOLD_NUGGET, 1, "§cStats loading..", Arrays.asList("")));
      inventory.setItem(13, ItemBuilder.buildItem(Material.COOKIE, 1, "§a§lClick to farm Cookies", Arrays.asList("", "§eCookies farmed §8» §6" + database.getCookies(uuid))));
      inventory.setItem(15, ItemBuilder.buildItem(Material.CRIMSON_SIGN, 1, "§3§lYour Profile", Arrays.asList("", "§eCookies farmed §8» §6" + database.getCookies(uuid), "§3Rank §8» §b" + database.getRank(uuid))));

      player.openInventory(inventory);

      List<String> lines = new ArrayList<>();
      for(String topUser : database.getTop(5)) {
        String line = "§7- §a" + NameFetcher.getName(topUser, topUser) + " §7(§d" + database.getCookies(UUID.fromString(topUser)) + " §6Cookies§7)";
        lines.add(line);
      }

      inventory.setItem(11, ItemBuilder.buildItem(Material.GOLD_NUGGET, 1, "§6Top §e5", lines));
    }


  }

  @EventHandler
  public void on(InventoryDragEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void on(InventoryMoveItemEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {

    if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

    if(event.getCurrentItem().getItemMeta() == null) return;

    final Player player = (Player) event.getWhoClicked();
    final ItemStack currentItem = event.getCurrentItem();
    final Database database = CookieClicker.getInstance().getDatabase();
    final UUID uuid = player.getUniqueId();
    final     String name = CookieClicker.getInstance().getConfig().getString("CookieItem.name").replace("&", "§");
    if(currentItem.getItemMeta().getDisplayName().equalsIgnoreCase("§a§lClick to farm Cookies")) {
      event.setCancelled(true);

      if(event.isShiftClick() || event.getClick().isKeyboardClick()) {
        return;
      }

      database.addCookie(uuid);
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);

      int random = new Random().nextInt(20);

      if(random == 15) {
        event.getView().setItem(13, ItemBuilder.buildItem(Material.BARRIER, 1, "§4§lERROR 404", Arrays.asList("", "§cCookie Clicker not found")));
        new BukkitRunnable() {
          @Override
          public void run() {
            event.getView().setItem(13, ItemBuilder.buildItem(Material.COOKIE, 1, "§a§lClick to farm Cookies", Arrays.asList("", "§eCookies farmed §8» §6" + database.getCookies(uuid))));

          }
        }.runTaskLaterAsynchronously(CookieClicker.getInstance(), 20 * 3);
      } else {
        event.getView().setItem(15, ItemBuilder.buildItem(Material.CRIMSON_SIGN, 1, "§3§lYour Profile", Arrays.asList("", "§eCookies farmed §8» §6" + database.getCookies(uuid), "§3Rank §8» §b" + database.getRank(uuid))));
        event.getView().setItem(13, ItemBuilder.buildItem(Material.COOKIE, 1, "§a§lClick to farm Cookies", Arrays.asList("", "§eCookies farmed §8» §6" + database.getCookies(uuid))));
      }
    }

    if(event.getView().getTitle().equalsIgnoreCase(name)) {
      if(Via.getAPI().getPlayerVersion(player.getUniqueId()) < 108 && event.isLeftClick()) {
        new BukkitRunnable() {
          @Override
          public void run() {
            player.updateInventory();
          }
        }.runTaskLater(CookieClicker.getInstance(), 2);
      }
    }

  }

}
