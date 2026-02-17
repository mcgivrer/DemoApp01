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

    /** Whether to automatically deactivate when exhausted (no active particles and not emitting). */
    private boolean autoDeactivate = true;

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

    // ==================== Render configuration ====================

    /** Render shape type. */
    public enum RenderShape { CIRCLE, SQUARE, LINE }

    /** The render shape for this particle system. */
    private RenderShape renderShape = RenderShape.CIRCLE;

    /** Line rendering: length multiplier (line length = velocity * multiplier). */
    private float lineLengthMultiplier = 0.015f;

    /** Line rendering: minimum line length. */
    private float minLineLength = 5f;

    /** Line rendering: maximum line length. */
    private float maxLineLength = 25f;

    /** Line rendering: stroke width. */
    private float lineWidth = 1.5f;

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
        
        // Auto-deactivate if exhausted
        if (autoDeactivate && isExhausted()) {
            setActive(false);
        }
        
        return activeCount;
    }

    /**
     * Checks if the particle system is exhausted (no active particles and not emitting).
     * Useful for one-shot effects like explosions.
     *
     * @return true if exhausted.
     */
    public boolean isExhausted() {
        return activeCount == 0 && !emitting;
    }

    /**
     * Whether auto-deactivation is enabled.
     */
    public boolean isAutoDeactivate() {
        return autoDeactivate;
    }

    /**
     * Enable or disable auto-deactivation when exhausted.
     * Default is true.
     *
     * @param autoDeactivate true to auto-deactivate when no particles remain.
     * @return this for chaining.
     */
    public ParticleSystem setAutoDeactivate(boolean autoDeactivate) {
        this.autoDeactivate = autoDeactivate;
        return this;
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

    // ==================== Render shape getters/setters ====================

    /**
     * Get the render shape for this particle system.
     */
    public RenderShape getRenderShape() {
        return renderShape;
    }

    /**
     * Set the render shape for this particle system.
     */
    public ParticleSystem setRenderShape(RenderShape shape) {
        this.renderShape = shape;
        return this;
    }

    /**
     * Configure to render as lines (ideal for rain).
     */
    public ParticleSystem setRenderAsLines() {
        this.renderShape = RenderShape.LINE;
        return this;
    }

    /**
     * Configure to render as circles (default).
     */
    public ParticleSystem setRenderAsCircles() {
        this.renderShape = RenderShape.CIRCLE;
        return this;
    }

    /**
     * Configure to render as squares.
     */
    public ParticleSystem setRenderAsSquares() {
        this.renderShape = RenderShape.SQUARE;
        return this;
    }

    /**
     * Get line length multiplier.
     */
    public float getLineLengthMultiplier() {
        return lineLengthMultiplier;
    }

    /**
     * Set line length multiplier (line length = velocity * multiplier).
     */
    public ParticleSystem setLineLengthMultiplier(float multiplier) {
        this.lineLengthMultiplier = multiplier;
        return this;
    }

    /**
     * Get minimum line length.
     */
    public float getMinLineLength() {
        return minLineLength;
    }

    /**
     * Get maximum line length.
     */
    public float getMaxLineLength() {
        return maxLineLength;
    }

    /**
     * Set line length range.
     */
    public ParticleSystem setLineLengthRange(float min, float max) {
        this.minLineLength = min;
        this.maxLineLength = max;
        return this;
    }

    /**
     * Get line stroke width.
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Set line stroke width.
     */
    public ParticleSystem setLineWidth(float width) {
        this.lineWidth = width;
        return this;
    }

    /**
     * Configure line rendering with presets for rain.
     */
    public ParticleSystem setLineRenderingForRain() {
        return setRenderAsLines()
                .setLineLengthMultiplier(0.015f)
                .setLineLengthRange(5f, 25f)
                .setLineWidth(1.5f);
    }

    /**
     * Configure line rendering with presets for heavy rain/storm.
     */
    public ParticleSystem setLineRenderingForStorm() {
        return setRenderAsLines()
                .setLineLengthMultiplier(0.02f)
                .setLineLengthRange(8f, 35f)
                .setLineWidth(2f);
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
