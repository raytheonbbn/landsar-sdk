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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.bbn.landsar.geospatial.BoundingBox;
import com.bbn.landsar.utils.StatusUpdateMessage;
import com.metsci.glimpse.util.geo.LatLonGeo;

/**
 * Interface for utilities provided to MotionModel Plugins
 * @author crock
 *
 */
public interface MotionModelManager {

	/**
	 * @param status Already filled out statusUpdateMessage
	 */
	void sendStatusUpdateMessage(StatusUpdateMessage status);
	
	/**
	 * Adds the update to the provided status, sets the state of the StatusUpdateMessage to IN_PROGRESS, and sends that modified StatusUpdateMessage to the user. 
	 * @param update for the user
	 * @param status current status update message
	 * @return an updated StatusUpdateMessage (newer timestamp so that statuses appear in order to users) to use for the next update
	 */
	StatusUpdateMessage sendInProgressUpdate(String update, StatusUpdateMessage status);
	
	/**
	 * Creates a probability array (as used by Probability Distribution) from a List of points.
	 * Points outside the bounding box are ignored. 
	 * @param bbox
	 * @param pts
	 * @return
	 * @see com.bbn.landsar.motionmodel.ProbabilityDistribution
	 */
	default ProbabilityDistribution createProbabilityDistribution(UUID lpiId, long time, BoundingBox bbox, List<LatLonGeo> pts) {
		ProbabilityDistribution distribution = new ProbabilityDistribution(lpiId, time, createArrayFromPointsList(bbox, pts), bbox);
		return distribution;
	}
	
	/**
	 * Creates a probability array (as used by Probability Distribution) from a List of points and their weights
	 * Points outside the bounding box are ignored. 
	 * @param bbox
	 * @param pts
	 * @param pointWeights - relative weights of the points. Must be the same length as pts.
	 * @return
	 * @see com.bbn.landsar.motionmodel.ProbabilityDistribution
	 */
	default ProbabilityDistribution createProbabilityDistribution(UUID lpiId, long time, BoundingBox bbox, List<LatLonGeo> pts, List<Double> pointWeights) {
		if (pts == null || pointWeights == null || pts.size() != pointWeights.size()) {
			throw new IllegalArgumentException("pts and pointWeights must be the same size!");
		}
		ProbabilityDistribution distribution = new ProbabilityDistribution(lpiId, time, createArrayFromWeightedPointsList(bbox, pts, pointWeights), bbox);
		return distribution;
	}
	
	/**
	 * Creates an array that can be used as the data representation of a Probability Distribution
	 * @param bbox
	 * @param pts
	 * @return
	 * @see #createProbabilityDistribution
	 */
	default double[][] createArrayFromPointsList(BoundingBox bbox, List<LatLonGeo> pts) {
		final Double oneOfSize = 1.0/pts.size();
		List<Double> ptProbs = Collections.nCopies(pts.size(), oneOfSize);
		return createArrayFromWeightedPointsList(bbox, pts, ptProbs);
	}
	
	/**
	 * Creates an array that can be used as the data representation of a Probability Distribution
	 * @param bbox
	 * @param pts
	 * @return
	 * @see #createProbabilityDistribution
	 */
	double[][] createArrayFromWeightedPointsList(BoundingBox bbox, List<LatLonGeo> pts, List<Double> pointWeights);
	
	 /**
     * Gets the set of pluggable motion model names
     *
     * @return Set of motion model names
     */
    Set<String> getPluggableMotionModelNames();

    MotionModelPlugin getMotionModelPlugin(String name);


	/**
	 * Motion model plugins may call this to save their internal state across deployment shutdowns/restarts/upgrades
	 *
	 * <p>For Android deployments, this will be written out to the SD Card</p>
	 *
	 * @param lostPersonId - lost person UUID
	 * @param pluginName - name provided by Motion Model Creator
	 * @param tag - plugin specified data descriptor (used to differentiate between multiple stored objects for the same LPI)
	 *
	 * @throws IOException - if file could not be read or written to
	 */
	File getOrCreateFileForModelData(UUID lostPersonId, String pluginName, String tag) throws IOException;

	/**
	 * <pre>
	 * Loads file from relative path. This works in both Java/AWT and Android.
	 *
	 * For Java/AWT - this will load a file with the relative path of [name of plugin]/[relative/path]
	 * For Android - this will instantiate a file with relative path from SD Card (e.g. My/Relative/Path -> /path/to/sdcard/atak/osppre/[name of plugin]/[relative/path])
	 * </pre>
	 *
	 * @param motionModelPluginName - name of this motion model plugin
	 * @param relativePath - Relative Path for File to load
	 * @return File
	 *
	 * @throws FileNotFoundException, IOException - if file could not be loaded
	 */
	byte[] loadFileFromRelativePath(String motionModelPluginName, String relativePath) throws FileNotFoundException, IOException;

	/**
	 * <pre>
	 * Writes file to relative path. This works in both Java/AWT and Android.
	 *
	 * For Java/AWT - this will simply write a file to the relative path of [name of plugin]/[relative/path]
	 * For Android - this will write a File with relative path to SD Card (e.g. relative/path -> /path/to/sdcard/atak/osppre/[name of plugin]/[relative/path])
	 * </pre>
	 *
	 * @param contents - contents File to write
	 * @param relativePath - relativePath to write the File to
	 *
	 * @throws FileNotFoundException, IOException - if file could not be written to
	 */
	void writeFileToRelativePath(String motionModelPluginName, byte[] contents, String relativePath) throws FileNotFoundException, IOException;

	void appendToFile(String motionModelPluginName, String contents, String relativePath) throws FileNotFoundException, IOException;
}
