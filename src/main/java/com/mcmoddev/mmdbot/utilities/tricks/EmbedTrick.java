package com.mcmoddev.mmdbot.utilities.tricks;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;
import java.util.List;

/**
 * The type Embed trick.
 *
 * @author williambl
 */
public class EmbedTrick implements Trick {

    /**
     * Name of the trick.
     */
    private final List<String> names;

    /**
     * Trick title.
     */
    private final String title;

    /**
     * The description of the trick.
     */
    private final String description;

    /**
     * The embed edge color for the trick.
     */
    private final int color;

    /**
     * The Fields.
     */
    private final List<MessageEmbed.Field> fields;

    /**
     * Instantiates a new Embed trick.
     *
     * @param pNames       Name of the trick.
     * @param pTitle       Trick title.
     * @param pDescription The description of the trick.
     * @param pColor       The embed edge color for the trick.
     * @param pFields      the fields
     */
    public EmbedTrick(final List<String> pNames, final String pTitle, final String pDescription, final int pColor,
                      final MessageEmbed.Field... pFields) {
        this.names = pNames;
        this.title = pTitle;
        this.description = pDescription;
        this.color = pColor;
        this.fields = Arrays.asList(pFields);
    }

    /**
     * Gets names.
     *
     * @return The names of the tricks.
     */
    @Override
    public List<String> getNames() {
        return names;
    }

    /**
     * Gets message.
     *
     * @param args the args
     * @return message
     */
    @Override
    public Message getMessage(final String[] args) {
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(getTitle())
            .setDescription(getDescription())
            .setColor(color);
        for (MessageEmbed.Field field : getFields()) {
            builder.addField(field);
        }
        return new MessageBuilder(builder.build()).build();
    }

    /**
     * Gets description.
     *
     * @return Get the tricks description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets title.
     *
     * @return Get the title of the trick.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets color.
     *
     * @return Get the embed color for the trick.
     */
    public int getColor() {
        return color;
    }

    /**
     * Gets fields.
     *
     * @return fields fields
     */
    public List<MessageEmbed.Field> getFields() {
        return fields;
    }

    /**
     * The type Type.
     */
    static class Type implements TrickType<EmbedTrick> {

        /**
         * Gets clazz.
         *
         * @return clazz
         */
        @Override
        public Class<EmbedTrick> getClazz() {
            return EmbedTrick.class;
        }

        /**
         * Gets arg names.
         *
         * @return arg names
         */
        @Override
        public List<String> getArgNames() {
            return Lists.newArrayList("names", "title", "description", "color", "fields");
        }

        /**
         * Create from args embed trick.
         *
         * @param args the args
         * @return embed trick
         */
        @Override
        public EmbedTrick createFromArgs(final String args) {
            String[] argsArray = args.split(" \\| ");

            final int colorField = 3;
            final int base16 = 16;

            return new EmbedTrick(
                Arrays.asList(argsArray[0].split(" ")),
                argsArray[1],
                argsArray[2],
                Integer.parseInt(argsArray[colorField].replace("#", ""), base16)
            );
        }
    }
}
