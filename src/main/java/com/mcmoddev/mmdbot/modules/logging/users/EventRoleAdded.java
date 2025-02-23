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
package com.mcmoddev.mmdbot.modules.logging.users;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mcmoddev.mmdbot.modules.logging.misc.EventReactionAdded;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;

/**
 * The type Event role added.
 *
 * @author
 */
public final class EventRoleAdded extends ListenerAdapter {

    /**
     * Multimap of users to roles which will be ignored when encountered for the first time.
     * <p>
     * Once a user to role entry has been encountered, it is removed from the map.
     * <p>
     * Used to ignore roles added back by {@link EventUserJoined role persistance}.
     */
    public static final Multimap<User, Role> IGNORE_ONCE = HashMultimap.create();

    /**
     * On guild member role add.
     *
     * @param event the event
     */
    @Override
    public void onGuildMemberRoleAdd(final GuildMemberRoleAddEvent event) {
        final var guild = event.getGuild();

        if (getConfig().getGuildID() != guild.getIdLong()) {
            return; // Make sure that we don't post if it's not related to 'our' guild
        }

        if (event.getRoles().stream().anyMatch(r -> EventReactionAdded.REACTION_ROLES.contains(r.getIdLong()))) {
            return; // If this was a reaction role, don't log it
        }

        final List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
        final List<Role> addedRoles = new ArrayList<>(event.getRoles());
        previousRoles.removeAll(addedRoles); // Just if the member has already been updated

        final var target = event.getUser();
        if (IGNORE_ONCE.containsKey(target)) { // Check for ignored roles
            final Iterator<Role> ignoredRoles = IGNORE_ONCE.get(target).iterator();
            while (ignoredRoles.hasNext()) {
                final var ignored = ignoredRoles.next();
                if (addedRoles.contains(ignored)) { // Remove ignored roles from event listing and ignore map
                    LOGGER.info(MMDMarkers.EVENTS, "Role {} for {} was in role ignore map, removing from map and ignoring",
                        ignored, target);
                    addedRoles.remove(ignored);
                    ignoredRoles.remove();
                }
            }
            if (addedRoles.isEmpty()) { // If all the roles were ignored, exit out.
                return;
            }
        }

        final long channelID = getConfig().getChannel("events.important");
        Utils.getChannelIfPresent(channelID, channel ->
            guild.retrieveAuditLogs()
                .type(ActionType.MEMBER_ROLE_UPDATE)
                .limit(1)
                .cache(false)
                .map(list -> list.get(0))
                .flatMap(entry -> {
                    final var embed = new EmbedBuilder();

                    embed.setColor(Color.YELLOW);
                    embed.setTitle("User Role(s) Added");
                    embed.setThumbnail(target.getEffectiveAvatarUrl());
                    embed.addField("User:", target.getAsMention() + " (" + target.getId() + ")",
                        true);
                    if (entry.getTargetIdLong() != target.getIdLong()) {
                        LOGGER.warn(MMDMarkers.EVENTS, "Inconsistency between target of retrieved audit log entry and actual "
                            + "role event target: retrieved is {}, but target is {}", entry.getUser(), target);
                    } else if (entry.getUser() != null) {
                        final var editor = entry.getUser();
                        embed.addField("Editor:", editor.getAsMention() + " (" + editor.getId() + ")",
                            true);
                    }
                    embed.addField("Previous Role(s):", previousRoles.stream().map(IMentionable::getAsMention)
                        .collect(Collectors.joining(" ")), false);
                    embed.addField("Added Role(s):", addedRoles.stream().map(IMentionable::getAsMention)
                        .collect(Collectors.joining(" ")), false);
                    embed.setTimestamp(Instant.now());

                    LOGGER.info(MMDMarkers.EVENTS, "Role(s) {} was added to user {} by {}", addedRoles, target, entry.getUser());

                    return channel.sendMessageEmbeds(embed.build());
                })
                .queue()
        );
    }
}
