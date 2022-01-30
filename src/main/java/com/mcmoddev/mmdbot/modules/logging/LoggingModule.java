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
package com.mcmoddev.mmdbot.modules.logging;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.logging.misc.EventReactionAdded;
import com.mcmoddev.mmdbot.modules.logging.misc.ScamDetector;
import com.mcmoddev.mmdbot.modules.logging.users.EventNicknameChanged;
import com.mcmoddev.mmdbot.modules.logging.users.EventRoleAdded;
import com.mcmoddev.mmdbot.modules.logging.users.EventRoleRemoved;
import com.mcmoddev.mmdbot.modules.logging.users.EventUserJoined;
import com.mcmoddev.mmdbot.modules.logging.users.EventUserLeft;
import com.mcmoddev.mmdbot.modules.logging.users.UserBanned;
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Splits off event logging so we can disable it if the API ever breaks or if we are in dev,
 * this way we can avoid spamming errors or duplicate logging of events.
 *
 * @author ProxyNeko
 */
public class LoggingModule {

    /**
     * Setup and load the bots logging module.
     */
    public static void setupLoggingModule() {
        if (MMDBot.getConfig().isEventLoggingModuleEnabled()) {
            MMDBot.getInstance()
                .addEventListener(
                    new EventUserJoined(),
                    new EventUserLeft(),
                    new EventNicknameChanged(),
                    new EventRoleAdded(),
                    new EventRoleRemoved(),
                    new EventReactionAdded(),
                    new UserBanned(),
                    new ThreadedEventListener(new ScamDetector(), Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r, "ScamDetector"), true))));
            MMDBot.LOGGER.warn("Event logging module enabled and loaded.");
        } else {
            MMDBot.LOGGER.warn("Event logging module disabled via config, Discord event logging won't work right now!");
        }
    }

    public static void executeInLoggingChannel(LoggingType loggingType, Consumer<TextChannel> channel) {
        Utils.getChannelIfPresent(MMDBot.getConfig().getChannel("events." + loggingType.toString()), channel);
    }

    public enum LoggingType {
        IMPORTANT("important");

        private final String string;
        LoggingType(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
