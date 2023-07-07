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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 *
 * Custom OSPPRE representation of a 2D array Map Entry for each search List for each sample path of p(detection) for
 * that search
 *
 * the likelihood functions for each search
 *
 * this is an updated version using a map of SearchID -->List for that search
 * 
 * @author crock
 *
 */
public class PdValuesWithUUID implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3517294179027358469L;
    int numPaths;
    Map<UUID, List<Double>> pdValues; // Search ID --> p(detection) for that search for each sample path

    PdValuesWithUUID() {

    }

    public PdValuesWithUUID(int numSamplePaths) {
        this.numPaths = numSamplePaths;
    }

    public int getNumPaths() {
        return numPaths;
    }

    public void setNumPaths(int numPaths) {
        this.numPaths = numPaths;
    }

    public Map<UUID, List<Double>> getPdValues() {
        return pdValues;
    }

    public void setPdValues(Map<UUID, List<Double>> pdValues) {
        this.pdValues = pdValues;
    }

    public void addProbDetect(UUID searchId, List<Double> probDetection) {
        if (pdValues == null) {
            pdValues = new HashMap<>();
        }
        pdValues.put(searchId, probDetection);
    }

    // Assume original prior is uniform
    @JsonIgnore
    public List<Double> getProbNotDetectedForEachPath() {
        return getProbNotDetectedForEachPath(null);
    }

    /**
     * gets the probability of nonDetection for each path
     * 
     * @param searchIdsToInclude the ids of the searches we would like included in the results, or null for all searches
     */
    @JsonIgnore
    public List<Double> getProbNotDetectedForEachPath(Set<UUID> searchIdsToInclude) {

        int numSearches = 0;
        if (pdValues != null) {
            numSearches = pdValues.size();
        }

        List<Double> posterior = new ArrayList<>();

        // Will return product(1-p_s) where s runs over searches for each path
        for (int pIndx = 0; pIndx < numPaths; pIndx++) {
            posterior.add(pIndx, 1.0);
        }

        if (numSearches == 0) {
            return posterior;
        }

        // At this point, there is at least one search

        if (searchIdsToInclude == null) {
            // null -> include all searches
            searchIdsToInclude = pdValues.keySet();
        }

        // Loop over searches
        for (UUID searchId : searchIdsToInclude) {
            List<Double> pds = pdValues.get(searchId);

            // Loop over samples, setting value for each
            for (int pIndx = 0; pIndx < numPaths; pIndx++) {
                posterior.set(pIndx, posterior.get(pIndx) * (1 - pds.get(pIndx)));
            }
        }

        return posterior;
    }

    public void remove(UUID uuid) {
        pdValues.remove(uuid);
    }
}
