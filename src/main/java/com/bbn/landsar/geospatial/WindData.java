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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.metsci.glimpse.util.geo.LatLonGeo;

public class WindData implements Serializable{
	
	double[] altitude;
	double[] latitude;
	double[] longitude;
	double[][][] eastWind;
	double[][][] northWind;
	
    double minAltitude;
    double maxAltitude;
    double minLat;
    double maxLat;
    double minLon;
    double maxLon;
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String fileName = 
				"C:\\Users\\anderson\\Desktop\\JPRA\\EclipseWorkspace - Delivery1309\\JPRA\\BridgeportCA";
		fileName = fileName + "\\NOAAWindData.txt";
		
		WindData windData = new WindData(new File(fileName));

		LatLonGeo location = new LatLonGeo(38.51, -119);
		double aboveSeaLevelMeters = 120;
		double[] w = windData.getWind(location, aboveSeaLevelMeters);
		System.out.println(w[0] + "\t" + w[1]);
		
		location = new LatLonGeo(38.5,-119.0);
		aboveSeaLevelMeters = 300;
		w = windData.getWind(location, aboveSeaLevelMeters);
		System.out.println(w[0] + "\t" + w[1]);
		
	}
	
	public WindData(File windDataFile) {
		
		Set<Double> altitudeSet = new TreeSet<Double>();
		Set<Double> latSet = new TreeSet<Double>();
		Set<Double> lonSet = new TreeSet<Double>();
		
		HashMap<LatLonAltitude, Velocity2d> winds = new HashMap<LatLonAltitude, Velocity2d>();
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(windDataFile));
			
			// First line is a header, second is blank
			in.readLine();
			in.readLine();
			
			String line = in.readLine();
			while (line != null) {
				
				/*
				 * p[0] = latitude (deg)
				 * p[1] = longitude (deg)
				 * p[2] = altitude (meters above sea level)
				 * p[3] = date/time 
				 * p[4] = wind east component (meters / sec)
				 * p[5] = wind north component (meters / sec)
				 * p[6] = wind east error (meters / sec)
				 * p[7] = wind north error (meters / sec)
				 */
				String[] p = line.split("\t");
				
				double altitude = Double.parseDouble(p[2]);
				
				if (altitude > 11.0) {
					double latitude = Double.parseDouble(p[0]);
					double longitude = Double.parseDouble(p[1]);
					double windEast = Double.parseDouble(p[4]);
					double windNorth = Double.parseDouble(p[5]);
					
					latSet.add(latitude);
					lonSet.add(longitude);
					altitudeSet.add(altitude);
					
					winds.put(new LatLonAltitude(latitude, longitude, altitude), 
							new Velocity2d(windEast, windNorth));
				}
				
				line = in.readLine();
			}
			in.close();
		} catch (IOException e) {
		    //TODO log this
			e.printStackTrace();
		}
		
		altitude = new double[altitudeSet.size()];
		int i = 0;
		for (double a : altitudeSet) {
			altitude[i] = a;
			i++;
		}
	    Arrays.sort(altitude);
	    minAltitude = altitude[0];
	    maxAltitude = altitude[altitude.length - 1];
	    
		latitude = new double[latSet.size()];
		i = 0;
		for (double a : latSet) {
			latitude[i] = a;
			i++;
		}
		Arrays.sort(latitude);
		minLat = latitude[0];
		maxLat = latitude[latitude.length - 1];
	
		longitude = new double[lonSet.size()];
		i = 0;
		for (double a : lonSet) {
			longitude[i] = a;
			i++;
		}
		Arrays.sort(longitude);
		minLon = longitude[0];
		maxLon = longitude[longitude.length - 1];
		
		eastWind = new double[altitude.length][latitude.length][longitude.length];
		northWind = new double[altitude.length][latitude.length][longitude.length];
		
		for (int aIndx = 0; aIndx < altitude.length; aIndx++) {
			for (int latIndx = 0; latIndx < latitude.length; latIndx++) {
				for (int lonIndx = 0; lonIndx < longitude.length; lonIndx++) {
					LatLonAltitude key = 
							new LatLonAltitude(latitude[latIndx], longitude[lonIndx], altitude[aIndx]);
					Velocity2d vel = winds.get(key);
					eastWind[aIndx][latIndx][lonIndx] = vel.east;
					northWind[aIndx][latIndx][lonIndx] = vel.north;
				}
			}
		}
	}
	
	public long getEarliestTime() {
		return -1L;
	}
	
	public long getLatestTime() {
		return -1L;
	}

	public double getResolutionHours() {
		return Double.NaN;
	}
	
	/**
	 * may differ slightly than Area Data
	 * @return
	 */
	public BoundingBox getBounds() {
		return new BoundingBox(maxLat, minLat, maxLon, minLon);
	}
	
	/*
	 * Simple linear interpolation assuming a uniform grid
	 */
	public Velocity2d getWind(LatLonGeo location, long time) {
		
		double aboveSeaLevelMeters = 0;
		
		double latIn = location.getLatDeg();
		double lonIn = location.getLonDeg();
		double altIn = aboveSeaLevelMeters;
		
		int latIndx0 = getLowerIndex(latIn, latitude, minLat, maxLat);
		int lonIndx0 = getLowerIndex(lonIn, longitude, minLon, maxLon);
		int altIndx0 = getLowerIndex(altIn, altitude, minAltitude, maxAltitude);
		
		int latIndx1 = Math.min(latIndx0 + 1, latitude.length - 1);
		int lonIndx1 = Math.min(lonIndx0 + 1, longitude.length - 1);
		int altIndx1 = Math.min(altIndx0 + 1, altitude.length - 1);
		
		double e = getInterpolatedValue(latIn, lonIn, altIn, eastWind, 
				latIndx0, latIndx1, lonIndx0, lonIndx1, altIndx0, altIndx1);
		double n = getInterpolatedValue(latIn, lonIn, altIn, northWind, 
				latIndx0, latIndx1, lonIndx0, lonIndx1, altIndx0, altIndx1);
		
		return new Velocity2d(e,n);
	}
	
	
	/*
	 * Simple linear interpolation assuming a uniform grid
	 */
	public double[] getWind(LatLonGeo location, double aboveSeaLevelMeters) {
		
		double latIn = location.getLatDeg();
		double lonIn = location.getLonDeg();
		double altIn = aboveSeaLevelMeters;
		
		int latIndx0 = getLowerIndex(latIn, latitude, minLat, maxLat);
		int lonIndx0 = getLowerIndex(lonIn, longitude, minLon, maxLon);
		int altIndx0 = getLowerIndex(altIn, altitude, minAltitude, maxAltitude);
		
		int latIndx1 = Math.min(latIndx0 + 1, latitude.length - 1);
		int lonIndx1 = Math.min(lonIndx0 + 1, longitude.length - 1);
		int altIndx1 = Math.min(altIndx0 + 1, altitude.length - 1);
		
		double e = getInterpolatedValue(latIn, lonIn, altIn, eastWind, 
				latIndx0, latIndx1, lonIndx0, lonIndx1, altIndx0, altIndx1);
		double n = getInterpolatedValue(latIn, lonIn, altIn, northWind, 
				latIndx0, latIndx1, lonIndx0, lonIndx1, altIndx0, altIndx1);
		
		return new double[] {e,n};
	}

	private double getInterpolatedValue(double lat, double lon, double alt, 
			double[][][] w, int latIndx0, int latIndx1, 
			int lonIndx0, int lonIndx1, int altIndx0, int altIndx1) {
		
		double latAlpha = getAlpha(lat, latitude, latIndx0, latIndx1);
		
		double lonAlpha = getAlpha(lon, longitude, lonIndx0, lonIndx1);
		
		double altAlpha = getAlpha(alt, altitude, altIndx0, altIndx1);
		
		double v = 0;
				
		double alpha0 = latAlpha;
		int i0 = latIndx0;
		for (int k0 = 0; k0 < 2; k0++) {
			
			double alpha1 = lonAlpha;
			int i1 = lonIndx0;
			for (int k1 = 0; k1 < 2; k1++) {
				
				double alpha2 = altAlpha;
				int i2 = altIndx0;
				for (int k2 = 0; k2 < 2; k2++) {
					 v += (1 - alpha0) * (1 - alpha1) * (1 - alpha2) * w[i2][i0][i1];
//					 System.out.println(((1 - alpha0) * (1 - alpha1) * (1 - alpha2)));
					i2 = altIndx1;
					alpha2 = 1 - altAlpha;
				}
				alpha1 = 1 - lonAlpha;
				i1 = lonIndx1;
			}
			alpha0 = 1 - latAlpha;
			i0 = latIndx1;
		}
		return v;
	}
	
	private double getAlpha(double v, double[] a, int indx0, int indx1) {
		double alpha = 0.0; // If indicies are equal
		if (indx0 != indx1) {
			alpha = (v - a[indx0]) / (a[indx1] - a[indx0]);
		}
		return alpha;
	}
	
	private int getLowerIndex(double v, double[] a, double minA, double maxA) {
		if (v <= minA) return 0;
		if (v >= maxA) return a.length - 1;
		int vIndx = 0;
		while (a[vIndx] < v) vIndx++;
		return vIndx - 1;
	}
	
	private static class LatLonAltitude {
				
		double lat;
		double lon;
		double altitude;
		
		public LatLonAltitude(double lat, double lon, double altitude) {
			super();
			this.lat = lat;
			this.lon = lon;
			this.altitude = altitude;
		}
		
		public boolean equals(Object o) {
		    if (o == null || this.getClass()!= o.getClass()){
		        return false;
		    }
			LatLonAltitude a = (LatLonAltitude)o;
			return (this.lat == a.lat) && (this.lon == a.lon) && (this.altitude == a.altitude);
		}
		
		public int hashCode() {
			return (int)(100 * lat) + (int)(100 * lon) + (int)(100 * altitude);
		}	
	}
}
