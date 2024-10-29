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
import java.util.stream.Collectors;

import com.bbn.landsar.motionmodel.*;
import com.bbn.landsar.search.SearchUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.geospatial.AreaData;
import com.bbn.landsar.motionmodel.GeospatialInputDescriptions;
import com.bbn.landsar.motionmodel.LandMotionModelPlugin;
import com.bbn.landsar.motionmodel.MotionModelAttributeDescription;
import com.bbn.landsar.motionmodel.MotionModelConstants;
import com.bbn.landsar.motionmodel.MotionModelInput;
import com.bbn.landsar.motionmodel.MotionModelManager;
import com.bbn.landsar.motionmodel.MotionModelResult;
import com.bbn.landsar.motionmodel.ProbabilityDistribution;
import com.bbn.landsar.motionmodel.Unit;
import com.bbn.landsar.motionmodel.UserEnteredGeospatialData;
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
public class ExampleMotionModelPlugin implements LandMotionModelPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMotionModelPlugin.class);

    protected static final Set<MotionModelAttributeDescription> PER_LPI_ATTRS = new HashSet<>();
	protected static final Set<GeospatialInputDescriptions> PER_LPI_GEOSPATIAL_ATTRS = new HashSet<>();
	
    public static final String DISTANCE = "distance";
    public static final String DIRECTION = "direction";
    public static final String SPEED = "speed";

    /**
     * Motion Model Manager provides access to utility methods
     */
    private MotionModelManager motionModelManager;
    
    private String name = ""; // name will be set by Motion Model Manager via setter

	private boolean requireElevationData = false;
    /**
     * Motion Model Manager has per-LPI locking, but my Motion Model Plugin may be called to create / update two different LPIs at once
     */
	private Map<UUID, InternalModel> lpiData = new ConcurrentHashMap<>();
	
	
	
	final String INTERNAL_MODEL_FILENAME = "internal_model.json";

	private boolean started;
    
    static class InternalModel{

		double direction;
    	double distance;
    	double speed;
    	
    	// save where we think the person is so we can update based on searches
    	Map<Long, List<LatLonGeo>> pointsAtTimes;
    	
    	InternalModel(Double direction, Double distance, Double speed) {
			this.direction = direction;
			this.distance = distance;
			this.speed = speed;
			this.pointsAtTimes = new HashMap<>();
		}
    }
    
    
	static {
		PER_LPI_ATTRS.add(new MotionModelAttributeDescription(DISTANCE,
				"distance person might travel", true, Double.class, Unit.KILOMETERS));
		PER_LPI_ATTRS.add(new MotionModelAttributeDescription(DIRECTION,
				"direction (in degrees [0-360)) person was believed to be going", true, Double.class, Unit.DEGREES));
		PER_LPI_ATTRS.add(new MotionModelAttributeDescription(SPEED,
				"average speed", true, Double.class, Unit.KILOMETERS_PER_HOUR));

		// adding this here (not required) will still give the user the option to enter in this data type
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


	}

	@Override
	public Set<AreaDataType> getRequiredAreaData() {
		Set<AreaDataType> desiredAndRequiredAreaData = new HashSet<>();
		// Landcover is desired, but not required, so it gets added to the set with required=false
		desiredAndRequiredAreaData.add(new AreaDataType(AreaDataType.LANDCOVER, false));

		// If required, LandSAR will throw an error if it is unable to provide this Motion Model Plugin with Elevation Data
		// If not required, but present in the returned set, LandSAR will attempt to provide Elevation Data to the plugin as part of the AreaData
		// If AreaDataType.ELEVATION is not included in the returned set, LandSAR will not even attempt to download Elevation data when this plugin is selected
		desiredAndRequiredAreaData.add(new AreaDataType(AreaDataType.ELEVATION, this.requireElevationData));
		return desiredAndRequiredAreaData;
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

		Double direction = (Double) motionModelParameters.get(DIRECTION);
		Double speed = (Double) motionModelParameters.get(SPEED);
		Double distance = (Double) motionModelParameters.get(DISTANCE);
		
		boolean anyErrors = false;
		if (distance < 0.0) {
			status.addError("invalid parameter", String.format("invalid value for '%s. Value: %1$,.2f", DISTANCE, distance));
			anyErrors = true;
		}
		if (direction == null || direction < 0.0 || direction >= 360 ) {
			status.addError("invalid parameter", String.format("invalid value for '%s. Value: %1$,.2f", DIRECTION, direction));
			anyErrors = true;
		}
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
	public MotionModelResult generateInitialDistribution(MotionModelInput input) {
		Map<String, Object> motionModelParameters = input.getMotionModelParameters();
		UUID lpiId = input.getLpiId();
		long startTime = input.getStartTime();
		List<LatLonGeo> startTimeDistribution = input.getStartTimeDistribution();
		AreaData areaData = input.getAreaData();
		StatusUpdateMessage status = input.getStatus();
		
		// get general parameter(s) provided to all motion models
		Boolean stayOutOfWater = (Boolean) motionModelParameters.get(MotionModelConstants.STAY_OUT_OF_WATER);

		// Read in Model-Specific parameters (these values are set by the user when creating a new Lost Person Instance and selecting this motion model)
		// already validated direction, distance, speed in validateMotionModelParameters method
		Double direction = (Double) motionModelParameters.get(DIRECTION);
		Double speed = (Double) motionModelParameters.get(SPEED);
		Double distance = (Double) motionModelParameters.get(DISTANCE);
		
		// Create an object internal to my plugin implementation to store data my motion model plugin uses. 
		// InternalModel is our example model in an example representation. This could be a ML model or set of Paths for the traditional statistical models. 
		// If you don't have paths, you need some way to track how a decrease in probability over an area at one time (due to a failed search) would impact your probability distributions at future times. 
		
		InternalModel modelForThisLPI = new InternalModel(direction, distance, speed);
		lpiData.put(lpiId, modelForThisLPI);
		
		// create list of distributions to return
		Map<Long, ProbabilityDistribution> distributions = new HashMap<>();

		// The array used for probabilities in Probability Distribution does NOT require a list of points, but can be created from a list points using the below utility method
		// add the startTimeDistribution to the list of distributions
		distributions.put(startTime, motionModelManager.createProbabilityDistribution(lpiId, startTime, areaData.getBoundingBox(), startTimeDistribution));
		List<LatLonGeo> currentPoints = new ArrayList<>(startTimeDistribution);
		modelForThisLPI.pointsAtTimes.put(startTime, currentPoints);
		
		long time = startTime;
		Double distanceTraveled = 0.0;
		while (distanceTraveled < distance && !currentPoints.isEmpty()) {
			// All motion models should create distributions for each hour, starting from the start time  
			time += DateTimeUtilities.millisecInHour;
			
			// Movement schedule models when the Lost Person is moving 
			if (input.getMovementSchedule().isMoving(time)) {
			
				// if this iteration, we are crossing the halfway mark, send an update
				if (distanceTraveled < distance/2 && (distanceTraveled + speed) >= distance/2) {
					status = motionModelManager.sendInProgressUpdate(this.name + ": halfway done computing.", status);
				}
				
				// speed in km/hour, distributions every hour - this will vary slightly with landcover speed of advance factor
				distanceTraveled += speed;
	
				currentPoints = currentPoints.parallelStream()
						.filter(pt -> pt != null)
						// multiply base speed (entered by user) by Speed of Advance Factor for current point landcover type (trees, water, etc)
						// multiply that by 1000 since displacedBy takes distance in meters
						.map(pt -> pt.displacedBy(1000 * speed * areaData.getLandcoverData().getSoaFactor(pt.getLatDeg(), pt.getLonDeg()), direction))
						// Motion Models can't represent points outside the bounding box, so filter those out (a better model might find the nearest point inside the box and keep it)
						.filter(pt -> areaData.getBoundingBox().contains(pt))
						// for the new points, remove any that are in the exclusion zones
						.filter(pt -> !GroundUtilities.exclusionZonesContainsPoint(input.getGeospatialInputs().getExclusionZones(), pt))
						// for the new points, remove any that are in water if stayOutOfWater 
						.filter(pt -> {
							if (stayOutOfWater) {
								return (!areaData.getLandcoverData().isWater(pt.getLatDeg(), pt.getLonDeg()));
							}
							// otherwise, keep all points regardless of water status
							return true;
						})
						.collect(Collectors.toList());
				logSetOfPoints(currentPoints);
			}
	
			// don't create a distribution if all of the paths have moved outside the box
			if (!currentPoints.isEmpty()) {
				// save the points for each time step: 
				// keep detailed Internal Model so that we can update based on searches later -- make sure to copy the list
				modelForThisLPI.pointsAtTimes.put(time, new ArrayList<>(currentPoints));
				// add to time-based distribution (which will be shown to users as a KMZ), every hour, even if we're not moving at the time
				distributions.put(time, motionModelManager.createProbabilityDistribution(lpiId, time, areaData.getBoundingBox(), currentPoints));
			}
			
		}
		if (currentPoints.isEmpty()) {
			status.addWarning(this.name + ": Ran out of viable points. Look at results carefully!");
		}
		motionModelManager.sendInProgressUpdate(this.name + ": finished computing.", status);
	
		
		MotionModelResult result = new MotionModelResult();
		result.setLpiId(input.getLpiId());
		result.setInitialDistribution(distributions);
		result.copyInitialDistToDistWithSearches();
		result.setGeneratedTimestamp(System.currentTimeMillis());
		result.setGeneratingModelName(this.getName());
		return result;		
	}

	private void logSetOfPoints(List<LatLonGeo> currentPoints) {
		Set<LatLonGeo> points = new HashSet<>();
		points.addAll(currentPoints);
		LOGGER.debug("current points: {}", points);
		
	}

	@Override
	public MotionModelResult updateProbabilityDistributionForSearch(MotionModelResult latestResult, LandsarSearch search,
		SearchUtilities searchUtilities) {
		InternalModel internalModel = this.lpiData.get(latestResult.getLpiId());
		return null;
	}

	@Override
	public MotionModelResult cancelSearch(MotionModelResult latestResult, LandsarSearch search) {
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
				File dataFile = motionModelManager.getOrCreateFileForModelData(entry.getKey(), this.getName(), INTERNAL_MODEL_FILENAME);
				LOGGER.info("writing data to file: {}", dataFile);
				mapper.writeValue(dataFile, entry.getValue());
			} catch (IOException e) {
				LOGGER.error("Error saving {} for LPI ID: {}", INTERNAL_MODEL_FILENAME, entry.getKey(), e);
			}
		}
		this.started = false;
	}


	@Override
	public void restoreModelState(UUID lostPersonId, Map<String, File> tagToDataFile) {
		// LandSAR system restarted with saved LPIs - restore state my plugin expects to be in memory 
		
		if (tagToDataFile.containsKey(INTERNAL_MODEL_FILENAME)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				lpiData.put(lostPersonId, mapper.readValue(tagToDataFile.get(INTERNAL_MODEL_FILENAME), InternalModel.class));
			} catch (IOException e) {
				LOGGER.error("Error restoring internal model data for LPI {}", lostPersonId, e);
			}
		}
	}

}
