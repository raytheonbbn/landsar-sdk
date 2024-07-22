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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractLandCoverData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractLandCoverData.class);
	
	public static final String latLonFileName = "LandcoverLatLon.txt";
	public static final String landcoverDataFileName = "landcover.txt";
	public static final String landcoverMetadataFileName = "landcoverMetadata.txt";
	static final String T = "\t";
	
	
	// These values define a lat/lon rectangle
	protected double minLat;
	protected double maxLat;
	protected double minLon;
	protected double maxLon;
	
	// These values define subdivision of the rectangle
	protected int numLat;
	protected int numLon;
	
	// These values are used to prevent round off errors for 
	// subscripts corresponding to the edges of the rectangle
	private double minLat1;
	private double maxLat1;
	private double minLon1;
	private double maxLon1;
	
	
	protected AbstractLandCoverData() {
		super();
	}
	
	/**
	 * Lat/Lon in Degrees
	 * @param minLat
	 * @param maxLat
	 * @param minLon
	 * @param maxLon
	 * @param numLat
	 * @param numLon
	 */
	protected AbstractLandCoverData(double minLat,
			double maxLat, double minLon, double maxLon, int numLat, int numLon) {
		super();
		
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.numLat = numLat;
		this.numLon = numLon;
		
		initializeArrays();
	}
	
	/**
	 * 
	 * @param lat - latitude in degrees
	 * @param lon - longitude in degrees
	 * @return true if this landcover covers this geopoint, false if this geopoint is outside the boundary of this landcover data
	 */
	public boolean contains(double lat, double lon) {
		boolean latInBounds = lat >= minLat && lat <= maxLat;
		boolean lonInBounds = lon >= minLon && lon <= maxLon;
		return latInBounds && lonInBounds;
	}
	
	@JsonIgnore
	public String getBoundsAsString() {
		return "Lat: " + minLat + ", " + maxLat + "; Lon: " + minLon + ", " + maxLon + ".";
	}
	
	protected void initializeArrays() {
		
		double latEdge = (maxLat - minLat) / (100 * numLat);
		double lonEdge = (maxLon - minLon) / (100 * numLon);
		minLat1 = minLat + latEdge;
		maxLat1 = maxLat - latEdge;
		minLon1 = minLon + lonEdge;
		maxLon1 = maxLon - lonEdge;
		
//		this.cost = new int[numLat * numLon];
//		this.soaFactor = new double[numLat * numLon];
//		this.terrainResourceParameter = new double[numLat * numLon];

//		for (int i = 0; i < cost.length; i++) {
//			cost[i] = Integer.MIN_VALUE;
//			soaFactor[i] = Double.NaN;
//			terrainResourceParameter[i] = Double.NaN;
//		}
	}
	
	/**
	 * @see LandCoverMetaData
	 * @param metadata
	 */
	public abstract void setMetaData(LandCoverMetaData metadata);
	
	public abstract LandCoverMetaData getMetaData();
	
	public abstract int getLandcoverCodeForLatLon(double latDeg, double lonDeg);
	
	public abstract boolean isWater(double latDeg, double lonDeg);
	
	public abstract boolean isWater(int code);
	
	public abstract boolean isDeveloped(double latDeg, double lonDeg);
	
	public abstract boolean isDeveloped(int code);
	
	public abstract void writeFiles(File dir);
    
//    public List<Integer> getLegendCodes() {
    public List<Short> getLegendCodes() {
    	return getMetaData().getCodes();
    }
    
    /**
     * 
     * @return actual codes in this instance of the data
     */
    @JsonIgnore
    abstract public Set<Short> getDataCodes();
	
	
	public int getCost(double latDeg, double lonDeg){
		int sub = getSubscriptForLatLon(latDeg, lonDeg);
		
//		int c = cost[sub];
//		if (c != Integer.MIN_VALUE) return c;
		
		double centerLat = centerValue(latDeg, minLat, maxLat, numLat);
		double centerLon = centerValue(lonDeg, minLon, maxLon, numLon);
		int c = getLandCoverCost(centerLat, centerLon);
//		cost[sub] = c;
		return c;
	}

	
	
	public double getSoaFactor(double latDeg, double lonDeg) {
		double soa = getLandCoverSoa(latDeg, lonDeg);
		return soa;		
	}
	
	public double getTerrainResourceParameter(double latDeg, double lonDeg) {
//		int sub = getSubscriptForLatLon(latDeg, lonDeg);
//
//		double trp = terrainResourceParameter[sub];
//		if (!Double.isNaN(trp)) return trp;
		
		double trp = getLandCoverTerrainResourceParameter(latDeg, lonDeg);
//		terrainResourceParameter[sub] = trp;
		return trp;
	}
	
	private double getLandCoverTerrainResourceParameter(double latDeg, double lonDeg) {
		int index = getLandcoverCodeForLatLon(latDeg, lonDeg);
		return getLandCoverTerrainResourceParameter(index);
	}
	
	private double getLandCoverSoa(double latDeg, double lonDeg) {
		int index = getLandcoverCodeForLatLon(latDeg, lonDeg);
		return getLandCoverSoaFactor(index);
	}
	
	private int getLandCoverCost(double latDeg, double lonDeg) {
		int index = getLandcoverCodeForLatLon(latDeg, lonDeg);
		return getLandCoverCostFactor(index);
	}
	

	protected double centerValue(double value, double min, double max, int num) {
		double size = (max - min) / num;
		return min + (subscriptFor(value, min, max, num) + 0.5) * size;
	}
	
	protected int subscriptFor(double value, double min, double max, int num) {
		double size = (max - min) / num;
        return (int)((value - min) / size);
	}
	
	/*
	 * The important thing here is which locations are mapped to the 
	 * same subscripts.  In particular, there is no special reason for
	 * the array to be doubly subscripted (or for that matter to be an
	 * array rather than some other data structure).
	 * 
	 * Actually, one might want to map latDeg, lonDeg to the center of
	 * a cell to assure that the calling order does not change the stored
	 * results.
	 */
	private int getSubscriptForLatLon(double latDeg, double lonDeg) {
		double lat = Math.max(minLat1, Math.min(maxLat1, latDeg));
		double lon = Math.max(minLon1, Math.min(maxLon1, lonDeg));
		
		int latIndx = subscriptFor(lat, minLat, maxLat, numLat);
		int lonIndx = subscriptFor(lon, minLon, maxLon, numLon);
		
		return latIndx * numLon + lonIndx;
	}

	/*
	 * Should the abstract method store values in a lat/lon grid for 
	 * reuse?  For each lat/lon call one could load the cost and soa 
	 * factor into a grid.  (Indeed there is no actual need for the
	 * land cover index grid except in so far as it supports these calls.)
	 * On subsequent calls the values would simply be obtained from
	 * the grid if they were already there.
	 * Note that this assumes that there will be repeated calls for
	 * the same (or close) locations.  That seems a reasonable 
	 * assumption given the types of models being used.  The amount of
	 * storage seems unlikely to be an issue.  
	 */
	
	private int getLandCoverCostFactor(int landcoverCode) {
		return getMetaData().getMetaDataItem(landcoverCode).getCost();
	}

	private double getLandCoverSoaFactor(int landcoverCode) {
		return getMetaData().getMetaDataItem(landcoverCode).getSoaFactor();
	}
	
	private double getLandCoverTerrainResourceParameter(int landcoverCode) {
		return getMetaData().getMetaDataItem(landcoverCode).getTerrainResourceParameter();
	}
	
	
	private int[] getLandCoverRgbColor(int landcoverCode) {
		return getMetaData().getMetaDataItem(landcoverCode).getRgbColor();
	}



	public void writeMetadataToFile(File outputFile) throws IOException {
		this.getMetaData().writeToFile(outputFile);
	}
	

	
	public static class LandcoverException extends Exception {
        private String error;
        

        
		public LandcoverException(Exception e, String url) {
			super("URL for land cover data:\n    " + url, e);
			this.error = e.getMessage();
			
		}

		public LandcoverException(String error, String url) {
            super("URL for land cover data:\n    " + url + "\nError: " + error);
            this.error = error;
        }
		

        private static final long serialVersionUID = 1L;
        
        public String getErrorForUser() {
            return error;
        }
		
	}

    public abstract String getLandCoverType();
    
    public static final String NLCDB = "National Land Cover Data Base";
    public static final String VISNAV = "VISNAV";
    public static final String TRIVIAL = "Trivial";
    public static final String WORLDCOVER = "Worldcover"; // https://esa-worldcover.org/en
	public static String[] getLandCoverFormats() {
		String[] options = new String[4];
		options[0] = NLCDB;
		options[1] = VISNAV;
		options[2] = TRIVIAL;
		options[3] = WORLDCOVER;
		return options;
	}
}
