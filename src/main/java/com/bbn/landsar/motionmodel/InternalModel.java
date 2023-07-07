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

import com.bbn.landsar.motionmodel.path.Sample;
import com.bbn.landsar.search.LandsarSearch;
import com.metsci.glimpse.util.geo.LatLonGeo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maintain Internal-to-the-Motion-Model State for each Lost Person Instance
 * @author crock
 *
 */
public abstract class InternalModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalModel.class);
    private List<Sample> samplePaths = new ArrayList<>();
    /**
     * 2D array For each search List for each sample path of p(detection) for that
     * search
     */
    private PdValuesWithUUID probabilityOfDetectionValues;
    private List<LandsarSearch> searches = new ArrayList<>();

    public InternalModel() {
    }

    public InternalModel(List<LandsarSearch> searches) {
        this.searches = searches;
    }

    /**
     * This method copied from Scenario class
     *
     * @return sample weights list
     */
    public List<Double> calcOverallSampleWeights() {

        // Get the probability not detected for each sample
        List<Double> pathWgts = this.getProbabilityOfDetectionValues().getProbNotDetectedForEachPath();

        // Get prior (uniform) weight for each path in the scenario
        int numPaths = this.getSamplePaths().size();
        double priorPathWeight = 1.0 / numPaths;

        // Weight the scenario path probabilities by the baseline
        pathWgts.replaceAll(aDouble -> aDouble * priorPathWeight);
        List<Double> overallSampleWeights = new ArrayList<>(pathWgts);

        // Normalize the total
        double c = 0;
        for (Double d : overallSampleWeights)
            c += d;
        for (int i = 0; i < overallSampleWeights.size(); i++) {
            overallSampleWeights.set(i, overallSampleWeights.get(i) / c);

        }
        return overallSampleWeights;
    }

    /**
     * Calculate the cumulative probability of detection
     *
     * @param setSoFar the set of searches (that we've already added to this model) to consider when calculating this
     *            cumulativePd
     */
    public double calcCumulativePd(Set<? extends LandsarSearch> setSoFar) {
        LOGGER.debug("Calculating cumulative pd...");
        double cumulativePd = 0.0;
        if (setSoFar.isEmpty()) {
            // no searches, so cumulative probability is zero
            return cumulativePd;
        }
        // Get the probability not detected for each sample
        List<Double> pathWgts = this.probabilityOfDetectionValues.getProbNotDetectedForEachPath(
                setSoFar.stream().map(LandsarSearch::getSearchId).collect(Collectors.toCollection(
                        LinkedHashSet::new)));

        // Get prior (uniform) weight for each path in the scenario
        int numPaths = samplePaths.size();
        double priorPathWeight = 1.0 / numPaths;

        for (Double pathWgt : pathWgts) {
            cumulativePd += (1 - pathWgt) * priorPathWeight;// * baseWgt;
        }

        return cumulativePd;
    }

    /**
     * Set sample paths
     *
     * @param samplePaths list of samples
     */
    public void setSamples(List<Sample> samplePaths) {
        setSamplePaths(samplePaths);
        this.probabilityOfDetectionValues = new PdValuesWithUUID(samplePaths.size());

    }

    /**
     * Remove search
     *
     * @param latestResult latest motion model result
     * @param search search to add
     *
     * @return map of time to distribution by sample points
     */
    public Map<Long, DistributionBySamplePoints> removeSearch(MotionModelResult latestResult, LandsarSearch search) {
        this.probabilityOfDetectionValues.remove(search.getSearchId());
        return updateDistributionWithSearches(latestResult.getInitialDistribution().keySet());
    }

    /**
     * Update distribution with searches
     *
     * @param times set of times
     *
     * @return Map of time to distribution by sample points
     */
    public Map<Long, DistributionBySamplePoints> updateDistributionWithSearches(Set<Long> times) {
        Map<Long, DistributionBySamplePoints> updatedDistributions = new HashMap<>();
        // recalculate distribution with searches
        for (long time : times) {
            List<LatLonGeo> locationsAtTime = samplePaths.stream().map(sample -> sample.getLocation(time))
                    .collect(Collectors.toList());
            DistributionBySamplePoints distForTime = new DistributionBySamplePoints(locationsAtTime,
                    calcOverallSampleWeights());

            updatedDistributions.put(time, distForTime);
        }
        return updatedDistributions;
    }

    /**
     * Add a Search
     *
     * @param latestResult latest motion model result
     * @param search the search to add
     * @param pathPds probability of search detecting on each samplePath
     *
     * @return map of time to distribution by sample points
     */
    public Map<Long, DistributionBySamplePoints> addSearch(MotionModelResult latestResult, LandsarSearch search,
            List<Double> pathPds) {
        this.searches.add(search);
        getProbabilityOfDetectionValues().addProbDetect(search.getSearchId(), pathPds);
        return updateDistributionWithSearches(latestResult.getInitialDistribution().keySet());
    }

    /**
     * Get the end time
     *
     * @return end time
     */
    public long endTime() {
        return samplePaths.stream().map(Sample::getEndTime).max(Comparator.comparingLong(longg -> longg)).get();
    }

    /**
     * Get location at time
     *
     * @param time of location
     * @return list of locations
     */
    public List<LatLonGeo> getLocationsAtTime(long time) {
        return this.samplePaths.stream().map(sample -> sample.getLocation(time)).collect(Collectors.toList());
    }

    public List<Sample> getSamplePaths() {
        return samplePaths;
    }

    public void setSamplePaths(List<Sample> samplePaths) {
        this.samplePaths = samplePaths;
    }

    public PdValuesWithUUID getProbabilityOfDetectionValues() {
        return probabilityOfDetectionValues;
    }

    public void setProbabilityOfDetectionValues(PdValuesWithUUID probabilityOfDetectionValues) {
        this.probabilityOfDetectionValues = probabilityOfDetectionValues;
    }

    public List<LandsarSearch> getSearches() {
        return searches;
    }

    public void setSearches(List<LandsarSearch> searches) {
        this.searches = searches;
    }
}
