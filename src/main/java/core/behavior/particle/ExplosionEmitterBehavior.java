package core.behavior.particle;

import core.entity.Entity;
import core.entity.Particle;
import core.entity.ParticleSystem;

import java.awt.*;

/**
 * Explosion-style particle emitter that emits all particles in a single burst,
 * spreading radially in all directions from the emission point.
 *
 * <p>Usage example:
 * <pre>
 * ParticleSystem explosion = new ParticleSystem(100);
 * explosion.setPosition(x, y);
 * explosion.addBehavior(new ExplosionEmitterBehavior()
 *     .setParticleCount(50)
 *     .setMinVelocity(100)
 *     .setMaxVelocity(300)
 *     .setStartColor(Color.ORANGE)
 *     .setEndColor(new Color(255, 50, 0, 0)));
 * explosion.addBehavior(new ParticlePhysicsBehavior());
 * </pre>
 */
public class ExplosionEmitterBehavior implements ParticleEmitterBehavior {

    // Emission configuration
    private int particleCount = 30;
    private boolean hasExploded = false;
    private boolean autoTrigger = true;

    // Velocity (radial spread)
    private float minVelocity = 100f;
    private float maxVelocity = 250f;

    // Angular spread (in radians, 2*PI = full circle)
    private float angleStart = 0f;
    private float angleSpan = (float) (2 * Math.PI);

    // Particle appearance
    private Color startColor = Color.ORANGE;
    private Color endColor = new Color(255, 100, 0, 0);
    private float minSize = 4f;
    private float maxSize = 10f;
    private float endSize = 1f;

    // Particle lifetime
    private float minLifetime = 0.5f;
    private float maxLifetime = 1.5f;

    // Physics
    private float gravityScale = 0.3f;
    private float drag = 0.02f;

    // Emission offset from particle system center
    private float offsetX = 0f;
    private float offsetY = 0f;

    // ==================== Smoke phase configuration ====================
    
    /** Whether to emit smoke after the explosion. */
    private boolean enableSmoke = false;
    
    /** Delay before smoke starts (seconds after explosion). */
    private float smokeDelay = 0.1f;
    
    /** Duration of smoke emission (seconds). */
    private float smokeDuration = 1.5f;
    
    /** Number of smoke particles to emit over the duration. */
    private int smokeParticleCount = 20;
    
    /** Smoke particle colors. */
    private Color smokeStartColor = new Color(100, 100, 100, 200);
    private Color smokeEndColor = new Color(50, 50, 50, 0);
    
    /** Smoke particle sizes. */
    private float smokeMinSize = 8f;
    private float smokeMaxSize = 16f;
    private float smokeEndSize = 30f;
    
    /** Smoke particle lifetime. */
    private float smokeMinLifetime = 1.0f;
    private float smokeMaxLifetime = 2.5f;
    
    /** Smoke physics. */
    private float smokeGravityScale = -0.1f;  // Float upward
    private float smokeDrag = 0.05f;
    private float smokeVelocityMin = 20f;
    private float smokeVelocityMax = 60f;
    
    /** Smoke emission state. */
    private float smokeTimer = 0f;
    private float smokeEmissionAccumulator = 0f;
    private boolean smokePhaseComplete = false;

    // Reference to last used particle system (for explode() convenience method)
    private ParticleSystem lastSystem;

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        if (!(entity instanceof ParticleSystem system)) {
            return;
        }

        lastSystem = system;

        if (autoTrigger && !hasExploded) {
            burst(system);
        }
        
        // Handle smoke phase after explosion
        if (enableSmoke && hasExploded && !smokePhaseComplete) {
            smokeTimer += deltaTime;
            
            if (smokeTimer >= smokeDelay) {
                float smokeElapsed = smokeTimer - smokeDelay;
                
                if (smokeElapsed < smokeDuration) {
                    // Calculate emission rate
                    float emissionRate = smokeParticleCount / smokeDuration;
                    smokeEmissionAccumulator += deltaTime * emissionRate;
                    
                    while (smokeEmissionAccumulator >= 1.0f) {
                        smokeEmissionAccumulator -= 1.0f;
                        emitSmokeParticle(system);
                    }
                } else {
                    smokePhaseComplete = true;
                    // Emission complete - mark system as not emitting
                    system.setEmitting(false);
                }
            }
        }
        
        // If no smoke phase and explosion done, stop emitting
        if (hasExploded && !enableSmoke) {
            system.setEmitting(false);
        }
    }
    
    /**
     * Emits a single smoke particle.
     */
    private void emitSmokeParticle(ParticleSystem system) {
        Particle p = system.getInactiveParticle();
        if (p == null) return;
        
        float emitX = system.getX() + offsetX;
        float emitY = system.getY() + offsetY;
        
        // Random angle (mostly upward, -120° to -60° = roughly up)
        float angle = (float) (-Math.PI / 2 + (Math.random() - 0.5) * Math.PI / 3);
        float velocity = smokeVelocityMin + (float) Math.random() * (smokeVelocityMax - smokeVelocityMin);
        
        float vx = (float) Math.cos(angle) * velocity;
        float vy = (float) Math.sin(angle) * velocity;
        
        // Add some horizontal spread based on explosion position
        vx += (float) (Math.random() - 0.5) * 40;
        
        float lifetime = smokeMinLifetime + (float) Math.random() * (smokeMaxLifetime - smokeMinLifetime);
        float size = smokeMinSize + (float) Math.random() * (smokeMaxSize - smokeMinSize);
        
        p.init(emitX, emitY, vx, vy,
                size, smokeEndSize,
                smokeStartColor, smokeEndColor,
                lifetime, smokeGravityScale, smokeDrag);
    }

    @Override
    public void burst(ParticleSystem system) {
        if (system == null) return;

        float emitX = system.getX() + offsetX;
        float emitY = system.getY() + offsetY;

        for (int i = 0; i < particleCount; i++) {
            Particle p = system.getInactiveParticle();
            if (p == null) break;

            // Random angle within the specified arc
            float angle = angleStart + (float) Math.random() * angleSpan;

            // Random velocity magnitude
            float velocity = minVelocity + (float) Math.random() * (maxVelocity - minVelocity);

            // Convert polar to cartesian
            float vx = (float) Math.cos(angle) * velocity;
            float vy = (float) Math.sin(angle) * velocity;

            // Random lifetime
            float lifetime = minLifetime + (float) Math.random() * (maxLifetime - minLifetime);

            // Random starting size
            float size = minSize + (float) Math.random() * (maxSize - minSize);

            // Initialize particle
            p.init(emitX, emitY, vx, vy,
                    size, endSize,
                    startColor, endColor,
                    lifetime,
                    gravityScale, drag);
        }

        hasExploded = true;
    }

    @Override
    public void reset() {
        hasExploded = false;
        smokeTimer = 0f;
        smokeEmissionAccumulator = 0f;
        smokePhaseComplete = false;
    }

    /**
     * Check if the explosion has already been triggered.
     */
    public boolean hasExploded() {
        return hasExploded;
    }

    /**
     * Manually trigger the explosion. Use this when autoTrigger is false.
     * Note: update() must have been called at least once to set the system reference.
     */
    public void explode() {
        if (lastSystem != null) {
            burst(lastSystem);
        }
    }

    // ==================== Builder-style setters ====================

    /**
     * Set the number of particles to emit in the burst.
     */
    public ExplosionEmitterBehavior setParticleCount(int count) {
        this.particleCount = count;
        return this;
    }

    /**
     * Set whether the explosion triggers automatically on first update.
     * If false, call explode() or burst() manually.
     */
    public ExplosionEmitterBehavior setAutoTrigger(boolean autoTrigger) {
        this.autoTrigger = autoTrigger;
        return this;
    }

    /**
     * Set the minimum radial velocity for particles.
     */
    public ExplosionEmitterBehavior setMinVelocity(float minVelocity) {
        this.minVelocity = minVelocity;
        return this;
    }

    /**
     * Set the maximum radial velocity for particles.
     */
    public ExplosionEmitterBehavior setMaxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
        return this;
    }

    /**
     * Set the velocity range for particles.
     */
    public ExplosionEmitterBehavior setVelocityRange(float min, float max) {
        this.minVelocity = min;
        this.maxVelocity = max;
        return this;
    }

    /**
     * Set the angular spread in radians.
     * Default is full circle (2*PI starting at 0).
     *
     * @param start Starting angle in radians (0 = right, PI/2 = down)
     * @param span  Angular span in radians
     */
    public ExplosionEmitterBehavior setAngleRange(float start, float span) {
        this.angleStart = start;
        this.angleSpan = span;
        return this;
    }

    /**
     * Set up a directional explosion (e.g., upward cone).
     *
     * @param directionDegrees Direction in degrees (0 = right, 90 = down, 180 = left, 270 = up)
     * @param spreadDegrees    Total spread angle in degrees
     */
    public ExplosionEmitterBehavior setDirectionalSpread(float directionDegrees, float spreadDegrees) {
        float dirRad = (float) Math.toRadians(directionDegrees);
        float spreadRad = (float) Math.toRadians(spreadDegrees);
        this.angleStart = dirRad - spreadRad / 2;
        this.angleSpan = spreadRad;
        return this;
    }

    /**
     * Set the starting color for particles.
     */
    public ExplosionEmitterBehavior setStartColor(Color startColor) {
        this.startColor = startColor;
        return this;
    }

    /**
     * Set the ending color for particles (at end of lifetime).
     */
    public ExplosionEmitterBehavior setEndColor(Color endColor) {
        this.endColor = endColor;
        return this;
    }

    /**
     * Set both start and end colors.
     */
    public ExplosionEmitterBehavior setColors(Color start, Color end) {
        this.startColor = start;
        this.endColor = end;
        return this;
    }

    /**
     * Set the starting size range for particles.
     */
    public ExplosionEmitterBehavior setSizeRange(float min, float max) {
        this.minSize = min;
        this.maxSize = max;
        return this;
    }

    /**
     * Set the ending size for particles.
     */
    public ExplosionEmitterBehavior setEndSize(float endSize) {
        this.endSize = endSize;
        return this;
    }

    /**
     * Set the lifetime range for particles.
     */
    public ExplosionEmitterBehavior setLifetimeRange(float min, float max) {
        this.minLifetime = min;
        this.maxLifetime = max;
        return this;
    }

    /**
     * Set the gravity scale for particles.
     * 0 = no gravity, 1 = full gravity, negative = float upward.
     */
    public ExplosionEmitterBehavior setGravityScale(float gravityScale) {
        this.gravityScale = gravityScale;
        return this;
    }

    /**
     * Set the drag coefficient for particles.
     * Higher values slow particles down faster.
     */
    public ExplosionEmitterBehavior setDrag(float drag) {
        this.drag = drag;
        return this;
    }

    /**
     * Set the emission offset from the particle system's position.
     */
    public ExplosionEmitterBehavior setOffset(float x, float y) {
        this.offsetX = x;
        this.offsetY = y;
        return this;
    }

    // ==================== Smoke configuration ====================

    /**
     * Enable or disable smoke emission after the explosion.
     */
    public ExplosionEmitterBehavior setEnableSmoke(boolean enable) {
        this.enableSmoke = enable;
        return this;
    }

    /**
     * Set the delay before smoke starts after the explosion (seconds).
     */
    public ExplosionEmitterBehavior setSmokeDelay(float delay) {
        this.smokeDelay = delay;
        return this;
    }

    /**
     * Set the duration of smoke emission (seconds).
     */
    public ExplosionEmitterBehavior setSmokeDuration(float duration) {
        this.smokeDuration = duration;
        return this;
    }

    /**
     * Set the number of smoke particles to emit.
     */
    public ExplosionEmitterBehavior setSmokeParticleCount(int count) {
        this.smokeParticleCount = count;
        return this;
    }

    /**
     * Set smoke particle colors.
     */
    public ExplosionEmitterBehavior setSmokeColors(Color start, Color end) {
        this.smokeStartColor = start;
        this.smokeEndColor = end;
        return this;
    }

    /**
     * Set smoke particle size range.
     */
    public ExplosionEmitterBehavior setSmokeSizeRange(float minSize, float maxSize, float endSize) {
        this.smokeMinSize = minSize;
        this.smokeMaxSize = maxSize;
        this.smokeEndSize = endSize;
        return this;
    }

    /**
     * Set smoke particle lifetime range.
     */
    public ExplosionEmitterBehavior setSmokeLifetimeRange(float min, float max) {
        this.smokeMinLifetime = min;
        this.smokeMaxLifetime = max;
        return this;
    }

    /**
     * Set smoke physics parameters.
     */
    public ExplosionEmitterBehavior setSmokePhysics(float gravityScale, float drag) {
        this.smokeGravityScale = gravityScale;
        this.smokeDrag = drag;
        return this;
    }

    /**
     * Set smoke velocity range.
     */
    public ExplosionEmitterBehavior setSmokeVelocityRange(float min, float max) {
        this.smokeVelocityMin = min;
        this.smokeVelocityMax = max;
        return this;
    }

    // ==================== Preset configurations ====================

    /**
     * Configure as a fiery explosion with smoke trail.
     */
    public ExplosionEmitterBehavior presetFireExplosion() {
        return setParticleCount(50)
                .setVelocityRange(150, 350)
                .setColors(new Color(255, 200, 50), new Color(255, 50, 0, 0))
                .setSizeRange(6, 14)
                .setEndSize(2)
                .setLifetimeRange(0.4f, 1.0f)
                .setGravityScale(-0.2f)
                .setDrag(0.03f)
                // Add smoke
                .setEnableSmoke(true)
                .setSmokeDelay(0.15f)
                .setSmokeDuration(1.2f)
                .setSmokeParticleCount(15)
                .setSmokeColors(new Color(80, 80, 80, 180), new Color(40, 40, 40, 0))
                .setSmokeSizeRange(10, 18, 35)
                .setSmokeLifetimeRange(1.5f, 3.0f)
                .setSmokePhysics(-0.08f, 0.04f);
    }

    /**
     * Configure as a spark burst.
     */
    public ExplosionEmitterBehavior presetSparkBurst() {
        return setParticleCount(30)
                .setVelocityRange(200, 400)
                .setColors(Color.YELLOW, new Color(255, 150, 0, 0))
                .setSizeRange(2, 4)
                .setEndSize(1)
                .setLifetimeRange(0.3f, 0.7f)
                .setGravityScale(0.5f)
                .setDrag(0.01f);
    }

    /**
     * Configure as a smoke puff.
     */
    public ExplosionEmitterBehavior presetSmokePuff() {
        return setParticleCount(20)
                .setVelocityRange(30, 80)
                .setColors(new Color(100, 100, 100, 180), new Color(50, 50, 50, 0))
                .setSizeRange(10, 20)
                .setEndSize(30)
                .setLifetimeRange(1.0f, 2.0f)
                .setGravityScale(-0.1f)
                .setDrag(0.05f);
    }

    /**
     * Configure as a debris burst (e.g., destruction effect).
     */
    public ExplosionEmitterBehavior presetDebris() {
        return setParticleCount(25)
                .setVelocityRange(100, 250)
                .setColors(new Color(139, 90, 43), new Color(80, 50, 20, 100))
                .setSizeRange(3, 8)
                .setEndSize(2)
                .setLifetimeRange(0.8f, 1.5f)
                .setGravityScale(1.0f)
                .setDrag(0.02f)
                // Add dust/smoke
                .setEnableSmoke(true)
                .setSmokeDelay(0.2f)
                .setSmokeDuration(0.8f)
                .setSmokeParticleCount(10)
                .setSmokeColors(new Color(120, 100, 80, 150), new Color(60, 50, 40, 0))
                .setSmokeSizeRange(6, 12, 20)
                .setSmokeLifetimeRange(1.0f, 2.0f);
    }

    /**
     * Configure as a realistic bomb explosion with fire, debris and heavy smoke.
     */
    public ExplosionEmitterBehavior presetBombExplosion() {
        return setParticleCount(80)
                .setVelocityRange(200, 500)
                .setColors(new Color(255, 220, 100), new Color(255, 80, 0, 0))
                .setSizeRange(8, 20)
                .setEndSize(3)
                .setLifetimeRange(0.3f, 0.8f)
                .setGravityScale(-0.3f)
                .setDrag(0.02f)
                // Heavy smoke
                .setEnableSmoke(true)
                .setSmokeDelay(0.1f)
                .setSmokeDuration(2.0f)
                .setSmokeParticleCount(30)
                .setSmokeColors(new Color(60, 60, 60, 220), new Color(30, 30, 30, 0))
                .setSmokeSizeRange(15, 25, 50)
                .setSmokeLifetimeRange(2.0f, 4.0f)
                .setSmokePhysics(-0.05f, 0.03f)
                .setSmokeVelocityRange(15, 40);
    }
}
