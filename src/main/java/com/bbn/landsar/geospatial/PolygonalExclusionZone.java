/*
LandSAR Motion Model Software Development Kit
Copyright (c) 2023 Raytheon Technologies 

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
https://github.com/atapas/add-copyright.git
*/

package com.bbn.landsar.geospatial;

import com.bbn.landsar.utils.GeometryUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.TangentPlane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * WARNING:  This code is designed for small regions, i.e. the choice of
 * projection is irrelevant in the larger picture.  Note that, like
 * many other aspects of the code, it will not function properly at the
 * crossover from east to west or near the poles.
 *
 * @author anderson
 *
 */
public class PolygonalExclusionZone implements GeographicDrawable, Serializable {

    private static final long serialVersionUID = -4110475220019113506L;

    private List<LatLonGeo> vertices;

    @JsonIgnore
    private TangentPlane tangentPlane;

    @JsonIgnore
    private double westLonDeg;
    @JsonIgnore
    private double eastLonDeg;
    @JsonIgnore
    private double northLatDeg;
    @JsonIgnore
    private double southLatDeg;

    public PolygonalExclusionZone() {}

    public PolygonalExclusionZone(List<LatLonGeo> vertices) {
        this.vertices = vertices;

        // Set the tangent plane using the first point
        tangentPlane = new TangentPlane(vertices.get(0));

        westLonDeg = vertices.get(0).getLonDeg();
        eastLonDeg = westLonDeg;
        northLatDeg = vertices.get(0).getLatDeg();
        southLatDeg = northLatDeg;

        for (LatLonGeo pt : vertices) {
            westLonDeg = Math.min(westLonDeg, pt.getLonDeg());
            eastLonDeg = Math.max(eastLonDeg, pt.getLonDeg());
            northLatDeg = Math.max(northLatDeg, pt.getLatDeg());
            southLatDeg = Math.min(southLatDeg, pt.getLatDeg());
        }

    }

    /**
     * WARNING:  This method assumes a small polygon, i.e. one for
     * which the choice of projection is irrelevant.  (Technically
     * the inside of a polygon is a meaningless concept on a sphere.)
     *
     * @param pt - location to be checked
     * @return - true if the point is inside the polygon
     */
    public boolean pointInside(LatLonGeo pt) {

        if (pt.getLatDeg() > northLatDeg) return false;
        if (pt.getLatDeg() < southLatDeg) return false;
        if (pt.getLonDeg() > eastLonDeg) return false;
        if (pt.getLonDeg() < westLonDeg) return false;

        return GeometryUtils.polygonContainsPoint(pt, vertices);
    }

    public List<LatLonGeo> getVertices() {
        return vertices;
    }

    public void setVertices(List<LatLonGeo> vertices) {
        this.vertices = vertices;
    }

    public static boolean pointInsideSet(List<PolygonalExclusionZone> zones,
                                         LatLonGeo pt) {
        for (PolygonalExclusionZone zone : zones) {
            if (zone.pointInside(pt)) return true;
        }
        return false;
    }

    @Override
    public List<GeographicGeometry> getGeographicGeometry() {
        List<GeographicGeometry> geoDrawables = new ArrayList<GeographicGeometry>();

        GeographicGeometry geoDrawable =
                new GeographicPolyPoints(GeographicPolyPoints.Type.Polygon, vertices);
        geoDrawables.add(geoDrawable);
        return geoDrawables;
    }
}

