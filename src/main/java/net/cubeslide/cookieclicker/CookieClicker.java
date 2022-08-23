package net.cubeslide.cookieclicker;

import java.sql.SQLException;
import net.cubeslide.cookieclicker.listeners.PlayerListeners;
import net.cubeslide.cookieclicker.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class CookieClicker extends JavaPlugin {

  private static CookieClicker instance;
  private Database database;

  @Override
  public void onEnable() {
    instance = this;
    final PluginManager pluginManager = getServer().getPluginManager();
    final Configuration config = getConfig();

    config.addDefault("CookieItem.name", "&6&lCookie Clicker");
    config.addDefault("CookieItem.slot", 5);

    config.addDefault("MYSQL.HOSTNAME", "localhost");
    config.addDefault("MYSQL.USERNAME", "root");
    config.addDefault("MYSQL.PASSWORD", "");
    config.addDefault("MYSQL.DATABASE", "minecraft");
    config.addDefault("MYSQL.PORT", 3306);

    getConfig().options().copyDefaults(true);
    saveConfig();

    database = new Database();
    database.createTable();

    pluginManager.registerEvents(new PlayerListeners(), this);


    new BukkitRunnable() {
      @Override
      public void run() {
        if(Bukkit.getOnlinePlayers().size() == 0) {
          database.sendKeepAlivePing();
          getLogger().info("Database-KeepAlive-Ping send.");
        }
        getDatabase().saveCookiesToDatabase();
      }
    }.runTaskTimer(getInstance(), 20 * 300, 20 * 300);
  }

  @Override
  public void onDisable() {
    try {
      getDatabase().close();
      getDatabase().saveCookiesToDatabase();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Database getDatabase() {
    return database;
  }

  public static CookieClicker getInstance() {
    return instance;
  }
}
