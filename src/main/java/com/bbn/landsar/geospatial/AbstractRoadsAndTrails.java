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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.util.geo.LatLonGeo;

public abstract class AbstractRoadsAndTrails implements Serializable {
	
	private static final String T = "\t";

	public static final int ROAD = 1;
	public static final int TRAIL = 2;
	public static final int ROAD_OR_TRAIL = 3;
	public static final int NOT_ROAD_OR_TRAIL = 0;
	
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
	
	private transient BufferedImage roadsAndTrailsImage;

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
		return getRoadsAndTrailsForLatLon(pos) != NOT_ROAD_OR_TRAIL;
	}

	public abstract int getRoadsAndTrailsForLatLon(double latDeg, double lonDeg);
	
	public abstract void writeFiles(File outputDir);
	
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
							roadsAndTrails[i0][j0] = ROAD_OR_TRAIL;
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
	
	public BufferedImage getRoadsAndTrailsImage() {
        return roadsAndTrailsImage;
    }

    public void setRoadsAndTrailsImage(BufferedImage roadsAndTrailsImage) {
        this.roadsAndTrailsImage = roadsAndTrailsImage;
    }

    public static class RoadsAndTrailsMetaData {
		String name;
		
		List<RoadsAndTrailsMetaDataItem> metaData;

		public RoadsAndTrailsMetaData(String name) {
			super();
			this.name = name;
			
			metaData = new ArrayList<RoadsAndTrailsMetaDataItem>();
		}
		
		public void addMetaDataItem(RoadsAndTrailsMetaDataItem item) {
			metaData.add(item);
		}
		
		public RoadsAndTrailsMetaDataItem getItem(int rtCode) {
			for (RoadsAndTrailsMetaDataItem item : metaData) {
				if (item.rtCode == rtCode) return item;
			}
			return null;
		}
		
		public List<Integer> getRtCodes() {
			List<Integer> rtCodes = new ArrayList<Integer>();
			
			for (RoadsAndTrailsMetaDataItem item : metaData) {
				rtCodes.add(item.rtCode);
			}
			return rtCodes;
		}
		
		public static RoadsAndTrailsMetaData getDefaultMetaData() {
			RoadsAndTrailsMetaData metaData = 
					new RoadsAndTrailsMetaData("Default");
			
			RoadsAndTrailsMetaDataItem item = 
					new RoadsAndTrailsMetaDataItem(NOT_ROAD_OR_TRAIL, 
//							new Color(0), "Not road or trail");
        			new Color(255,255,255), "Not road or trail");
			metaData.addMetaDataItem(item);
			item = new RoadsAndTrailsMetaDataItem(ROAD_OR_TRAIL, 
      			new Color(168,168,168), "Road or trail");
			metaData.addMetaDataItem(item);
			
			return metaData;
		}
	}
	
	public static class RoadsAndTrailsMetaDataItem {
			
		public static final int ROAD = 1;
		public static final int TRAIL = 2;
		public static final int ROAD_OR_TRAIL = 3;
		public static final int NOT_ROAD_OR_TRAIL = 0;
		
		int rtCode;
		Color color;
		String shortDescription;
		
		public RoadsAndTrailsMetaDataItem(int rtCode, Color color, String shortDescription) {
			super();
			this.rtCode = rtCode;
			this.color = color;
			this.shortDescription = shortDescription;
		}
		
		public RoadsAndTrailsMetaDataItem(String line) {
			String[] s = line.split(T);
			rtCode = Integer.parseInt(s[0]);
			int r = Integer.parseInt(s[1]);
			int g = Integer.parseInt(s[2]);
			int b = Integer.parseInt(s[3]);
			color = new Color(r,g,b);
			shortDescription = s[4];
		}
		
		
		public int getRtCode() {
			return rtCode;
		}

		public Color getColor() {
			return color;
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
					color.getRed() + T +
					color.getGreen() + T +
					color.getBlue() + T +
					shortDescription;
		}
	}
}
