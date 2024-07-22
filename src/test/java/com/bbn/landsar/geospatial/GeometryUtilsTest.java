package com.bbn.landsar.geospatial;

import com.bbn.landsar.utils.GeometryUtils;
import com.metsci.glimpse.util.geo.LatLonGeo;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

/**
 * Tests functionality with GeometryUtils
 */
public class GeometryUtilsTest {

    @Test
    public void testPointInsidePolygon() {
        List<LatLonGeo> polygon = Arrays.asList(
                new LatLonGeo(0, 0),
                new LatLonGeo(0, 10),
                new LatLonGeo(10, 10),
                new LatLonGeo(10, 0)
        );
        LatLonGeo point = new LatLonGeo(5, 5);
        assertTrue(GeometryUtils.polygonContainsPoint(point, polygon));
    }

    @Test
    public void testPointOutsidePolygon() {
        List<LatLonGeo> polygon = Arrays.asList(
                new LatLonGeo(0, 0),
                new LatLonGeo(0, 10),
                new LatLonGeo(10, 10),
                new LatLonGeo(10, 0)
        );
        LatLonGeo point = new LatLonGeo(15, 5);
        assertFalse(GeometryUtils.polygonContainsPoint(point, polygon));
    }

    @Test
    public void testPointOnEdgeOfPolygon() {
        List<LatLonGeo> polygon = Arrays.asList(
                new LatLonGeo(0, 0),
                new LatLonGeo(0, 10),
                new LatLonGeo(10, 10),
                new LatLonGeo(10, 0)
        );
        LatLonGeo point = new LatLonGeo(0, 5);
        assertTrue(GeometryUtils.polygonContainsPoint(point, polygon));
    }

    @Test
    public void testPointOnVertexOfPolygon() {
        List<LatLonGeo> polygon = Arrays.asList(
                new LatLonGeo(0, 0),
                new LatLonGeo(0, 10),
                new LatLonGeo(10, 10),
                new LatLonGeo(10, 0)
        );
        LatLonGeo point = new LatLonGeo(0, 0);
        assertTrue(GeometryUtils.polygonContainsPoint(point, polygon));
    }

    @Test
    public void testPointInConcavePolygon() {
        List<LatLonGeo> polygon = Arrays.asList(
                new LatLonGeo(0, 0),
                new LatLonGeo(5, 5),
                new LatLonGeo(10, 0),
                new LatLonGeo(5, 10)
        );
        LatLonGeo point = new LatLonGeo(4, 4);
        assertTrue(GeometryUtils.polygonContainsPoint(point, polygon));
    }

    @Test
    public void testPointOutsideConcavePolygon() {
        List<LatLonGeo> polygon = Arrays.asList(
                new LatLonGeo(0, 0),
                new LatLonGeo(5, 5),
                new LatLonGeo(10, 0),
                new LatLonGeo(5, 10)
        );
        LatLonGeo point = new LatLonGeo(5, 4);
        assertFalse(GeometryUtils.polygonContainsPoint(point, polygon));
    }
}

