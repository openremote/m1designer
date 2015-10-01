package org.openremote.shared.inventory;

import com.google.gwt.core.client.js.JsType;

import java.util.Locale;

@JsType
public class ClientPreset {

    public Long id;
    public String name;
    public String agentLike;
    public int minWidth;
    public int maxWidth;
    public int minHeight;
    public int maxHeight;
    public String initialFlowId;

    public ClientPreset() {
    }

    public ClientPreset(Long id, String name, String agentLike) {
        this.id = id;
        this.name = name;
        this.agentLike = agentLike;
    }

    public ClientPreset(Long id, String name, String agentLike, int minWidth, int maxWidth, int minHeight, int maxHeight) {
        this.id = id;
        this.name = name;
        this.agentLike = agentLike;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAgentLike() {
        return agentLike;
    }

    public void setAgentLike(String agentLike) {
        this.agentLike = agentLike;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public String getInitialFlowId() {
        return initialFlowId;
    }

    public void setInitialFlowId(String initialFlowId) {
        this.initialFlowId = initialFlowId;
    }

    public boolean matches(ClientPresetVariant variant) {
        boolean agentMatch = false;
        if (getAgentLike() != null && variant.getUserAgent() != null) {
            String variantAgent = variant.getUserAgent().toLowerCase(Locale.ROOT);
            String agentLike = getAgentLike().toLowerCase(Locale.ROOT);
            if (variantAgent.contains(agentLike)) {
                agentMatch = true;
            }
        }

        boolean haveVariantDimensions = variant.getWidthPixels() > 0 && variant.getHeightPixels() > 0;
        boolean havePresetMinDimensions = getMinWidth() > 0 || getMinHeight() > 0;
        boolean havePresetMaxDimensions = getMaxWidth() > 0 || getMaxHeight() > 0;

        if (!havePresetMinDimensions && !havePresetMaxDimensions) {
            // No further constraints, agent match is the only constraint...
            return agentMatch;
        } else if (!haveVariantDimensions) {
            // Have min/max constraints but missing client variant details, no match...
            return false;
        }

        // Apply min/max size constraints
        boolean dimensionsMatch = true;
        if (getMinHeight() > 0 && variant.getHeightPixels() < getMinHeight()) {
            dimensionsMatch = false;
        }
        if (getMaxHeight() > 0 && variant.getHeightPixels() > getMaxHeight()) {
            dimensionsMatch = false;
        }
        if (getMinWidth() > 0 && variant.getWidthPixels() < getMinWidth()) {
            dimensionsMatch = false;
        }
        if (getMaxWidth() > 0 && variant.getWidthPixels() > getMaxWidth()) {
            dimensionsMatch = false;
        }

        return agentMatch && dimensionsMatch;
    }

    @Override
    public String toString() {
        return "ClientPreset{" +
            "name='" + name + '\'' +
            ", agentLike='" + agentLike + '\'' +
            ", minWidth=" + minWidth +
            ", maxWidth=" + maxWidth +
            ", minHeight=" + minHeight +
            ", maxHeight=" + maxHeight +
            ", initialFlowId='" + initialFlowId + '\'' +
            '}';
    }
}
