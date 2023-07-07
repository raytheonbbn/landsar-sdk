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

package com.bbn.landsar.motionmodel.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.bbn.landsar.motionmodel.*;
import com.bbn.landsar.search.SearchUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.MovementSchedule;
import com.bbn.landsar.geospatial.AreaData;
import com.bbn.landsar.search.LandsarSearch;
import com.bbn.landsar.utils.DateTimeUtilities;
import com.bbn.landsar.utils.GroundUtilities;
import com.bbn.landsar.utils.StatusUpdateMessage;
import com.bbn.roger.annotation.Plugin;
import com.bbn.roger.config.AttributeDescription;
import com.bbn.roger.plugin.PluginContext;
import com.bbn.roger.plugin.exception.InsufficientConfigurationException;
import com.bbn.roger.plugin.exception.RogerInstantiationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metsci.glimpse.util.geo.LatLonGeo;

@Plugin
public class ExampleGoalPointMotionModelPlugin implements LandMotionModelPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleGoalPointMotionModelPlugin.class);

	protected static final Set<MotionModelAttributeDescription> PER_LPI_ATTRS = new HashSet<>();
	protected static final Set<GeospatialInputDescriptions> PER_LPI_GEOSPATIAL_ATTRS = new HashSet<>();

	public static final String SPEED = "speed";

	/**
	 * Motion Model Manager provides access to utility methods
	 */
	private MotionModelManager motionModelManager;

	private String name = ""; // name will be set by Motion Model Manager via setter

	/**
	 * Motion Model Manager has per-LPI locking, but my Motion Model Plugin may be called to create / update two different LPIs at once
	 */
	private Map<UUID, InternalModel> lpiData = new ConcurrentHashMap<>();



	final String INTERNAL_MODEL = "Internal Model";

	private boolean started;

	static class InternalModel{

		double distance;
		double speed;

		// save where we think the person is so we can update based on searches
		Map<Long, Map<LatLonGeo, Double>> pointsAtTimes;
		private LatLonGeo goalPoint;

		InternalModel(LatLonGeo goalPoint, Double speed) {
			this.goalPoint = goalPoint;
			this.speed = speed;
			this.pointsAtTimes = new HashMap<>();
		}
	}


	static {
		// KM/Hour is more user-friendly than meters/hour; we'll convert to meters/hour later
		PER_LPI_ATTRS.add(new MotionModelAttributeDescription(SPEED,
			"average speed", true, Double.class, Unit.KILOMETERS_PER_HOUR));

		PER_LPI_GEOSPATIAL_ATTRS.add(new GeospatialInputDescriptions(UserEnteredGeospatialData.GeospatialDataType.GOAL_POINTS,
			true));
		PER_LPI_GEOSPATIAL_ATTRS.add(new GeospatialInputDescriptions(UserEnteredGeospatialData.GeospatialDataType.EXCLUSION_ZONE,
			false));
	}

	@Override
	public MotionModelPluginMetadata getMetadata() {
		return new MotionModelPluginMetadata(
				this.name,
				PER_LPI_ATTRS,
				PER_LPI_GEOSPATIAL_ATTRS,
				true,
				true
		);
	}

	@Override
	public void configure(Map<String, Object> configurationOptions, PluginContext context)
		throws InsufficientConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize() throws RogerInstantiationException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<AttributeDescription> getConfigurationAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<MotionModelAttributeDescription> getMotionModelParameters() {
		return PER_LPI_ATTRS;
	}

	@Override
	public Set<GeospatialInputDescriptions> getMotionModelGeospatialDescriptions() {
		return PER_LPI_GEOSPATIAL_ATTRS;
	}

	@Override
	public boolean validateMotionModelParameters(Map<String, Object> motionModelParameters, UserEnteredGeospatialData geospatialInputs, StatusUpdateMessage status) {
		LOGGER.debug("Motion model parameters: {}", motionModelParameters);
		for (MotionModelAttributeDescription attribute : PER_LPI_ATTRS) {
			if (attribute.isRequired() && !motionModelParameters.containsKey(attribute.getName())) {
				status.addError(this.getName() + ": Missing required attribute '" + attribute.getName()+"'", "Missing requiredAttribute: " + attribute);
				// don't validate missing values
				motionModelManager.sendStatusUpdateMessage(status);
				return false;
			}
		}

		int numPoints = geospatialInputs.getGoalPoints().size();
		if (numPoints !=1) {
			status.addError(this.getName() + ": This plugin requires exactly one goal point. Have " + numPoints);
			// don't validate missing values
			motionModelManager.sendStatusUpdateMessage(status);
			return false;
		}

		Double speed = (Double) motionModelParameters.get(SPEED);

		boolean anyErrors = false;
		if (speed == null || speed <= 0.0) {
			status.addError("invalid parameter", String.format("invalid value for '%s. Value: %1$,.2f", SPEED, speed));
			anyErrors = true;
		}

		if (anyErrors) {
			// send statusUpdateMessage with all validation errors if there are any
			motionModelManager.sendStatusUpdateMessage(status);
		}

		return !anyErrors;
	}

	@Override
	public Map<Long, ProbabilityDistribution> createInitialDistribution(UUID lpiId,
		UserEnteredGeospatialData geospatialInputs, AreaData areaData, Map<String, Object> motionModelParameters,
		MovementSchedule schedule, long startTime, List<LatLonGeo> startTimeDistribution, StatusUpdateMessage status) {

		// Read in Model-Specific parameters (these values are set by the user when creating a new Lost Person Instance and selecting this motion model)
		// already validated direction, distance, speed in validateMotionModelParameters method
		Double speedMetersPerHour = (Double) motionModelParameters.get(SPEED) * 1000;

		Boolean stayOutOfWater = (Boolean) motionModelParameters.get(MotionModelConstants.STAY_OUT_OF_WATER);

		List<LatLonGeo> goalPoints = geospatialInputs.getGoalPoints();
		LatLonGeo goalPoint = goalPoints.get(0);

		// Create an object internal to my plugin implementation to store data my motion model plugin uses. 
		// InternalModel is our example model in an example representation. This could be a ML model or set of Paths for the traditional statistical models 
		InternalModel modelForThisLPI = new InternalModel(goalPoint, speedMetersPerHour);
		lpiData.put(lpiId, modelForThisLPI);

		// create list of distributions to return
		Map<Long, ProbabilityDistribution> distributions = new HashMap<>();
		// The array used for probabilities in Probability Distribution does NOT require a list of points, but can be created from a list points using the below utility method
		// add the startTimeDistribution to the list of distributions
		distributions.put(startTime, motionModelManager.createProbabilityDistribution(lpiId, startTime, areaData.getBoundingBox(), startTimeDistribution));
		Map<LatLonGeo, Double> currentPoints = createInitialPointMap(startTimeDistribution, goalPoint);
		modelForThisLPI.pointsAtTimes.put(startTime, currentPoints);

		LOGGER.debug("current points: {}", currentPoints);
		long time = startTime;
		// while we have any points that aren't the goal point
		while (currentPoints.keySet().size() > 1) {
			// All motion models should create distributions for each hour, starting from the start time  
			time += DateTimeUtilities.millisecInHour;

			Map<LatLonGeo, Double> newPoints = new HashMap<>();
			newPoints.put(goalPoint, 0.0);


			// Movement schedule models when the Lost Person is moving 
			if (schedule.isMoving(time)) {
				for (Entry<LatLonGeo, Double> entry : currentPoints.entrySet()) {
					LatLonGeo oldPoint = entry.getKey();
					Double oldPointWeight = entry.getValue();
					double speedForThisPoint = speedMetersPerHour * areaData.getLandcoverData().getSoaFactor(oldPoint.getLatDeg(), oldPoint.getLonDeg());
					// one hour time steps means hourly speed is equal to distance
					if (oldPoint.getDistanceTo(goalPoint) < speedForThisPoint) {
						// reached goal point from oldPoint. Add oldPoint's weight to goalPoint
						newPoints.compute(goalPoint, (key, value) -> value += oldPointWeight);
					} else {
						LatLonGeo newPoint = oldPoint.displacedBy(speedForThisPoint, oldPoint.getAzimuthTo(goalPoint));
						// for the new points, don't add any that are in the exclusion zones (this model doesn't worry about the weights associated with that point if we remove it since weights are relative)
						if (GroundUtilities.exclusionZonesContainsPoint(geospatialInputs.getExclusionZones(), newPoint)){
							continue;
						}
						// same for stay out of water
						if (stayOutOfWater && areaData.getLandcoverData().isWater(newPoint.getLatDeg(), newPoint.getLonDeg())) {
							continue;
						}
						newPoints.computeIfPresent(newPoint, (key, value) -> value + oldPointWeight);
						newPoints.putIfAbsent(newPoint, oldPointWeight);
					}

				}
			} else {
				// still copy the points so we're not referencing the same map in the data structure if we change them in teh future
				newPoints.putAll(currentPoints);
			}

			currentPoints = newPoints;
			LOGGER.debug("current points: {}", currentPoints);
			// save the points for each time step: 
			// keep detailed Internal Model so that we can update based on searches later -- make sure to copy the current data structure, which we did above
			modelForThisLPI.pointsAtTimes.put(time, currentPoints);
			// add to time-based distribution (which will be shown to users as a KMZ)
			List<LatLonGeo> pts = new ArrayList<>();
			List<Double> weights = new ArrayList<>();
			currentPoints.forEach((pt, wgt) -> {
				pts.add(pt);
				weights.add(wgt);
			});
			distributions.put(time, motionModelManager.createProbabilityDistribution(lpiId, time, areaData.getBoundingBox(), pts, weights));

		}
		LOGGER.debug("current points: {}", currentPoints);
		if (currentPoints.isEmpty()) {
			status.addWarning(this.name + ": Ran out of viable points. Look at results carefully!");
		}
		motionModelManager.sendInProgressUpdate(this.name + ": finished computing.", status);
		return distributions;
	}



	// create map of point -> probability at point
	private Map<LatLonGeo, Double> createInitialPointMap(List<LatLonGeo> startTimeDistribution, LatLonGeo goalPoint) {
		Map<LatLonGeo, Double> pointMap = new HashMap<>();
		final Double onePointWeight = 1.0/startTimeDistribution.size();
		for (LatLonGeo pt : startTimeDistribution) {
			pointMap.computeIfPresent(pt, (thePoint, currentWeight) -> currentWeight+=onePointWeight);
			pointMap.putIfAbsent(pt, onePointWeight);
		}
		// if the goal point isn't in the map, add it with weight 0
		pointMap.putIfAbsent(goalPoint, 0.0);
		return pointMap;
	}

	@Override public
	MotionModelResult generateInitialDistribution(MotionModelInput input) {
		MotionModelResult result = new MotionModelResult();
		result.setLpiId(input.getLpiId());
		Map<Long, ProbabilityDistribution> initialDistribution = createInitialDistribution(input.getLpiId(), input.getGeospatialInputs(), input.getAreaData(), input.getMotionModelParameters(),
			input.getMovementSchedule(), input.getStartTime(), input.getStartTimeDistribution(), input.getStatus());
		result.setInitialDistribution(initialDistribution);
		result.setDistributionWithSearches(initialDistribution);
		result.setGeneratedTimestamp(input.getStartTime());
		result.setGeneratingModelName(this.getName());
		return result;
	}

	@Override
	public MotionModelResult updateProbabilityDistributionForSearch(MotionModelResult latestResult, LandsarSearch search,
		SearchUtilities searchUtilities) {
		InternalModel internalModel = this.lpiData.get(latestResult.getLpiId());
		return null;
	}

	@Override
	public MotionModelResult cancelSearch(MotionModelResult latestResult, LandsarSearch search) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double calculateProbabilityOfSuccess(UUID lostPersonId, Set<? extends LandsarSearch> searchesToInclude) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deleteModelState(List<UUID> lpisToDelete) {
		// this is called when a user deletes an LPI
		for (UUID id : lpisToDelete) {
			lpiData.remove(id);
		}

	}

	@Override
	public void setMotionModelManager(MotionModelManager motionModelManager) {
		this.motionModelManager = motionModelManager;

	}

	@Override
	public void start() {
		this.started = true;
	}


	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public void stop() {
		// LandSAR system will shutdown - save Internal Model currently in memory to disk
		ObjectMapper mapper = new ObjectMapper();
		for (Entry<UUID, InternalModel> entry : lpiData.entrySet()) {
			try {
				File dataFile = motionModelManager.getOrCreateFileForModelData(entry.getKey(), this.getName(), INTERNAL_MODEL);
				mapper.writeValue(dataFile, entry.getValue());
			} catch (IOException e) {
				LOGGER.error("Error saving {} for LPI ID: {}", INTERNAL_MODEL, entry.getKey(), e);
			}
		}
		this.started = false;
	}


	@Override
	public void restoreModelState(UUID lostPersonId, Map<String, File> tagToDataFile) {
		// LandSAR system restarted with saved LPIs - restore state my plugin expects to be in memory 

		if (tagToDataFile.containsKey(INTERNAL_MODEL)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				lpiData.put(lostPersonId, mapper.readValue(tagToDataFile.get(INTERNAL_MODEL), InternalModel.class));
			} catch (IOException e) {
				LOGGER.error("Error restoring internal model data for LPI {}", lostPersonId, e);
			}
		}
	}



}
