package ca.wacos.nametagedit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.OfflinePlayer;

/**
 * This class dynamically creates teams with numerical names and certain
 * prefixes/suffixes (it ignores teams with other characters) to assign unique
 * prefixes and suffixes to specific players in the game. This class makes edits
 * to the <b>scoreboard.dat</b> file, adding and removing teams on the fly.
 * 
 * @author Levi Webb
 * 
 */
class NametagManager {

	// Prefix to append to all team names (nothing to do with prefix/suffix)
	private static final String TEAM_NAME_PREFIX = "NTE";

	private static List<Integer> list = new ArrayList<>();

	private static HashMap<TeamInfo, List<String>> teams = new HashMap<>();

	private static void addToTeam(TeamInfo team, String player) {
		removeFromTeam(player);
		List<String> list = teams.get(team);
		if (list != null) {
			list.add(player);
			Player p = Bukkit.getPlayerExact(player);
			if (p != null) {
				sendPacketsAddToTeam(team, p.getName());
			} else {
				OfflinePlayer p2 = Bukkit.getOfflinePlayer(player);
				sendPacketsAddToTeam(team, p2.getName());
			}
		}
	}

	private static void register(TeamInfo team) {
		teams.put(team, new ArrayList<String>());
		sendPacketsAddTeam(team);
	}

	@SuppressWarnings("unused")
	private static boolean removeTeam(String name) {
		for (TeamInfo team : teams.keySet().toArray(new TeamInfo[teams.size()])) {
			if (team.getName().equals(name)) {
				removeTeam(team);
				return true;
			}
		}
		return false;
	}

	private static void removeTeam(TeamInfo team) {
		sendPacketsRemoveTeam(team);
		teams.remove(team);
	}

	private static TeamInfo removeFromTeam(String player) {

		for (TeamInfo team : teams.keySet().toArray(new TeamInfo[teams.size()])) {
			List<String> list = teams.get(team);
			for (String p : list.toArray(new String[list.size()])) {
				if (p.equals(player)) {
					Player pl = Bukkit.getPlayerExact(player);
					if (pl != null) {
						sendPacketsRemoveFromTeam(team, pl.getName());
					} else {
						OfflinePlayer p2 = Bukkit.getOfflinePlayer(p);
						sendPacketsRemoveFromTeam(team, p2.getName());
					}
					list.remove(p);

					return team;
				}
			}
		}
		return null;
	}

	private static TeamInfo getTeam(String name) {

		for (TeamInfo team : teams.keySet().toArray(new TeamInfo[teams.size()])) {
			if (team.getName().equals(name)) {
				return team;
			}
		}
		return null;
	}

	private static TeamInfo[] getTeams() {
		TeamInfo[] list = new TeamInfo[teams.size()];
		int at = 0;
		for (TeamInfo team : teams.keySet().toArray(new TeamInfo[teams.size()])) {
			list[at] = team;
			at++;
		}
		return list;
	}

	private static String[] getTeamPlayers(TeamInfo team) {
		List<String> list = teams.get(team);
		if (list != null) {

			return list.toArray(new String[list.size()]);
		} else {
			return new String[0];
		}
	}

	/**
	 * Initializes this class and loads current teams that are manipulated by
	 * this plugin.
	 */
	static void load() {
		for (TeamInfo t : getTeams()) {
			int entry = -1;
			try {
				entry = Integer.parseInt(t.getName());
			} catch (Exception e) {
			}
			;
			if (entry != -1) {
				list.add(entry);
			}
		}
	}

	/**
	 * Updates a player's prefix and suffix in the scoreboard and above their
	 * head.<br>
	 * <br>
	 * 
	 * If either the prefix or suffix is null or empty, it will be replaced with
	 * the current prefix/suffix
	 * 
	 * @param player
	 *            the specified player
	 * @param prefix
	 *            the prefix to set for the given player
	 * @param suffix
	 *            the suffix to set for the given player
	 */
	static void update(String player, String prefix, String suffix) {

		if (prefix == null || prefix.isEmpty()) {
			prefix = getPrefix(player);

		}
		if (suffix == null || suffix.isEmpty()) {
			suffix = getSuffix(player);

		}

		TeamInfo t = get(prefix, suffix);

		addToTeam(t, player);

	}

	/**
	 * Updates a player's prefix and suffix in the scoreboard and above their
	 * head.<br>
	 * <br>
	 * 
	 * If either the prefix or suffix is null or empty, it will be removed from
	 * the player's nametag.
	 * 
	 * @param player
	 *            the specified player
	 * @param prefix
	 *            the prefix to set for the given player
	 * @param suffix
	 *            the suffix to set for the given player
	 */
	static void overlap(String player, String prefix, String suffix) {

		if (prefix == null) {
			prefix = "";
		}
		if (suffix == null) {
			suffix = "";
		}

		TeamInfo t = get(prefix, suffix);

		addToTeam(t, player);
	}

	/**
	 * Clears a player's nametag.
	 * 
	 * @param player
	 *            the specified player
	 */
	static void clear(String player) {

		removeFromTeam(player);
	}

	/**
	 * Retrieves a player's prefix
	 * 
	 * @param player
	 *            the specified player
	 * @return the player's prefix
	 */
	static String getPrefix(String player) {
		for (TeamInfo team : getTeams()) {
			for (String p : getTeamPlayers(team)) {

				if (p.equals(player)) {
					return team.getPrefix();
				}
			}
		}
		return "";
	}

	/**
	 * Retrieves a player's suffix
	 * 
	 * @param player
	 *            the specified player
	 * @return the player's suffix
	 */
	static String getSuffix(String player) {
		for (TeamInfo team : getTeams()) {
			for (String p : getTeamPlayers(team)) {
				if (p.equals(player)) {
					return team.getSuffix();
				}
			}
		}
		return "";
	}

	/**
	 * Retrieves the player's entire name with both the prefix and suffix.
	 * 
	 * @param player
	 *            the specified player
	 * @return the entire nametag
	 */
	static String getFormattedName(String player) {
		return getPrefix(player) + player + getSuffix(player);
	}

	/**
	 * Declares a new team in the scoreboard.dat of the given main world.
	 * 
	 * @param name
	 *            the team name
	 * @param prefix
	 *            the team's prefix
	 * @param suffix
	 *            the team's suffix
	 * @return the created team
	 */
	private static TeamInfo declareTeam(String name, String prefix,
			String suffix) {
		if (getTeam(name) != null) {
			TeamInfo team = getTeam(name);
			removeTeam(team);
		}

		TeamInfo team = new TeamInfo(name);

		team.setPrefix(prefix);
		team.setSuffix(suffix);

		register(team);

		return team;
	}

	/**
	 * Gets the {@link net.minecraft.server.v1_5_R3.ScoreboardTeam} for the
	 * given prefix and suffix, and if none matches, creates a new team with the
	 * provided info. This also removes teams that currently have no players.
	 * 
	 * @param prefix
	 *            the team's prefix
	 * @param suffix
	 *            the team's suffix
	 * @return a team with the corresponding prefix/suffix
	 */
	private static TeamInfo get(String prefix, String suffix) {

		update();

		for (int t : list.toArray(new Integer[list.size()])) {

			if (getTeam(TEAM_NAME_PREFIX + t) != null) {
				TeamInfo team = getTeam(TEAM_NAME_PREFIX + t);

				if (team.getSuffix().equals(suffix)
						&& team.getPrefix().equals(prefix)) {
					return team;
				}
			}
		}

		return declareTeam(TEAM_NAME_PREFIX + nextName(), prefix, suffix);
	}

	/**
	 * Returns the next available team name that is not taken.
	 * 
	 * @return an integer that for a team name that is not taken.
	 */
	private static int nextName() {
		int at = 0;
		boolean cont = true;
		while (cont) {
			cont = false;
			for (int t : list.toArray(new Integer[list.size()])) {
				if (t == at) {
					at++;
					cont = true;
				}

			}
		}
		list.add(at);
		return at;
	}

	/**
	 * Removes any teams that do not have any players in them.
	 */
	private static void update() {

		for (TeamInfo team : getTeams()) {
			int entry = -1;
			try {
				entry = Integer.parseInt(team.getName());
			} catch (Exception e) {
			}
			;
			if (entry != -1) {
				if (getTeamPlayers(team).length == 0) {
					removeTeam(team);
					list.remove(new Integer(entry));
				}
			}
		}
	}

	/**
	 * Sends the current team setup and their players to the given player. This
	 * should be called when players join the server.
	 * 
	 * @param p
	 *            The player to send the packets to.
	 */
	static void sendTeamsToPlayer(Player p) {
		try {

			for (TeamInfo team : getTeams()) {
				PacketPlayOut mod = new PacketPlayOut(team.getName(),
						team.getPrefix(), team.getSuffix(),
						new ArrayList<String>(), 0);
				mod.sendToPlayer(p);
				mod = new PacketPlayOut(team.getName(),
						Arrays.asList(getTeamPlayers(team)), 3);
				mod.sendToPlayer(p);
			}
		} catch (Exception e) {
			System.out
					.println("Failed to send packet for player (Packet209SetScoreboardTeam) : ");
			e.printStackTrace();
		}
	}

	/**
	 * Sends packets out to players to add the given team
	 * 
	 * @param team
	 *            the team to add
	 */
	private static void sendPacketsAddTeam(TeamInfo team) {

		try {

			for (Player p : Bukkit.getOnlinePlayers()) {
				PacketPlayOut mod = new PacketPlayOut(team.getName(),
						team.getPrefix(), team.getSuffix(),
						new ArrayList<String>(), 0);
				mod.sendToPlayer(p);
			}
		} catch (Exception e) {
			System.out
					.println("Failed to send packet for player (Packet209SetScoreboardTeam) : ");
			e.printStackTrace();
		}
	}

	/**
	 * Sends packets out to players to remove the given team
	 * 
	 * @param team
	 *            the team to remove
	 */
	private static void sendPacketsRemoveTeam(TeamInfo team) {

		boolean cont = false;
		for (TeamInfo t : getTeams()) {
			if (t == team) {
				cont = true;
			}
		}
		if (!cont) {
			return;
		}

		try {

			for (Player p : Bukkit.getOnlinePlayers()) {

				PacketPlayOut mod = new PacketPlayOut(team.getName(),
						team.getPrefix(), team.getSuffix(),
						new ArrayList<String>(), 1);
				mod.sendToPlayer(p);
			}
		} catch (Exception e) {
			System.out
					.println("Failed to send packet for player (Packet209SetScoreboardTeam) : ");
			e.printStackTrace();
		}
	}

	/**
	 * Sends out packets to players to add the given player to the given team
	 * 
	 * @param team
	 *            the team to use
	 * @param player
	 *            the player to add
	 */
	private static void sendPacketsAddToTeam(TeamInfo team, String player) {

		boolean cont = false;
		for (TeamInfo t : getTeams()) {
			if (t == team) {
				cont = true;
			}
		}
		if (!cont) {
			return;
		}

		try {

			for (Player p : Bukkit.getOnlinePlayers()) {
				PacketPlayOut mod = new PacketPlayOut(team.getName(),
						Arrays.asList(player), 3);
				mod.sendToPlayer(p);
			}
		} catch (Exception e) {
			System.out
					.println("Failed to send packet for player (Packet209SetScoreboardTeam) : ");
			e.printStackTrace();
		}
	}

	/**
	 * Sends out packets to players to remove the given player from the given
	 * team.
	 * 
	 * @param team
	 *            the team to remove from
	 * @param player
	 *            the player to remove
	 */
	private static void sendPacketsRemoveFromTeam(TeamInfo team, String player) {

		boolean cont = false;
		for (TeamInfo t : getTeams()) {
			if (t == team) {
				for (String p : getTeamPlayers(t)) {
					if (p.equals(player)) {
						cont = true;
					}
				}
			}
		}
		if (!cont) {
			return;
		}

		try {

			for (Player p : Bukkit.getOnlinePlayers()) {
				PacketPlayOut mod = new PacketPlayOut(team.getName(),
						Arrays.asList(player), 4);

				mod.sendToPlayer(p);

			}
		} catch (Exception e) {
			System.out
					.println("Failed to send packet for player (Packet209SetScoreboardTeam) : ");
			e.printStackTrace();
		}
	}

	/**
	 * Clears out all teams and removes them for all the players. Called when
	 * the plugin is disabled.
	 */
	static void reset() {
		for (TeamInfo team : getTeams()) {
			removeTeam(team);
		}
	}
}
