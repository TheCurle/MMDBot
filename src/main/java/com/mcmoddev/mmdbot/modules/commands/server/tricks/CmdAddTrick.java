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
package com.mcmoddev.mmdbot.modules.commands.server.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Trick;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

/**
 * Adds a trick to the list.
 * <p>
 * Has two subcommands;
 * - string;
 * - Takes one parameter - the content of the string trick.
 * - embed;
 * - Takes three parameters; name, description and color. All used for constructing the embed.
 *
 * @author Will BL
 * @author Curle
 */
public final class CmdAddTrick extends SlashCommand {

    /**
     * Instantiates a new Cmd add trick.
     */
    public CmdAddTrick() {
        super();
        name = "addtrick";
        help = "Adds a new trick, either a string or an embed, if a string you only need the <names> and <body>.";
        category = new Category("Info");
        arguments = "(<string> <trick content body> (or) <embed> <title> "
            + "<description> <colour-as-hex-code>";
        aliases = new String[]{"add-trick"};
        enabledRoles = new String[]{Long.toString(MMDBot.getConfig().getRole("bot_maintainer"))};
        guildOnly = true;
        // we need to use this unfortunately :( can't create more than one commandclient
        //guildId = Long.toString(MMDBot.getConfig().getGuildID());

        children = Tricks.getTrickTypes().entrySet().stream().map(entry -> new SubCommand(entry.getKey(), entry.getValue())).toArray(SlashCommand[]::new);
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
    }

    /**
     * A child command of AddTrick, handles adding a particular type of trick.
     *
     * @author Curle
     * @author Will BL
     */
    private static class SubCommand extends SlashCommand {
        private final Trick.TrickType<?> trickType;

        public SubCommand(String name, Trick.TrickType<?> trickType) {
            this.trickType = trickType;
            this.name = name;
            this.help = "Create a " + name + "-type trick.";
            this.guildOnly = true;
            this.options = trickType.getArgs();
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            try {
                Tricks.addTrick(trickType.createFromCommand(event));
                event.reply("Added trick!").mentionRepliedUser(false).setEphemeral(true).queue();
            } catch (IllegalArgumentException e) {
                event.reply("A command with that name already exists!").mentionRepliedUser(false).setEphemeral(true).queue();
                MMDBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
            }
        }
    }

    public static final class Prefix extends Command {

        public Prefix() {
            name = "addtrick";
            arguments = "(<string> <trick content body> (or) <embed> <title> "
                + "<description> <colour-as-hex-code>";
            aliases = new String[]{"add-trick"};
            requiredRole = "Bot Maintainer";
            guildOnly = true;
            children = Tricks.getTrickTypes().entrySet().stream().map(e -> new PrefixSubCmd(e.getKey(), e.getValue())).toArray(Command[]::new);
        }

        @Override
        protected void execute(final CommandEvent event) {

        }
    }

    private static final class PrefixSubCmd extends Command {

        private final Trick.TrickType<?> trickType;

        private PrefixSubCmd(final String name, final Trick.TrickType<?> trickType) {
            this.trickType = trickType;
            this.name = name;
        }

        @Override
        protected void execute(final CommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            Tricks.addTrick(trickType.createFromArgs(event.getArgs()));
            event.getMessage().reply("Added trick!").mentionRepliedUser(false).queue();
        }
    }
}
