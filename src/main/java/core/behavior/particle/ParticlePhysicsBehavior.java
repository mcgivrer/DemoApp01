package core.behavior.particle;

import core.behavior.Behavior;
import core.entity.Entity;
import core.entity.Particle;
import core.entity.ParticleSystem;

import java.awt.Color;

/**
 * Behavior that updates particle physics: position, velocity, gravity, drag,
 * and lifetime. Also handles color/size interpolation over the particle's lifetime.
 *
 * <p>This behavior should be added to a {@link ParticleSystem} along with
 * an emitter behavior. It processes all active particles each frame.
 *
 * <p>Usage example:
 * <pre>
 * ParticleSystem ps = new ParticleSystem("sparks", 200);
 * ps.addBehavior(new FountainEmitterBehavior());
 * ps.addBehavior(new ParticlePhysicsBehavior());
 * </pre>
 *
 * @see ParticleSystem
 * @see Particle
 * @see ParticleEmitterBehavior
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public class ParticlePhysicsBehavior implements Behavior<ParticleSystem> {

    /** Base gravity acceleration (pixels/second²). */
    private float gravity = 980f;

    /** Whether to apply gravity to particles. */
    private boolean applyGravity = true;

    /** Whether to apply drag to particles. */
    private boolean applyDrag = true;

    /** Whether to interpolate colors over lifetime. */
    private boolean interpolateColor = true;

    /** Whether to interpolate size over lifetime. */
    private boolean interpolateSize = true;

    /** Whether to interpolate alpha over lifetime. */
    private boolean interpolateAlpha = true;

    /** Whether to update rotation angle. */
    private boolean updateRotation = true;

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        if (!(entity instanceof ParticleSystem ps)) {
            return;
        }

        Particle[] particles = ps.getParticles();

        for (Particle p : particles) {
            if (!p.active) continue;

            // Update lifetime
            p.lifetime -= deltaTime;
            if (p.lifetime <= 0) {
                p.deactivate();
                continue;
            }

            // Apply gravity
            if (applyGravity && p.gravityScale != 0) {
                p.vy += gravity * p.gravityScale * deltaTime;
            }

            // Apply drag
            if (applyDrag && p.drag > 0) {
                float dragFactor = 1.0f - (p.drag * deltaTime);
                if (dragFactor < 0) dragFactor = 0;
                p.vx *= dragFactor;
                p.vy *= dragFactor;
            }

            // Update position
            p.x += p.vx * deltaTime;
            p.y += p.vy * deltaTime;

            // Update rotation
            if (updateRotation && p.va != 0) {
                p.angle += p.va * deltaTime;
            }

            // Calculate lifetime progress (0 = just spawned, 1 = about to die)
            float progress = p.getLifetimeProgress();

            // Interpolate size
            if (interpolateSize) {
                p.size = lerp(p.initialSize, p.finalSize, progress);
            }

            // Interpolate alpha
            if (interpolateAlpha) {
                p.alpha = lerp(p.initialAlpha, p.finalAlpha, progress);
            }

            // Interpolate color
            if (interpolateColor) {
                p.color = lerpColor(p.initialColor, p.finalColor, progress);
            }
        }

        // Update active count
        ps.updateActiveCount();
    }

    /**
     * Linear interpolation between two values.
     */
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Linear interpolation between two colors.
     */
    private Color lerpColor(Color c1, Color c2, float t) {
        int r = (int) lerp(c1.getRed(), c2.getRed(), t);
        int g = (int) lerp(c1.getGreen(), c2.getGreen(), t);
        int b = (int) lerp(c1.getBlue(), c2.getBlue(), t);
        int a = (int) lerp(c1.getAlpha(), c2.getAlpha(), t);

        // Clamp values
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        a = Math.max(0, Math.min(255, a));

        return new Color(r, g, b, a);
    }

    // ==================== Getters and Setters ====================

    /**
     * Get the base gravity acceleration.
     */
    public float getGravity() {
        return gravity;
    }

    /**
     * Set the base gravity acceleration (pixels/second²).
     * Default is 980 (approximately 10m/s² at 100 pixels per meter).
     */
    public ParticlePhysicsBehavior setGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * Enable or disable gravity application.
     */
    public ParticlePhysicsBehavior setApplyGravity(boolean applyGravity) {
        this.applyGravity = applyGravity;
        return this;
    }

    /**
     * Enable or disable drag application.
     */
    public ParticlePhysicsBehavior setApplyDrag(boolean applyDrag) {
        this.applyDrag = applyDrag;
        return this;
    }

    /**
     * Enable or disable color interpolation over lifetime.
     */
    public ParticlePhysicsBehavior setInterpolateColor(boolean interpolateColor) {
        this.interpolateColor = interpolateColor;
        return this;
    }

    /**
     * Enable or disable size interpolation over lifetime.
     */
    public ParticlePhysicsBehavior setInterpolateSize(boolean interpolateSize) {
        this.interpolateSize = interpolateSize;
        return this;
    }

    /**
     * Enable or disable alpha interpolation over lifetime.
     */
    public ParticlePhysicsBehavior setInterpolateAlpha(boolean interpolateAlpha) {
        this.interpolateAlpha = interpolateAlpha;
        return this;
    }

    /**
     * Enable or disable rotation update.
     */
    public ParticlePhysicsBehavior setUpdateRotation(boolean updateRotation) {
        this.updateRotation = updateRotation;
        return this;
    }

    /**
     * Disable all interpolation (color, size, alpha).
     * Useful for particles that maintain constant appearance.
     */
    public ParticlePhysicsBehavior disableAllInterpolation() {
        this.interpolateColor = false;
        this.interpolateSize = false;
        this.interpolateAlpha = false;
        return this;
    }

    /**
     * Configure for space-style particles (no gravity, no drag).
     */
    public ParticlePhysicsBehavior presetSpace() {
        this.applyGravity = false;
        this.applyDrag = false;
        return this;
    }

    /**
     * Configure for smoke-style particles (negative gravity, high drag).
     */
    public ParticlePhysicsBehavior presetSmoke() {
        this.gravity = -50f;
        this.applyGravity = true;
        this.applyDrag = true;
        return this;
    }

    /**
     * Configure for rain-style particles (fast fall, no drag).
     */
    public ParticlePhysicsBehavior presetRain() {
        this.gravity = 2000f;
        this.applyGravity = true;
        this.applyDrag = false;
        this.interpolateSize = false;
        return this;
    }
}
