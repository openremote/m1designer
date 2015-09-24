package org.openremote.beta.client.shell.floweditor;

import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.IColor;

import static com.ait.lienzo.shared.core.types.ColorName.*;

public interface FlowDesignerConstants {

    String FONT_FAMILY = "Helvetica";

    double ZOOM_FACTOR = 0.015;
    double ZOOM_MAX_SCALE = 2;
    double ZOOM_MIN_SCALE = 0.4;
    double ZOOM_TOUCH_THRESHOLD = 10;
    boolean ZOOM_INVERT = false;

    double PATCH_MIN_WIDTH = 120;
    double PATCH_PADDING = 15;
    double PATCH_LABEL_FONT_SIZE = 12;
    double PATCH_TITLE_FONT_SIZE = 10;
    double PATCH_CORNER_RADIUS = 5;
    IColor PATCH_COLOR = new Color(69, 90, 100);
    IColor PATCH_SELECTED_COLOR = new Color(192, 214, 46);
    IColor PATCH_SELECTED_TEXT_COLOR = new Color(69, 90, 100);
    IColor PATCH_SENSOR_ACTUATOR_COLOR = new Color(102, 0, 146);
    IColor PATCH_VIRTUAL_COLOR = new Color(145, 145, 148);
    IColor PATCH_CLIENT_COLOR = new Color(25, 118, 210);
    IColor PATCH_HEADER_TEXT_COLOR = WHITE;

    String SLOT_SINK_LABEL = "INPUT";
    String SLOT_SOURCE_LABEL = "OUTPUT";
    int SLOT_VALUE_MAX_LENGTH = 7;
    double SLOT_RADIUS = 13;
    double SLOT_PADDING = 2;
    double SLOT_FONT_SIZE = 9;
    double SLOT_VALUE_FONT_SIZE = 8;
    IColor SLOT_SINK_COLOR = new Color(245, 245, 245);
    IColor SLOT_SINK_TEXT_COLOR = BLACK;
    IColor SLOT_SOURCE_COLOR = new Color(204, 204, 204);
    IColor SLOT_SOURCE_TEXT_COLOR = BLACK;
    IColor SLOT_VALUE_TEXT_COLOR = new Color(145, 145, 148);
    IColor SLOT_VALUE_TEXT_HIGHLIGHT_SHADOW_COLOR = new Color(192, 214, 46);
    IColor SLOT_VALUE_TEXT_HIGHLIGHT_COLOR = BLACK;
    IColor SLOT_HANDLE_HIGHLIGHT_OUTLINE_COLOR = BLACK;
    IColor SLOT_SINK_ATTACHED_COLOR = new Color(192, 214, 46);
    IColor SLOT_SOURCE_ATTACHED_COLOR = new Color(192, 214, 46);

    double WIRE_CUBE_DISTANCE = 50;
    double WIRE_WIDTH = 8;
    int WIRE_DELETE_DISTANCE = 50;
    IColor WIRE_COLOR = new Color(192, 214, 46, 0.7);
    IColor WIRE_HANDLE_COLOR = new Color(251, 192, 45);
    IColor WIRE_HANDLE_ATTACH_COLOR = BLACK;
    IColor WIRE_HANDLE_ATTACH_VETO_COLOR = new Color(230, 74, 25);
    IColor WIRE_DELETE_COLOR = new Color(230, 74, 25);

    int TOOLTIP_AUTO_HIDE_MILLIS = 3000;
    IColor TOOLTIP_BACKGROUND_COLOR = new Color(245, 245, 245);
    int TOOLTIP_MAX_SHOW_TIMES = 3;

}
