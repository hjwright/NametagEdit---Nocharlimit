package ca.wacos.nametagedit;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * This class is responsible for loading group information from
 * plugins/groups.txt
 *
 * @author Levi Webb
 *
 */
class GroupLoader {

    private static final String PREFIX = "[NAMETAG CONFIG] ";
    static final boolean DEBUG = true;

    /**
     * Loads the groups.txt from the plugin folder, returning a
     * {@link java.util.LinkedHashMap} containing all the groups, which is blank
     * if the groups failed to load.
     *
     * @param plugin the plugin instance
     * @return a {@link java.util.LinkedHashMap} of groups, with a
     * {@link String} key (player names), and another
     * {@link java.util.LinkedHashMap} as the value, containing both the
     * operation (prefix/suffix) and the actual value.
     */
    static LinkedHashMap<String, LinkedHashMap<String, String>> load(JavaPlugin plugin) {
        String folder = "plugins/" + plugin.getName();
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        String path = "plugins/" + plugin.getName() + "/groups.txt";
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
     * Generates a new groups file, then returns some predefined configuration
     * data in a {@link java.util.LinkedHashMap}.
     *
     * @param target the target file to generate.
     * @return a {@link java.util.LinkedHashMap} of groups, with a
     * {@link String} key (player names), and another
     * {@link java.util.LinkedHashMap} as the value, containing both the
     * operation (prefix/suffix) and the actual value.
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
        out.println("// This file declares custom permissions and ties prefixes and suffixes to them.");
        out.println("// Players who possess these permissions will have the prefix and suffix assigned to the given permission.");
        out.println("nametag.group.admin prefix = \"[&cAdmin&f] \"");
        out.println("nametag.group.mod prefix = \"[&bMod&f] \"");
        out.println("nametag.group.member prefix = \"[&eMember&f] \"");
        out.println("nametag.group.swag prefix = \"&eThe &b\"");
        out.println("nametag.group.swag suffix = \" &cSwagmaster\"");

        LinkedHashMap<String, LinkedHashMap<String, String>> map = new LinkedHashMap<String, LinkedHashMap<String, String>>();

        LinkedHashMap<String, String> admin = new LinkedHashMap<String, String>();
        admin.put("prefix", "[&cAdmin&f] ");
        map.put("nametag.group.admin", admin);

        LinkedHashMap<String, String> mod = new LinkedHashMap<String, String>();
        mod.put("prefix", "[&bMod&f] ");
        map.put("nametag.group.mod", mod);

        LinkedHashMap<String, String> member = new LinkedHashMap<String, String>();
        member.put("prefix", "[&eMember&f] ");
        map.put("nametag.group.member", member);

        LinkedHashMap<String, String> swag = new LinkedHashMap<String, String>();
        swag.put("prefix", "&eThe &b");
        swag.put("suffix", " &cSwagmaster");
        map.put("nametag.group.swag", swag);

        out.close();

        return map;
    }

    /**
     * Loads the specified groups file and returns the
     * {@link java.util.LinkedHashMap} with all the groups data.
     *
     * @param source the target file to load.
     * @return a {@link java.util.LinkedHashMap} of groups, with a
     * {@link String} key (player names), and another
     * {@link java.util.LinkedHashMap} as the value, containing both the
     * operation (prefix/suffix) and the actual value.
     */
    static LinkedHashMap<String, LinkedHashMap<String, String>> loadConfig(File source) {
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
        if (DEBUG) {
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
     * Checks the given line to see if it is enclosed in quotation marks.
     *
     * @param rawValue the line to check
     * @return true if the line is enclosed in quotation marks, false if not.
     */
    private static boolean checkValue(String rawValue) {
        rawValue = rawValue.trim();
        if (!rawValue.startsWith("\"")) {
            return true;
        }
        if (!rawValue.endsWith("\"")) {
            return true;
        }
        return false;

    }

    /**
     * Removes the first and last character of a string, then cuts off any
     * excess data at the end to keep a maximum length of 16.
     *
     * @param rawValue the string to edit
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
