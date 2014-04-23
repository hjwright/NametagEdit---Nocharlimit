package ca.wacos.nametagedit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.events.PermissionEntityEvent;

public class NametagHookPEX implements Listener {

	@EventHandler
	void onPermissionChangePEX(PermissionEntityEvent e) {
		if (e.getEntity() instanceof PermissionGroup) {
			PermissionGroup permGroup = (PermissionGroup) e.getEntity();
			for (PermissionUser u : permGroup.getUsers()) {
				String name = u.getName();
				if (!NametagAPI.hasCustomNametag(name)) {
					NametagAPI.resetNametag(name);
				}
			}
		} else if (e.getEntity() != null) {
			String name = e.getEntity().getName();
			if (!NametagAPI.hasCustomNametag(name)) {
				NametagAPI.resetNametag(name);
			}
		}
	}

}
