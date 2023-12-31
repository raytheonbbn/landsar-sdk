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

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.bbn.landsar.motionmodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.bbn.landsar.motionmodel.example.ExampleMotionModelPluginWithSearchEval;
import com.bbn.landsar.utils.StatusUpdateMessage;

public class ExamplePluginTest {
  
    
    @Test
    public void testNotValidDirection() {
    	MotionModelPlugin plugin = new ExampleMotionModelPluginWithSearchEval();
    	plugin.setMotionModelManager(new TestMotionModelManager());
    	Map<String, Object> parameters = new HashMap<>();
    	parameters.put(ExampleMotionModelPluginWithSearchEval.DIRECTION, "notAValidDirection");
    	parameters.put(ExampleMotionModelPluginWithSearchEval.DISTANCE, 90000d);
    	parameters.put(ExampleMotionModelPluginWithSearchEval.SPEED, 2d);
		boolean validParams = plugin.validateMotionModelParameters(parameters, 
    			new UserEnteredGeospatialData(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
    			new StatusUpdateMessage());
		assertFalse(validParams);
    }
    
    @Test
    public void testValidDirection() {
    	MotionModelPlugin plugin = new ExampleMotionModelPluginWithSearchEval();
    	plugin.setMotionModelManager(new TestMotionModelManager());
    	Map<String, Object> parameters = new HashMap<>();
    	parameters.put(ExampleMotionModelPluginWithSearchEval.DIRECTION, "n");
    	parameters.put(ExampleMotionModelPluginWithSearchEval.DISTANCE, 90000d);
    	parameters.put(ExampleMotionModelPluginWithSearchEval.SPEED, 2d);
		boolean validParams = plugin.validateMotionModelParameters(parameters, 
    			new UserEnteredGeospatialData(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
    			new StatusUpdateMessage());
		assertTrue(validParams);
    }
}
