package org.openremote.shared.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.util.Util;

@JsType
@JsonSubTypes({
    @JsonSubTypes.Type(value = FlowDeployEvent.class),
    @JsonSubTypes.Type(value = FlowDeploymentFailureEvent.class),
    @JsonSubTypes.Type(value = FlowRuntimeFailureEvent.class),
    @JsonSubTypes.Type(value = FlowRequestStatusEvent.class),
    @JsonSubTypes.Type(value = FlowStatusEvent.class),
    @JsonSubTypes.Type(value = FlowStopEvent.class),
    @JsonSubTypes.Type(value = Message.class),
})
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "event"
)
public abstract class Event {

  public static String getType(String simpleClassName) {
        String type = Util.toLowerCaseDash(simpleClassName);

        if (type.length() > 6 && type.substring(type.length()-6).equals("-event"))
            type = type.substring(0, type.length()-6);

        return type;
    }

    public static String getType(Class<? extends Event> actionClass) {
        return getType(actionClass.getSimpleName());
    }

    public String getType() {
        return getType(getClass());
    }

}
