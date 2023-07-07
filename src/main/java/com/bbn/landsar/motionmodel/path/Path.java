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

package com.bbn.landsar.motionmodel.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.geospatial.GeographicDrawable;
import com.bbn.landsar.geospatial.GeographicGeometry;
import com.bbn.landsar.geospatial.GeographicPolyPoints;
import com.bbn.landsar.utils.LandsarUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.metsci.glimpse.util.geo.LatLonGeo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class Path implements GeographicDrawable {

	public static final String PATH_GENERATION = "path-gen";
	protected static final Logger LOGGER = LoggerFactory.getLogger(PATH_GENERATION);

	
	/**
	 * The points on the path. Some implementations create "intermediate points" in between the anchor points. 
	 */
	protected LatLonGeo[] anchorPoints;
	
	public Path() {
	}
	
	public Path(LatLonGeo[] anchorPoints) {
		this.anchorPoints = anchorPoints;
	}

	public Path(List<LatLonGeo> points) {
		this.anchorPoints = points.toArray(new LatLonGeo[points.size()]);
	}	
	
	@JsonIgnore
	public LatLonGeo getStartPoint() {
		return anchorPoints[0];
	}
	
	@JsonIgnore
	public LatLonGeo getEndPoint() {
		return anchorPoints[anchorPoints.length-1];
	}
	
	
	@JsonIgnore
	public List<LatLonGeo> getPoints() {
		return Arrays.asList(anchorPoints);
	}

	@Override
	public List<GeographicGeometry> getGeographicGeometry() {
		List<GeographicGeometry> drawables = new ArrayList<GeographicGeometry>();
	
		GeographicPolyPoints drawable = new GeographicPolyPoints(GeographicPolyPoints.Type.Polyline,
				getPoints());
		drawables.add(drawable);
		return drawables;
	}

	public String getPathString() {
	    return LandsarUtils.getStringForPts(anchorPoints);
	}

	public void setPathString(String pathString) {
	    anchorPoints = LandsarUtils.stringToPts(pathString);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(anchorPoints);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Path other = (Path) obj;
		return Arrays.equals(anchorPoints, other.anchorPoints);
	}
	
	

}
