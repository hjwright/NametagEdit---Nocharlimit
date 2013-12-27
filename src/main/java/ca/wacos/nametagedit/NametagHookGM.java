package ca.wacos.nametagedit;

import org.anjocaido.groupmanager.events.GMUserEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NametagHookGM implements Listener {

	@EventHandler
	void onPermissionChangeGM(GMUserEvent e) {
		if (e.getUserName() != null) {
			String name = e.getUserName();
			if (!NametagAPI.hasCustomNametag(name)) {
				NametagAPI.resetNametag(name);
			}
		}
	}

	void getPermissions(String player) {

	}
}
