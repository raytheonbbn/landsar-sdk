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

package com.bbn.landsar.motionmodel.path;

import java.util.List;

import com.bbn.landsar.geospatial.AreaData;
import com.metsci.glimpse.util.geo.LatLonGeo;

/**
 * 
 * Caches Areadata information for a path during computations
 *
 */
public abstract class AbstractPathProfile {

	abstract public List<LatLonGeo> getIntermediatePoints();  // Used many places
	
	abstract public double[] getLandcoverSoaProfile(AreaData areaData); // NOT USED????
	
	abstract public int[] getLandcoverCostProfile(AreaData areaData); // generatePath
	
	abstract public double[] getDistances();
	
	abstract public double[] getElevationProfile(AreaData areaData); // traverseRoute; generatePath
	
	abstract public int[] getLandcoverCodeProfile(AreaData areaData); // generatePath
	
	abstract public double getTotalDistance(); // traverseRoute
	
	abstract public double getTotalVariation(AreaData areaData); // generatePath
	
	abstract public void clearStorage();
}
