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

package com.bbn.landsar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.metsci.glimpse.util.geo.LatLonGeo;

public class LandsarUtils {
	


    /*
     * Returns a CSV string of lat/lons defining the points
     */
    public static String getStringForPts(List<LatLonGeo> pts) {
        StringBuffer buffer = new StringBuffer();

        for (int pIndx = 0; pIndx < pts.size(); pIndx++) {
            if (pIndx > 0) buffer.append(",");
            LatLonGeo pt = pts.get(pIndx);
            buffer.append(pt.getLatDeg() + "," + pt.getLonDeg());
        }
        return buffer.toString();
    }

    public static LatLonGeo[] stringToPts(String ptsString) {
        String[] latLons = ptsString.split(",");
        LatLonGeo[] pts = new LatLonGeo[latLons.length/2];
        for (int index = 0; index< latLons.length-1; index+=2) {
            double lat = Double.parseDouble(latLons[index]);
            double lon = Double.parseDouble(latLons[index+1]);
            pts[index/2] = LatLonGeo.fromDeg(lat, lon);
        }
        return pts;
    }

    public static ArrayList<LatLonGeo> stringToPtsList(String ptsString) {
        String[] latLons = ptsString.split(",");
        ArrayList<LatLonGeo> pts = new ArrayList<LatLonGeo>(latLons.length/2);
        for (int index = 0; index< latLons.length-1; index+=2) {
            double lat = Double.parseDouble(latLons[index]);
            double lon = Double.parseDouble(latLons[index+1]);
            pts.add(LatLonGeo.fromDeg(lat, lon));
        }
        return pts;
    }
    /*
     * Returns a CSV string of lat/lons defining the path
     */
    public static String getStringForPts(LatLonGeo[] pts) {
        StringBuffer buffer = new StringBuffer();

        for (int pIndx = 0; pIndx < pts.length; pIndx++) {
            if (pIndx > 0) buffer.append(",");
            LatLonGeo pt = pts[pIndx];
            buffer.append(pt.getLatDeg() + "," + pt.getLonDeg());
        }
        return buffer.toString();
    }
    
    public static <T> Map<T, String> getStringForPointsListInMap(Map<T, List<LatLonGeo>> mapWithPointsList){
    	Map<T, String> newMap = new HashMap<>();
    	for (Entry<T, List<LatLonGeo>> entry : mapWithPointsList.entrySet()) {
    		newMap.put(entry.getKey(), getStringForPts(entry.getValue()));
    	}
    	return newMap;
    }
    
    public static <T> Map<T, List<LatLonGeo>> getListForPointsStringInMap(Map<T, String> mapWithPointsString){
    	Map<T, List<LatLonGeo>> newMap = new HashMap<>();
    	for (Entry<T, String> entry : mapWithPointsString.entrySet()) {
    		newMap.put(entry.getKey(), stringToPtsList(entry.getValue()));
    	}
    	return newMap;
    }
}
