/*  This file is part of MeggaChat.
 * 
 *  MeggaChat is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MeggaChat is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MeggaChat.  If not, see <http://www.gnu.org/licenses/>.
 */
package co.mcme.meggachat.listeners;

import co.mcme.meggachat.MeggaChatPlugin;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;

public class ColoredListListener implements Listener {

    public void onJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        String oldname = event.getPlayer().getName();
        String newname = oldname;
        if (joiner.getName().length() == 15) {
            int len = oldname.length();
            int removechars = len - 1;
            newname = oldname.substring(0, removechars);
        }
        if (joiner.getName().length() == 16) {
            int len = oldname.length();
            int removechars = len - 2;
            newname = oldname.substring(0, removechars);
        }
        ColorName(joiner, newname);
    }

    public void ColorName(Player player, String name) {
        Character color = 'f';
        for (Entry<Character, Permission> entry : MeggaChatPlugin.getPermissionsUtil().getTabColorPermissions().entrySet()) {
            if (player.hasPermission(entry.getValue())) {
                color = entry.getKey();
            }
        }
        player.setPlayerListName("§" + color + name);
    }
}