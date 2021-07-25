package com.mcmoddev.mmdbot.modules.commands.server.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Trick;

import java.util.List;

/**
 * @author williambl
 *
 * The type Cmd run trick.
 */
public final class CmdRunTrick extends Command {

    /**
     * The Trick.
     */
    private final Trick trick;

    /**
     * Instantiates a new Cmd run trick.
     *
     * @param pTrick the trick
     */
    public CmdRunTrick(final Trick pTrick) {
        super();
        this.trick = pTrick;
        List<String> trickNames = pTrick.getNames();
        name = trickNames.get(0);
        aliases = trickNames.size() > 1 ? trickNames.subList(1, trickNames.size())
            .toArray(new String[0]) : new String[0];
        hidden = true;
    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var channel = event.getTextChannel();

        channel.sendMessage(trick.getMessage(event.getArgs().split(" "))).queue();
    }
}
