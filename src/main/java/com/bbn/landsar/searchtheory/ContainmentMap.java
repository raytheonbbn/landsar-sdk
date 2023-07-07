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

package com.bbn.landsar.searchtheory;

import java.io.Serializable;
import java.util.List;

import com.bbn.landsar.geospatial.BoundingBox;
import com.metsci.glimpse.util.geo.LatLonGeo;

public interface ContainmentMap extends Serializable {
	
	public static double MIN_CELL_SIDE_KM = 0.2;

	public static int MAX_NUM_CELLS = 2500; //120 * 120;
	
	static final double DEG_2_KM = 1.0e4 / 90;
	

	BoundingBox getBoundingBox();

	double getEWCellExtentKm();

	double getNSCellExtentKm();
	
	int getNumLat();
	
	int getNumLon();

	/**
	 *
	 * @return the array of probabilities for each cell in the containment
	 * mapping
	 * double[lat][lon]
	 */
	double[][] getCellProbs();
	
	// Adjusts grid extent to cover the pts	
	public static BoundingBox determineBoundingBoxFromPoints(List<LatLonGeo> pts) {
		
		
		double minLat = Double.POSITIVE_INFINITY;
    	double maxLat = Double.NEGATIVE_INFINITY;
    	double minLon = Double.POSITIVE_INFINITY;
    	double maxLon = Double.NEGATIVE_INFINITY;
    	
    	for (LatLonGeo pt : pts) {
    		minLat = Math.min(minLat, pt.getLatDeg());
    		maxLat = Math.max(maxLat, pt.getLatDeg());
    		minLon = Math.min(minLon, pt.getLonDeg());
    		maxLon = Math.max(maxLon, pt.getLonDeg());
    	}
    	
    	double latLonDelta = 0.02;
    	minLat -= latLonDelta;
    	maxLat += latLonDelta;
    	minLon -= latLonDelta;
    	maxLon += latLonDelta;
    	
    	return new BoundingBox(maxLat, minLat, maxLon, minLon );
	}
	
	/**
	 * Perform a deep copy of this ContainmentMap
	 * @return a new ContainmentMap instance with the same values (but that can be modified without changing the current instance)
	 */
	public ContainmentMap copy();
	
	/**
	 * @param latIndex
	 * @param lonIndex
	 * @return the lat/lon points (as a bounding box) corresponding to the "cell" at the given indices
	 */
	public BoundingBox calculateCellForIndices(int latIndex, int lonIndex);
	
	public static int[] determineNumLonAndNumLat(BoundingBox bbox) {
		return determineNumLonAndNumLat(bbox, MAX_NUM_CELLS, MIN_CELL_SIDE_KM);
	}
	
	public static int[] determineNumLonAndNumLat(
			BoundingBox bbox, int maxNumCells, double minCellSideKm) {

    	int numLat = 0;
    	int numLon = 0;
    	
    	double latExtentKm = (bbox.getNorthLatDeg() - bbox.getSouthLatDeg()) * DEG_2_KM;
    	double latAdjustment = Math.cos(Math.toRadians((bbox.getNorthLatDeg() + bbox.getSouthLatDeg()) / 2));
    	double lonExtentKm = (bbox.getEastLonDeg() - bbox.getWestLonDeg()) * latAdjustment * DEG_2_KM;
    	
    	int numCells = (int) (latExtentKm * lonExtentKm / (minCellSideKm * minCellSideKm));
    	
    	if (numCells <= maxNumCells) {
    		numLat = (int)(latExtentKm / minCellSideKm);
    		numLon = (int)(lonExtentKm / minCellSideKm);
    	}
    	else {
    		double cellSide = Math.sqrt(((latExtentKm * lonExtentKm) / maxNumCells));
    		numLat = (int)(latExtentKm / cellSide);
    		numLon = (int)(lonExtentKm / cellSide);
    	}
    	
    	numLat = Math.max(numLat, 1);
    	numLon = Math.max(numLon, 1);
    	
    	return new int[] {numLat, numLon};
    }

}
