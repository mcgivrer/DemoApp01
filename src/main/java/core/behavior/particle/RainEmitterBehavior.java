package core.behavior.particle;

import java.awt.Color;

import core.entity.Entity;
import core.entity.Particle;
import core.entity.ParticleSystem;

/**
 * Emitter behavior that simulates rain with variable wind.
 * <p>
 * Particles are emitted from the top of the emission area and fall
 * downward with gravity. Wind affects the horizontal velocity and
 * changes randomly over time for a natural effect.
 * <p>
 * Rain characteristics:
 * <ul>
 *   <li>Continuous emission from top of area</li>
 *   <li>Downward velocity with wind influence</li>
 *   <li>Wind strength and direction change over time</li>
 *   <li>Elongated particles (simulating raindrops)</li>
 *   <li>Optional splash effect when particles expire</li>
 * </ul>
 *
 * @see ParticleEmitterBehavior
 * @see ParticleSystem
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public class RainEmitterBehavior implements ParticleEmitterBehavior {

    // ==================== Emission settings ====================

    /** Particles emitted per second. */
    private float emissionRate = 100.0f;

    /** Accumulated time for emission. */
    private float emissionAccumulator = 0.0f;

    /** Width of the emission area (particles spawn randomly across this width). */
    private float emissionWidth = 800.0f;

    /** Height offset from system Y position (negative = above). */
    private float emissionYOffset = -50.0f;

    /** Extra width on sides to account for wind blowing rain into view. */
    private float emissionOverflow = 200.0f;

    // ==================== Rain velocity ====================

    /** Base downward velocity (positive Y = down). */
    private float baseVelocityY = 600.0f;

    /** Random variation in Y velocity. */
    private float velocityYVariation = 100.0f;

    /** Gravity scale for rain drops. */
    private float gravityScale = 0.5f;

    /** Drag coefficient (low for rain). */
    private float drag = 0.005f;

    // ==================== Wind settings ====================

    /** Current wind velocity (horizontal component). */
    private float currentWindX = 0.0f;

    /** Target wind velocity (wind smoothly transitions to this). */
    private float targetWindX = 0.0f;

    /** Maximum wind strength (absolute value). */
    private float maxWindStrength = 300.0f;

    /** How quickly wind changes to target (higher = faster). */
    private float windTransitionSpeed = 0.5f;

    /** Time until next wind change (seconds). */
    private float windChangeTimer = 0.0f;

    /** Minimum time between wind changes. */
    private float windChangeIntervalMin = 2.0f;

    /** Maximum time between wind changes. */
    private float windChangeIntervalMax = 8.0f;

    /** Whether wind can change direction (true) or only intensity (false). */
    private boolean windDirectionChanges = true;

    /** Wind gust intensity (random per-particle variation). */
    private float gustIntensity = 50.0f;

    // ==================== Particle appearance ====================

    /** Rain drop color (usually light blue/white with alpha). */
    private Color rainColor = new Color(180, 200, 255, 180);

    /** End color (usually same but more transparent). */
    private Color rainEndColor = new Color(150, 180, 255, 100);

    /** Rain drop width. */
    private float dropWidth = 2.0f;

    /** Rain drop height (elongated). */
    private float dropHeight = 8.0f;

    /** End size (usually same or slightly smaller). */
    private float dropEndSize = 1.5f;

    /** Minimum lifetime (depends on fall distance). */
    private float minLifetime = 0.8f;

    /** Maximum lifetime. */
    private float maxLifetime = 1.5f;

    // ==================== State ====================

    /** Whether rain is currently active. */
    private boolean raining = true;

    /**
     * Creates a rain emitter with default settings.
     */
    public RainEmitterBehavior() {
        // Initialize first wind change
        scheduleNextWindChange();
    }

    /**
     * Creates a rain emitter with custom emission rate and area width.
     *
     * @param emissionRate Particles per second.
     * @param emissionWidth Width of the rain area.
     */
    public RainEmitterBehavior(float emissionRate, float emissionWidth) {
        this.emissionRate = emissionRate;
        this.emissionWidth = emissionWidth;
        scheduleNextWindChange();
    }

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        if (!(entity instanceof ParticleSystem system)) {
            return;
        }

        if (!raining || !system.isEmitting()) {
            return;
        }

        // Update wind
        updateWind(deltaTime);

        // Accumulate time for emission
        emissionAccumulator += deltaTime * emissionRate;

        // Spawn particles based on accumulated time
        while (emissionAccumulator >= 1.0f) {
            emissionAccumulator -= 1.0f;
            spawnRaindrop(system);
        }
    }

    /**
     * Updates the wind simulation.
     */
    private void updateWind(float deltaTime) {
        // Smoothly transition current wind to target
        float windDiff = targetWindX - currentWindX;
        currentWindX += windDiff * windTransitionSpeed * deltaTime;

        // Check if it's time to change wind
        windChangeTimer -= deltaTime;
        if (windChangeTimer <= 0) {
            changeWind();
            scheduleNextWindChange();
        }
    }

    /**
     * Changes the target wind to a new random value.
     */
    private void changeWind() {
        if (windDirectionChanges) {
            // Full range: -maxWindStrength to +maxWindStrength
            targetWindX = (float) (Math.random() * 2 - 1) * maxWindStrength;
        } else {
            // Keep same direction, just change intensity
            float sign = currentWindX >= 0 ? 1 : -1;
            if (currentWindX == 0) sign = Math.random() > 0.5 ? 1 : -1;
            targetWindX = sign * (float) Math.random() * maxWindStrength;
        }
    }

    /**
     * Schedules the next wind change.
     */
    private void scheduleNextWindChange() {
        windChangeTimer = windChangeIntervalMin +
                (float) Math.random() * (windChangeIntervalMax - windChangeIntervalMin);
    }

    /**
     * Spawns a single raindrop particle.
     */
    private void spawnRaindrop(ParticleSystem system) {
        Particle p = system.getInactiveParticle();
        if (p == null) return;

        // Calculate spawn position across the emission width
        // Account for wind by extending spawn area in wind direction
        float windOffset = currentWindX > 0 ? -emissionOverflow : emissionOverflow;
        float totalWidth = emissionWidth + emissionOverflow;

        float spawnX = system.getX() + windOffset + (float) Math.random() * totalWidth;
        float spawnY = system.getY() + emissionYOffset;

        // Velocity: base downward + wind + random gust
        float gust = (float) (Math.random() - 0.5) * 2 * gustIntensity;
        float vx = currentWindX + gust;
        float vy = baseVelocityY + (float) (Math.random() - 0.5) * 2 * velocityYVariation;

        // Random lifetime
        float lifetime = minLifetime + (float) Math.random() * (maxLifetime - minLifetime);

        // Use height as size (for elongated drops, renderer might use special handling)
        // For now, use average of width/height
        float size = (dropWidth + dropHeight) / 2;

        // Initialize particle
        p.init(spawnX, spawnY, vx, vy,
                size, dropEndSize,
                rainColor, rainEndColor,
                lifetime, gravityScale, drag);

        // Store angle to indicate rain direction (for potential elongated rendering)
        // Angle based on velocity direction
        p.angle = (float) Math.toDegrees(Math.atan2(vy, vx)) - 90;
        p.va = 0; // No rotation
    }

    @Override
    public void burst(ParticleSystem system) {
        // Rain doesn't burst, but we can create a sudden downpour
        float savedRate = emissionRate;
        emissionRate *= 3; // Triple rate temporarily
        for (int i = 0; i < 50; i++) {
            spawnRaindrop(system);
        }
        emissionRate = savedRate;
    }

    @Override
    public void reset() {
        emissionAccumulator = 0.0f;
        currentWindX = 0.0f;
        targetWindX = 0.0f;
        scheduleNextWindChange();
    }

    // ==================== Getters ====================

    /**
     * Returns the current wind velocity.
     */
    public float getCurrentWindX() {
        return currentWindX;
    }

    /**
     * Returns the target wind velocity.
     */
    public float getTargetWindX() {
        return targetWindX;
    }

    /**
     * Returns whether it's currently raining.
     */
    public boolean isRaining() {
        return raining;
    }

    // ==================== Builder-style setters ====================

    /**
     * Set the emission rate (particles per second).
     */
    public RainEmitterBehavior setEmissionRate(float rate) {
        this.emissionRate = rate;
        return this;
    }

    /**
     * Set the width of the emission area.
     */
    public RainEmitterBehavior setEmissionWidth(float width) {
        this.emissionWidth = width;
        return this;
    }

    /**
     * Set the Y offset for emission (negative = above system position).
     */
    public RainEmitterBehavior setEmissionYOffset(float offset) {
        this.emissionYOffset = offset;
        return this;
    }

    /**
     * Set how much extra width to add for wind compensation.
     */
    public RainEmitterBehavior setEmissionOverflow(float overflow) {
        this.emissionOverflow = overflow;
        return this;
    }

    /**
     * Set the base downward velocity.
     */
    public RainEmitterBehavior setBaseVelocityY(float velocity) {
        this.baseVelocityY = velocity;
        return this;
    }

    /**
     * Set the gravity scale for rain drops.
     */
    public RainEmitterBehavior setGravityScale(float scale) {
        this.gravityScale = scale;
        return this;
    }

    /**
     * Set the drag coefficient.
     */
    public RainEmitterBehavior setDrag(float drag) {
        this.drag = drag;
        return this;
    }

    /**
     * Set the maximum wind strength.
     */
    public RainEmitterBehavior setMaxWindStrength(float strength) {
        this.maxWindStrength = strength;
        return this;
    }

    /**
     * Set how quickly wind transitions to new values.
     */
    public RainEmitterBehavior setWindTransitionSpeed(float speed) {
        this.windTransitionSpeed = speed;
        return this;
    }

    /**
     * Set the interval range for wind changes.
     */
    public RainEmitterBehavior setWindChangeInterval(float min, float max) {
        this.windChangeIntervalMin = min;
        this.windChangeIntervalMax = max;
        return this;
    }

    /**
     * Set whether wind can change direction or only intensity.
     */
    public RainEmitterBehavior setWindDirectionChanges(boolean changes) {
        this.windDirectionChanges = changes;
        return this;
    }

    /**
     * Set the gust intensity (random per-particle wind variation).
     */
    public RainEmitterBehavior setGustIntensity(float intensity) {
        this.gustIntensity = intensity;
        return this;
    }

    /**
     * Set the rain drop color (both start and end, with end being more transparent).
     */
    public RainEmitterBehavior setRainColor(Color color) {
        this.rainColor = color;
        // Create a more transparent version for the end color
        this.rainEndColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0, color.getAlpha() / 2));
        return this;
    }

    /**
     * Set both rain colors (start and end).
     */
    public RainEmitterBehavior setRainColors(Color start, Color end) {
        this.rainColor = start;
        this.rainEndColor = end;
        return this;
    }

    /**
     * Set the rain drop size.
     */
    public RainEmitterBehavior setDropSize(float width, float height) {
        this.dropWidth = width;
        this.dropHeight = height;
        return this;
    }

    /**
     * Set the particle lifetime range.
     */
    public RainEmitterBehavior setLifetimeRange(float min, float max) {
        this.minLifetime = min;
        this.maxLifetime = max;
        return this;
    }

    /**
     * Start or stop the rain.
     */
    public RainEmitterBehavior setRaining(boolean raining) {
        this.raining = raining;
        return this;
    }

    /**
     * Force wind to a specific value (useful for scripted weather).
     */
    public RainEmitterBehavior setWindForce(float windX) {
        this.currentWindX = windX;
        this.targetWindX = windX;
        return this;
    }

    // ==================== Preset configurations ====================

    /**
     * Configure as light drizzle.
     */
    public RainEmitterBehavior presetDrizzle() {
        return setEmissionRate(40)
                .setBaseVelocityY(300)
                .setMaxWindStrength(100)
                .setGustIntensity(20)
                .setDropSize(1.5f, 4f)
                .setRainColors(
                        new Color(200, 210, 255, 120),
                        new Color(180, 190, 240, 60))
                .setLifetimeRange(1.0f, 1.8f)
                .setGravityScale(0.3f);
    }

    /**
     * Configure as normal rain.
     */
    public RainEmitterBehavior presetNormalRain() {
        return setEmissionRate(100)
                .setBaseVelocityY(500)
                .setMaxWindStrength(200)
                .setGustIntensity(40)
                .setDropSize(2f, 8f)
                .setRainColors(
                        new Color(180, 200, 255, 180),
                        new Color(150, 180, 255, 80))
                .setLifetimeRange(0.8f, 1.5f)
                .setGravityScale(0.5f);
    }

    /**
     * Configure as heavy storm.
     */
    public RainEmitterBehavior presetStorm() {
        return setEmissionRate(250)
                .setBaseVelocityY(800)
                .setMaxWindStrength(400)
                .setWindTransitionSpeed(1.5f)
                .setWindChangeInterval(1.0f, 4.0f)
                .setGustIntensity(100)
                .setDropSize(2.5f, 12f)
                .setRainColors(
                        new Color(160, 180, 220, 200),
                        new Color(140, 160, 200, 100))
                .setLifetimeRange(0.5f, 1.0f)
                .setGravityScale(0.8f);
    }

    /**
     * Configure as monsoon (extreme rain with strong variable wind).
     */
    public RainEmitterBehavior presetMonsoon() {
        return setEmissionRate(400)
                .setBaseVelocityY(1000)
                .setMaxWindStrength(600)
                .setWindTransitionSpeed(2.0f)
                .setWindChangeInterval(0.5f, 2.0f)
                .setGustIntensity(150)
                .setDropSize(3f, 15f)
                .setRainColors(
                        new Color(150, 170, 210, 220),
                        new Color(130, 150, 190, 120))
                .setLifetimeRange(0.4f, 0.8f)
                .setGravityScale(1.0f)
                .setEmissionOverflow(400);
    }

    /**
     * Configure as snow (slow falling, minimal wind effect).
     */
    public RainEmitterBehavior presetSnow() {
        return setEmissionRate(80)
                .setBaseVelocityY(80)
                .setMaxWindStrength(60)
                .setWindTransitionSpeed(0.2f)
                .setWindChangeInterval(5.0f, 15.0f)
                .setGustIntensity(30)
                .setDropSize(4f, 4f)  // Round snowflakes
                .setRainColors(
                        new Color(255, 255, 255, 220),
                        new Color(240, 245, 255, 100))
                .setLifetimeRange(3.0f, 6.0f)
                .setGravityScale(0.05f)
                .setDrag(0.1f);  // More air resistance
    }
}
