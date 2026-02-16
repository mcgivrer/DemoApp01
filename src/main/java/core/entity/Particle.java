package core.entity;

import java.awt.Color;

/**
 * Represents a single particle in a {@link ParticleSystem}.
 * <p>
 * Particles are lightweight objects with position, velocity, color, size, and lifetime.
 * They are managed and recycled by their parent ParticleSystem.
 * <p>
 * Unlike full Entity objects, Particles are simple data containers without behaviors
 * to maximize performance when dealing with thousands of particles.
 *
 * @see ParticleSystem
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public class Particle {

    /** X position in world coordinates. */
    public float x;
    /** Y position in world coordinates. */
    public float y;

    /** X velocity in pixels per second. */
    public float vx;
    /** Y velocity in pixels per second. */
    public float vy;

    /** Current particle size (radius or half-width). */
    public float size;
    /** Initial size at spawn (for interpolation). */
    public float initialSize;
    /** Final size at end of life (for interpolation). */
    public float finalSize;

    /** Current color of the particle. */
    public Color color;
    /** Initial color at spawn. */
    public Color initialColor;
    /** Final color at end of life. */
    public Color finalColor;

    /** Current alpha (transparency, 0.0 to 1.0). */
    public float alpha = 1.0f;
    /** Initial alpha at spawn. */
    public float initialAlpha = 1.0f;
    /** Final alpha at end of life. */
    public float finalAlpha = 0.0f;

    /** Remaining lifetime in seconds. When <= 0, particle is inactive. */
    public float lifetime;
    /** Total lifetime at spawn (for interpolation calculations). */
    public float totalLifetime;

    /** Whether this particle is currently active. */
    public boolean active = false;

    /** Rotation angle in degrees (optional, for sprite particles). */
    public float angle = 0;
    /** Angular velocity in degrees per second. */
    public float va = 0;

    /** Gravity multiplier for this particle (0 = no gravity, 1 = normal). */
    public float gravityScale = 1.0f;

    /** Drag coefficient (0 = no drag, 1 = full drag). */
    public float drag = 0.0f;

    /**
     * Creates a new inactive particle.
     */
    public Particle() {
        this.active = false;
    }

    /**
     * Initializes/resets this particle with the given parameters.
     *
     * @param x            Initial X position.
     * @param y            Initial Y position.
     * @param vx           Initial X velocity.
     * @param vy           Initial Y velocity.
     * @param size         Initial size.
     * @param finalSize    Final size at end of life.
     * @param color        Initial color.
     * @param finalColor   Final color at end of life.
     * @param lifetime     Total lifetime in seconds.
     * @param gravityScale Gravity multiplier.
     * @param drag         Drag coefficient.
     * @return this particle for chaining.
     */
    public Particle init(float x, float y, float vx, float vy,
                         float size, float finalSize,
                         Color color, Color finalColor,
                         float lifetime, float gravityScale, float drag) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        this.initialSize = size;
        this.finalSize = finalSize;
        this.color = color;
        this.initialColor = color;
        this.finalColor = finalColor;
        this.alpha = 1.0f;
        this.initialAlpha = 1.0f;
        this.finalAlpha = 0.0f;
        this.lifetime = lifetime;
        this.totalLifetime = lifetime;
        this.gravityScale = gravityScale;
        this.drag = drag;
        this.angle = 0;
        this.va = 0;
        this.active = true;
        return this;
    }

    /**
     * Returns the normalized lifetime (0.0 = just spawned, 1.0 = about to die).
     *
     * @return Progress through lifetime from 0.0 to 1.0.
     */
    public float getLifetimeProgress() {
        if (totalLifetime <= 0) return 1.0f;
        return 1.0f - (lifetime / totalLifetime);
    }

    /**
     * Deactivates this particle, making it available for recycling.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Checks if this particle is still alive (active and has remaining lifetime).
     *
     * @return true if the particle is active and alive.
     */
    public boolean isAlive() {
        return active && lifetime > 0;
    }
}
