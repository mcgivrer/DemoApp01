package core.behavior.particle;

import core.behavior.Behavior;
import core.entity.Entity;
import core.entity.ParticleSystem;

/**
 * Base interface for particle emitter behaviors.
 * <p>
 * Emitter behaviors control how and when particles are spawned from a
 * {@link ParticleSystem}. Implementations can define different emission
 * patterns such as fountains, explosions, trails, etc.
 *
 * @see ParticleSystem
 * @see FountainEmitterBehavior
 * @see ExplosionEmitterBehavior
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public interface ParticleEmitterBehavior extends Behavior<ParticleSystem> {

    /**
     * Updates the emitter, potentially spawning new particles.
     *
     * @param entity    The entity (should be cast to ParticleSystem).
     * @param deltaTime Time elapsed since last update in seconds.
     */
    @Override
    void update(Entity<?> entity, float deltaTime);

    /**
     * Triggers an emission burst (for burst-type emitters like explosions).
     * <p>
     * Continuous emitters may ignore this method; burst emitters should
     * spawn particles immediately when called.
     *
     * @param system The particle system to emit from.
     */
    default void burst(ParticleSystem system) {
        // Default: do nothing (continuous emitters)
    }

    /**
     * Resets the emitter to its initial state.
     */
    default void reset() {
        // Default: do nothing
    }
}
