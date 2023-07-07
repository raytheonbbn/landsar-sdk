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

import org.junit.Test;

import com.bbn.landsar.geospatial.BoundingBox;
import com.bbn.landsar.searchtheory.ContainmentMap;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MotionModelsSDKTest {
    @Test 
    public void aTest() {
        assertTrue("true should be 'true'", true);
    }
   
    
    // Test the default implementation of calcDistributionWithSearches
    @Test
    public void testInterpolation() {
    	MotionModelPlugin testPlugin = new MotionModelTestPlugin();
    	final long testTime = 1673808478549l;
    	long resultTime1 = testTime - 1000l;
    	long resultTime2 = testTime + 1000l;
    	
    	Map<Long, ProbabilityDistribution> initialDistribution = new HashMap<>();
        
    	// this is a park in Canada
    	final BoundingBox testBoundingBox = new BoundingBox(53.98, 53.96, -106.39, -106.42);
    	UUID lpiId = UUID.randomUUID();
    	
    	
    	int[] numLonNumLat = ContainmentMap.determineNumLonAndNumLat(testBoundingBox);
    	
    	System.out.println(Arrays.toString(numLonNumLat));
    	
    	initialDistribution.put(resultTime1, new ProbabilityDistribution(lpiId, resultTime1, new double[][] {
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1},
			{0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1}}, testBoundingBox));
    	initialDistribution.put(resultTime2, new ProbabilityDistribution(lpiId, resultTime2,  new double[][] {
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},
			{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0}}, testBoundingBox));
    			
		MotionModelResult testMotionModelResult = new MotionModelResult();
    	testMotionModelResult.setLpiId(lpiId);
    	testMotionModelResult.setGeneratingModelName("testModel");
    	testMotionModelResult.setInitialDistribution(initialDistribution);
    	testMotionModelResult.copyInitialDistToDistWithSearches();
    	
    	// Validate the input we built
    	ValidationInfo validationInput = testMotionModelResult.validate();
    	assertTrue(validationInput.errors.toString(), validationInput.isValid());
    	
    	// This method is the one we're testing 
    	DistOrMap distOrMap = testPlugin.calcDistributionWithSearches(testMotionModelResult, testTime);
    	double[][] averageDist = distOrMap.map.getCellProbs();
    	
    	for (double[] array : averageDist) {
    		System.out.println(Arrays.toString(array));
    	}
    	
    	ValidationInfo validationOutput = ((ProbabilityDistribution) distOrMap.map).validate();
    	assertTrue(validationOutput.errors.toString(), validationOutput.isValid());
    	assertEquals(0.05, (averageDist[0][0]), 0.00001);
    	assertEquals(0.55, (averageDist[5][4]), 0.00001);
    	assertTrue(testBoundingBox.equals(distOrMap.map.getBoundingBox()));
    }
}