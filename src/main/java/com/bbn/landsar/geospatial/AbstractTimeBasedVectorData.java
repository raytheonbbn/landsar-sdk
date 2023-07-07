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
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.motionmodel.AreaDataType;
import com.bbn.landsar.motionmodel.Unit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metsci.glimpse.util.geo.LatLonGeo;

/**
 * Don't want an interface because that makes serialization harder
 * 
 * timeBasedData, minTime, maxTime, latitudeValues, longitudeValues, maxLon, minLon, maxLat, minLat must all be set for interpolation methods to work
 * 
 *TODO make sure the calculated bounding box is consistent with the area data's bounding box
 * @author crock
 *
 */
public abstract class AbstractTimeBasedVectorData {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTimeBasedVectorData.class);
	
	protected double[] latitudeValues;
	protected double[] longitudeValues;
	
	protected double maxLon, minLon, maxLat, minLat;
	
	protected Long minTime;
	protected Long maxTime;
	
	/**
	 * Unix epoch time to --> array[latIndex][lonIndex]
	 */
	protected NavigableMap<Long, Velocity2d[][]> timeBasedData = new TreeMap<>();

	protected GeospatialMetadata geospatialMetadata;
	
	/**
	 * Interpolation methods may not work when using this constructor, unless the required values are set using setters.
	 */
	AbstractTimeBasedVectorData(){
		
	}
	
	public AbstractTimeBasedVectorData(double[] latitudeValues, double[] longitudeValues, NavigableMap<Long, Velocity2d[][]> timeBasedData, 
			GeospatialMetadata metadata){
		this.setLatitudeValues(latitudeValues);
		this.setLongitudeValues(longitudeValues);
		this.setTimeBasedData(timeBasedData);
		this.geospatialMetadata = metadata;
	}
	

	@JsonIgnore
	public long getEarliestTime() {
		return minTime;
	}

	/** 
	 * This method is for JSON Serialization. 
	 * 
	 * @return
	 */
	public NavigableMap<Long, Velocity2d[][]> getTimeBasedData() {
		return timeBasedData;
	}


	public void setTimeBasedData(NavigableMap<Long, Velocity2d[][]> timeBasedData) {
		this.timeBasedData = timeBasedData;
		minTime = timeBasedData.firstKey();
		maxTime = timeBasedData.lastKey();
	}


	@JsonIgnore
	public long getLatestTime() {
		return maxTime;
	}
	
	@JsonIgnore
	public BoundingBox getBoundingBox() {
		return new BoundingBox(maxLat, minLat, maxLon, minLon);
	}
	/*
	 * see {@link #getDataUnit()} for data units
	 * 
	 * @return array[latIndex][lonIndex]
	 */
	abstract public Velocity2d[][] getData(long time);
	
	/*
	 * see {@link #getDataUnit()} for data units
	 */
	public Velocity2d getData(LatLonGeo location, long time) {
		return interpolate(location, getData(time));
	}
	
	
	/**
	 * Required that timeBasedData, minTime, maxTime are set and non-null
	 * @param time
	 * @return
	 */
	protected Velocity2d[][] interpolateTime(long time){
		if (timeBasedData == null || minTime == null || maxTime == null) {
			LOGGER.error("No time-based data, or missing minTime or maxTime");
			return null;
		}
		if (time <= minTime) {
			return timeBasedData.firstEntry().getValue();
		} else if (time >= maxTime) {
			return timeBasedData.lastEntry().getValue();
		} else {
			Entry<Long, Velocity2d[][]> higherEntry = timeBasedData.ceilingEntry(time);
			Entry<Long, Velocity2d[][]> lowerEntry = timeBasedData.floorEntry(time);
			long earlierTime = lowerEntry.getKey();
			long laterTime = higherEntry.getKey();
			
			if (earlierTime == laterTime) {
				return higherEntry.getValue();
			}
			// Math Justification:
			// consider a < b < c (earlierTime < time < laterTime)
			// want weightA + weightC = 1 
			// want weightC to be inversely proportional to distance between b and c (since it's linear, proportional to distance between a and c is good), and weightC to equal 1 if b=c
			// consider example where indicies we have are 2 and 5 and someone requests values for time=4. (a=2, b=4, c=5). 
			// 			weightC=(b-a)/(c-a) = 2/3 makes sense; weightA = (c-b)/(c-a) = 1/3, makes sense
			
					
			// calculate weightLater (weightC)
			double weightLater = ((double) time - earlierTime)/((double) laterTime - earlierTime);
			// calculate weightEarlier (weightA)
			double weightEarlier = ((double) laterTime - time)/((double) laterTime - earlierTime);
			
			Velocity2d[][] laterValues = higherEntry.getValue();
			Velocity2d[][] earlierValues = lowerEntry.getValue();
			
			Velocity2d[][] result = new Velocity2d[laterValues.length][laterValues[0].length];
			
			for (int i = 0; i < laterValues.length; i++) {
				for (int j = 0; j < laterValues[0].length; j++) {
					result[i][j] = laterValues[i][j].scalarFactor(weightLater).plusEquals(earlierValues[i][j].scalarFactor(weightEarlier));
				}
			}
			return result;
			
			
			
		}
	}
		
	/**
	 * Units used for data returned by {@link #getData(LatLonGeo, long)}
	 * @return
	 */
	public Unit getDataUnit() {
		return geospatialMetadata.dataUnit;
	}
	
	/**
	 * 
	 * @return String representing Data Type. ex. {@link AreaDataType.WINDS}, {@link AreaDataType.CURRENTS}
	 */
	abstract public String getDataType();
	
	// write enough info to file to enable restoring data from disk
    abstract public void writeFiles(File directory);
	
	public double[] getLatitudeValues() {
		return Arrays.copyOf(latitudeValues, latitudeValues.length);
	}

	public void setLatitudeValues(double[] latitudeValues) {
		this.latitudeValues = latitudeValues;
		this.minLat = latitudeValues[0];
		this.maxLat = latitudeValues[latitudeValues.length-1];
	}

	public double[] getLongitudeValues() {
		return Arrays.copyOf(longitudeValues, longitudeValues.length);
	}

	public void setLongitudeValues(double[] longitudeValues) {
		this.longitudeValues = longitudeValues;
		this.minLon = longitudeValues[0];
		this.maxLon = longitudeValues[longitudeValues.length-1];
	}
	
	public static int getLowerIndex(double v, double[] a, double minA, double maxA) {
		if (v <= minA) return 0;
		if (v >= maxA) return a.length - 1;
		int vIndx = 0;
		while (a[vIndx] < v) vIndx++;
		return vIndx - 1;
	}
	
	public static double getAlpha(double v, double[] a, int indx0, int indx1) {
		double alpha = 0.0; // If indicies are equal
		if (indx0 != indx1) {
			alpha = (v - a[indx0]) / (a[indx1] - a[indx0]);
		}
		return alpha;
	}
	
	
	
	/*
	 * Simple linear interpolation assuming a uniform grid
	 * @throws IllegalArgumentException if location.getLatDeg() or location.getLonDeg() return NaN
	 */
	public Velocity2d interpolate(LatLonGeo location, Velocity2d[][] data) throws IllegalArgumentException{
		
		double latIn = location.getLatDeg();
		double lonIn = location.getLonDeg();
		
		if (Double.isNaN(latIn) || Double.isNaN(lonIn)) {
			throw new IllegalArgumentException("provided latitude, " + latIn + ", or longitude, " + lonIn
					+ ", is not a number!");
		}
		
		int latIndx0 = getLowerIndex(latIn, latitudeValues, minLat, maxLat);
		int lonIndx0 = getLowerIndex(lonIn, longitudeValues, minLon, maxLon);
		
		int latIndx1 = Math.min(latIndx0 + 1, latitudeValues.length - 1);
		int lonIndx1 = Math.min(lonIndx0 + 1, longitudeValues.length - 1);
		
		return getInterpolatedValue(data, latIn, lonIn, latIndx0, latIndx1, lonIndx0, lonIndx1);
	}
	
	
	Velocity2d getInterpolatedValue(Velocity2d[][] data, double lat, double lon, int latIndx0, int latIndx1, 
			int lonIndx0, int lonIndx1) {

		double latAlpha = getAlpha(lat, latitudeValues, latIndx0, latIndx1);
		
		double lonAlpha = getAlpha(lon, longitudeValues, lonIndx0, lonIndx1);
		
		Velocity2d v = new Velocity2d(0, 0);
				
		double alpha0 = latAlpha;
		int i0 = latIndx0;
		for (int k0 = 0; k0 < 2; k0++) {
			
			double alpha1 = lonAlpha;
			int i1 = lonIndx0;
			for (int k1 = 0; k1 < 2; k1++) {
				v.plusEquals(data[i0][i1].scalarFactor((1 - alpha0) * (1 - alpha1)));
				alpha1 = 1 - lonAlpha;
				i1 = lonIndx1;
			}
			alpha0 = 1 - latAlpha;
			i0 = latIndx1;
		}
		return v;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(latitudeValues);
		result = prime * result + Arrays.hashCode(longitudeValues);
		result = prime * result
				+ Objects.hash(geospatialMetadata, maxLat, maxLon, maxTime, minLat, minLon, minTime);
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
		AbstractTimeBasedVectorData other = (AbstractTimeBasedVectorData) obj;
		return Objects.equals(geospatialMetadata, other.geospatialMetadata)
				&& Arrays.equals(latitudeValues, other.latitudeValues)
				&& Arrays.equals(longitudeValues, other.longitudeValues)
				&& Double.doubleToLongBits(maxLat) == Double.doubleToLongBits(other.maxLat)
				&& Double.doubleToLongBits(maxLon) == Double.doubleToLongBits(other.maxLon)
				&& Objects.equals(maxTime, other.maxTime)
				&& Double.doubleToLongBits(minLat) == Double.doubleToLongBits(other.minLat)
				&& Double.doubleToLongBits(minLon) == Double.doubleToLongBits(other.minLon)
				&& Objects.equals(minTime, other.minTime);
		// NavigableMap doesn't have a reliable equals method
	}


	
	

	
}

