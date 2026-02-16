package core.physics;

/**
 * Represents a bounding shape for collision detection, supporting both
 * oriented bounding boxes (OBB) for rectangles/lines and ellipses for circles.
 * <p>
 * For RECTANGLE and LINE shapes, stores the 4 corners of the oriented rectangle.
 * For CIRCLE shapes, stores an ellipse defined by center and radii.
 * <p>
 * Provides intersection tests using the Separating Axis Theorem (SAT) for OBB
 * and geometric tests for ellipse intersections.
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public class BoundingShape {

    /**
     * Type of bounding shape.
     */
    public enum Type {
        /** Oriented Bounding Box - uses 4 corners */
        OBB,
        /** Ellipse/Circle - uses center and radii */
        ELLIPSE
    }

    private final Type type;

    // --- OBB data (4 corners in world coordinates) ---
    // Corners are ordered: top-left, top-right, bottom-right, bottom-left
    private final float[] cornersX = new float[4];
    private final float[] cornersY = new float[4];

    // --- Ellipse data ---
    private float centerX, centerY;
    private float radiusX, radiusY;

    // --- Cached axes for SAT (OBB only) ---
    // Two edge vectors (normalized) - only need 2 axes for a rectangle
    private float axis1X, axis1Y;
    private float axis2X, axis2Y;

    /**
     * Creates a new BoundingShape with the specified type.
     *
     * @param type The type of bounding shape (OBB or ELLIPSE).
     */
    public BoundingShape(Type type) {
        this.type = type;
    }

    /**
     * Creates an OBB BoundingShape.
     *
     * @return A new OBB BoundingShape.
     */
    public static BoundingShape createOBB() {
        return new BoundingShape(Type.OBB);
    }

    /**
     * Creates an Ellipse BoundingShape.
     *
     * @return A new Ellipse BoundingShape.
     */
    public static BoundingShape createEllipse() {
        return new BoundingShape(Type.ELLIPSE);
    }

    /**
     * Updates this OBB with the given rectangle parameters.
     * Computes the 4 corners based on position, size, and rotation angle.
     *
     * @param x      Top-left X position (before rotation).
     * @param y      Top-left Y position (before rotation).
     * @param width  Width of the rectangle.
     * @param height Height of the rectangle.
     * @param angle  Rotation angle in degrees (around center).
     * @return this BoundingShape for chaining.
     */
    public BoundingShape updateOBB(float x, float y, float width, float height, float angle) {
        if (type != Type.OBB) {
            throw new IllegalStateException("Cannot update OBB on an ELLIPSE shape");
        }

        float cx = x + width / 2.0f;
        float cy = y + height / 2.0f;
        float halfW = width / 2.0f;
        float halfH = height / 2.0f;

        float angleRad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);

        // Local corners relative to center (before rotation)
        // Order: top-left, top-right, bottom-right, bottom-left
        float[] localX = { -halfW, halfW, halfW, -halfW };
        float[] localY = { -halfH, -halfH, halfH, halfH };

        // Rotate and translate to world coordinates
        for (int i = 0; i < 4; i++) {
            cornersX[i] = localX[i] * cos - localY[i] * sin + cx;
            cornersY[i] = localX[i] * sin + localY[i] * cos + cy;
        }

        // Cache center
        this.centerX = cx;
        this.centerY = cy;

        // Compute and cache the two edge axes (normalized)
        // Axis 1: from corner 0 to corner 1 (top edge)
        float edge1X = cornersX[1] - cornersX[0];
        float edge1Y = cornersY[1] - cornersY[0];
        float len1 = (float) Math.sqrt(edge1X * edge1X + edge1Y * edge1Y);
        if (len1 > 0) {
            axis1X = edge1X / len1;
            axis1Y = edge1Y / len1;
        }

        // Axis 2: from corner 0 to corner 3 (left edge)
        float edge2X = cornersX[3] - cornersX[0];
        float edge2Y = cornersY[3] - cornersY[0];
        float len2 = (float) Math.sqrt(edge2X * edge2X + edge2Y * edge2Y);
        if (len2 > 0) {
            axis2X = edge2X / len2;
            axis2Y = edge2Y / len2;
        }

        return this;
    }

    /**
     * Updates this Ellipse with the given circle/ellipse parameters.
     *
     * @param centerX Center X position.
     * @param centerY Center Y position.
     * @param radiusX Horizontal radius (half width).
     * @param radiusY Vertical radius (half height).
     * @return this BoundingShape for chaining.
     */
    public BoundingShape updateEllipse(float centerX, float centerY, float radiusX, float radiusY) {
        if (type != Type.ELLIPSE) {
            throw new IllegalStateException("Cannot update Ellipse on an OBB shape");
        }
        this.centerX = centerX;
        this.centerY = centerY;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        return this;
    }

    /**
     * Tests if this BoundingShape intersects with another.
     * Handles OBB-OBB, OBB-Ellipse, and Ellipse-Ellipse intersections.
     *
     * @param other The other BoundingShape to test against.
     * @return true if the shapes intersect, false otherwise.
     */
    public boolean intersects(BoundingShape other) {
        if (this.type == Type.OBB && other.type == Type.OBB) {
            return intersectsOBB_OBB(other);
        } else if (this.type == Type.ELLIPSE && other.type == Type.ELLIPSE) {
            return intersectsEllipse_Ellipse(other);
        } else {
            // Mixed: OBB vs Ellipse
            if (this.type == Type.OBB) {
                return intersectsOBB_Ellipse(this, other);
            } else {
                return intersectsOBB_Ellipse(other, this);
            }
        }
    }

    /**
     * OBB vs OBB intersection using the Separating Axis Theorem (SAT).
     * Tests 4 potential separating axes (2 from each OBB).
     *
     * @param other The other OBB.
     * @return true if the OBBs intersect.
     */
    private boolean intersectsOBB_OBB(BoundingShape other) {
        // Test all 4 axes (2 from each OBB)
        // If any axis separates the two OBBs, they don't intersect
        return !isSeparatingAxis(this.axis1X, this.axis1Y, this, other) &&
               !isSeparatingAxis(this.axis2X, this.axis2Y, this, other) &&
               !isSeparatingAxis(other.axis1X, other.axis1Y, this, other) &&
               !isSeparatingAxis(other.axis2X, other.axis2Y, this, other);
    }

    /**
     * Projects an OBB onto an axis and returns min/max values.
     *
     * @param axisX  Axis X component (normalized).
     * @param axisY  Axis Y component (normalized).
     * @param obb    The OBB to project.
     * @param result Array of size 2 to store [min, max].
     */
    private static void projectOBB(float axisX, float axisY, BoundingShape obb, float[] result) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for (int i = 0; i < 4; i++) {
            float projection = obb.cornersX[i] * axisX + obb.cornersY[i] * axisY;
            min = Math.min(min, projection);
            max = Math.max(max, projection);
        }

        result[0] = min;
        result[1] = max;
    }

    /**
     * Checks if an axis separates two OBBs.
     *
     * @param axisX Axis X component.
     * @param axisY Axis Y component.
     * @param a     First OBB.
     * @param b     Second OBB.
     * @return true if the axis separates the OBBs (no overlap).
     */
    private static boolean isSeparatingAxis(float axisX, float axisY,
                                            BoundingShape a, BoundingShape b) {
        float[] projA = new float[2];
        float[] projB = new float[2];

        projectOBB(axisX, axisY, a, projA);
        projectOBB(axisX, axisY, b, projB);

        // Check for overlap: projA.max >= projB.min && projB.max >= projA.min
        return projA[1] < projB[0] || projB[1] < projA[0];
    }

    /**
     * Ellipse vs Ellipse intersection test.
     * Uses a conservative approximation by treating ellipses as circles
     * with the average of their radii.
     *
     * @param other The other ellipse.
     * @return true if the ellipses intersect.
     */
    private boolean intersectsEllipse_Ellipse(BoundingShape other) {
        // Distance between centers
        float dx = this.centerX - other.centerX;
        float dy = this.centerY - other.centerY;
        float distSq = dx * dx + dy * dy;

        // For a more accurate test, we check along the axis connecting centers
        // and use the radius in that direction for each ellipse
        float dist = (float) Math.sqrt(distSq);
        if (dist < 0.0001f) {
            // Centers coincide - definitely intersecting
            return true;
        }

        // Unit vector from other to this
        float ux = dx / dist;
        float uy = dy / dist;

        // Radius of each ellipse in the direction of the other center
        float r1 = ellipseRadiusInDirection(this.radiusX, this.radiusY, ux, uy);
        float r2 = ellipseRadiusInDirection(other.radiusX, other.radiusY, ux, uy);

        return dist <= r1 + r2;
    }

    /**
     * Computes the radius of an axis-aligned ellipse in a given direction.
     *
     * @param rx Horizontal radius.
     * @param ry Vertical radius.
     * @param dx Direction X (normalized).
     * @param dy Direction Y (normalized).
     * @return The radius of the ellipse in that direction.
     */
    private static float ellipseRadiusInDirection(float rx, float ry, float dx, float dy) {
        // For an axis-aligned ellipse: (x/rx)^2 + (y/ry)^2 = 1
        // Point on ellipse in direction (dx, dy) at distance r:
        // (r*dx/rx)^2 + (r*dy/ry)^2 = 1
        // r^2 * ((dx/rx)^2 + (dy/ry)^2) = 1
        // r = 1 / sqrt((dx/rx)^2 + (dy/ry)^2)
        float term = (dx / rx) * (dx / rx) + (dy / ry) * (dy / ry);
        if (term < 0.0001f) return Math.max(rx, ry);
        return 1.0f / (float) Math.sqrt(term);
    }

    /**
     * OBB vs Ellipse intersection test.
     * Uses the closest point on the OBB to the ellipse center approach.
     *
     * @param obb     The OBB shape.
     * @param ellipse The Ellipse shape.
     * @return true if they intersect.
     */
    private static boolean intersectsOBB_Ellipse(BoundingShape obb, BoundingShape ellipse) {
        // Transform ellipse center to OBB local space
        // In local space, OBB is axis-aligned with center at origin

        // Vector from OBB center to ellipse center
        float dx = ellipse.centerX - obb.centerX;
        float dy = ellipse.centerY - obb.centerY;

        // Project onto OBB axes to get local coordinates
        float localX = dx * obb.axis1X + dy * obb.axis1Y;
        float localY = dx * obb.axis2X + dy * obb.axis2Y;

        // Half-extents of OBB (computed from corners)
        float halfW = distance(obb.cornersX[0], obb.cornersY[0],
                               obb.cornersX[1], obb.cornersY[1]) / 2.0f;
        float halfH = distance(obb.cornersX[0], obb.cornersY[0],
                               obb.cornersX[3], obb.cornersY[3]) / 2.0f;

        // Clamp to OBB bounds to find closest point
        float closestX = Math.max(-halfW, Math.min(halfW, localX));
        float closestY = Math.max(-halfH, Math.min(halfH, localY));

        // Transform back to world space
        float worldClosestX = obb.centerX + closestX * obb.axis1X + closestY * obb.axis2X;
        float worldClosestY = obb.centerY + closestX * obb.axis1Y + closestY * obb.axis2Y;

        // Check if closest point is inside ellipse
        float relX = worldClosestX - ellipse.centerX;
        float relY = worldClosestY - ellipse.centerY;

        // Ellipse equation: (x/rx)^2 + (y/ry)^2 <= 1
        float normalizedDist = (relX / ellipse.radiusX) * (relX / ellipse.radiusX)
                             + (relY / ellipse.radiusY) * (relY / ellipse.radiusY);

        return normalizedDist <= 1.0f;
    }

    /**
     * Computes distance between two points.
     */
    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    // --- Getters ---

    public Type getType() {
        return type;
    }

    public float[] getCornersX() {
        return cornersX;
    }

    public float[] getCornersY() {
        return cornersY;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getRadiusX() {
        return radiusX;
    }

    public float getRadiusY() {
        return radiusY;
    }

    public float getAxis1X() {
        return axis1X;
    }

    public float getAxis1Y() {
        return axis1Y;
    }

    public float getAxis2X() {
        return axis2X;
    }

    public float getAxis2Y() {
        return axis2Y;
    }

    /**
     * Returns the axis-aligned bounding box (AABB) that encloses this shape.
     * Useful for broad-phase optimization before detailed SAT tests.
     *
     * @return An array [minX, minY, maxX, maxY].
     */
    public float[] getAABB() {
        if (type == Type.OBB) {
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
            for (int i = 0; i < 4; i++) {
                minX = Math.min(minX, cornersX[i]);
                minY = Math.min(minY, cornersY[i]);
                maxX = Math.max(maxX, cornersX[i]);
                maxY = Math.max(maxY, cornersY[i]);
            }
            return new float[] { minX, minY, maxX, maxY };
        } else {
            return new float[] {
                centerX - radiusX, centerY - radiusY,
                centerX + radiusX, centerY + radiusY
            };
        }
    }

    @Override
    public String toString() {
        if (type == Type.OBB) {
            return "BoundingShape.OBB[center=(%.2f,%.2f), corners=[...]]"
                    .formatted(centerX, centerY);
        } else {
            return "BoundingShape.ELLIPSE[center=(%.2f,%.2f), radii=(%.2f,%.2f)]"
                    .formatted(centerX, centerY, radiusX, radiusY);
        }
    }
}
