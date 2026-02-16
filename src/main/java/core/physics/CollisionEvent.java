package core.physics;

import core.entity.GameObject;

/**
 * Immutable data describing a collision between two {@link GameObject} entities,
 * from the perspective of the <em>self</em> entity.
 * <p>
 * The collision normal ({@code nx}, {@code ny}) always points <strong>away</strong>
 * from the {@code other} entity toward {@code self}.
 *
 * @param other         The other entity involved in the collision.
 * @param nx            X component of the collision normal (unit vector).
 * @param ny            Y component of the collision normal (unit vector).
 * @param penetration   Depth of penetration along the normal axis (always &ge; 0).
 * @param selfDynamic   {@code true} if the receiving entity is DYNAMIC.
 * @param otherDynamic  {@code true} if the other entity is DYNAMIC.
 * @param otherKinematic {@code true} if the other entity is KINEMATIC.
 *
 * @see CollisionBehavior
 * @see CollisionManager
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.2
 */
public record CollisionEvent(
        GameObject other,
        float nx, float ny,
        float penetration,
        boolean selfDynamic,
        boolean otherDynamic,
        boolean otherKinematic) {
}
