package org.openremote.beta.client.flowdesigner;

import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.IColor;

import static com.ait.lienzo.shared.core.types.ColorName.*;

public interface FlowDesignerConstants {

    String FONT_FAMILY = "Helvetica";

    int TOOLTIP_AUTO_HIDE_MILLIS = 3000;

    double ZOOM_FACTOR = 0.04;
    double ZOOM_MAX_SCALE = 2;
    double ZOOM_MIN_SCALE = 0.4;
    double ZOOM_TOUCH_THRESHOLD = 10;
    boolean ZOOM_INVERT = false;

    double PATCH_PADDING = 15;
    double PATCH_TITLE_PADDING = 5;
    double SLOT_PADDING = 10;

    double PATCH_LABEL_FONT_SIZE = 12;
    double PATCH_TITLE_FONT_SIZE = 10;
    double SLOT_FONT_SIZE = 10;

    double PATCH_CORNER_RADIUS = 5;
    double PATCH_TITLE_CORNER_RADIUS = 5;
    double SLOT_CORNER_RADIUS = 5;

    double WIRE_CUBE_DISTANCE = 80;
    double WIRE_WIDTH = 8;
    int WIRE_DELETE_DISTANCE = 50;

    IColor BACKGROUND_COLOR = WHITE;
    IColor TOOLTIP_BACKGROUND_COLOR = new Color(240, 240, 240);
    IColor PATCH_COLOR = new Color(69, 90, 100);
    IColor PATCH_TITLE_COLOR = new Color(240, 240, 240);
    IColor PATCH_TITLE_TEXT_COLOR = new Color(0, 98, 36);
    IColor PATCH_LABEL_TEXT_COLOR = WHITE;

    IColor SLOT_SINK_COLOR = new Color(240, 240, 240);
    IColor SLOT_SINK_TEXT_COLOR = BLACK;
    IColor SLOT_SOURCE_COLOR = new Color(240, 240, 240);
    IColor SLOT_SOURCE_TEXT_COLOR = BLACK;

    IColor SLOT_SINK_ATTACHED_COLOR = new Color(192, 214, 46);
    IColor SLOT_SOURCE_ATTACHED_COLOR = new Color(192, 214, 46);
    IColor WIRE_COLOR = new Color(192, 214, 46);
    IColor WIRE_HANDLE_COLOR = new Color(251, 192, 45);
    IColor WIRE_HANDLE_ATTACH_COLOR = BLACK;
    IColor WIRE_HANDLE_ATTACH_TEXT_COLOR = new Color(192, 214, 46);
    IColor WIRE_HANDLE_ATTACH_VETO_COLOR = new Color(230, 74, 25);
    IColor WIRE_DELETE_COLOR = new Color(230, 74, 25);

}
