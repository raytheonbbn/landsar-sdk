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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import com.bbn.landsar.motionmodel.path.Path;

/**
 * Container object for Motion Model Output / Result
 * <ul>
 * <li>containmentMapping: initialDistribution and distributionWithSearches</li>
 * <li>paths (optional) If present, will be included in KML visualization</li>
 * <li>generatedTimeStamp - when the result was generated</li>
 * <li>lpiId - UUID for the lost person instance</li>
 * <li>generatingModelName</li> the name of the plugin that created this result (so it can be queried for updated distributions when we add searches)
 * </ul>
 *  
 * 
 * @author crock
 * Implementation Notes: 
 * use setters and getters so that we can do transformations if necessary due to refactoring in the future
 */
public class MotionModelResult {
	private static final Logger LOGGER = LoggerFactory.getLogger(MotionModelResult.class.getName());
	
	public MotionModelResult() {
		// default constructor
	}

	/**
	 * Containment mapping assuming no searches have occurred
	 * always required
	 */
	private Map<Long, ProbabilityDistribution> initialDistribution; 
	
	/**
	 * Containment mapping updated to represent distribution accounting for (assumed failed) searches
	 * this is expected to be equivalent to the initial distribution when there are no searches
	 */
	private Map<Long, ProbabilityDistribution> distributionWithSearches = new HashMap<>();
	
	/**
	 * "Sample paths", if used by the model (optional)
	 */
	protected List<Path> paths = new ArrayList<>();
	
	/**
	 * approximately when the result was generated
	 */
	protected long generatedTimestamp;
	
	/**
	 * Name of the plugin that generated this result
	 */
	protected String generatingModelName = "";
	
	/**
	 * Lost Person Instance ID
	 */
	protected UUID lpiId;

	public List<Path> getPaths() {
		return paths;
	}

	public void setPaths(List<Path> paths) {
		this.paths = paths;
	}

	public long getGeneratedTimestamp() {
		return generatedTimestamp;
	}

	public void setGeneratedTimestamp(long generatedTimestamp) {
		this.generatedTimestamp = generatedTimestamp;
	}

	public String getGeneratingModelName() {
		return generatingModelName;
	}

	public void setGeneratingModelName(String generatingModelName) {
		this.generatingModelName = generatingModelName;
	}

	public UUID getLpiId() {
		return lpiId;
	}

	public void setLpiId(UUID lpiId) {
		this.lpiId = lpiId;
	}

	public Map<Long, ProbabilityDistribution> getInitialDistribution() {
		return initialDistribution;
	}

	public void setInitialDistribution(Map<Long, ProbabilityDistribution> initialDistribution) {
		this.initialDistribution = initialDistribution;
	}

	public Map<Long, ProbabilityDistribution> getDistributionWithSearches() {
		return distributionWithSearches;
	}

	public void setDistributionWithSearches(Map<Long, ProbabilityDistribution> distributionWithSearches) {
		this.distributionWithSearches = distributionWithSearches;
	}
	
	/**
	 * Copy all fields except distributinoWithSearches into a new result object with the provided new Distribution With Searches
	 * @param newDistWithSearches
	 * @return
	 */
	public MotionModelResult copy(Map<Long, ProbabilityDistribution> newDistWithSearches) {
		MotionModelResult newResult = new MotionModelResult();
		
		HashMap<Long, ProbabilityDistribution> initialDistCopy = new HashMap<>();
		for (Entry<Long, ProbabilityDistribution> entry : initialDistribution.entrySet()) {
			initialDistCopy.put(entry.getKey(), entry.getValue().copy());
		}
		
		newResult.setDistributionWithSearches(newDistWithSearches);
		newResult.setInitialDistribution(initialDistCopy);
		newResult.setLpiId(this.lpiId);
		newResult.setGeneratedTimestamp(this.generatedTimestamp);
		if (this.paths != null) {
			newResult.setPaths(new ArrayList<>(paths));
		} else {
			newResult.setPaths(this.getPaths());
		}
		newResult.setGeneratingModelName(this.getGeneratingModelName());
		
		return newResult;
	}
	
	public MotionModelResult copy() {
		Map<Long, ProbabilityDistribution> distWithSearchesCopy = new HashMap<>();
		for (Entry<Long, ProbabilityDistribution> entry : distributionWithSearches.entrySet()) {
			distWithSearchesCopy.put(entry.getKey(), entry.getValue().copy());
		}
		return this.copy(distWithSearchesCopy);
	}
	
	/**
	 * The LandSAR Server calls this method to validate motion model results. It's a great method to call when testing a motion model plugin. 
	 * @return
	 */
	public ValidationInfo validate() {
		ValidationInfo validationInfo = new ValidationInfo();
		if (this.generatingModelName == null || this.generatingModelName.isEmpty()) {
			validationInfo.addError("Missing model name");
		}
		if (this.getLpiId() == null) {
			validationInfo.addError("Missing lpiId");
		}
		if (this.getInitialDistribution().isEmpty()) {
			validationInfo.addError("No initial distribution");
		} else {
			for (Entry<Long, ProbabilityDistribution> entry : initialDistribution.entrySet()) {
				ProbabilityDistribution distribution = entry.getValue();
				if (distribution == null) {
					validationInfo.addError("Null initial probability distribution for time: " + entry.getKey());
				}else {
					validationInfo.add(distribution.validate());
				}
			}
		}
		if (this.getDistributionWithSearches().isEmpty()) {
			validationInfo.addError("No distribution with searches");
		} else {
			for (Entry<Long, ProbabilityDistribution> entry : distributionWithSearches.entrySet()) {
				ProbabilityDistribution distribution = entry.getValue();
				if (distribution == null) {
					validationInfo.addError("Null probability distribution (with searches) for time: " + entry.getKey());
				}else {
					validationInfo.add(distribution.validate());
				}
			}
		}
		
		
		return validationInfo;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MotionModelResult [initialDistribution=");
		builder.append(initialDistribution);
		builder.append(", distributionWithSearches=");
		builder.append(distributionWithSearches);
		builder.append(", paths=");
		builder.append(paths);
		builder.append(", generatedTimestamp=");
		builder.append(generatedTimestamp);
		builder.append(", generatingModelName=");
		builder.append(generatingModelName);
		builder.append(", lpiId=");
		builder.append(lpiId);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Convenience method to copy the initial distribution as the distributionWithSearches, since we start with no searches, but we need distributionWithSearches to be set
	 */
	public void copyInitialDistToDistWithSearches() {
		Map<Long, ProbabilityDistribution> initialDist = this.getInitialDistribution();
		Map<Long, ProbabilityDistribution> distWithSearches = new HashMap<>();
		for (Entry<Long, ProbabilityDistribution> entry : initialDist.entrySet()) {
			distWithSearches.put(entry.getKey(), entry.getValue().copy());
		}
		this.setDistributionWithSearches(distWithSearches);
		
	}

}
