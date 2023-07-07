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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.bbn.landsar.search.LandsarSearch;
import com.bbn.landsar.search.SearchUtilities;
import com.bbn.landsar.utils.StatusUpdateMessage;
import com.bbn.roger.config.AttributeDescription;
import com.bbn.roger.plugin.PluginContext;
import com.bbn.roger.plugin.exception.InsufficientConfigurationException;
import com.bbn.roger.plugin.exception.RogerInstantiationException;

public class MotionModelTestPlugin implements MotionModelPlugin {

	
	public MotionModelTestPlugin() {
		
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
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
	public MotionModelPluginMetadata getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMotionModelManager(MotionModelManager motionModelManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<MotionModelAttributeDescription> getMotionModelParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<GeospatialInputDescriptions> getMotionModelGeospatialDescriptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<AreaDataType> getRequiredAreaData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateMotionModelParameters(Map<String, Object> motionModelParameters,
			UserEnteredGeospatialData geospatialInputs, StatusUpdateMessage status) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MotionModelResult generateInitialDistribution(MotionModelInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double calculateProbabilityOfSuccess(UUID lostPersonId, Set<? extends LandsarSearch> searchesToInclude) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MotionModelResult updateProbabilityDistributionForSearch(MotionModelResult currentResult,
			LandsarSearch search, SearchUtilities searchUtilities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MotionModelResult cancelSearch(MotionModelResult currentResult, LandsarSearch search) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteModelState(List<UUID> lpisToDelete) {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreModelState(UUID lostPersonId, Map<String, File> tagToDataFile) {
		// TODO Auto-generated method stub

	}

}
