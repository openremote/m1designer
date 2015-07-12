package org.openremote.beta.client.flowdesigner;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.IColor;

import java.util.logging.Logger;

/**
 * A box with rounded corners and optional centered text label.
 */
public class Box extends Group {

    private static final Logger LOG = Logger.getLogger(Box.class.getName());

    public static class TextLabel extends Text {

        public static String space(double spaces) {
            String space = " ";
            for (int i = 0; i < spaces; i++) {
                space += " ";
            }
            return space;
        }

        public TextLabel(String text, String family, double fontSize, IColor textColor) {
            super(text, family, fontSize);
            setFillColor(textColor);
        }

    }

    final protected TextLabel textLabel;
    final protected double padding;
    final protected Rectangle body;

    public Box(double cornerRadius, IColor backgroundColor, TextLabel textLabel, double padding) {
        this.textLabel = textLabel;
        this.padding = padding;

        body = new Rectangle(0, 0, cornerRadius);
        body.setFillColor(backgroundColor);
        add(body);

        if (textLabel != null) {
            add(textLabel);
        }
        setWidth(0);
        setHeight(0);
    }

    public double getWidth() {
        return body.getWidth();
    }

    public double getHeight() {
        return body.getHeight();
    }

    public void setWidth(double width) {
        setWidth(width, false);
    }

    public void setWidth(double width, boolean force) {
        // Set width of rectangle if the given size is larger than text + padding or force
        body.setWidth(
            force || textLabel == null || width > textLabel.getBoundingBox().getWidth() + padding * 2
                ? width
                : textLabel.getBoundingBox().getWidth() + padding * 2
        );
        if (textLabel != null)
            textLabel.setX(body.getX() + body.getWidth() / 2 - textLabel.getBoundingBox().getWidth() / 2);
    }

    public void setHeight(double height) {
        setHeight(height, false);
    }

    public void setHeight(double height, boolean force) {
        // Set height of rectangle if the given size is larger than text + padding
        body.setHeight(
            force || textLabel == null || height > textLabel.getBoundingBox().getHeight() + padding * 2
                ? height
                : textLabel.getBoundingBox().getHeight() + padding * 2
        );
        if (textLabel != null)
            textLabel.setY(body.getY() + body.getHeight() / 2 + textLabel.getFontSize() * 0.45);
    }

    public void centerHorizontal(Box reference) {
        setX(getX() + reference.getWidth() / 2 - getWidth() / 2);
    }

    public void setFillColor(IColor color) {
        body.setFillColor(color);
    }

    public void setText(String text) {
        textLabel.setText(text);
    }

    public String getText() {
        return textLabel.getText();
    }


}
