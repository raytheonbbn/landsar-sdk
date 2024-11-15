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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



public interface AreaData extends Serializable {
	public static final String ELEVATION_LAT_LON_FILENAME = "ElevationLatLon.txt";
    public static final String ELEVATION_GRID_FILENAME = "elevation.txt";
    
	public static final String ELEVATION_UNKNOWN_VAL_FILENAME = "unknownValues.txt";
    
	public static final double metersPerLatDeg = 1.0e7 / 90;
	
	public static final double MINIMUM_EXTENT_KM = 20;

    public void writeToFiles(File dir);

	public AbstractElevationData getElevationData();
	
	public AbstractLandCoverData getLandcoverData();

	/**
	 * No current implementations
	 * @return
	 */
	public WindData getWindData();
	
	public AbstractTimeBasedVectorData getCurrentData();
	
	public BoundingBox getBoundingBox();

	public String getAreaName();
	
    public UUID getId();
	
	public static BoundingBox readBoundsFile(File latLonFile) throws FileNotFoundException, IOException {
		// Read the lat lon file and create the sector
		try (BufferedReader in = new BufferedReader(new FileReader(latLonFile))){
		String latLonLine = in.readLine();
		return parseBoundsString(latLonLine);
		}
	}

	public static BoundingBox parseBoundsString(String latLonLine) {
		String[] latLons = latLonLine.split(",");

		return new BoundingBox(Double.parseDouble(latLons[1]), 
				Double.parseDouble(latLons[0]), 
				Double.parseDouble(latLons[3]), 
				Double.parseDouble(latLons[2]));
	}
	
	public static BoundingBox readBoundsFileInDirectory(File directory) throws FileNotFoundException, IOException {
		// Read the lat lon file and create the sector
		File latLonFile = new File(directory, AreaData.ELEVATION_LAT_LON_FILENAME);
		return readBoundsFile(latLonFile);
	}
	
	public static void writeBoundsFile(File outputDir, BoundingBox bbox) throws IOException {
		// keeping this name so that old instances of AreaData (which always had elevation data) are compatible with new ones that may not actually include elevation data
		File latLonFile = new File(outputDir, AreaData.ELEVATION_LAT_LON_FILENAME);
		try (PrintWriter out = new PrintWriter(new FileWriter(latLonFile))){
			out.print(getBoundsString(bbox));
			out.flush();
		}
	}
	
	public static String getBoundsString(BoundingBox bbox) {
		return bbox.getSouthLatDeg() + "," + bbox.getNorthLatDeg() + "," + bbox.getWestLonDeg() + "," + bbox.getEastLonDeg();
	}

	AdditionalData getAdditionalData(String dataType);
	Map<String, AdditionalData> getAdditionalData();
}
