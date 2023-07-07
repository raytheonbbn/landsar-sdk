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

import com.bbn.landsar.MovementSchedule;
import com.bbn.landsar.geospatial.AreaData;
import com.bbn.landsar.geospatial.Velocity2d;
import com.bbn.landsar.motionmodel.UserEnteredGeospatialData;
import com.bbn.landsar.utils.DateTimeUtilities;
import com.bbn.landsar.utils.GroundUtilities;
import com.metsci.glimpse.util.geo.LatLonGeo;

public class SampleBuilder {
	

	
	public static class PointAndTime{
		public final LatLonGeo point;
		public final long time;
		
		public PointAndTime(LatLonGeo point, long time) {
			this.point = point;
			this.time = time;
		}
	}

	private final long timeDelta;
	private PathBuilder pathBuilder;
	private long startTime;
	private Long endTime = null;
	private PointAndTime mostRecentPointTime;
	private double direction;
	private double maxDistanceMeters;
	private double speedKph;

	public SampleBuilder(long startTime, long timeDelta, LatLonGeo point, double direction, double distanceKm, double speedKph) {
		// can only be in one place at a time 
		this.pathBuilder = new PathBuilder(point);
		this.mostRecentPointTime = new PointAndTime(point, startTime);
		this.startTime = startTime;
		this.timeDelta = timeDelta;
		this.direction = direction;
		this.maxDistanceMeters = distanceKm * 1000.0;
		this.speedKph = speedKph; // kilometers per hour
		
	}
	
	public SampleBuilder(long startTime, long timeDelta, LatLonGeo point, Velocity2d directionVector,
			Double distanceKm, Double speed) {
		this(startTime, timeDelta, point, directionVector.getHeading(), distanceKm, speed);
	}

	void setEndTime(long endTime){
		this.endTime = endTime;
	}
	
	/**
	 * 
	 * @return true iff the endTime has been set and the provided time is equal to or after the endTime
	 */
	public boolean reachedLogicalEnd(long time) {
		if (endTime == null) {
			return false;
		}
		return (time > endTime);
	}

	public void computeNextPoint(long time, MovementSchedule schedule, AreaData areaData, UserEnteredGeospatialData geospatialInputs, boolean stayOutOfWater) {
		PointAndTime previousPointAndTime = this.mostRecentPointTime;
		
		if (previousPointAndTime.time + this.timeDelta != time) {
			//TODO instead of throwing an exception, just add extra points at the same location?
			throw new IllegalArgumentException("method called with unexpected time: " + time + ", expecting :" + (previousPointAndTime.time + this.timeDelta));
		}
		LatLonGeo prevPt = previousPointAndTime.point;
		
		// Movement schedule models when the Lost Person is moving 
		long movingTime = schedule.timeSpentMoving(previousPointAndTime.time, time);
		double movingTimeInHours = movingTime / DateTimeUtilities.millisecInHour;

		double movingDistanceInMeters = 1000 * this.speedKph * movingTimeInHours * areaData.getLandcoverData().getSoaFactor(prevPt.getLatDeg(), prevPt.getLonDeg());
		LatLonGeo newPoint;
		if ((pathBuilder.getPathLength() + movingDistanceInMeters) < maxDistanceMeters) {

			newPoint = prevPt.displacedBy(movingDistanceInMeters, direction);
		} else { // ((pathBuilder.getPathLength() + movingDistanceInMeters) >= maxDistance) {
			// just move up to the max distance
			movingDistanceInMeters = maxDistanceMeters - pathBuilder.getPathLength();
			newPoint = prevPt.displacedBy(movingDistanceInMeters, direction);
			if (isPointValid(newPoint, geospatialInputs, areaData, stayOutOfWater)) {
				this.pathBuilder.appendPoint(newPoint);
				this.mostRecentPointTime = new PointAndTime(newPoint, time);
				this.endTime = time;
			} else { // the point was not valid
				// no randomness, so don't try to do the same thing again
				this.endTime = previousPointAndTime.time;
			}
			return;
		}
		
		boolean validPoint = isPointValid(newPoint, geospatialInputs, areaData, stayOutOfWater);
		if (validPoint) {
			this.pathBuilder.appendPoint(newPoint);
			this.mostRecentPointTime = new PointAndTime(newPoint, time);
		} else {
			this.endTime = previousPointAndTime.time;
		}

	}

	private boolean isPointValid(LatLonGeo newPoint, UserEnteredGeospatialData geospatialInputs, AreaData areaData, boolean stayOutOfWater) {
		// a more detailed / robust model might handle the point in the exclusion zone by going around it, but we'll assume the lost person just stops if they hit an exclusion zone. 
		if (GroundUtilities.exclusionZonesContainsPoint(geospatialInputs.getExclusionZones(), newPoint)
				// Motion Models can't represent points outside the bounding box, so filter those out
				|| !areaData.getBoundingBox().contains(newPoint)) {
			return false;			
		}
		if (stayOutOfWater) {
			return (!areaData.getLandcoverData().isWater(newPoint.getLatDeg(), newPoint.getLonDeg()));
		}
		return true;
	}

	public Sample build() {
		if (this.endTime == null) {
			throw new IllegalStateException("No end time!");
		}
		//long startTime, long timeDelta, List<LatLonGeo> pts, 	List<Double> resourceLevelChange
		// since we add the points to the pathBuilder with the time delta in mind we should have the correct (time) spacing in the points from pathBuilder.build().getPoints()
		return new Sample(startTime, timeDelta, pathBuilder.build().getPoints(), new ArrayList<Double>());
	}

}
