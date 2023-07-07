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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.metsci.glimpse.util.geo.LatLonGeo;

public class RiverNetworkData extends AdditionalData{
    private Map<String, LinkedList<String>> wayIdToNodeIds;
    private Map<String, LatLonGeo> nodesToLatLon;
    private Map<String, Set<String>> nodeIdToWayIds;

    public RiverNetworkData(Map<String, LinkedList<String>> wayIdToNodeIds, Map<String, LatLonGeo> nodesToLatLon,
            Map<String, Set<String>> nodeIdToWayId) {
        this.wayIdToNodeIds = wayIdToNodeIds;
        this.nodesToLatLon = nodesToLatLon;
        this.nodeIdToWayIds = nodeIdToWayId;
    }

    public Map<String, LinkedList<String>> getWayIdToNodeIds() {
        return wayIdToNodeIds;
    }

    public void setWayIdToNodeIds(Map<String, LinkedList<String>> wayIdToNodeIds) {
        this.wayIdToNodeIds = wayIdToNodeIds;
    }

    public Map<String, LatLonGeo> getNodesToLatLon() {
        return nodesToLatLon;
    }

    public void setNodesToLatLon(Map<String, LatLonGeo> nodesToLatLon) {
        this.nodesToLatLon = nodesToLatLon;
    }

    public Map<String, Set<String>> getNodeIdToWayIds() {
        return nodeIdToWayIds;
    }

    public void setNodeIdToWayIds(Map<String, Set<String>> nodeIdToWayIds) {
        this.nodeIdToWayIds = nodeIdToWayIds;
    }

	@Override
	public Collection<File> writeFiles(File outputDir) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}
}
