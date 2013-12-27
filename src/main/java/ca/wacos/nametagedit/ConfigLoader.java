package ca.wacos.nametagedit;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * This class is responsible for loading plugin configurations from
 * plugins/players.txt
 *
 * @author Levi Webb
 *
 */
class ConfigLoader {

    private static final String PREFIX = "[NAMETAG CONFIG] ";

    /**
     * Loads the config.txt from the plugin folder, returning a
     * {@link java.util.LinkedHashMap} containing all the configuration info,
     * which is blank if the configuration failed to load.
     *
     * @param plugin the plugin instance
     * @return a {@link java.util.LinkedHashMap} of configurations, with a
     * {@link String} key, and another {@link java.util.LinkedHashMap} as the
     * value, containing both the operation and the actual configuration value.
     */
    static LinkedHashMap<String, LinkedHashMap<String, String>> load(JavaPlugin plugin) {
        String folder = "plugins/" + plugin.getName();
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        String path = "plugins/" + plugin.getName() + "/config.txt";
        File source = new File(path);
        if (source.exists()) {
            return loadConfig(source);
        } else {
            try {
                source.createNewFile();
            } catch (IOException e) {
                print("Failed to create config file: ");
                e.printStackTrace();
            }
            return generateConfig(source);
        }
    }

    /**
     * Generates a new config file, loads it, then returns the
     * {@link java.util.LinkedHashMap} with all the configuration data.
     *
     * @param target the target file to generate and load.
     * @return a {@link java.util.LinkedHashMap} of configurations, with a
     * {@link String} key, and another {@link java.util.LinkedHashMap} as the
     * value, containing both the operation and the actual configuration value.
     * @see #loadConfig(java.io.File)
     * @see #load(org.bukkit.plugin.java.JavaPlugin)
     */
    private static LinkedHashMap<String, LinkedHashMap<String, String>> generateConfig(File target) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(target);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.println("// This file contains several configurable properties for this plugin.");
        out.println("// You may delete this file and reload the plugin / server to re-generate this file.");
        out.println("");
        out.println("// If enabled, this will remove any formatting created in the death messages by prefixes / suffixes");
        out.println("// Do not set this to true if you already have a plugin that changes death messages");
        out.println("death-message-mask enabled = false");
        out.println("");
        out.println("// If enabled, this will remove any formatting created in the tab list by prefixes / suffixes");
        out.println("// Do not set this to true if you already have a plugin that changes the tab list");
        out.println("tab-list-mask enabled = false");
        out.println("");
        out.println("// If enabled, this plugin will check for updates when a operator logs in.");
        out.println("check-for-updates enabled = true");

        out.close();

        return loadConfig(target);
    }

    /**
     * Loads the specified config file and returns the
     * {@link java.util.LinkedHashMap} with all the configuration data.
     *
     * @param source the target file to load.
     * @return a {@link java.util.LinkedHashMap} of configurations, with a
     * {@link String} key, and another {@link java.util.LinkedHashMap} as the
     * value, containing both the operation and the actual configuration value.
     */
    private static LinkedHashMap<String, LinkedHashMap<String, String>> loadConfig(File source) {
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
                    print("Error in syntax, \"=\" expected at third element, \"" + equals + "\" given.");
                    syntaxError = true;
                    break;
                }
                String value = lineScanner.nextLine().trim();

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

        if (syntaxError) {
            return new LinkedHashMap<String, LinkedHashMap<String, String>>();
        }
        return map;
    }

    /**
     * Prints the given text with the prefix according to this object.
     *
     * @param p the text to print
     */
    private static void print(String p) {
        System.out.println(PREFIX + p);
    }

    /**
     * Prints the given text if debugging is enabled.
     *
     * @param p the text to print
     */
    @SuppressWarnings("unused")
    private static void printDebug(String p) {
        if (GroupLoader.DEBUG) {
            System.out.println(PREFIX + p);
        }
    }

    /**
     * Checks the given line and returns true if at least four elements exist in
     * it, separated by spaces.
     *
     * @param line the line to check
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
        if (count >= 4) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Parses the given config data for a specific configuration and operation,
     * then makes an attempt to parse to retrieved value as a boolean.
     *
     * @param name the configuration name
     * @param operation the configuration operation
     * @param config the configuration data
     * @param d the default value to return if an error occurs.
     * @return true if the found value equals "true", false if it equals
     * "false", and <i>d</i> if the value could not be found or parsed.
     */
    static boolean parseBoolean(String name, String operation, LinkedHashMap<String, LinkedHashMap<String, String>> config, boolean d) {
        if (config.containsKey(name) && config.get(name).containsKey(operation)) {
            String value = config.get(name).get(operation);
            if (value.equalsIgnoreCase("true")) {
                return true;
            } else if (value.equalsIgnoreCase("false")) {
                return false;
            }
            System.out.println("[NametagEdit] Could not parse boolean for \"" + name + " " + operation + "\" in config.txt, value given: " + value + ", defaulting to " + d);
        }
        return d;
    }
}
