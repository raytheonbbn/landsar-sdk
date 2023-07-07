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

package com.bbn.landsar.motionmodel;

/**
 * The static constants in this class should be used if applicable. 
 * @author crock
 *
 */
public class AreaDataType {
	
	public static final String ELEVATION = "Elevation"; 
	public static final String LANDCOVER = "Landcover"; 
	public static final String CURRENTS = "Currents"; 
	public static final String WINDS = "Winds"; 
	public static final String RIVERWAY = "Riverway"; 
	
	/**
	 * Trail data is not currently supported
	 */
	public static final String TRAIL= "Trail"; 
	
	public final String typeOfAreaData;
	public final boolean required;

	/**
	 * 
	 * @param typeOfAreaData - should be one of the string constants defined in this class, but they are stings so that they are easier to be extended by DataDownloaders. 
	 * @param required if false, the Motion Model Plugin plugin should still operate without this data. 
	 * If "required" data cannot be loaded, the plugin will not be called at all. 
	 */
	public AreaDataType(String typeOfAreaData, boolean required) {
		this.typeOfAreaData = typeOfAreaData;
		this.required = required;
	}
}
