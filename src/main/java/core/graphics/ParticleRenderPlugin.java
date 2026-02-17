package core.graphics;

import core.entity.Particle;
import core.entity.ParticleSystem;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * Render plugin for drawing particles from a {@link ParticleSystem}.
 *
 * <p>Renders each active particle as a filled circle, square, or line with
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

    /** Particle rendering shape types. */
    public enum RenderShape {
        CIRCLE,
        SQUARE,
        LINE
    }

    /** Shape used for rendering particles (for performance). */
    private final Ellipse2D.Float ellipse = new Ellipse2D.Float();

    /** Line used for line rendering (for performance). */
    private final Line2D.Float line = new Line2D.Float();

    /** Whether to use additive blending (for glow effects). */
    private boolean additiveBlending = false;

    /** Whether to draw particles as squares instead of circles. */
    private boolean drawAsSquares = false;

    /** Whether to apply rotation to particles. */
    private boolean applyRotation = false;

    /** Current render shape. */
    private RenderShape renderShape = RenderShape.CIRCLE;

    /** Line length multiplier (for LINE shape, based on velocity). */
    private float lineLengthMultiplier = 0.02f;

    /** Minimum line length (pixels). */
    private float minLineLength = 4f;

    /** Maximum line length (pixels). */
    private float maxLineLength = 30f;

    /** Line stroke width (for LINE shape). */
    private float lineWidth = 2f;

    @Override
    public Class<ParticleSystem> getSupportedEntityType() {
        return ParticleSystem.class;
    }

    @Override
    public void render(ParticleSystem ps, Graphics2D g) {
        Particle[] particles = ps.getParticles();

        // Save original state for restoration
        Composite originalComposite = g.getComposite();
        Stroke originalStroke = g.getStroke();

        // Apply additive blending if enabled
        if (additiveBlending) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        }

        // Determine render shape from ParticleSystem or plugin default
        ParticleSystem.RenderShape psShape = ps.getRenderShape();
        RenderShape effectiveShape = switch (psShape) {
            case LINE -> RenderShape.LINE;
            case SQUARE -> RenderShape.SQUARE;
            default -> renderShape;  // Use plugin default
        };

        // Override with legacy drawAsSquares if set
        if (drawAsSquares && effectiveShape == RenderShape.CIRCLE) {
            effectiveShape = RenderShape.SQUARE;
        }

        // Set stroke for line rendering
        if (effectiveShape == RenderShape.LINE) {
            float effectiveLineWidth = ps.getRenderShape() == ParticleSystem.RenderShape.LINE 
                    ? ps.getLineWidth() : lineWidth;
            g.setStroke(new BasicStroke(effectiveLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        // Get line parameters from ParticleSystem if it's configured for lines
        float effectiveLineLengthMultiplier = ps.getRenderShape() == ParticleSystem.RenderShape.LINE 
                ? ps.getLineLengthMultiplier() : lineLengthMultiplier;
        float effectiveMinLineLength = ps.getRenderShape() == ParticleSystem.RenderShape.LINE 
                ? ps.getMinLineLength() : minLineLength;
        float effectiveMaxLineLength = ps.getRenderShape() == ParticleSystem.RenderShape.LINE 
                ? ps.getMaxLineLength() : maxLineLength;

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

            switch (effectiveShape) {
                case LINE -> renderLine(g, p, effectiveLineLengthMultiplier, 
                        effectiveMinLineLength, effectiveMaxLineLength);
                case SQUARE -> renderSquare(g, p, size, halfSize);
                default -> renderCircle(g, p, size, halfSize);
            }
        }

        // Restore original state
        g.setComposite(originalComposite);
        g.setStroke(originalStroke);
    }

    /**
     * Renders a particle as a line based on its velocity.
     */
    private void renderLine(Graphics2D g, Particle p, 
            float lengthMultiplier, float minLength, float maxLength) {
        // Calculate line length based on velocity
        float velocityMag = (float) Math.sqrt(p.vx * p.vx + p.vy * p.vy);
        float length = velocityMag * lengthMultiplier;
        length = Math.max(minLength, Math.min(maxLength, length));

        // Normalize velocity for direction
        float nx = 0, ny = 1;  // Default: downward
        if (velocityMag > 0.001f) {
            nx = p.vx / velocityMag;
            ny = p.vy / velocityMag;
        }

        // Calculate line endpoints
        // Line extends backward from particle position (tail effect)
        float x1 = p.x;
        float y1 = p.y;
        float x2 = p.x - nx * length;
        float y2 = p.y - ny * length;

        line.setLine(x1, y1, x2, y2);
        g.draw(line);
    }

    /**
     * Renders a particle as a square.
     */
    private void renderSquare(Graphics2D g, Particle p, float size, float halfSize) {
        if (applyRotation && p.angle != 0) {
            AffineTransform oldTransform = g.getTransform();
            g.translate(p.x, p.y);
            g.rotate(Math.toRadians(p.angle));
            g.fillRect((int) -halfSize, (int) -halfSize, (int) size, (int) size);
            g.setTransform(oldTransform);
        } else {
            g.fillRect((int) (p.x - halfSize), (int) (p.y - halfSize), (int) size, (int) size);
        }
    }

    /**
     * Renders a particle as a circle.
     */
    private void renderCircle(Graphics2D g, Particle p, float size, float halfSize) {
        if (applyRotation && p.angle != 0) {
            AffineTransform oldTransform = g.getTransform();
            g.translate(p.x, p.y);
            g.rotate(Math.toRadians(p.angle));
            ellipse.setFrame(-halfSize, -halfSize, size, size);
            g.fill(ellipse);
            g.setTransform(oldTransform);
        } else {
            ellipse.setFrame(p.x - halfSize, p.y - halfSize, size, size);
            g.fill(ellipse);
        }
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

    /**
     * Set the render shape for particles.
     */
    public ParticleRenderPlugin setRenderShape(RenderShape shape) {
        this.renderShape = shape;
        return this;
    }

    /**
     * Enable line rendering (ideal for rain).
     */
    public ParticleRenderPlugin setDrawAsLines(boolean lines) {
        this.renderShape = lines ? RenderShape.LINE : RenderShape.CIRCLE;
        return this;
    }

    /**
     * Set the line length multiplier (for LINE shape).
     * Line length = velocity * multiplier.
     */
    public ParticleRenderPlugin setLineLengthMultiplier(float multiplier) {
        this.lineLengthMultiplier = multiplier;
        return this;
    }

    /**
     * Set the minimum and maximum line length.
     */
    public ParticleRenderPlugin setLineLengthRange(float min, float max) {
        this.minLineLength = min;
        this.maxLineLength = max;
        return this;
    }

    /**
     * Set the line stroke width.
     */
    public ParticleRenderPlugin setLineWidth(float width) {
        this.lineWidth = width;
        return this;
    }

    /**
     * Configure for rain rendering (lines with appropriate settings).
     */
    public ParticleRenderPlugin presetRainLines() {
        return setRenderShape(RenderShape.LINE)
                .setLineLengthMultiplier(0.015f)
                .setLineLengthRange(5f, 25f)
                .setLineWidth(1.5f);
    }

    /**
     * Configure for heavy rain rendering.
     */
    public ParticleRenderPlugin presetHeavyRainLines() {
        return setRenderShape(RenderShape.LINE)
                .setLineLengthMultiplier(0.02f)
                .setLineLengthRange(8f, 35f)
                .setLineWidth(2f);
    }

    /**
     * Configure for bullet trails or fast particles.
     */
    public ParticleRenderPlugin presetTrails() {
        return setRenderShape(RenderShape.LINE)
                .setLineLengthMultiplier(0.05f)
                .setLineLengthRange(10f, 50f)
                .setLineWidth(3f);
    }
}
