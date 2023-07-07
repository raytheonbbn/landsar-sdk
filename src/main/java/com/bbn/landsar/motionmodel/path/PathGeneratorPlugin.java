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

import java.util.Map;
import java.util.Random;

import com.bbn.landsar.geospatial.AreaData;
import com.bbn.roger.plugin.Plugin;
import com.metsci.glimpse.util.geo.LatLonGeo;

/**
 * This plugin can be implemented and then paired with a PathGeneratorPlugin to support path-based motion models in LandSAR.
 */
public interface PathGeneratorPlugin extends Plugin {

    /**
     * Needs to be thread-safe, should also add the groudmovement model that generated the path to the path
     * @param rs
     * @param landingPoint
     * TODO the motionModelParameters here used to be the MovementModelParameters class for CORE OSPPRE models. Should capture those variables in the motion model plugin that calls this method
     * @return
     */
    Path generatePath(Random rs, LatLonGeo landingPoint, AreaData areaData, Map<String, Object> motionModelParameters);
}
