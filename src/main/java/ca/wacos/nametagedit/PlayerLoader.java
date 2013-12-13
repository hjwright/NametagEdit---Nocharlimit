package ca.wacos.nametagedit;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * This class is responsible for loading player information from
 * plugins/players.txt
 * 
 * @author Levi Webb
 * 
 */
class PlayerLoader {

	private static final String PREFIX = "[NAMETAG CONFIG] ";
	private static String path = null;

	/**
	 * Loads all players from the players.txt file, and returns a
	 * {@link java.util.LinkedHashMap} of all the players and their
	 * prefixes/suffixes.
	 * 
	 * @param plugin
	 *            the plugin that this is being loaded from
	 * @return a {@link java.util.LinkedHashMap} with a {@link String} as the
	 *         key (player names), and another {@link java.util.LinkedHashMap}
	 *         as the value with a {@link String} key (prefix/suffix) and a
	 *         {@link String} value (text).
	 */
	static LinkedHashMap<String, LinkedHashMap<String, String>> load(
			JavaPlugin plugin) {
		String folder = "plugins/" + plugin.getName();
		File folderFile = new File(folder);
		if (!folderFile.exists()) {
			folderFile.mkdir();
		}
		path = "plugins/" + plugin.getName() + "/players.txt";
		File source = new File(path);
		if (source.exists()) {
			return loadConfig();
		} else {
			try {
				source.createNewFile();
			} catch (IOException e) {
				print("Failed to create config file: ");
				e.printStackTrace();
			}
			return generateConfig(source, plugin);
		}
	}

	/**
	 * Adds a player to the file, writes it, and saves it.
	 * 
	 * @param name
	 *            the player name
	 * @param operation
	 *            the operation (either "prefix" or "suffix")
	 * @param value
	 *            the prefix/suffix text
	 */
	static void addPlayer(String name, String operation, String value) {
		ArrayList<String> buffer = new ArrayList<String>();
		File file = new File(path);
		Scanner in = null;
		PrintWriter out = null;
		value = value.replace("§", "&");
		try {
			in = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (in.hasNext()) {
			buffer.add(in.nextLine());
		}
		in.close();
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (String line : buffer.toArray(new String[buffer.size()])) {
			out.println(line);
		}
		out.println(name + " " + operation + " = \"" + value + "\"");
		out.close();

	}

	/**
	 * Updates a player's information in the players.txt file, overwriting their
	 * previous entries if they exist. If either <i>prefix</i> or <i>suffix</i>
	 * is set to null or is empty, data for that field will not be changed.
	 * 
	 * @param name
	 *            the name of the player
	 * @param prefix
	 *            the prefix text to set
	 * @param suffix
	 *            the suffix text to set
	 */
	static void update(String name, String prefix, String suffix) {
		LinkedHashMap<String, String> player = getPlayer(name);
		prefix = prefix.replace("§", "&");
		suffix = suffix.replace("§", "&");
		removePlayer(name, null);
		if (prefix != null && !prefix.isEmpty())
			addPlayer(name, "prefix", prefix);
		else if (player != null) {
			if (player.get("prefix") != null)
				addPlayer(name, "prefix", player.get("prefix"));
		}
		if (suffix != null && !suffix.isEmpty())
			addPlayer(name, "suffix", suffix);
		else if (player != null) {
			if (player.get("suffix") != null)
				addPlayer(name, "suffix", player.get("suffix"));
		}
	}

	/**
	 * Updates a player's information in the players.txt file, overwriting their
	 * previous entries if they exist. If either <i>prefix</i> or <i>suffix</i>
	 * is set to null or is empty, data for that field will be removed.
	 * 
	 * @param name
	 *            the name of the player
	 * @param prefix
	 *            the prefix text to set
	 * @param suffix
	 *            the suffix text to set
	 */
	static void overlap(String name, String prefix, String suffix) {
		prefix = prefix.replace("§", "&");
		suffix = suffix.replace("§", "&");
		removePlayer(name, null);
		if (prefix != null && !prefix.isEmpty())
			addPlayer(name, "prefix", prefix);
		if (suffix != null && !suffix.isEmpty())
			addPlayer(name, "suffix", suffix);
	}

	/**
	 * Removes all traces of a player from the file, or a specific operation
	 * from a player.
	 * 
	 * @param name
	 *            the player name
	 * @param operation
	 *            the operation (either "prefix" or "suffix")
	 */
	static void removePlayer(String name, String operation) {
		ArrayList<String> buffer = new ArrayList<String>();
		File file = new File(path);
		Scanner in = null;
		PrintWriter out = null;
		try {
			in = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (in.hasNext()) {
			buffer.add(in.nextLine());
		}
		in.close();
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (String line : buffer.toArray(new String[buffer.size()])) {
			Scanner lineScanner = new Scanner(line);

			String lName = lineScanner.next();
			String lOperation = lineScanner.next();

			lineScanner.close();

			boolean skip = false;

			if (name.equals(lName)) {
				if (operation != null && operation.equals(lOperation))
					skip = true;
				else if (operation == null)
					skip = true;
			}
			if (!skip)
				out.println(line);
		}
		out.close();
	}

	/**
	 * Returns the {@link java.util.LinkedHashMap} for a given player,
	 * containing their prefix/suffix data. The key in this
	 * {@link java.util.LinkedHashMap} is the operation type, and the value is
	 * the text.
	 * 
	 * @param name
	 *            the player name
	 * @return a {@link java.util.LinkedHashMap} of the player's data.
	 */
	static LinkedHashMap<String, String> getPlayer(String name) {
		LinkedHashMap<String, LinkedHashMap<String, String>> playerMap = loadConfig();
		for (String key : playerMap.keySet().toArray(
				new String[playerMap.keySet().size()])) {
			if (key.equals(name)) {
				return playerMap.get(key);
			}
		}
		return null;
	}

	/**
	 * Generates and loads the given file with default example configurations /
	 * data.
	 * 
	 * @param target
	 *            the target {@link java.io.File}
	 * @param plugin
	 *            the plugin that this is being generated from
	 * @return a {@link java.util.LinkedHashMap} with a {@link String} as the
	 *         key (player names), and another {@link java.util.LinkedHashMap}
	 *         as the value with a {@link String} key (prefix/suffix) and a
	 *         {@link String} value (text).
	 */
	private static LinkedHashMap<String, LinkedHashMap<String, String>> generateConfig(
			File target, JavaPlugin plugin) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(target);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		out.println("// This file declares any user specific prefixes and suffixes set with the /ne command.");
		out.println("Notch prefix = \"&b< &a\"");
		out.println("Notch suffix = \" &b>\"");

		out.close();

		return loadConfig();
	}

	/**
	 * Loads the file with default example configurations / data.
	 * 
	 * @return a {@link java.util.LinkedHashMap} with a {@link String} as the
	 *         key (player names), and another {@link java.util.LinkedHashMap}
	 *         as the value with a {@link String} key (prefix/suffix) and a
	 *         {@link String} value (text).
	 */
	private static LinkedHashMap<String, LinkedHashMap<String, String>> loadConfig() {
		File source = new File(path);
		Scanner in = null;
		try {
			in = new Scanner(source);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		LinkedHashMap<String, LinkedHashMap<String, String>> map = new LinkedHashMap<String, LinkedHashMap<String, String>>();

		boolean syntaxError = false;

		while (in.hasNext()) {
			String line = in.nextLine();
			if (!line.trim().startsWith("//") && !line.isEmpty()) {

				syntaxError = checkWords(line);
				if (syntaxError) {
					print("Error in syntax, not enough elements on line!");
				}

				Scanner lineScanner = new Scanner(line);

				String node = lineScanner.next();
				String operation = lineScanner.next();
				String equals = lineScanner.next();
				if (!equals.trim().equals("=")) {
					print("Error in syntax, \"=\" expected at third element, \""
							+ equals + "\" given.");
					syntaxError = true;
					break;
				}
				String rawValue = lineScanner.nextLine();
				syntaxError = checkValue(rawValue);
				if (syntaxError) {
					print("Error in syntax, value not encased in quotation marks!");
					break;
				}
				String value = getValue(rawValue);

				LinkedHashMap<String, String> entry = new LinkedHashMap<String, String>();

				if (map.get(node) != null) {
					entry = map.get(node);
				}

				entry.put(operation.toLowerCase(), value);

				if (map.get(node) == null) {
					map.put(node, entry);
				}
				lineScanner.close();
			}
		}
		in.close();

		if (syntaxError)
			return new LinkedHashMap<String, LinkedHashMap<String, String>>();
		return map;
	}

	/**
	 * Prints the given text with the prefix according to this object.
	 * 
	 * @param p
	 *            the text to print
	 */
	private static void print(String p) {
		System.out.println(PREFIX + p);
	}

	/**
	 * Prints the given text if debugging is enabled.
	 * 
	 * @param p
	 *            the text to print
	 */
	@SuppressWarnings("unused")
	private static void printDebug(String p) {
		if (GroupLoader.DEBUG)
			System.out.println(PREFIX + p);
	}

	/**
	 * Checks the given line and returns true if at least four elements exist in
	 * it, separated by spaces.
	 * 
	 * @param line
	 *            the line to check
	 * @return true if four or more elements exist, false if not.
	 */
	private static boolean checkWords(String line) {
		int count = 0;
		Scanner reader = new Scanner(line);
		while (reader.hasNext()) {
			count++;
			reader.next();
		}
		reader.close();
		if (count >= 4)
			return false;
		else
			return true;
	}

	/**
	 * Checks the given line to see if it is enclosed in quotation marks.
	 * 
	 * @param rawValue
	 *            the line to check
	 * @return true if the line is enclosed in quotation marks, false if not.
	 */
	private static boolean checkValue(String rawValue) {
		rawValue = rawValue.trim();
		if (!rawValue.startsWith("\""))
			return true;
		if (!rawValue.endsWith("\""))
			return true;
		return false;

	}

	/**
	 * Removes the first and last character of a string, then cuts off any
	 * excess data at the end to keep a maximum length of 16.
	 * 
	 * @param rawValue
	 *            the string to edit
	 * @return the modified string
	 */
	private static String getValue(String rawValue) {
		rawValue = rawValue.trim();
		String f1 = "";
		String f2 = "";
		for (int t = 1; t < rawValue.length() - 1; t++) {
			f1 += rawValue.charAt(t);
		}
		for (int t = 0; t < f1.length() && t < 16; t++) {
			f2 += f1.charAt(t);
		}
		return f2;
	}
}
