/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.commands.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * The type Cmd guild.
 *
 * @author ProxyNeko
 */
public final class CmdGuild extends Command {

    /**
     * Instantiates a new Cmd guild.
     */
    public CmdGuild() {
        super();
        name = "guild";
        aliases = new String[]{"server"};
        help = "Gives info about this guild.";
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var guild = event.getGuild();
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();
        final var dateGuildCreated = guild.getTimeCreated().toInstant();

        embed.setTitle("Guild info");
        embed.setColor(Color.GREEN);
        embed.setThumbnail(guild.getIconUrl());
        embed.addField("Guilds name:", guild.getName(), true);
        embed.addField("Member count:", Integer.toString(guild.getMemberCount()), true);
        embed.addField("Emote count:", Long.toString(guild.getEmoteCache().size()), true);
        embed.addField("Category count:", Long.toString(guild.getCategoryCache().size()), true);
        embed.addField("Channel count:", Integer.toString(guild.getChannels().size()), true);
        embed.addField("Role count:", Long.toString(guild.getRoleCache().size()), true);
        embed.addField("Date created:", new SimpleDateFormat("yyyy/MM/dd HH:mm",
            Locale.ENGLISH).format(dateGuildCreated.toEpochMilli()), true);
        embed.addField("Guilds age:", Utils.getTimeDifference(Utils.getLocalTime(dateGuildCreated),
            LocalDateTime.now()), true);
        embed.setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
