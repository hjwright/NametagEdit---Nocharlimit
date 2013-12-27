package ca.wacos.nametagedit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.tehkode.permissions.events.PermissionEntityEvent;

public class NametagHookPEX implements Listener {

	@EventHandler
	void onPermissionChangePEX(PermissionEntityEvent e) {

		if (e.getEntity() != null) {
			String name = e.getEntity().getName();
			if (!NametagAPI.hasCustomNametag(name)) {
				NametagAPI.resetNametag(name);
			}
		}
	}

}
