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

package com.bbn.landsar.motionmodel;

import com.bbn.landsar.geospatial.ExclusionZone;
import com.bbn.landsar.geospatial.PolygonalExclusionZone;
import com.metsci.glimpse.util.geo.LatLonGeo;

import java.util.List;

/**
 * LandSAR currently supports user-entered geospatial inputs of the following types: 
- Goal Points: a set of destinations an lost person may be trying to get to
- Waypoints: a set of locations a lost person might be drawn to, or stop at along the way to their end destination, for example, water sources in a desert. 
- Exclusion Zones: areas that will be avoided by a lost person. Usually, exclusion zones are implemented as circles since they are easier to find a path around, but some models may specify polygon exclusion zones. 
	- known exclusion zones are avoided at a distance, whereas unknown exclusion zones are avoided once encountered. 

All user-entered geospatial inputs are currently constrained to the confines of the bounding box. 
*/
public class UserEnteredGeospatialData {
	public enum GeospatialDataType {
		EXCLUSION_ZONE,
		POLYGON_EXCLUSIONS,
		GOAL_POINTS,
		WAY_POINTS
	}

	private List<ExclusionZone> exclusionZones;
	private List<PolygonalExclusionZone> polyExclusions;
	private List<LatLonGeo> goalPoints;
	private List<LatLonGeo> waypoints;


	public UserEnteredGeospatialData(List<ExclusionZone> exclusionZones,
									 List<PolygonalExclusionZone> polyExclusions,
									 List<LatLonGeo> goalPoints,
									 List<LatLonGeo> waypoints) {
		this.exclusionZones = exclusionZones;
		this.polyExclusions = polyExclusions;
		this.goalPoints = goalPoints;
		this.waypoints = waypoints;
	}

	public List<ExclusionZone> getExclusionZones() {
		return exclusionZones;
	}

	public void setExclusionZones(List<ExclusionZone> exclusionZones) {
		this.exclusionZones = exclusionZones;
	}

	public List<PolygonalExclusionZone> getPolyExclusions() {
		return polyExclusions;
	}

	public void setPolyExclusions(List<PolygonalExclusionZone> polyExclusions) {
		this.polyExclusions = polyExclusions;
	}

	public List<LatLonGeo> getGoalPoints() {
		return goalPoints;
	}

	public void setGoalPoints(List<LatLonGeo> goalPoints) {
		this.goalPoints = goalPoints;
	}

	public List<LatLonGeo> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(List<LatLonGeo> waypoints) {
		this.waypoints = waypoints;
	}
}
