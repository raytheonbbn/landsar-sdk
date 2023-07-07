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

package com.bbn.landsar.search;

import java.util.List;
import java.util.UUID;

import com.bbn.landsar.geospatial.GeographicDrawable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.metsci.glimpse.util.geo.LatLonGeo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface LandsarSearch extends GeographicDrawable{

	UUID getSearchId();

	/**
	 * Was the search already completed when the user told us about it?
	 * @return true if a user entered a search after the search was completed, false if LandSAR planned the search
	 */
	boolean isCompleted();

	void setCompleted(boolean isCompleted);

	// this should be the start time
	long getTime();

	void setTime(long time);
	
	// return duration of the search in hours
	Double getDuration();
	
	//duration of the search in hours
	void setDuration(Double durationHours);

	void setSearchId(UUID id);

	/**
	 * 
	 * @return this search's probability of detection
	 */
	double getPd();
	
	void setPd(double pd);
	
	double getPointPd(LatLonGeo pt);

	//SearchGenerationParameters getSearchGenerationParameters();

	List<LatLonGeo> getRepresentativePoints();

	/**
	 * 
	 * @return Returns the area of the search region in km^2
	 */
	double getSearchRegionArea();

	// Returns a single location associated with at least one contained SimpleSearch
	LatLonGeo getLocation();

	/**
	 * Checks if this search and the other search have the same representative points, not considering the order they are listed in.
	 * @param other
	 * @return
	 */
	boolean sameRepresentativePoints(LandsarSearch other);

	/**
	 * compares time, pd, and representative points. Does not compare UUID. Should not be used for equals. 
	 * Created to support equivalent results with Randoms
	 * @param other
	 * @return
	 */
	boolean equivalent(LandsarSearch other);

}
