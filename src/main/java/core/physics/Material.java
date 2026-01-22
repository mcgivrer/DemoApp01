package core.physics;

public record Material(String name, float friction, float restitution) {
    public static final Material DEFAULT = new Material("default", 0.5f, 0.5f);
    public static final Material ICE = new Material("ice", 0.1f, 0.0f);
    public static final Material RUBBER = new Material("rubber", 0.9f, 0.8f);
    public static final Material WOOD = new Material("wood", 0.7f, 0.3f);
    public static final Material STEEL = new Material("steel", 0.2f, 0.1f);
    public static final Material WATER = new Material("water", 0.05f, 0.0f);
    public static final Material SUPERBALL = new Material("superball", 0.2f, 0.9f);
}
