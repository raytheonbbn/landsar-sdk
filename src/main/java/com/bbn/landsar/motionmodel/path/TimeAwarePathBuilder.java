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
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.utils.GroundUtilities;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.vector.Vector2d;

public class TimeAwarePathBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeAwarePathBuilder.class);
			
	List<LatLonGeo> anchorPoints= new ArrayList<>();
	double lengthInMeters = Double.NaN;

	List<Long> times = new ArrayList<>();

    public TimeAwarePathBuilder(LatLonGeo[] points) {
		this.anchorPoints = Arrays.asList(points);
	}

	public TimeAwarePathBuilder(LatLonGeo point, long startTime) {
		this.anchorPoints = new ArrayList<>();
		this.anchorPoints.add(point);
		this.times = new ArrayList<>();
		this.times.add(startTime);
	}

	public void appendPoint(LatLonGeo point, long time) {
    	if (point == null) {
    		LOGGER.warn("Not appending null point to path");
    	}else {	
    		times.add(time);
    		anchorPoints.add(point);
    		// recalculate path length if/when we need it
    		this.lengthInMeters = Double.NaN;
    	}
    }
    
    public void appendPath(TimeAwarePathBuilder other) {
        anchorPoints.addAll(other.anchorPoints);
        times.addAll(other.times);
        lengthInMeters=this.getPathLength() + 
        		this.anchorPoints.get(this.anchorPoints.size()-1).getDistanceTo(other.anchorPoints.get(0))
        		+ other.getPathLength();
    }
    
    public LatLonGeo removePointAtIndex(int index) {
    	times.remove(index);
    	// recalculate path length if/when we need it
    	this.lengthInMeters = Double.NaN;
    	return anchorPoints.remove(index);
    	
    }

    public TimeAwarePath build() {
    	if (times.size() != anchorPoints.size()) {
    		throw new IllegalStateException("Times and points are not in sync");
    	}
        return new TimeAwarePath(anchorPoints, times);
    }
    
    /**
     * In meters
     * @return
     */
    public double getPathLength() {
    	if (this.lengthInMeters == Double.NaN){
    		lengthInMeters = 0;
    		for (int i = 0; i< anchorPoints.size()-1; i++) {
    			lengthInMeters += anchorPoints.get(i).getDistanceTo(anchorPoints.get(i+1));
    		}
    		return lengthInMeters;
    	}
    	//already calculated
    	return lengthInMeters;
    }
    
    public int getNumPoints() {
    	return anchorPoints.size();
    }

	public void prependPoint(LatLonGeo first, long time) {
		times.add(0, time);
		anchorPoints.add(0, first);
    	// recalculate path length if/when we need it
    	this.lengthInMeters = Double.NaN;
	}

	public LatLonGeo getLastPoint() {
		return anchorPoints.get(getNumPoints()-1);
	}

}
