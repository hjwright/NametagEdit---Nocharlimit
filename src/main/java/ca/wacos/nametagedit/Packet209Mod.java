package ca.wacos.nametagedit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * A small wrapper for the Packet209SetScoreboardTeam packet.
 */
class Packet209Mod {

	Object packet;

	private static Method getHandle;
	private static Method sendPacket;
	private static Field playerConnection;

	private static Class<?> packetType;

	static {
		try {

			packetType = Class.forName(getPacketTeamClasspath());

			Class<?> typeCraftPlayer = Class.forName(getCraftPlayerClasspath());
			Class<?> typeNMSPlayer = Class.forName(getNMSPlayerClasspath());
			Class<?> typePlayerConnection = Class
					.forName(getPlayerConnectionClasspath());

			getHandle = typeCraftPlayer.getMethod("getHandle");
			playerConnection = typeNMSPlayer.getField("playerConnection");
			sendPacket = typePlayerConnection.getMethod("sendPacket",
					Class.forName(getPacketClasspath()));
		} catch (Exception e) {
			System.out.println("Failed to setup reflection for Packet209Mod!");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	Packet209Mod(String name, String prefix, String suffix, Collection players,
			int paramInt) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException,
			NoSuchMethodException, NoSuchFieldException,
			InvocationTargetException {

		packet = packetType.newInstance();
		setField("a", name);
		setField("f", paramInt);

		if ((paramInt == 0) || (paramInt == 2)) {
			setField("b", name);
			setField("c", prefix);
			setField("d", suffix);
			setField("g", 1);
		}
		if (paramInt == 0) {
			addAll(players);
		}
	}

	@SuppressWarnings("rawtypes")
	Packet209Mod(String name, Collection players, int paramInt)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, NoSuchMethodException,
			NoSuchFieldException, InvocationTargetException {

		packet = packetType.newInstance();

		if ((paramInt != 3) && (paramInt != 4)) {
			throw new IllegalArgumentException(
					"Method must be join or leave for player constructor");
		}
		if ((players == null) || (players.isEmpty())) {
			players = new ArrayList<String>();
		}

		setField("a", name);
		setField("f", paramInt);
		addAll(players);
	}

	void sendToPlayer(Player bukkitPlayer) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException,
			NoSuchFieldException {

		Object player = getHandle.invoke(bukkitPlayer);

		Object connection = playerConnection.get(player);

		sendPacket.invoke(connection, packet);
	}

	private void setField(String field, Object value)
			throws NoSuchFieldException, IllegalAccessException {
		Field f = packet.getClass().getDeclaredField(field);
		f.setAccessible(true);
		f.set(packet, value);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addAll(Collection<?> col) throws NoSuchFieldException,
			IllegalAccessException {
		Field f = packet.getClass().getDeclaredField("e");
		f.setAccessible(true);
		((Collection) f.get(packet)).addAll(col);
	}

	private static String getCraftPlayerClasspath() {
		return "org.bukkit.craftbukkit." + PackageChecker.getVersion()
				+ ".entity.CraftPlayer";
	}

	private static String getPlayerConnectionClasspath() {
		return "net.minecraft.server." + PackageChecker.getVersion()
				+ ".PlayerConnection";
	}

	private static String getNMSPlayerClasspath() {
		return "net.minecraft.server." + PackageChecker.getVersion()
				+ ".EntityPlayer";
	}

	private static String getPacketClasspath() {
		return "net.minecraft.server." + PackageChecker.getVersion()
				+ ".Packet";
	}

	private static String getPacketTeamClasspath() {
		// v1_(7)_R1

		if (Integer.valueOf(PackageChecker.getVersion().split("_")[1]) < 7
				&& Integer.valueOf(PackageChecker.getVersion().toLowerCase()
						.split("_")[0].replace("v", "")) == 1) {
			return "net.minecraft.server." + PackageChecker.getVersion()
					+ ".Packet209SetScoreboardTeam";
		} else {
			return "net.minecraft.server." + PackageChecker.getVersion()
					+ ".PacketPlayOutScoreboardTeam";
		}
	}
}
