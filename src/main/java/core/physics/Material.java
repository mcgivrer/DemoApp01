package core.physics;

/**
 * {@link Material} class representing physical properties such as friction and
 * restitution. These properties influence how entities interact with each other
 * and with the environment in a physics simulation.
 * 
 * @param name        The name of the material.
 * @param friction    The friction coefficient of the material (0.0 to 1.0).
 * @param restitution The restitution (bounciness) coefficient of the material
 *                    (0.0 to 1.0).
 * @see core.entity.Entity
 * @see core.physics.PhysicsEngine
 */
public record Material(String name, float friction, float restitution) {
    public static final Material DEFAULT = new Material("default", 0.1f, 0.001f);
    public static final Material ICE = new Material("ice", 0.01f, 0.0f);
    public static final Material RUBBER = new Material("rubber", 0.9f, 0.8f);
    public static final Material WOOD = new Material("wood", 0.7f, 0.3f);
    public static final Material STEEL = new Material("steel", 0.2f, 0.1f);
    public static final Material WATER = new Material("water", 0.05f, 0.0f);
    public static final Material SUPERBALL = new Material("superball", 0.002f, 0.001f);
}
