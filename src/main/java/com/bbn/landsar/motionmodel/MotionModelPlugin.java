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

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.bbn.landsar.search.LandsarSearch;
import com.bbn.landsar.search.SearchUtilities;
import com.bbn.landsar.utils.StatusUpdateMessage;
import com.bbn.roger.plugin.StartablePlugin;

public interface MotionModelPlugin extends StartablePlugin {

	/**
	 * Returns serializable information about this plugin that can be shared
	 * with OSPPRE clients.
	 * @return serializable information about this plugin.
	 */
	public MotionModelPluginMetadata getMetadata();

	/**
	 * Sets this plugin's name (also used as the model name) from the config file
	 */
	public void setName(String name);
	
	/**
	 * Gets this plugin's name (also used as the model name) from {@link #setName(String name)}
	 */
	public String getName();
	

	public void setMotionModelManager(MotionModelManager motionModelManager);
	
	/**
	 * Returns the names/descriptions of the per-Lost Person Instance parameters (required
	 * or optional), that will be shown to the user for the user to input in the LandSAR User Interface. 
	 * It is recommended, but not required, to modify or extend the Set of default parameters: ({see {@link MotionModelConstants#getDefaultMotionModelParameters()}.
	 */
	public Set<MotionModelAttributeDescription> getMotionModelParameters();

	/**
	 * Returns the geospatial inputs that are applicable to motion model (e.g. waypoints, exclusion zones,
	 * poly-exclusion zones, goal points), which are either required or optional
	 * This geospatial data is traditionally user-entered and constrained to the bounding box
	 */
	public Set<GeospatialInputDescriptions> getMotionModelGeospatialDescriptions();
	
	/**
	 * 
	 * @return Types of (required and optional) AreaData like Elevation, Landcover, Current and Wind Data
	 */
	public Set<AreaDataType> getRequiredAreaData();

	/**
	 * If returning false, an Error type status update message should be sent to the user by this method. 
	 * @param motionModelParameters - user-entered parameters according to {@link #getMotionModelParameters()}
	 * @param geospatialInputs - user-entered geospatial inputs according to {@link LandMotionModelPlugin#getMotionModelGeospatialDescriptions()}
	 * @return true if the parameters are a valid combination, 
	 * false if any parameter or combination of parameters is invalid
	 */
	public boolean validateMotionModelParameters(Map<String, Object> motionModelParameters,
			UserEnteredGeospatialData geospatialInputs, StatusUpdateMessage status);
	
	/**
	 * Create a new Model for a new Lost Person Instance and return the initial distribution (over time), 
	 * mapping time to the distribution over the Bounding Box at that time. 
	 * The MotionModelResult will have its validate method called by the server's MotionModelManager.
	 * @param input - a container object for the following inputs:<ul>
	 * 
	 * <li>lpiId - a unique identifier for this Lost Person Instance. This id is referenced when calling calculateProbabilityOfSuccess, updateProbabilityDistributionForSearch, cancelSearch, deleteModelState. 
	 * </li><li>geospatialInputs - Rendezvous Points, Exclusion Zones, etc. entered by user
	 * </li><li> areaData - The AreaData includes the <b>Bounding Box</b> for the Lost Person Instance, as well as Elevation and LandCover data over the Bounding Box.
	 * </li><li> motionModelParameters - values of parameters for this LPI. Prior to calling this method, {@link #validateMotionModelParameters(Map, StatusUpdateMessage)} will be called with the parameters.
	 * </li><li> schedule - The Movement Schedule represents when the lost person is expected to be moving. Most current movement schedules are the same for any 24-hour period, but this interface allows the Movement Schedules and types of Movement Schedules to be extended. 
	 * </li><li> startTime - The time (in Unix/Epoch Time) the person was last seen or estimated to be located at the startTimeDistribution. 
	 * </li><li> startTimeDistribution - The Last Known Point of the lost person. This may be a single point or a distribution based on user-entered data. 
	 * </li><li> StatusUpdateMessage status - The Status Update Message can be used to send information back to the LandSAR user about the progress of modeling and computation, since this method may be compute/time intensive.
	 * </li></ul>
	 * @return MotionModelResult - a container object encapsulating a result from a Motion Model Plugin. The MotionModelResult should contain the <b>Initial Distribution</b>: The estimated location of the lost person before considering any searches. This Initial Distribution is realized as a set of geographic distributions modeled over discrete times. 
	 * The geographic distributions should be constrained to the provided Bounding Box. 
	 * The times included in any distribution should be hourly, starting at the startTime, and ending when the model anticipates no further movement within the bounding box. 
	 * <br>The Distribution With Searches must also be set, and it is expected to be equivalent to the Initial Distribution when there are no searches.
	 */
	public MotionModelResult generateInitialDistribution(MotionModelInput input);

	/**
	 * This method should calculate the overall probability of success over the set of searchesToInclude
	 * This method should not change the probability distribution (or have any other side affects). 
	 * The returned value is displayed to the user as cumulative probability of success for this LPI
	 * @param lostPersonId, - UUID of lost person instance
	 * @param searchesToInclude - set of searches to consider
	 * @return calculated or estimated probability of success in range [0, 1] of finding the person, assuming all searches in searchesToInclude are executed. 
	 * Probability of success = sum (over bounding box, over searches) of probability of detection * probability of containment  
	 * 
	 * Probability of detection for a specific location can be accessed via 	
	 * <pre>
	 * LatLonGeo location = LatLonGeo.fromDeg(42.0, -117.0);
	 * double probabilityOfDetectionAtLocation = search.getPointPd(location);
	 * </pre>
	 * When using motion model plugins, the LandSAR system is unaware of how success (or lack thereof) of Search A at location A_loc at time A_time effects the 
	 * probability of containment for location B_loc at time B_time, which is needed to calculate the overall probability of success for search B
	 */
	public double calculateProbabilityOfSuccess(UUID lostPersonId, Set<? extends LandsarSearch> searchesToInclude);
	
	/**
	 * This method should return an updated probability distribution, and may modify the one passed in.  
	 * @param currentResult - the most recent MotionModelResult for this Lost Person Instance, including distribution with other planned searches and the UUID of the lost Person instance 
	 * @param search - a planned search to be included in the updated probability distribution
	 * @param searchUtilities - set of search utilities useful during calculation
	 * @return an updated probability distribution (over time) accounting for the search.
	 */

	public MotionModelResult updateProbabilityDistributionForSearch(MotionModelResult currentResult, LandsarSearch search,
		SearchUtilities searchUtilities);
	
	/**
	 * This method should cancel (remove) the search, and update the probability distribution as if that search was unable to occur. 
	 * This method is essentially the opposite of {@link #updateProbabilityDistributionForSearch}
 	 * @param currentResult - the most recent MotionModelResult for this Lost Person Instance, including distribution with other planned searches and the UUID of the lost Person instance 
	 * @param search - search to cancel / remove
	 * @return updated probability distribution (over time) no longer accounting for search
	 */
	public MotionModelResult cancelSearch(MotionModelResult currentResult, LandsarSearch search);
	
	
	
	/**
	 * This method is used when calculating searches
	 * @param time
	 * @return DistOrMap: either a ProbabilityDistribution (which extends ContainmentMap), or a DistributionByPoints, which allows for more fine grained search evaluation
	 * 
	 * This default implementation does linear interpolation across the Probability Distributions.  
	 */
	public default DistOrMap calcDistributionWithSearches(MotionModelResult currentResult, long time) {
		Map<Long, ProbabilityDistribution> distributionWithSearches = currentResult.getDistributionWithSearches();		
		
		if (distributionWithSearches != null && distributionWithSearches.containsKey(time)) {
			return new DistOrMap(distributionWithSearches.get(time));
		}
		
		Long maxTime = distributionWithSearches.keySet().stream().max(Comparator.comparingLong(x->x)).get();
		if (maxTime < time) {
			// requested time is after latest distribution we have, so use that last distribution
			return new DistOrMap(distributionWithSearches.get(maxTime));
		}
		
		
		// TODO use a more efficient data structure so this interpolation is faster
		long bestBeforeDelta = Long.MAX_VALUE;
		long bestAfterDelta = Long.MAX_VALUE;
		long closestBeforeTime = Long.MIN_VALUE;
		long closestAfterTime = Long.MAX_VALUE;
		
		for (Entry<Long, ProbabilityDistribution> entry : currentResult.getDistributionWithSearches().entrySet()) {
			long distTime = entry.getKey();
			if (distTime > time) {
				// delta will be a positive #
				long afterDelta = distTime - time;
				if (afterDelta < bestAfterDelta) {
					bestAfterDelta = afterDelta;
					closestAfterTime = distTime;
				}
			} else { // distTime < time
				// delta will be a positive #
				long beforeDelta = time - distTime;
				if (beforeDelta < bestBeforeDelta) {
					bestBeforeDelta = beforeDelta;
					closestBeforeTime = distTime;
				}
			}
		}	
		// we'll compute linear interpolation between these two distributions
		ProbabilityDistribution before = currentResult.getDistributionWithSearches().get(closestBeforeTime);
		ProbabilityDistribution after = currentResult.getDistributionWithSearches().get(closestAfterTime);
		
		
		// Math Justification:
		// consider a < b < c (earlierTime < time < laterTime)
		// want weightA + weightC = 1 
		// want weightC to be inversely proportional to distance between b and c (since it's linear, proportional to distance between a and c is good), and weightC to equal 1 if b=c
		// consider example where indicies we have are 2 and 5 and someone requests values for time=4. (a=2, b=4, c=5). 
		// 			weightC=(b-a)/(c-a) = 2/3 makes sense; weightA = (c-b)/(c-a) = 1/3, makes sense
		
				
		// calculate weightAfter (weightC)
		double weightAfter = ((double) bestBeforeDelta/((double) closestAfterTime - closestBeforeTime));
		
		// weightBefore + weightAfter = 1; want to keep total probability the same
		double weightBefore = 1.0 - weightAfter;
		
		ProbabilityDistribution beforeFactored = before.scalarMultiply(weightBefore);
		ProbabilityDistribution afterFactored = after.scalarMultiply(weightAfter);
		ProbabilityDistribution combined = beforeFactored.add(afterFactored);
		combined.setTime(time);
		combined.setBoundingBox(before.getBoundingBox());
		
		return new DistOrMap(combined);
		
	}
	
	
	
	/**
	 * Called to tell the plugin to clean up state related to deleted Lost Person Instances
	 * @param lpisToDelete - a list of UUIDs corresponding to lost person instances that have been deleted 
	 */
	public void deleteModelState(List<UUID> lpisToDelete);

	/**
	 * Called once per LPI on system re-start with data stored via saveInternalModelData
	 * @param lostPersonId - UUID for lost person instance
	 * @param tagToDataFile
	 * {see {@link MotionModelManager#getOrCreateFileForModelData()}.
	 */
	void restoreModelState(UUID lostPersonId, Map<String, File> tagToDataFile);

}
