package org.wanna.jabbot.command.messaging.body;

/**
 * This is the Raw Text implementation of BodyPart
 *
 * @see {@link org.wanna.jabbot.command.messaging.body.BodyPart}
 *
 * @author Vincent Morsiani [vmorsiani@voxbone.com]
 * @since 2015-07-14
 */
public class TextBodyPart implements BodyPart {
    private final String text;

    /**
     * Constructor
     * @param text raw text
     */
    public TextBodyPart(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BodyPart.Type getType() {
        return Type.TEXT;
    }
}