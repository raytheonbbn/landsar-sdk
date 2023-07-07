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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metsci.glimpse.util.geo.LatLonGeo;

/*
 * This class is used for a model in which the IP does not move. 
 * It should have exactly one point in the "path". 
 */
public class StationaryPath extends Path {
	private static final Logger LOGGER = LoggerFactory.getLogger(StationaryPath.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	long totalTime;

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	
	public StationaryPath() {
		super();
	}
	
	public StationaryPath(LatLonGeo point) {
		super();
		this.anchorPoints = new LatLonGeo[] {point};
	}

}
