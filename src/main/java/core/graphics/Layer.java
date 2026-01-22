package core.graphics;

import core.entity.Entity;

public class Layer extends Entity<Layer> {
    public enum LayerType {
        BACKGROUND, MIDGROUND, FOREGROUND, UI, OVERLAY
    }

    private LayerType layerType;
    private int zIndex;

    public Layer(String name, LayerType layerType, int zIndex) {
        super(name);
        this.layerType = layerType;
        this.zIndex = zIndex;
    }

    public Layer add(Entity<?> entity) {
        entity.setLayer(this);
        return this;
    }

    public LayerType getLayerType() {
        return layerType;
    }

    public void setLayerType(LayerType layerType) {
        this.layerType = layerType;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

}
