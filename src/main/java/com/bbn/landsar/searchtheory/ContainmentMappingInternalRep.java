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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.geospatial.BoundingBox;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.util.DistanceAzimuth;


/**
 * this is the "heat map" (red/yellow boxes) / probability distribution. 
 * Which has "containment level" – 50% containment level is the smallest area that contains 50% of the probability. 
 * 'Containment Mapping' is used in probability theory. 
 *
 * TODO refactor this class to use ImageUtils to create the ContainmentMap Image
 */
public class ContainmentMappingInternalRep implements ContainmentMap {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ContainmentMappingInternalRep.class.getName());

	public static final Integer HIGH_THRESHOLD = 99;
	public static final Integer MIDDLE_THRESHOLD = 90;
	public static final Integer LOW_THRESHOLD = 50;

	final BoundingBox bbox;	
	final int numLat;
	final int numLon;

	transient final List<LatLonGeo> pts;
	transient final List<Double> ptProbs;
	
	double fiftyPercentCutoff;
	double ninetyPercentCutoff;
	double ninetyNinePercentCutoff;

    double[][] cellProbs;

	/**
	 * The ContainmentMapping.Node class holds information about the probability
	 * that a lost person will be found at a specific location.
	 */
	public static class Node {
		private LatLonGeo location;
		private double probability;

		public Node(LatLonGeo location, double probability) {
			this.location = location;
			this.probability = probability;
		}

		public LatLonGeo getLocation() {
			return location;
		}

		public double getProbability() {
			return probability;
		}

		/**
		 * Two containment mapping nodes compare equal iff they are at the same
		 * location. The probability associated with nodes on the same location
		 * should be the same, but they don't have to be.
		 * @return true if equal, false otherwise.
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Node node = (Node) o;
			return Objects.equals(location, node.location);
		}

		@Override
		public int hashCode() {
			return Objects.hash(location);
		}
	}


	public static ContainmentMap getContainmentMapping(	int maxNumCells, double minCellSideKm, List<LatLonGeo> pts,
			List<Double> ptProbs) {
		
		BoundingBox bbox = ContainmentMap.determineBoundingBoxFromPoints(pts);
		int[] numLatnumLon = ContainmentMap.determineNumLonAndNumLat(bbox, maxNumCells, minCellSideKm);
    	
    	return new ContainmentMappingInternalRep(bbox, numLatnumLon[0], numLatnumLon[1], pts, ptProbs);
    }

	public ContainmentMappingInternalRep(BoundingBox bbox, int numLat, int numLon, List<LatLonGeo> pts, List<Double> ptProbs){


		this.bbox = bbox;
		
		this.numLat = numLat;
		this.numLon = numLon;
		this.pts = new ArrayList<>(pts);
		this.ptProbs = new ArrayList<>(ptProbs);
		
		cellProbs = new double[numLat][numLon];
		
		for (int i = 0; i < pts.size(); i++) {
			LatLonGeo pt = pts.get(i);
			double lat = pt.getLatDeg();
			double lon = pt.getLonDeg();
			// determine the indices for each point
			int latIndx = (int)(((bbox.getNorthLatDeg() - lat) / (bbox.getNorthLatDeg() - bbox.getSouthLatDeg())) * numLat);
			int lonIndx = (int)(((lon - bbox.getWestLonDeg()) / (bbox.getEastLonDeg() - bbox.getWestLonDeg())) * numLon);

			// ensure we obey bounds of the area
			latIndx = Math.max(latIndx, 0);
			latIndx = Math.min(latIndx, numLat - 1);
			lonIndx = Math.max(lonIndx, 0);
			lonIndx = Math.min(lonIndx, numLon - 1);
			
			// add probability of being at each point to the probability for that cell
			cellProbs[latIndx][lonIndx] += ptProbs.get(i);
		}
		
		// Get the maximum value for the cells
		// Also get a listing of the values for containment level computation
		double totalProb = 0.0;
		List<Double> values = new ArrayList<Double>();
		double maxProb = 0;
		for (int i = 0; i < cellProbs.length; i++) {
			for (int j = 0; j < cellProbs[i].length; j++) {
				maxProb = Math.max(maxProb, cellProbs[i][j]);
				totalProb += cellProbs[i][j];
				values.add(cellProbs[i][j]);
			}
		}
		
		Collections.sort(values);
		Collections.reverse(values);
		
		List<Double> cumSum = new ArrayList<>();
		cumSum.add(values.get(0));
		for (int i = 1; i < values.size(); i++){
			cumSum.add(cumSum.get(i - 1) + values.get(i));
		}
		
		// Set cutoff values
		fiftyPercentCutoff = 0;
		for (int i = 1; i < values.size(); i++) {
			if (cumSum.get(i) > 0.5 * totalProb) {
				fiftyPercentCutoff = values.get(i);
				break;
			}
		}
		ninetyNinePercentCutoff = 0;
		for (int i = 1; i < values.size(); i++) {
			if (cumSum.get(i) > 0.99 * totalProb) {
				ninetyNinePercentCutoff = values.get(i);
				break;
			}
		}		
		ninetyPercentCutoff = 0;
		for (int i = 1; i < values.size(); i++) {
			if (cumSum.get(i) > 0.90 * totalProb) {
				ninetyPercentCutoff = values.get(i);
				break;
			}
		}		

	}
	
	private ContainmentMappingInternalRep(BoundingBox bbox, int numLat, int numLon, List<LatLonGeo> pts, List<Double> ptProbs, double fiftyPercentCutoff,double ninetyPercentCutoff, double ninetyNinePercentCutoff, double[][] cellProbs) {
		this.bbox = bbox;
		this.numLat = numLat;
		this.numLon = numLon;
		this.pts = pts;
		this.ptProbs = ptProbs;
		this.fiftyPercentCutoff = fiftyPercentCutoff;
		this.ninetyPercentCutoff = ninetyPercentCutoff;
		this.ninetyNinePercentCutoff = ninetyNinePercentCutoff;
		this.cellProbs = cellProbs;
		
	}
	
	@Override
	public ContainmentMap copy() {
		return new ContainmentMappingInternalRep(bbox, numLat, numLon, new ArrayList<>(pts), new ArrayList<>(ptProbs),
				fiftyPercentCutoff, ninetyPercentCutoff, ninetyNinePercentCutoff, Arrays.stream(cellProbs).map(double[]::clone).toArray(double[][]::new));
	}

	@Override
	public BoundingBox getBoundingBox() {
		return bbox;
	}

	@Override
	public double getEWCellExtentKm() {
		double ewExtentDeg = bbox.getEastLonDeg() - bbox.getWestLonDeg();
		double midLatDeg = (bbox.getNorthLatDeg() + bbox.getSouthLatDeg()) / 2;
		return Math.cos(Math.toRadians(midLatDeg)) * ewExtentDeg * DEG_2_KM / numLon;
	}
	
	@Override
	public double getNSCellExtentKm() {
		double nsExtentDeg = bbox.getNorthLatDeg() - bbox.getSouthLatDeg();
		return nsExtentDeg * DEG_2_KM / numLat;
	}

	/**
	 *
	 * @return the array of probabilities for each cell in the containment
	 * mapping
	 */
	@Override
	public double[][] getCellProbs() {
		return cellProbs;
	}

	/**
	 * Extract all locations from the containment mapping
	 * that lie within the given bounding box.
	 */
	public List<Node> getRegion(BoundingBox boundingBox) {
		// let the provided bounding box be bigger than the containment mapping for now; this method only used for drone search -Colleen
		
		
		// Make sure bounding box falls within containment mapping
		double bbNorth = boundingBox.getNorthLatDeg();
		double bbSouth = boundingBox.getSouthLatDeg();
		if (bbNorth < bbSouth) {
			LOGGER.warn("Invalid bounding box. North latitude must be greater than South latitude");
			return null;
		}
//		if ((bbNorth - bbSouth) > (bbox.getNorthLatDeg() - bbox.getSouthLatDeg())) {
			//LOGGER.warn("Bounding box cannot be longer than containment mapping."); 
			//return null;
//		}
		double bbEast = boundingBox.getEastLonDeg();
		double bbWest = boundingBox.getWestLonDeg();
		if (bbEast < bbWest) {
			LOGGER.warn("Invalid bounding box. East longitude must be greater than West longitude.");
			return null;
		}
		
//		if ((bbEast - bbWest) > (bbox.getEastLonDeg() - bbox.getWestLonDeg())) {
//			LOGGER.warn("Bounding box cannot be wider than containment mapping.");
//			return null;
//		}
		// Make sure containment mapping fields are valid.
		if (pts == null || ptProbs == null) {
			LOGGER.warn("pts and ptProbs arrays are not set.");
			return null;
		}
		if (pts.size() != ptProbs.size()) {
			LOGGER.warn("pts and ptProbs should have the same size.");
			return null;
		}
		// Extract region defined by bounding box from containment mapping.
		List<Node> resultSet = new ArrayList<>();
		for (int i = 0; i < pts.size(); ++i) {
			LatLonGeo location = pts.get(i);
			if (!boundingBox.contains(location)) {
				continue;
			}
			Node node = new Node(pts.get(i), ptProbs.get(i));
			resultSet.add(node);
		}
		return resultSet;
	}

	/**
	 * Returns a mapping from thresholds T to a list of points
	 * that have a probability of detection higher than T% of the other points.
	 * Currently, there are three thresholds: >= 50%, >= 90%, and >= 99%.
	 * @param containmentMappingPoints the input points
	 * @return the mapping
	 */
	public Map<Integer, List<Node>> filterByProbability(List<Node> containmentMappingPoints) {
		Map<Integer, List<Node>> resultSet = new HashMap<>();
		resultSet.put(LOW_THRESHOLD, new ArrayList<>());
		resultSet.put(MIDDLE_THRESHOLD, new ArrayList<>());
		resultSet.put(HIGH_THRESHOLD, new ArrayList<>());
		for (Node n : containmentMappingPoints) {
			if (n.getProbability() >= fiftyPercentCutoff) {
				resultSet.get(LOW_THRESHOLD).add(n);
			}
			if (n.getProbability() >= ninetyPercentCutoff) {
				resultSet.get(MIDDLE_THRESHOLD).add(n);
			}
			if (n.getProbability() >= ninetyNinePercentCutoff) {
				resultSet.get(HIGH_THRESHOLD).add(n);
			}
		}
		return resultSet;
	}
	
	static List<Double> getUniformPrior(List<LatLonGeo> o) {
		List<Double> unifPrior = new ArrayList<Double>();
		for (int i = 0; i < o.size(); i++) unifPrior.add(i, 1.0 / o.size());
		return unifPrior;
	}


	@Override
	public int getNumLat() {
		return this.numLat;
	}


	@Override
	public int getNumLon() {
		return this.numLon;
	}

	@Override
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



}
