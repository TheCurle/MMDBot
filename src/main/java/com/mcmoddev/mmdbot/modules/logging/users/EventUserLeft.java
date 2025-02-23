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

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.logging.misc.EventReactionAdded;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import com.mcmoddev.mmdbot.utilities.database.dao.PersistedRoles;
import com.mcmoddev.mmdbot.utilities.database.dao.UserFirstJoins;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;

/**
 * The type Event user left.
 *
 * @author
 */
public final class EventUserLeft extends ListenerAdapter {

    /**
     * On guild member remove.
     *
     * @param event the event
     */
    @Override
    public void onGuildMemberRemove(final GuildMemberRemoveEvent event) {
        final var guild = event.getGuild();
        final var channel = guild.getTextChannelById(getConfig().getChannel("events.basic"));
        if (channel == null) {
            return;
        }
        final var member = event.getMember();

        final var guildId = guild.getIdLong();
        if (getConfig().getGuildID() == guildId) {
            final var user = event.getUser();
            LOGGER.info(MMDMarkers.EVENTS, "User {} left the guild", user);
            List<Role> roles = null;
            if (member != null) {
                roles = member.getRoles();
                // Roles from a role panel should be ignored
                final List<Long> roleIds = roles.stream().map(ISnowflake::getIdLong)
                    .filter(id -> !EventReactionAdded.REACTION_ROLES.contains(id)).toList();
                MMDBot.database().useExtension(PersistedRoles.class,
                    persist -> persist.insert(user.getIdLong(), roleIds));
            } else {
                LOGGER.warn(MMDMarkers.EVENTS, "Could not get roles of leaving user {}", user);
            }
            if (member != null) {
                MMDBot.database().useExtension(UserFirstJoins.class,
                    joins -> joins.insert(user.getIdLong(), member.getTimeJoined().toInstant()));
            }

            deleteRecentRequests(guild, user);

            final var embed = new EmbedBuilder();
            embed.setColor(Color.RED);
            embed.setTitle("User Left");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            if (roles != null && !roles.isEmpty()) {
                embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention)
                    .collect(Collectors.joining()), true);
                LOGGER.info(MMDMarkers.EVENTS, "User {} had the following roles before leaving: {}", user, roles);
            } else if (roles == null) {
                embed.addField("Roles:", "_Could not obtain user's roles._", true);
            }
            embed.setFooter("User ID: " + user.getId());
            embed.setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    /**
     * Delete recent requests.
     *
     * @param guild       the guild
     * @param leavingUser the leaving user
     */
    private void deleteRecentRequests(final Guild guild, final User leavingUser) {
        final var requestsChannel = guild.getTextChannelById(getConfig()
            .getChannel("requests.main"));
        final int deletionTime = getConfig().getRequestLeaveDeletionTime();
        if (requestsChannel != null && deletionTime > 0) {
            final OffsetDateTime now = OffsetDateTime.now().minusHours(deletionTime);
            requestsChannel.getIterableHistory()
                .takeWhileAsync(message -> message.getTimeCreated().isAfter(now))
                .thenAccept(messages -> messages.stream()
                    .filter(message -> message.getAuthor().equals(leavingUser))
                    .forEach(message -> message.delete()
                        .reason(String.format("User left, message created at %s, within leave deletion threshold "
                            + "of %s hour(s)", message.getTimeCreated(), deletionTime))
                        .map(s -> true)
                        .onErrorMap(ErrorResponse.UNKNOWN_MESSAGE::test, s -> false)
                        .queue(success -> {
                            LOGGER.info(MMDMarkers.REQUESTS, "Removed request from {} (current leave deletion of "
                                    + "{} hour(s), message sent on {}) because they left the server",
                                leavingUser, deletionTime, message.getTimeCreated());

                            final var logChannel = guild.getTextChannelById(getConfig()
                                .getChannel("events.requests_deletion"));
                            if (logChannel != null) {
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setTitle("Automatic request deletion");
                                builder.setAuthor(leavingUser.getAsTag(), leavingUser.getEffectiveAvatarUrl());
                                builder.appendDescription("Deleted request from ")
                                    .appendDescription(leavingUser.getAsMention())
                                    .appendDescription(" as the user left the server.");
                                builder.addField("Message Creation Time",
                                    TimeFormat.DATE_TIME_SHORT.format(message.getTimeCreated()), true);
                                builder.addField("Auto-Deletion on Leave Duration", deletionTime + " hour(s)", true);
                                builder.setTimestamp(Instant.now());
                                builder.setColor(Color.PINK);
                                builder.setFooter("User ID: " + leavingUser.getId());

                                logChannel.sendMessage(message.getContentRaw())
                                    .setEmbeds(builder.build())
                                    .allowedMentions(Collections.emptySet())
                                    .queue();
                            }
                        })));
        }
    }
}
