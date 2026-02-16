package core.behavior.particle;

import java.awt.Color;

import core.entity.Entity;
import core.entity.Particle;
import core.entity.ParticleSystem;

/**
 * Emitter behavior that creates a continuous fountain of particles.
 * <p>
 * Particles are emitted upward from the system's position with some
 * random spread in velocity. The emission rate controls how many
 * particles are spawned per second.
 * <p>
 * Typical fountain characteristics:
 * <ul>
 *   <li>Continuous emission while {@code emitting = true}</li>
 *   <li>Upward initial velocity with horizontal spread</li>
 *   <li>Particles affected by gravity (fall back down)</li>
 *   <li>Color fades from initialColor to finalColor</li>
 * </ul>
 *
 * @see ParticleEmitterBehavior
 * @see ParticleSystem
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public class FountainEmitterBehavior implements ParticleEmitterBehavior {

    /** Particles emitted per second. */
    private float emissionRate = 50.0f;

    /** Accumulated time for emission (to handle fractional particles). */
    private float emissionAccumulator = 0.0f;

    /** Base upward velocity (negative Y = upward). */
    private float baseVelocityY = -400.0f;

    /** Random spread in X velocity (+/- this value). */
    private float spreadX = 100.0f;

    /** Random variation in Y velocity (+/- this value). */
    private float spreadY = 50.0f;

    /** Initial particle color. */
    private Color initialColor = Color.CYAN;

    /** Final particle color (at end of life). */
    private Color finalColor = new Color(0, 100, 255, 0);

    /** Particle lifetime range (min). */
    private float minLifetime = 1.0f;

    /** Particle lifetime range (max). */
    private float maxLifetime = 2.5f;

    /** Initial particle size. */
    private float initialSize = 6.0f;

    /** Final particle size. */
    private float finalSize = 2.0f;

    /** Gravity scale for particles. */
    private float gravityScale = 1.0f;

    /** Drag coefficient. */
    private float drag = 0.01f;

    /**
     * Creates a fountain emitter with default settings.
     */
    public FountainEmitterBehavior() {
    }

    /**
     * Creates a fountain emitter with custom emission rate.
     *
     * @param emissionRate Particles per second.
     */
    public FountainEmitterBehavior(float emissionRate) {
        this.emissionRate = emissionRate;
    }

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        if (!(entity instanceof ParticleSystem system)) {
            return;
        }

        if (!system.isEmitting()) {
            return;
        }

        // Accumulate time for emission
        emissionAccumulator += deltaTime * emissionRate;

        // Spawn particles based on accumulated time
        while (emissionAccumulator >= 1.0f) {
            emissionAccumulator -= 1.0f;
            spawnParticle(system);
        }
    }

    /**
     * Spawns a single particle from the fountain.
     *
     * @param system The particle system.
     */
    private void spawnParticle(ParticleSystem system) {
        Particle p = system.getInactiveParticle();
        if (p == null) {
            return; // Pool is full
        }

        // Position at system center
        float px = system.x + system.getWidth() / 2.0f;
        float py = system.y + system.getHeight() / 2.0f;

        // Velocity with random spread
        float vx = (float) (Math.random() * 2 - 1) * spreadX;
        float vy = baseVelocityY + (float) (Math.random() * 2 - 1) * spreadY;

        // Random lifetime
        float lifetime = minLifetime + (float) Math.random() * (maxLifetime - minLifetime);

        // Initialize particle
        p.init(px, py, vx, vy,
                initialSize, finalSize,
                initialColor, finalColor,
                lifetime, gravityScale, drag);
    }

    @Override
    public void reset() {
        emissionAccumulator = 0.0f;
    }

    // --- Builder-style setters ---

    public FountainEmitterBehavior setEmissionRate(float rate) {
        this.emissionRate = rate;
        return this;
    }

    public FountainEmitterBehavior setBaseVelocityY(float vy) {
        this.baseVelocityY = vy;
        return this;
    }

    public FountainEmitterBehavior setSpread(float spreadX, float spreadY) {
        this.spreadX = spreadX;
        this.spreadY = spreadY;
        return this;
    }

    public FountainEmitterBehavior setColors(Color initial, Color finalColor) {
        this.initialColor = initial;
        this.finalColor = finalColor;
        return this;
    }

    public FountainEmitterBehavior setLifetimeRange(float min, float max) {
        this.minLifetime = min;
        this.maxLifetime = max;
        return this;
    }

    public FountainEmitterBehavior setSizeRange(float initial, float finalSize) {
        this.initialSize = initial;
        this.finalSize = finalSize;
        return this;
    }

    public FountainEmitterBehavior setGravityScale(float scale) {
        this.gravityScale = scale;
        return this;
    }

    public FountainEmitterBehavior setDrag(float drag) {
        this.drag = drag;
        return this;
    }

    public float getEmissionRate() {
        return emissionRate;
    }
}
