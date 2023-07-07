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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bbn.landsar.MovementSchedule;
import com.bbn.landsar.geospatial.AreaData;
import com.bbn.landsar.utils.StatusUpdateMessage;
import com.metsci.glimpse.util.geo.LatLonGeo;



/**
 * Container object for Motion Model Inputs
 * 
 * <ul>
 * <li>lpiId</li>
 * <li>areaData geospatial data downloaded by the system (Elevation, Landcover, Currents)</li>
 * <li>geospatialInputs Rendezvous Points, Exclusion Zones, etc. entered by user</li>
 * <li>motionModelParameters per-LPI custom parameters for the plugin, entered by the user</li>
 * <li>startTime - time last seen</li>
 * <li>startTimeDistribution - The Last Known Point(s) of the lost person.</li>
 * <li>movementSchedule - The Movement Schedule represents when the lost person is expected to be moving.</li>
 * <li>status - object used to report status back to user as computation progresses</li>
 * </ul>
 * 
 * @author crock
 * Implementation Notes: 
 * use setters and getters so that we can do transformations if necessary due to refactoring in the future
 */
public class MotionModelInput {
	public MotionModelInput(UUID lpiId, UserEnteredGeospatialData geospatialInputs, AreaData areaData,
			Map<String, Object> motionModelParameters, MovementSchedule movementSchedule, long startTime, List<LatLonGeo> startTimeDistribution,
			StatusUpdateMessage status) {
		super();
		this.lpiId = lpiId;
		this.geospatialInputs = geospatialInputs;
		this.areaData = areaData;
		this.motionModelParameters = motionModelParameters;
		this.movementSchedule = movementSchedule;
		this.startTime = startTime;
		this.startTimeDistribution = startTimeDistribution;
		this.status = status;
	}

	/**
	 * a unique identifier for this Lost Person Instance. This id is referenced when calling calculateProbabilityOfSuccess, updateProbabilityDistributionForSearch, cancelSearch, deleteModelState. 
	 */
	protected UUID lpiId;
	/**
	 * geospatial inputs entered by the user, including RendezvousPoints and ExclusionZones
	 */
	protected UserEnteredGeospatialData geospatialInputs;
	
	protected AreaData areaData;
	/**
	 * 
	 * values of parameters for this LPI. Prior to calling the {@link MotionModelPlugin#generateInitialDistribution()} method, 
	 * {@link #validateMotionModelParameters(Map, StatusUpdateMessage)} will be called with the parameters.
	 */
	protected Map<String, Object> motionModelParameters;
	/**
	 * The time (in Unix/Epoch Time) the person was last seen or estimated to be located at the startTimeDistribution. 
	 * @see https://www.epochconverter.com/
	 */
	protected long startTime;
	/**
	 * 
	 * The Last Known Point of the lost person. This may be a single point or a distribution based on user-entered data. 
	 */
	protected List<LatLonGeo> startTimeDistribution;
	/**
	 *  The Status Update Message can be used to send information back to the LandSAR user about the progress of modeling and computation, since 
	 *  //TODO not this method this method may be compute/time intensive. 
	 */
	protected StatusUpdateMessage status;
	
	/**
	 * The Movement Schedule represents when the lost person is expected to be moving. Most current movement schedules are the same for any 24-hour period, but this interface allows the Movement Schedules and types of Movement Schedules to be extended. 
	 * 
	 */
	protected MovementSchedule movementSchedule;

	public MotionModelInput(UUID lpiId, UserEnteredGeospatialData geospatialInputs, AreaData areaData,
			Map<String, Object> motionModelParameters, long startTime, List<LatLonGeo> startTimeDistribution,
			StatusUpdateMessage status) {
		this.lpiId = lpiId;
		this.geospatialInputs = geospatialInputs;
		this.areaData = areaData;
		this.motionModelParameters = motionModelParameters;
		this.startTime = startTime;
		this.startTimeDistribution = startTimeDistribution;
		this.status = status;
	}

	public UUID getLpiId() {
		return lpiId;
	}

	public void setLpiId(UUID lpiId) {
		this.lpiId = lpiId;
	}

	public UserEnteredGeospatialData getGeospatialInputs() {
		return geospatialInputs;
	}

	public void setGeospatialInputs(UserEnteredGeospatialData geospatialInputs) {
		this.geospatialInputs = geospatialInputs;
	}

	public AreaData getAreaData() {
		return areaData;
	}

	public void setAreaData(AreaData areaData) {
		this.areaData = areaData;
	}

	public Map<String, Object> getMotionModelParameters() {
		return motionModelParameters;
	}

	public void setMotionModelParameters(Map<String, Object> motionModelParameters) {
		this.motionModelParameters = motionModelParameters;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public List<LatLonGeo> getStartTimeDistribution() {
		return startTimeDistribution;
	}

	public void setStartTimeDistribution(List<LatLonGeo> startTimeDistribution) {
		this.startTimeDistribution = startTimeDistribution;
	}

	public StatusUpdateMessage getStatus() {
		return status;
	}

	public void setStatus(StatusUpdateMessage status) {
		this.status = status;
	}

	public MovementSchedule getMovementSchedule() {
		return this.movementSchedule;
	}

	public void setMovementSchedule(MovementSchedule movementSchedule) {
		this.movementSchedule = movementSchedule;
	}
}
