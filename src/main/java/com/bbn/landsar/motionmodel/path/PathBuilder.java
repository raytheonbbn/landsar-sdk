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

import com.metsci.glimpse.util.geo.LatLonGeo;

public class PathBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(PathBuilder.class);
			
	List<LatLonGeo> anchorPoints= new ArrayList<>();
	double lengthInMeters = Double.NaN;
	

    public PathBuilder(LatLonGeo[] points) {
		this.anchorPoints = Arrays.asList(points);
	}

	public PathBuilder() {
		lengthInMeters = 0;
	}

	public PathBuilder(LatLonGeo point) {
		this.anchorPoints = new ArrayList<>();
		this.anchorPoints.add(point);
	}

	public void appendPoint(LatLonGeo point) {
    	if (point == null) {
    		LOGGER.warn("Not appending null point to path");
    	}else {	
    		anchorPoints.add(point);
    		// recalculate path length if/when we need it
    		this.lengthInMeters = Double.NaN;
    	}
    }
    
    public void appendPath(PathBuilder other) {
        anchorPoints.addAll(other.anchorPoints);
        lengthInMeters=this.getPathLength() + 
        		this.anchorPoints.get(this.anchorPoints.size()-1).getDistanceTo(other.anchorPoints.get(0))
        		+ other.getPathLength();
    }
    
    public LatLonGeo removePointAtIndex(int index) {
    	// recalculate path length if/when we need it
    	this.lengthInMeters = Double.NaN;
    	return anchorPoints.remove(index);
    	
    }

    public Path build() {
        return new Path(anchorPoints);
    }
    
    /**
     * In meters
     * @return
     */
    public double getPathLength() {
    	if (Double.isNaN(this.lengthInMeters)){
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

	public void prependPoint(LatLonGeo first) {
		anchorPoints.add(0, first);
    	// recalculate path length if/when we need it
    	this.lengthInMeters = Double.NaN;
	}

	public LatLonGeo getLastPoint() {
		return anchorPoints.get(getNumPoints()-1);
	}

}
