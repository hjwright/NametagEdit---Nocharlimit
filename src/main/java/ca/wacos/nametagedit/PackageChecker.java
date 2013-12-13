package ca.wacos.nametagedit;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class retrieves the craftbukkit version string that exists in the
 * package names for NMS and CB classes.
 */
class PackageChecker {
	private static final String PACKAGE_PREFIX = "org/bukkit/craftbukkit/v";
	private static String version = "";
	static {
		try {
			File file = new File(Bukkit.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI().getPath());

			ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

			ZipEntry entry;

			while ((entry = zis.getNextEntry()) != null) {

				// For them pesky Windows users...
				String name = entry.getName().replace("\\", "/");

				if (name.startsWith(PACKAGE_PREFIX)) {
					String ver = "";
					for (int t = PACKAGE_PREFIX.length(); t < name.length(); t++) {
						char c = name.charAt(t);
						if (c != '/')
							ver += c;
						else
							break;
					}
					version = "v" + ver;
					break;
				}
			}

			zis.close();
		} catch (Exception e) {
			System.out
					.println("Could not locate craftbukkit's package version (you're probably going to have a lot of errors after this!)");
			e.printStackTrace();
		}
	}

	public static String getVersion() {
		return version;
	}
}
