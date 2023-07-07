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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metsci.glimpse.util.geo.LatLonGeo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeographicDisk extends GeographicGeometry implements Serializable {

    /**
     * generated
     */
    private static final long serialVersionUID = -1989485638866640497L;
    LatLonGeo center;
    double radiusMeters;

    public GeographicDisk() {
        super();
    }

    public GeographicDisk(LatLonGeo center, double radiusMeters) {
        super();
        this.center = center;
        this.radiusMeters = radiusMeters;
    }

    public LatLonGeo getCenter() {
        return center;
    }

    public double getRadiusMeters() {
        return radiusMeters;
    }

    @JsonIgnore
    public List<LatLonGeo> getApproximatingPolygon() {
        List<LatLonGeo> pts = new ArrayList<LatLonGeo>();

        int numDiv = 32;
        double dTheta = (2 * Math.PI) / numDiv;

        for (int div = 0; div < numDiv; div++) {
            double theta = div * dTheta;
            pts.add(center.displacedBy(radiusMeters, theta));
        }
        return pts;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeographicDisk [center=");
        builder.append(center);
        builder.append(", radiusMeters=");
        builder.append(radiusMeters);
        builder.append("]");
        return builder.toString();
    }

}
