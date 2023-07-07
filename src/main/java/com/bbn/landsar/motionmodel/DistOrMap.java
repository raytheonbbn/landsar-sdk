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

import com.bbn.landsar.searchtheory.ContainmentMap;


/**
 * Holds either a DistributionBySamplePoints or a ContainmentMap
 * 
 * 
 * DistributionBySamplePoints is more precise (fine-grained); but Motion Models are only required
 * to produce a Containment Map, so in some scenarios that's all we have to work with. Either can be used to 
 * evaluate searches, but this class wraps that complexity so we don't need two methods for everything (one for the 
 * DistributionBySamplePoints and another for the ContainmentMap)
 * 
 * 
 * see Java Facade design pattern
 * @author crock
 *
 */
public class DistOrMap {

	
	public final DistributionBySamplePoints distribution;
	public final ContainmentMap map;
	
	
	public DistOrMap(DistributionBySamplePoints distribution) {
		this.distribution = distribution;
		this.map = null;
	}
	
	public DistOrMap(ContainmentMap map) {
		this.distribution = null;
		this.map = map;
	}
	


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DistOrMap [");
		if (distribution != null) {
			builder.append("distribution=");
			builder.append(distribution);
			builder.append(", ");
		}
		if (map != null) {
			builder.append("map=");
			builder.append(map);
		}
		builder.append("]");
		return builder.toString();
	}
}
