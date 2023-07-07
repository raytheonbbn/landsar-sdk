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

import com.metsci.glimpse.util.geo.LatLonGeo;

import java.util.List;

public class GeographicPolyPoints extends GeographicGeometry {

    public static enum Type {Polyline, Polygon}

    Type type;
    List<LatLonGeo> pts;

    public GeographicPolyPoints(Type type, List<LatLonGeo> pts) {
        super();
        this.type = type;
        this.pts = pts;
    }

    public Type getType() {
        return type;
    }

    public List<LatLonGeo> getPts() {
        return pts;
    }

}
