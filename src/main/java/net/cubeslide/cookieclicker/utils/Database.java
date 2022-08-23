package net.cubeslide.cookieclicker.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.cubeslide.cookieclicker.CookieClicker;
import org.bukkit.configuration.Configuration;

public class Database {

  private final HikariDataSource dataSource;
  private final Connection connection;

  public Database() {
    final HikariConfig hikariConfig = new HikariConfig();
    final Configuration configuration = CookieClicker.getInstance().getConfig();
    hikariConfig.setJdbcUrl(
        "jdbc:mysql://" + configuration.getString("MYSQL.HOSTNAME") + ":" + configuration.getInt(
            "MYSQL.PORT") + "/" + configuration.getString("MYSQL.DATABASE") + "?autoReconnect=true");
    hikariConfig.setUsername(configuration.getString("MYSQL.USERNAME"));
    hikariConfig.setPassword(configuration.getString("MYSQL.PASSWORD"));
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
    this.dataSource = new HikariDataSource(hikariConfig);
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void update(String query) {
    try (Connection connection = getConnection()) {
      PreparedStatement statement = connection.prepareStatement(query);
      statement.executeUpdate();
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  public boolean doesPlayerExistByUUID(UUID uuid) {
    try {
      final PreparedStatement prepareStatement = getConnection().prepareStatement(
          "SELECT * FROM `CookieClickerStats` WHERE UUID=? LIMIT 1;");
      prepareStatement.setString(1, uuid.toString());
      final ResultSet resultSet = prepareStatement.executeQuery();
      final boolean result = resultSet.next();
      prepareStatement.close();
      resultSet.close();
      return result;

    } catch (Exception exception) {
      exception.printStackTrace();
    }

    return false;
  }

  private Connection getConnection() throws SQLException {
    return connection;
  }


  public void createTable() {
    try {
      Connection conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(
          "CREATE TABLE IF NOT EXISTS `CookieClickerStats` " + "("
              + "UUID varchar(40), Cookies bigint" + ");");
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static final ConcurrentHashMap<UUID, Integer> cookieCache = new ConcurrentHashMap<>();

  public int getCookies(UUID uuid) {

    if(cookieCache.containsKey(uuid)) {
      return cookieCache.get(uuid);
    }

    try {
      final Connection connection = getConnection();
      final PreparedStatement preparedStatement = connection.prepareStatement(
          "SELECT * FROM CookieClickerStats WHERE uuid = '" + uuid + "';");
      ResultSet results = preparedStatement.executeQuery();
      if (results.next()) {
        cookieCache.put(uuid, results.getInt("Cookies"));
        return results.getInt("Cookies");
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
    return -1;
  }

  public int getRank(UUID uuid) {

    try {
      final Connection connection = getConnection();
      final PreparedStatement preparedStatement = connection.prepareStatement(
          "SELECT * FROM CookieClickerStats ORDER BY Cookies DESC;");
      ResultSet results = preparedStatement.executeQuery();
      int rank = 0;
      while (results.next()) {
        final UUID resultUUID = UUID.fromString(results.getString("UUID"));
        rank++;
        if(resultUUID.equals(uuid)) {
          return rank;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
    return -1;
  }

  public List<String> getTop(int count) {

    try {
      final Connection connection = getConnection();
      final PreparedStatement preparedStatement = connection.prepareStatement(
          "SELECT * FROM CookieClickerStats ORDER BY Cookies DESC LIMIT "  + count + ";");
      ResultSet results = preparedStatement.executeQuery();
      List<String> ranks = new ArrayList<>();
      while (results.next()) {
        ranks.add(results.getString("UUID"));
      }
      return ranks;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }


  public void sendKeepAlivePing() {
    try {
      final Connection connection = getConnection();
      final PreparedStatement preparedStatement = connection.prepareStatement(
          "SELECT * FROM CookieClickerStats;");
      preparedStatement.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  public void addCookie(UUID uuid) {

    if(cookieCache.containsKey(uuid)) {
      cookieCache.put(uuid, getCookies(uuid)+1);
    } else {
      cookieCache.put(uuid, 1);
    }
  }

  public void saveCookiesToDatabase() {
    for(UUID uuid : cookieCache.keySet()) {
      try {
        Connection connection = getConnection();
        PreparedStatement prepareStatement = connection.prepareStatement(
            "UPDATE CookieClickerStats SET Cookies=" + (getCookies(uuid) + 1) + " WHERE UUID='"
                + uuid.toString() + "'");
        prepareStatement.execute();
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  public void createNewUser(UUID uuid) {
    if (doesPlayerExistByUUID(uuid)) {
      return;
    }

    try {
      Connection connection = getConnection();
      PreparedStatement prepareStatement = connection.prepareStatement(
          "INSERT INTO CookieClickerStats(UUID, Cookies) VALUES(?, ?);");
      prepareStatement.setString(1, uuid.toString());
      prepareStatement.setInt(2, 0);
      prepareStatement.execute();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public void close() throws SQLException {

    if (getConnection() == null || getConnection().isClosed()) {
      return;
    }
    try {
      getConnection().close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }




}
