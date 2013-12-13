package ca.wacos.nametagedit;

/**
 * This class stores information about a plugin version.
 * 
 * @author Levi Webb
 * 
 */
class PluginVersion {
	private boolean snapshot;
	private int build = -1;
	private String version;

	/**
	 * Creates a new {@link ca.wacos.nametagedit.PluginVersion} object with the
	 * given version and development build number
	 * 
	 * @param version
	 *            the plugin version
	 * @param build
	 *            the development build number
	 */
	PluginVersion(String version, int build) {
		this.version = version;
		this.build = build;
		snapshot = true;
	}

	/**
	 * Creates a new {@link ca.wacos.nametagedit.PluginVersion} object with the
	 * given version.
	 * 
	 * @param version
	 *            the plugin version
	 */
	PluginVersion(String version) {
		this.version = version;
		snapshot = false;
	}

	/**
	 * Returns the build number if there is one, and -1 if there was none
	 * assigned.
	 * 
	 * @return the build number
	 */
	public int getBuild() {
		return build;
	}

	/**
	 * Returns true if this is a snapshot version, false otherwise.
	 * 
	 * @return true if this is a snapshot version, false if not.
	 */
	public boolean isSnapshot() {
		return snapshot;
	}

	/**
	 * Returns the version string.
	 * 
	 * @return the version string
	 */
	public String getVersion() {
		return version;
	}
}
