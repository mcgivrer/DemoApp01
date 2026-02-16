package core.entity;

import java.awt.Color;

/**
 * A particle system that manages a pool of {@link Particle} objects.
 * <p>
 * ParticleSystem extends {@link GameObject} and delegates particle creation,
 * animation, and rendering to {@link core.behavior.Behavior} implementations:
 * <ul>
 *   <li><strong>Emitter Behaviors</strong>: Control how/when particles are spawned
 *       (e.g., FountainEmitterBehavior, ExplosionEmitterBehavior)</li>
 *   <li><strong>Physics Behaviors</strong>: Update particle positions based on
 *       velocity, gravity, and drag (e.g., ParticlePhysicsBehavior)</li>
 *   <li><strong>Render Plugins</strong>: Draw particles to screen
 *       (e.g., ParticleRenderPlugin)</li>
 * </ul>
 * <p>
 * Particles are pre-allocated in a pool for performance. Inactive particles
 * are recycled when new particles need to be spawned.
 *
 * @see Particle
 * @see core.behavior.particle.ParticleEmitterBehavior
 * @see core.behavior.particle.ParticlePhysicsBehavior
 * @see core.graphics.ParticleRenderPlugin
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public class ParticleSystem extends GameObject {

    /** The particle pool (pre-allocated). */
    private final Particle[] particles;

    /** Maximum number of particles this system can hold. */
    private final int maxParticles;

    /** Number of currently active particles. */
    private int activeCount = 0;

    /** Whether the emitter is currently emitting. */
    private boolean emitting = true;

    /** Default particle lifetime in seconds. */
    private float defaultLifetime = 2.0f;

    /** Default initial particle size. */
    private float defaultSize = 4.0f;

    /** Default final particle size. */
    private float defaultFinalSize = 1.0f;

    /** Default particle color. */
    private Color defaultColor = Color.YELLOW;

    /** Default final particle color. */
    private Color defaultFinalColor = Color.RED;

    /** Default gravity scale for particles. */
    private float defaultGravityScale = 1.0f;

    /** Default drag coefficient for particles. */
    private float defaultDrag = 0.02f;

    /** World gravity (inherited from World or set manually). */
    private float gravity = 9.81f;

    /**
     * Creates a new ParticleSystem with the specified name and maximum particle count.
     *
     * @param name         The name of the particle system.
     * @param maxParticles Maximum number of particles in the pool.
     */
    public ParticleSystem(String name, int maxParticles) {
        super(name);
        this.maxParticles = maxParticles;
        this.particles = new Particle[maxParticles];

        // Pre-allocate all particles
        for (int i = 0; i < maxParticles; i++) {
            particles[i] = new Particle();
        }

        // By default, particle systems don't collide
        setPhysicsType(PhysicsType.NONE);
        setShapeType(ShapeType.POINT);
    }

    /**
     * Returns the particle array (for behaviors and renderers).
     *
     * @return The array of all particles (active and inactive).
     */
    public Particle[] getParticles() {
        return particles;
    }

    /**
     * Returns the maximum number of particles.
     *
     * @return Maximum particle count.
     */
    public int getMaxParticles() {
        return maxParticles;
    }

    /**
     * Returns the current number of active particles.
     *
     * @return Active particle count.
     */
    public int getActiveCount() {
        return activeCount;
    }

    /**
     * Recalculates and returns the count of active particles.
     * Call this after particle updates to keep the count accurate.
     *
     * @return Updated active particle count.
     */
    public int updateActiveCount() {
        activeCount = 0;
        for (Particle p : particles) {
            if (p.active) activeCount++;
        }
        return activeCount;
    }

    /**
     * Finds and returns an inactive particle from the pool for recycling.
     *
     * @return An inactive particle, or null if all particles are active.
     */
    public Particle getInactiveParticle() {
        for (Particle p : particles) {
            if (!p.active) {
                return p;
            }
        }
        return null;
    }

    /**
     * Spawns a new particle at the given position with the given velocity.
     * Uses default values for other particle properties.
     *
     * @param x  X position.
     * @param y  Y position.
     * @param vx X velocity.
     * @param vy Y velocity.
     * @return The spawned particle, or null if pool is full.
     */
    public Particle spawn(float x, float y, float vx, float vy) {
        Particle p = getInactiveParticle();
        if (p != null) {
            p.init(x, y, vx, vy,
                    defaultSize, defaultFinalSize,
                    defaultColor, defaultFinalColor,
                    defaultLifetime, defaultGravityScale, defaultDrag);
            activeCount++;
        }
        return p;
    }

    /**
     * Spawns a new particle with full customization.
     *
     * @param x            X position.
     * @param y            Y position.
     * @param vx           X velocity.
     * @param vy           Y velocity.
     * @param size         Initial size.
     * @param finalSize    Final size.
     * @param color        Initial color.
     * @param finalColor   Final color.
     * @param lifetime     Lifetime in seconds.
     * @param gravityScale Gravity multiplier.
     * @param drag         Drag coefficient.
     * @return The spawned particle, or null if pool is full.
     */
    public Particle spawn(float x, float y, float vx, float vy,
                          float size, float finalSize,
                          Color color, Color finalColor,
                          float lifetime, float gravityScale, float drag) {
        Particle p = getInactiveParticle();
        if (p != null) {
            p.init(x, y, vx, vy, size, finalSize, color, finalColor,
                    lifetime, gravityScale, drag);
            activeCount++;
        }
        return p;
    }

    /**
     * Deactivates all particles in the system.
     */
    public void clear() {
        for (Particle p : particles) {
            p.deactivate();
        }
        activeCount = 0;
    }

    // --- Getters and Setters ---

    public boolean isEmitting() {
        return emitting;
    }

    public ParticleSystem setEmitting(boolean emitting) {
        this.emitting = emitting;
        return this;
    }

    public float getDefaultLifetime() {
        return defaultLifetime;
    }

    public ParticleSystem setDefaultLifetime(float defaultLifetime) {
        this.defaultLifetime = defaultLifetime;
        return this;
    }

    public float getDefaultSize() {
        return defaultSize;
    }

    public ParticleSystem setDefaultSize(float defaultSize) {
        this.defaultSize = defaultSize;
        return this;
    }

    public float getDefaultFinalSize() {
        return defaultFinalSize;
    }

    public ParticleSystem setDefaultFinalSize(float defaultFinalSize) {
        this.defaultFinalSize = defaultFinalSize;
        return this;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public ParticleSystem setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }

    public Color getDefaultFinalColor() {
        return defaultFinalColor;
    }

    public ParticleSystem setDefaultFinalColor(Color defaultFinalColor) {
        this.defaultFinalColor = defaultFinalColor;
        return this;
    }

    public float getDefaultGravityScale() {
        return defaultGravityScale;
    }

    public ParticleSystem setDefaultGravityScale(float defaultGravityScale) {
        this.defaultGravityScale = defaultGravityScale;
        return this;
    }

    public float getDefaultDrag() {
        return defaultDrag;
    }

    public ParticleSystem setDefaultDrag(float defaultDrag) {
        this.defaultDrag = defaultDrag;
        return this;
    }

    public float getGravity() {
        return gravity;
    }

    public ParticleSystem setGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    @Override
    public String[] getDebugInfo() {
        return new String[] {
                "ParticleSystem: " + getName(),
                "Active: " + activeCount + "/" + maxParticles,
                "Emitting: " + emitting,
                "Position: (" + x + ", " + y + ")"
        };
    }
}
