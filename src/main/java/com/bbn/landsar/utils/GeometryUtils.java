package com.bbn.landsar.utils;

import com.metsci.glimpse.util.geo.LatLonGeo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GeometryUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryUtils.class);

    /**
     * Determines if a point is inside a polygon using the ray-casting algorithm.
     *
     * @param p The point to check.
     * @param polygon The list of vertices defining the polygon.
     * @return True if the point is inside the polygon, false otherwise.
     */
    public static boolean polygonContainsPoint(LatLonGeo p, List<LatLonGeo> polygon) {
        int numVertices = polygon.size();
        if (numVertices < 3) {
            LOGGER.warn("Polygon has less than 3 points, not valid");
            return false; // A polygon must have at least 3 vertices
        }

        boolean inside = false;
        double x = p.getLonDeg();
        double y = p.getLatDeg();

        for (int i = 0, j = numVertices - 1; i < numVertices; j = i++) {
            double xi = polygon.get(i).getLonDeg();
            double yi = polygon.get(i).getLatDeg();
            double xj = polygon.get(j).getLonDeg();
            double yj = polygon.get(j).getLatDeg();

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }
}
