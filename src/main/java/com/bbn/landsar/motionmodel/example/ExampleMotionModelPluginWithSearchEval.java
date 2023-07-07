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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.MovementSchedule;
import com.bbn.landsar.geospatial.AreaData;
import com.bbn.landsar.geospatial.BoundingBox;
import com.bbn.landsar.geospatial.Direction;
import com.bbn.landsar.motionmodel.DistOrMap;
import com.bbn.landsar.motionmodel.DistributionBySamplePoints;
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
import com.bbn.landsar.motionmodel.path.Path;
import com.bbn.landsar.motionmodel.path.Sample;
import com.bbn.landsar.motionmodel.path.SampleBuilder;
import com.bbn.landsar.search.LandsarSearch;
import com.bbn.landsar.search.SearchUtilities;
import com.bbn.landsar.utils.DateTimeUtilities;
import com.bbn.landsar.utils.StatusUpdateMessage;
import com.bbn.roger.annotation.Plugin;
import com.bbn.roger.config.AttributeDescription;
import com.bbn.roger.plugin.PluginContext;
import com.bbn.roger.plugin.exception.InsufficientConfigurationException;
import com.bbn.roger.plugin.exception.RogerInstantiationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metsci.glimpse.util.geo.LatLonGeo;

/**
 * This motion model plugin implements search-related methods
 * calcDistributionWithSearches, updateProbabilityDistributionForSearch, cancelSearch, and calculateProbabilityOfSuccess
 * @author crock
 *
 */
@Plugin
public class ExampleMotionModelPluginWithSearchEval implements LandMotionModelPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMotionModelPluginWithSearchEval.class);

	// Traditionally, the Sample class has used a time-delta of 5 minutes as part of its internal representation a Sample Path as traversed over time.
	public static final long TIME_DELTA = 60*DateTimeUtilities.millisecInMin;
	
    private static final Set<MotionModelAttributeDescription> PER_LPI_ATTRS = new HashSet<>();
	private static final Set<GeospatialInputDescriptions> PER_LPI_GEOSPATIAL_ATTRS = new HashSet<>();
	
    public static final String DISTANCE = "distance";
    public static final String DIRECTION = "direction";
    public static final String SPEED = "speed";

    /**
     * Motion Model Manager provides access to utility methods
     */
    private MotionModelManager motionModelManager;
    
    private String name = ""; // name will be set by Motion Model Manager via setter
    
    /**
     * Motion Model Manager has per-LPI locking, but my Motion Model Plugin may be called to create / update two different LPIs at once
     */
	private Map<UUID, ExampleInternalModel> lpiData = new ConcurrentHashMap<>();
	
	
	
	final String INTERNAL_MODEL_FILENAME = "internal_model.json";

	private boolean started;
    
	static {
		PER_LPI_ATTRS.add(new MotionModelAttributeDescription(DISTANCE,
				"distance person might travel", true, Double.class, Unit.KILOMETERS));
		PER_LPI_ATTRS.add(new MotionModelAttributeDescription(DIRECTION,
				"direction (Choose from: N, S, E, W, NE, NW, SE, SW) person was believed to be going", true, String.class, Unit.NONE));
		
		PER_LPI_ATTRS.add(new MotionModelAttributeDescription(SPEED,
				"average speed", true, Double.class, Unit.KILOMETERS_PER_HOUR));

		// adding this here (not required) will still give the user the option to enter in this data type
		PER_LPI_GEOSPATIAL_ATTRS.add(new GeospatialInputDescriptions(UserEnteredGeospatialData.GeospatialDataType.EXCLUSION_ZONE,
				false));
	}
	
	@Override
	public void configure(Map<String, Object> configurationOptions, PluginContext context)
			throws InsufficientConfigurationException {
		// read in anything configured in the config.json file (deployment-time configuration)

	}

	@Override
	public Set<AttributeDescription> getConfigurationAttributes() {
		// if we used deployment-time configuration, we should document that here
		return new HashSet<>();
	}
	
	@Override
	public void initialize() throws RogerInstantiationException {
		// No initialization for this plugin

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


		String directionStr = (String) motionModelParameters.get(DIRECTION);
		Double speed = (Double) motionModelParameters.get(SPEED);
		Double distance = (Double) motionModelParameters.get(DISTANCE);
		
		boolean anyErrors = false;
		if (distance < 0.0) {
			status.addError("invalid parameter", String.format("invalid value for '%s'. Value: %1$,.2f", DISTANCE, distance));
			anyErrors = true;
		}
		if (directionStr == null) {
			status.addError("invalid parameter", String.format("invalid value for '%s'. Value: '%s'", DIRECTION, directionStr));
			anyErrors = true;
		}else {
			try {
				Direction.getDirectionFromString(directionStr);
			} catch (IllegalArgumentException e) {
				status.addError("invalid parameter", String.format("invalid value for '%s'. Value: '%s', Valid Values: %s %s", DIRECTION, directionStr,
						Arrays.toString(Direction.values()), "and their common abbreviations"));
				anyErrors = true;
			}
		}
		
		if (speed == null || speed <= 0.0) {
			status.addError("invalid parameter", String.format("invalid value for '%s'. Value: %1$,.2f", SPEED, speed));
			anyErrors = true;
		}

		if (anyErrors) {
			// send statusUpdateMessage with all validation errors if there are any
			motionModelManager.sendStatusUpdateMessage(status);
		}		
		
		return !anyErrors;
	}

	/**
	 * 
	 * Create a new Model for a new Lost Person Instance and return the initial distribution (over time), 
	 * mapping time to the distribution over the bounding box at that time. 
	 * @param motionModelInput The MotionModelInput object is a collection of input parameters from the user as well as downloaded geospatial data and reference object(s), like the StatusUpdateMessage. It includes the following:
	 * <ul>
	 *   <li>lpiId - a unique identifier for this Lost Person Instance. This id is referenced when calling calculateProbabilityOfSuccess, updateProbabilityDistributionForSearch, cancelSearch, deleteModelState.</li>
	 *   <li>geospatialInputs: geospatial inputs entered by the user, including RendezvousPoints and ExclusionZones</li>
	 * 	 <li>areaData: geospatial data downloaded by the LandSAR Server over the <b>bounding box</b> area of interest. The areaData object has a getter for the bounding box, as well. The AreaData should contain types specified by {@link #getRequiredAreaData()}.
	 *	 <li>motionModelParameters - values of parameters for this LPI. Prior to calling this method, {@link #validateMotionModelParameters(Map, StatusUpdateMessage)} will be called with the parameters.</li>
	 * 	 <li>startTime - The time (in Unix/Epoch Time) the person was last seen or estimated to be located at the startTimeDistribution.<li>
	 * 	 <li>startTimeDistribution - The Last Known Point of the lost person. This may be a single point or a distribution based on user-entered data.</li>
	 * 	 <li>StatusUpdateMessage status - The Status Update Message can be used to send information back to the LandSAR user about the progress of modeling and computation, since this method may be compute/time intensive.</li>
	 * </ul>
	 * 
	 * @return MotionModelResult containing an Initial Distribution, Distribution With Searches, LPI ID, and name of this model
	 * - Initial Distribution: The estimated location of the lost person before considering any searches. This "initial distribution" is realized as a set of geographic distributions modeled over discrete times. The geographic distributions should be constrained to the provided Bounding Box. 
	 * The times included in any distribution should be hourly, starting at the startTime, and ending when the model anticipates no further movement within the bounding box. 
	 * 
	 */
	public MotionModelResult generateInitialDistribution(MotionModelInput motionModelInput) {
		// grab parameters that used to be passed into other method
		UUID lpiId = motionModelInput.getLpiId();
		UserEnteredGeospatialData geospatialInputs = motionModelInput.getGeospatialInputs();
		AreaData areaData = motionModelInput.getAreaData();
		Map<String, Object> motionModelParameters = motionModelInput.getMotionModelParameters();
		MovementSchedule schedule = motionModelInput.getMovementSchedule();
		long startTime = motionModelInput.getStartTime();
		List<LatLonGeo> startTimeDistribution = motionModelInput.getStartTimeDistribution();
		StatusUpdateMessage status = motionModelInput.getStatus();
		
		// get general parameter(s) provided to all motion models
		Boolean stayOutOfWater = (Boolean) motionModelParameters.get(MotionModelConstants.STAY_OUT_OF_WATER);

		// Read in Model-Specific parameters (these values are set by the user when creating a new Lost Person Instance and selecting this motion model)
		// already validated direction, distance, speed in validateMotionModelParameters method
		String directionStr = (String) motionModelParameters.get(DIRECTION);
		Double speed = (Double) motionModelParameters.get(SPEED);
		Double distanceKm = (Double) motionModelParameters.get(DISTANCE);
		
		Direction directionOfMovement = Direction.getDirectionFromString(directionStr);
		// Create an object internal to my plugin implementation to store data my motion model plugin uses. 
		// InternalModel is our example model in an example representation. This could be a ML model or set of Paths for the traditional statistical models. 
		// If you don't have paths, you need some way to track how a decrease in probability over an area at one time (due to a failed search) would impact your probability distributions at future times. 
		
		ExampleInternalModel modelForThisLPI = new ExampleInternalModel(directionOfMovement.getDirectionVector().getHeading(), distanceKm, speed);

		lpiData.put(lpiId, modelForThisLPI);
		
		// could change this in the future, so use a variable. It does need to evenly divide an hour, though. 
		final long timeDelta = TIME_DELTA;
		List<SampleBuilder> samplePathsInProgress = startTimeDistribution.stream().map(point -> new SampleBuilder(startTime, timeDelta, point, directionOfMovement.getDirectionVector(), distanceKm, speed)).collect(Collectors.toList());
		
		List<Sample> samplePaths = new ArrayList<>();
		// build up our internal model
		long time = startTime;
		while (!samplePathsInProgress.isEmpty()) {
			// All motion models should create distributions for each hour, starting from the start time  
			// "Sample" class uses 5-minute time resolution for internal model
			time += timeDelta;
			
			for (SampleBuilder modelBuilder : samplePathsInProgress) {
				modelBuilder.computeNextPoint(time, schedule, areaData, geospatialInputs, stayOutOfWater);
			}

			Iterator<SampleBuilder> modelIterator = samplePathsInProgress.iterator();
			while (modelIterator.hasNext()) {
				SampleBuilder modelBuilder = modelIterator.next();
				if (modelBuilder.reachedLogicalEnd(time)) {
					Sample sample = modelBuilder.build();
					samplePaths.add(sample);
					modelIterator.remove();
				}
			}			
		}
		// here, all of the sample paths have "reached a logical end"
		modelForThisLPI.setSamples(samplePaths);
		
		// create list of distributions to return
		Map<Long, ProbabilityDistribution> distributions = new HashMap<>();
		
		// add to time-based distribution (which will be shown to users as a KMZ), every hour
		for (long timeAgain = startTime; timeAgain <= modelForThisLPI.endTime(); timeAgain += DateTimeUtilities.millisecInHour) {
			distributions.put(timeAgain, motionModelManager.createProbabilityDistribution(lpiId, timeAgain, areaData.getBoundingBox(), modelForThisLPI.getLocationsAtTime(timeAgain)));
		}

		motionModelManager.sendInProgressUpdate(this.name + ": finished computing.", status);
		// build the result object
		MotionModelResult result = new MotionModelResult();
		result.setLpiId(motionModelInput.getLpiId());
		result.setInitialDistribution(distributions);
		result.copyInitialDistToDistWithSearches();
		// paths are optional, but if present, will be displayed in the UI
		List<Path> paths = samplePaths.stream().map(sample -> new Path(sample.getPoints())).collect(Collectors.toList());
		result.setPaths(paths);
		result.setGeneratedTimestamp(System.currentTimeMillis());
		result.setGeneratingModelName(this.getName());
		return result;	
	}

	/**
	 * Historically, searches have been evaluated at the start time of the search
	 * a DistributionBySamplePoints was created from Path-based models, using interpolation to determine where along each path the IP would be at the 
	 * specified time 
	 */
	@Override
	public DistOrMap calcDistributionWithSearches(MotionModelResult result, long time) {
		 ExampleInternalModel internalModel = this.lpiData.get(result.getLpiId());
		 // return a Distribution which is more precise than the ContainmentMap
		 return new DistOrMap(internalModel.updateDistributionWithSearches(Collections.singleton(time)).get(time));
	}


	@Override
	public MotionModelResult updateProbabilityDistributionForSearch(MotionModelResult latestResult, LandsarSearch search,
		SearchUtilities searchUtilities) {
		ExampleInternalModel internalModel = this.lpiData.get(latestResult.getLpiId());
		List<Double> pathPds = searchUtilities
			.getSampleProbabilityDistributions(internalModel.getSamplePaths(), search);
		Map<Long, DistributionBySamplePoints> samplePointDist = internalModel.addSearch(latestResult, search, pathPds);
		Map<Long, ProbabilityDistribution> probDistMap = convertToProbabilityDistMap(latestResult, samplePointDist);
		MotionModelResult newResult = latestResult.copy(probDistMap);
		newResult.setGeneratedTimestamp(System.currentTimeMillis());
		return newResult;

	}

	/**
	 * for each time step, convert the model's representation of state (samplePointDist) to Motion Model SDK compliant ProbabilityDistribution
	 * @param latestResult
	 * @param samplePointDist
	 * @return
	 */
	private Map<Long, ProbabilityDistribution> convertToProbabilityDistMap(MotionModelResult latestResult,
			Map<Long, DistributionBySamplePoints> samplePointDist) {
		long aValidTime = latestResult.getInitialDistribution().keySet().iterator().next();
		ProbabilityDistribution aDistribution = latestResult.getInitialDistribution().get(aValidTime);
		BoundingBox bbox = aDistribution.getBoundingBox();
		Map<Long, ProbabilityDistribution> probDistMap = new HashMap<>();
		for (Entry<Long, DistributionBySamplePoints> entry : samplePointDist.entrySet()) {
			DistributionBySamplePoints pointDist = entry.getValue();
			probDistMap.put(entry.getKey(), this.motionModelManager.createProbabilityDistribution(latestResult.getLpiId(), entry.getKey(), 
					bbox, pointDist.getPoints(), pointDist.getWeights()));
		}
		return probDistMap;
	}

	@Override
	public MotionModelResult cancelSearch(MotionModelResult latestResult, LandsarSearch search) {
		ExampleInternalModel internalModel = this.lpiData.get(latestResult.getLpiId());
		Map<Long, DistributionBySamplePoints> samplePointDist = internalModel.removeSearch(latestResult, search);
		Map<Long, ProbabilityDistribution> probDistMap = convertToProbabilityDistMap(latestResult, samplePointDist);
		
		MotionModelResult newResult = latestResult.copy(probDistMap);
		newResult.setGeneratedTimestamp(System.currentTimeMillis());
		return newResult;
	}
	
	// "calculate cumulativePd"
	// the LandSAR system is unaware of how success (or lack thereof) of Search A at location A_loc at time A_time effects the 
	// probability of containment for location B_loc at time B_time, which is needed to calculate the overall probability of success for search B
	@Override
	public double calculateProbabilityOfSuccess(UUID lostPersonId, Set<? extends LandsarSearch> searchesToInclude) {
		ExampleInternalModel internalModel = this.lpiData.get(lostPersonId);
		return internalModel.calcCumulativePd(searchesToInclude);
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
		for (Entry<UUID, ExampleInternalModel> entry : lpiData.entrySet()) {
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
		LOGGER.info("Restoring state for LPI ID={}, file info={}", lostPersonId, tagToDataFile);
		if (tagToDataFile.containsKey(INTERNAL_MODEL_FILENAME)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				lpiData.put(lostPersonId, mapper.readValue(tagToDataFile.get(INTERNAL_MODEL_FILENAME), ExampleInternalModel.class));
			} catch (IOException e) {
				LOGGER.error("Error restoring internal model data for LPI {}", lostPersonId, e);
			}
		}
	}

}
