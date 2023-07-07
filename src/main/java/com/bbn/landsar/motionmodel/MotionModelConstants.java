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

import java.util.HashSet;
import java.util.Set;

/**
 * Contains constants used for MotionModelParameters across multiple motion models
 * @author crock
 *
 */
public class MotionModelConstants {
	
	public static final String STAY_OUT_OF_WATER = "stay out of water";
	// todo add wandering params, isolated person parameters, goal-oriented params, Random, etc
	public static final String ISOLATED_PERSON_PARAMETERS = "isolated person parameters";
	public static final String GOAL_ORIENTED_PARAMETERS = "goal oriented parameters";
	public static final String RANDOM_SEED_PARAMETER = "random seed";
	
	
	private static final Set<MotionModelAttributeDescription> DEFAULT_MOTION_MODEL_PARAMS = new HashSet<>();
	
	static {
		DEFAULT_MOTION_MODEL_PARAMS.add(new MotionModelAttributeDescription(STAY_OUT_OF_WATER, "Will the person stay off/out of water-categorized landcover. "
				+ "For exapmle, a person may traverse water if lakes/rivers are frozen or known to be shallow.", true, Boolean.class, Unit.NONE));
		DEFAULT_MOTION_MODEL_PARAMS.add(new MotionModelAttributeDescription(ISOLATED_PERSON_PARAMETERS, 
				"This was an input used for all path based motion models", false, Object.class, Unit.NONE));
		DEFAULT_MOTION_MODEL_PARAMS.add(new MotionModelAttributeDescription(GOAL_ORIENTED_PARAMETERS, 
				"This was an input used for goal or destination focused path based motion models", false, Object.class, Unit.NONE));
		DEFAULT_MOTION_MODEL_PARAMS.add(new MotionModelAttributeDescription(RANDOM_SEED_PARAMETER, 
				"TODO refactor randomseeder over to SDK and document usage", false, long.class, Unit.NONE)); //TODO
	}
	
	public Set<MotionModelAttributeDescription> getDefaultMotionModelParameters() {
		
		return new HashSet<>(DEFAULT_MOTION_MODEL_PARAMS);
		
	}

}
