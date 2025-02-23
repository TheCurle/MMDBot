/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.utilities.scripting.object;

import com.mcmoddev.mmdbot.utilities.scripting.ExposeScripting;
import net.dv8tion.jda.api.entities.RoleIcon;

public class ScriptRoleIcon {

    @ExposeScripting
    public final String id;
    @ExposeScripting
    public final String url;
    @ExposeScripting
    public final String emoji;

    public ScriptRoleIcon(RoleIcon roleIcon) {
        this.id = roleIcon.getIconId();
        this.url = roleIcon.getIconUrl();
        this.emoji = roleIcon.getEmoji();
    }

}
