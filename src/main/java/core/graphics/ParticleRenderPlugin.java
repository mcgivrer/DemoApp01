package core.graphics;

import core.entity.Particle;
import core.entity.ParticleSystem;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Render plugin for drawing particles from a {@link ParticleSystem}.
 *
 * <p>Renders each active particle as a filled circle or ellipse with
 * the particle's current color, size, and alpha. Supports optional
 * additive blending for glowing effects.
 *
 * <p>Usage: Register this plugin with the {@link Renderer}:
 * <pre>
 * renderer.addPlugin(new ParticleRenderPlugin());
 * </pre>
 *
 * @see ParticleSystem
 * @see Particle
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public class ParticleRenderPlugin implements RenderPlugin<ParticleSystem> {

    /** Shape used for rendering particles (for performance). */
    private final Ellipse2D.Float ellipse = new Ellipse2D.Float();

    /** Whether to use additive blending (for glow effects). */
    private boolean additiveBlending = false;

    /** Whether to draw particles as squares instead of circles. */
    private boolean drawAsSquares = false;

    /** Whether to apply rotation to particles. */
    private boolean applyRotation = false;

    @Override
    public Class<ParticleSystem> getSupportedEntityType() {
        return ParticleSystem.class;
    }

    @Override
    public void render(ParticleSystem ps, Graphics2D g) {
        Particle[] particles = ps.getParticles();

        // Save original composite for restoration
        Composite originalComposite = g.getComposite();

        // Apply additive blending if enabled
        if (additiveBlending) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        }

        for (Particle p : particles) {
            if (!p.active) continue;

            // Calculate alpha composite
            float alpha = Math.max(0, Math.min(1, p.alpha));
            if (alpha <= 0) continue;

            // Apply alpha to color
            Color color = new Color(
                    p.color.getRed(),
                    p.color.getGreen(),
                    p.color.getBlue(),
                    (int) (alpha * (p.color.getAlpha() / 255f) * 255)
            );

            g.setColor(color);

            float size = p.size;
            float halfSize = size / 2f;

            if (applyRotation && p.angle != 0) {
                // Render with rotation
                AffineTransform oldTransform = g.getTransform();
                g.translate(p.x, p.y);
                g.rotate(Math.toRadians(p.angle));

                if (drawAsSquares) {
                    g.fillRect((int) -halfSize, (int) -halfSize, (int) size, (int) size);
                } else {
                    ellipse.setFrame(-halfSize, -halfSize, size, size);
                    g.fill(ellipse);
                }

                g.setTransform(oldTransform);
            } else {
                // Render without rotation
                if (drawAsSquares) {
                    g.fillRect((int) (p.x - halfSize), (int) (p.y - halfSize), (int) size, (int) size);
                } else {
                    ellipse.setFrame(p.x - halfSize, p.y - halfSize, size, size);
                    g.fill(ellipse);
                }
            }
        }

        // Restore original composite
        g.setComposite(originalComposite);
    }

    // ==================== Configuration Methods ====================

    /**
     * Enable or disable additive blending for glow effects.
     */
    public ParticleRenderPlugin setAdditiveBlending(boolean additive) {
        this.additiveBlending = additive;
        return this;
    }

    /**
     * Enable or disable drawing particles as squares instead of circles.
     */
    public ParticleRenderPlugin setDrawAsSquares(boolean squares) {
        this.drawAsSquares = squares;
        return this;
    }

    /**
     * Enable or disable applying rotation to particle rendering.
     * Only useful if particles have angular velocity.
     */
    public ParticleRenderPlugin setApplyRotation(boolean applyRotation) {
        this.applyRotation = applyRotation;
        return this;
    }
}
