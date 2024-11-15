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
import java.io.Serializable;
import java.util.Collection;


import com.bbn.roger.plugin.StartablePlugin;
import com.metsci.glimpse.util.geo.LatLonGeo;

public abstract class AbstractRoadsAndTrails implements Serializable, DataDownloader, StartablePlugin  {
	
	private static final String T = "\t";

	public static final String ROADS_TRAILS_DATA_DIR_PARAM = "roadsTrailsDataDir";
	public static final String ROADS_TRAILS_LAT_LON_FILE_PARAM = "roadsTrailsLatLon";
	public static final String ROADS_TRAILS_DATA_FILE_PARAM = "roadsTrailsData";
	public static final String ROADS_TRAILS_METADATA_FILE_PARAM = "roadsAndTrailsMeta";

	public static final String ROADS_TRAILS_DEFAULT_METADATA_FILE_NAME = "RoadsAndTrailsMetadata.txt";
	public static final String ROADS_TRAILS_DEFAULT_LAT_LON_FILE_NAME = "RoadsAndTrailsLatLon.txt";
	public static final String ROADS_TRAILS_DEFAULT_DATA_FILE_NAME = "RoadsAndTrails.txt";
	public static final String ROADS_TRAILS_DEFAULT_IMAGE_FILE_NAME = "RoadsAndTrails.png";
	
	protected double minLat;
	protected double maxLat;
	protected double minLon;
	protected double maxLon;
	
	// These values are used to prevent round off errors for 
	// subscripts corresponding to the edges of the rectangle
	private double minLat1;
	private double maxLat1;
	private double minLon1;
	private double maxLon1;
	
	protected int numLat;
	protected int numLon;
	
	protected int[][] roadsAndTrails;
	
	protected RoadsAndTrailsMetaData metaData;

	public double getMinLat() {
		return minLat;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public double getMinLon() {
		return minLon;
	}

	public double getMaxLon() {
		return maxLon;
	}

	public int getNumLat() {
		return numLat;
	}

	public int getNumLon() {
		return numLon;
	}

	public int getRoadsAndTrailsForLatLon(LatLonGeo pos) {
		return getRoadsAndTrailsForLatLon(pos.getLatDeg(), pos.getLonDeg());
	}
	
	public boolean isRoadOrTrail(LatLonGeo pos) {
		return getRoadsAndTrailsForLatLon(pos) != RoadsAndTrailsMetaData.NOT_ROAD_OR_TRAIL;
	}

	public abstract int getRoadsAndTrailsForLatLon(double latDeg, double lonDeg);
	
	public abstract Collection<File> writeFiles(File outputDir);
	
	public void spreadRoadsAndTrails(int factor) {
		for (int i = 0; i < roadsAndTrails.length; i++ ) {
			int iMinus = Math.max(0,  i - factor);
			int iPlus = Math.min(i + factor, roadsAndTrails.length - 1);
			for (int j = 0; j < roadsAndTrails[i].length; j++) {
				if (roadsAndTrails[i][j] > 0) {
					int jMinus = Math.max(0, j -factor);
					int jPlus = 
							Math.min(j + factor, 
									roadsAndTrails[i].length - 1);
					for (int i0 = iMinus; i0 <= iPlus; i0++) {
						for (int j0 = jMinus; j0 <= jPlus; j0++) {
							roadsAndTrails[i0][j0] = RoadsAndTrailsMetaData.ROAD_OR_TRAIL;
						}
					}
				}
			}
		}
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
	
	public static class RoadsAndTrailsMetaDataItem {
			
		public static final int ROAD = 1;
		public static final int TRAIL = 2;
		public static final int ROAD_OR_TRAIL = 3;
		public static final int NOT_ROAD_OR_TRAIL = 0;
		
		int rtCode;
		int r, g, b;
		String shortDescription;

		public RoadsAndTrailsMetaDataItem(int rtCode, int r, int g, int b, String shortDescription) {
			super();
			this.rtCode = rtCode;
			this.r = r;
			this.g = g;
			this.b = b;
			this.shortDescription = shortDescription;
		}
		
		public RoadsAndTrailsMetaDataItem(String line) {
			String[] s = line.split(T);
			rtCode = Integer.parseInt(s[0]);
			r = Integer.parseInt(s[1]);
			g = Integer.parseInt(s[2]);
			b = Integer.parseInt(s[3]);
			shortDescription = s[4];
		}
		
		
		public int getRtCode() {
			return rtCode;
		}

		public int[] getColor() {
			return new int[]{r, g, b};
		}

		public String getShortDescription() {
			return shortDescription;
		}

		public boolean isRoad() {
			return rtCode == ROAD;
		}
		
		public boolean isTrail() {
			return rtCode == TRAIL;
		}
		
		public boolean isRoadOrTrail() {
			return rtCode == ROAD_OR_TRAIL;
		}
		
		public String toString() {
			return rtCode + T + 
					r + T +
					g + T +
					b + T +
					shortDescription;
		}
	}
}
