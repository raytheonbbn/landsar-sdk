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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metsci.glimpse.util.geo.LatLonGeo;

public abstract class AbstractElevationData implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElevationData.class);

//	double[][] elevationData;
	protected float[][] elevationData;
	
	protected double minLat;
	protected double maxLat;
	protected double minLon;
	protected double maxLon;
	
	protected int numLat;
	protected int numLon;
	
	protected double minElevation;
	protected double maxElevation;
	
	protected double unknownValue = Double.NEGATIVE_INFINITY;

//	public double[][] getElevationData() {
	public float[][] getElevationData() {
		return elevationData;
	}

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

	public double getMinElevation() {
		return minElevation;
	}

	public double getMaxElevation() {
		return maxElevation;
	}

	public double getElevationMetersForLatLon(LatLonGeo pos) {
		return getElevationMetersForLatLon(pos.getLatDeg(), pos.getLonDeg());
	}

	public abstract double getElevationMetersForLatLon(double latDeg, double lonDeg);
	
	public abstract void writeFiles(File outputDir);
	
	public void writeLatLonFile(File outputDir) {
		File latLonFile = new File(outputDir, AreaData.ELEVATION_LAT_LON_FILENAME);
		try {
			PrintWriter out = new PrintWriter(new FileWriter(latLonFile));
			out.print(minLat + "," + maxLat + "," + minLon + "," + maxLon);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void determineMinMaxElevation() {
		minElevation = Double.POSITIVE_INFINITY;
		maxElevation = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < elevationData.length; i++) {
			for (int j = 0; j < elevationData[i].length; j++) {
				if (elevationData[i][j]!=unknownValue) {
					minElevation = Math.min(minElevation, elevationData[i][j]);
					maxElevation = Math.max(maxElevation, elevationData[i][j]);
				}
			}
		}
	}

	public double getPercentUnknown() {
		int count = 0;
		int countUnknown = 0;
		for (int i = 0; i < elevationData.length; i++) {
			for (int j = 0; j < elevationData[i].length; j++) {
				count++;
				if (elevationData[i][j]==unknownValue) {
					countUnknown++;
				}
			}
		}
		if (count == 0) {
			return 0.0;
		}
		return ((double) countUnknown) / count * 100.0;
	}
	
	/**
	 * 
	 * @return value used when the elevation is unknown (also called the missing data signal)
	 */
	public double getUnknownValue() {
		return unknownValue;
	}
/**
 * 
 * @param unknownValue value used when the elevation is unknown (also called the missing data signal)
 */
	public void setUnknownValue(double unknownValue) {
		this.unknownValue = unknownValue;
	}
}
