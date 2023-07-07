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

import java.util.Arrays;
import java.util.UUID;

import com.bbn.landsar.geospatial.BoundingBox;
import com.bbn.landsar.searchtheory.ContainmentMap;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.util.DistanceAzimuth;


/**
 * Represents a Probability Distribution at an instant in time for a LostPersonInstance. 
 * The distributions over time should each be over the entire bounding box,
 * with cellProbs[0][0] corresponding to the northwest corner of the box
 *
 * @author crock
 *
 */
public class ProbabilityDistribution implements ContainmentMap {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * This property is used for as part of the external search API
	 */
	@JsonProperty
	public static final String msgType = "ProbabilityDistribution";
	@JsonProperty
	private UUID lpiId;

	@JsonProperty
	private long time;

	@JsonProperty
	private double[][] cellProbs;

	@JsonProperty
	private BoundingBox bbox;


	@JsonIgnore
	private int numLat;

	@JsonIgnore
	private int numLon;


	public ProbabilityDistribution() {
		// json constructor
	}

	public ProbabilityDistribution(UUID lpiId, long time, double[][] probabilities, BoundingBox bbox) {
		this.lpiId = lpiId;
		this.time = time;
		this.cellProbs = probabilities;
		this.setBoundingBox(bbox);
	}


	public UUID getLpiId() {
		return lpiId;
	}


	public void setLpiId(UUID lpiId) {
		this.lpiId = lpiId;
	}


	public long getTime() {
		return time;
	}


	public void setTime(long time) {
		this.time = time;
	}


	@Override
	public BoundingBox getBoundingBox() {
		return this.bbox;
	}

	public void setBoundingBox(BoundingBox bbox) {
		this.bbox = bbox;
		int[] cellDimensions = ContainmentMap.determineNumLonAndNumLat(bbox, ContainmentMap.MAX_NUM_CELLS, ContainmentMap.MIN_CELL_SIDE_KM);
		this.numLat = cellDimensions[0];
		this.numLon = cellDimensions[1];

	}

	@JsonIgnore
	@Override
	public double getEWCellExtentKm() {
		return this.bbox.calcEwExtent() / 1000.0;
	}

	@JsonIgnore
	@Override
	public double getNSCellExtentKm() {
		return this.bbox.calcNsExtent() / 1000.0;
	}

	@JsonIgnore
	@Override
	public int getNumLat() {
		return numLat;
	}

	@JsonIgnore
	@Override
	public int getNumLon() {
		return numLon;
	}
	
	/**
	 * Return a new probability distribution with each cell probability multiplied by factor
	 * @param factor
	 * @return
	 */
	public ProbabilityDistribution scalarMultiply(double factor) {
		double[][] newProbs = new double[cellProbs.length][cellProbs[0].length];
		for (int i = 0; i< cellProbs.length; i++) {
			for (int j=0; j < cellProbs[0].length; j++) {
				newProbs[i][j] = cellProbs[i][j] * factor;
			}
		}
		return new ProbabilityDistribution(lpiId, time, newProbs, bbox);
	}
	
	/**
	 * Return a new probability distribution with each cell probability multiplied by factor
	 * @param factor
	 * @return
	 */
	public ProbabilityDistribution add(ProbabilityDistribution other) {
		if (!this.bbox.equals(other.getBoundingBox())) {
			throw new IllegalArgumentException("can't combine probability distributions when bounding boxes are not equal");
		}
		double[][] otherProbs = other.getCellProbs();
		double[][] newProbs = new double[cellProbs.length][cellProbs[0].length];
		for (int i = 0; i< cellProbs.length; i++) {
			for (int j=0; j < cellProbs[0].length; j++) {
				newProbs[i][j] = cellProbs[i][j] + otherProbs[i][j];
			}
		}
		return new ProbabilityDistribution(lpiId, time, newProbs, bbox);
	}


	@Override
	public double[][] getCellProbs() {
		return this.cellProbs;
	}
	
	public ValidationInfo validate() {
		ValidationInfo validationInfo = new ValidationInfo();
		if (bbox == null || bbox.calcEwExtent() == 0 || bbox.calcNsExtent() == 0) {
			validationInfo.addError("distribution at " + this.time + " has null or zero-dimention bounding box");
		} else {// invalid bounding box means we can't calculate this, so only try if it's valid 
			// calculate the cumulative sum like we do in the random cell selector
			int ewCount = this.getNumLon();
			int nsCount = this.getNumLat();
			double[][] cellProbs = this.getCellProbs();
			if (nsCount != cellProbs.length) {
				validationInfo.addError("Inconsistent probability array. Expecting " + nsCount + " rows but have " + cellProbs.length);
				// prevent ArrayIndex Out of Bounds Exception and return early
				return validationInfo;
			} else if (ewCount != this.getCellProbs()[0].length) {
				validationInfo.addError("Inconsistent probability array. Expecting " + ewCount + " cells in a row but have " + this.getCellProbs()[0].length);
				// prevent ArrayIndex Out of Bounds Exception and return early
				return validationInfo;
			}
			double cumulativeSum = 0.0;
			for (int i = 0; i < nsCount; i++) {
				for (int j = 0; j < ewCount; j++) {
					double thisCellProb = cellProbs[i][j];
					if (thisCellProb < 0) {
						validationInfo.addError("negative cell probability: " + thisCellProb);
					}
					cumulativeSum += thisCellProb;
				}
			}
			if (cumulativeSum < 0) {
				validationInfo.addError("Probability Distribution for " + this.time + " has cumulative probability of " + cumulativeSum );
			} else if (cumulativeSum == 0) {
				// search algs won't work -- maybe the right thing to do for 0 probability is send an error for the search instead of a validation error here
				validationInfo.addWarning("Probability Distribution for " + this.time + " has cumulative probability of " + cumulativeSum + ". Searches can't be suggested when the distribution has zero probability in the bounding box");
			}
		}
		return validationInfo;
	}
	
	public ProbabilityDistribution copy() {
		return new ProbabilityDistribution(lpiId, time, Arrays.stream(cellProbs).map(double[]::clone).toArray(double[][]::new), bbox);
	}

	public BoundingBox calculateCellForIndices(int latIndex, int lonIndex) {
   	
    	// selected the cell at [latIndex][lonIndex], calculate its lat and lon. 
    	// cellProbs[0][0] corresponds to the northwest corner of the bounding box
    	BoundingBox entireBox = bbox;
    	    	
    	final double ewCellExtentM = getEWCellExtentKm() * 1000.0;
    	final double nsCellExtentM = getNSCellExtentKm() * 1000.0;
    	LatLonGeo nwCorner = LatLonGeo.fromDeg(entireBox.getNorthLatDeg(), entireBox.getWestLonDeg());
    	double distanceEast = lonIndex * ewCellExtentM;
    	// actually in the south direction, so multiply by -1
    	double distanceNorth = -1 * latIndex * nsCellExtentM;
    	LatLonGeo cellNWcorner = nwCorner.displacedBy(DistanceAzimuth.fromEastNorth(distanceEast, distanceNorth));
    	LatLonGeo cellSEcorner = cellNWcorner.displacedBy(DistanceAzimuth.fromEastNorth(ewCellExtentM,  -1 * nsCellExtentM));

    	return new BoundingBox(cellNWcorner.getLatDeg(), cellSEcorner.getLatDeg(), cellSEcorner.getLonDeg(), cellNWcorner.getLonDeg());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProbabilityDistribution [lpiId=");
		builder.append(lpiId);
		builder.append(", time=");
		builder.append(time);
		builder.append(", bbox=");
		builder.append(bbox);
		builder.append(", numLat=");
		builder.append(numLat);
		builder.append(", numLon=");
		builder.append(numLon);
		builder.append("]");
		return builder.toString();
	}

}
