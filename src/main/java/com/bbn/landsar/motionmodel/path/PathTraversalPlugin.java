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

package com.bbn.landsar.motionmodel.path;

import com.bbn.landsar.MovementSchedule;
import com.bbn.landsar.geospatial.AreaData;
import com.bbn.roger.plugin.Plugin;

import java.util.Map;
/**
 * This plugin can be implemented and then paired with a PathGeneratorPlugin to support path-based motion models in LandSAR.
 */
public interface PathTraversalPlugin extends Plugin {

    /**
    //TODO the motionModelParameters here used to be the MovementModelParameters class for CORE OSPPRE models. Should capture those variables in the motion model plugin that calls this method
     */
    public Sample traverseRoute(Path path, long startTime, MovementSchedule schedule, AreaData areaData, Map<String, Object> motionModelParameters);

}
