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
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.bbn.landsar.geospatial.BoundingBox;
import com.bbn.landsar.utils.StatusUpdateMessage;
import com.metsci.glimpse.util.geo.LatLonGeo;

public class TestMotionModelManager implements MotionModelManager {

	@Override
	public void sendStatusUpdateMessage(StatusUpdateMessage status) {
		System.out.println(status);

	}

	@Override
	public StatusUpdateMessage sendInProgressUpdate(String update, StatusUpdateMessage status) {
		System.out.println(status);
		return status;
	}

	@Override
	public double[][] createArrayFromWeightedPointsList(BoundingBox bbox, List<LatLonGeo> pts,
			List<Double> pointWeights) {
		//This is not implemented here
		return new double[0][0];
	}

	@Override
	public File getOrCreateFileForModelData(UUID lostPersonId, String pluginName, String tag) throws IOException {
		return File.createTempFile(tag, "");
	}

	@Override
	public Set<String> getPluggableMotionModelNames() {
		return new HashSet<>();
	}

	@Override
	public MotionModelPlugin getMotionModelPlugin(String name) {
		//not implemented
		return null;
	}

}
