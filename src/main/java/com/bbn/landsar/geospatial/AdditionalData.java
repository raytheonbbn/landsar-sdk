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

import com.bbn.landsar.motionmodel.AreaDataType;

import java.io.File;
import java.util.Collection;

/**
 * Represents additional data associated with a Lost Person Instance. 
 * Currently assumed to be related to the bounding box and dataDownloaders, but could be extended.
 * 
 */
public interface AdditionalData {
	/**
	 * Returns the area data type, see {@link com.bbn.landsar.motionmodel.AreaDataType} for
	 * core types known by LandSAR, but custom types can be returned here as a new String
	 * @return Area Data Type
	 */
	String getAreaDataType();
	
	/**
	 * re-loading Lost Person Instances using additional area data types is not supported yet
	 * @param outputDir - directory in which to create a copy of the data on disk
	 * @return - collection of files written to by this method. All should be children of the given outputDir
	 */
	Collection<File> writeFiles(File outputDir);

	Object getValueLatLon(double latDeg, double lonDeg);
}
