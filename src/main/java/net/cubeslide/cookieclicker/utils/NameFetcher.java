package net.cubeslide.cookieclicker.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameFetcher {

  private static final String NAME_URL = "https://sessionserver.mojang.com"
      + "/session/minecraft/profile/";

  private static final Pattern NAME_PATTERN = Pattern.compile(",\\s*\"name\"\\s*:\\s*\"(.*?)\"");

  private NameFetcher() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the name of the searched player.
   *
   * @param uuid The UUID of a player.
   * @return The name of the given player.
   */
  public static String getName(UUID uuid) {
    return getName(uuid.toString(), uuid.toString());
  }

  /**
   * Returns the name of the searched player.
   *
   * @param uuid The UUID of a player (can be trimmed or the normal version).
   * @return The name of the given player.
   */

  private static final HashMap<UUID, String> nameCache = new HashMap<>();

  public static String getName(String uuid, String realUUID) {

    if(nameCache.containsKey(realUUID)) {
      return nameCache.get(realUUID);
    }

    uuid = uuid.replace("-", "");
    String output = callURL(NAME_URL + uuid);
    Matcher m = NAME_PATTERN.matcher(output);
    if (m.find()) {
      nameCache.put(UUID.fromString(realUUID), m.group(1));
      return m.group(1);
    }
    return null;
  }

  private static String callURL(String urlStr) {
    StringBuilder sb = new StringBuilder();
    URLConnection conn;
    BufferedReader br = null;
    InputStreamReader in = null;
    try {
      conn = new URL(urlStr).openConnection();
      if (conn != null) {
        conn.setReadTimeout(60 * 1000);
      }
      if (conn != null && conn.getInputStream() != null) {
        in = new InputStreamReader(conn.getInputStream(), "UTF-8");
        br = new BufferedReader(in);
        String line = br.readLine();
        while (line != null) {
          sb.append(line).append("\n");
          line = br.readLine();
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Throwable ignored) {
        }
      }
      if (in != null) {
        try {
          in.close();
        } catch (Throwable ignored) {
        }
      }
    }
    return sb.toString();
  }


}
