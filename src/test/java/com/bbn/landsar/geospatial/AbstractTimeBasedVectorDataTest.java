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
package com.bbn.landsar.geospatial;

import com.bbn.landsar.motionmodel.AreaDataType;
import org.junit.Assert;
import org.junit.Test;

import com.metsci.glimpse.util.geo.LatLonGeo;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.TreeMap;

public class AbstractTimeBasedVectorDataTest {
	
 	class TestDataSet extends AbstractTimeBasedVectorData {

 		TestDataSet(NavigableMap<Long, Velocity2d[][]>testDataByTime){
 			this.timeBasedData = testDataByTime;
 			this.minTime = testDataByTime.firstKey();
 			this.maxTime = testDataByTime.lastKey();
 		}
 		
		@Override
		public Velocity2d[][] getData(long time) {
			return super.interpolateTime(time);
		}

		@Override
		public String getDataType() {
			return AreaDataType.CURRENTS;
		}

		@Override
		public void writeFiles(File directory) {
			//TestDataset does not support re-loading from disk			
		}
 		
 	}
	
 	@Test
 	public void testVelocity2dObjectEquals() {
 		assertTrue(new Velocity2d(1, 1).equals(new Velocity2d(1, 1)));
 	}
	
 	
 	@Test
 	public void testBoundingBoxDoesNotContainNaNGeoPoint() {
 		// passing a lat lon geo to the interpolation method with NaN values breaks it. A reasonable way for motion models to exclude invalid points is checking if they are in the bounding box. 
 		// an undefined point (with NaN values) isn't in the box
 		BoundingBox bbox = new BoundingBox(42.5, 41.346, -115.430982, -117.09873);
 		assertFalse(bbox.contains(LatLonGeo.fromDeg(42.1, Double.NaN)));
 		assertFalse(bbox.contains(LatLonGeo.fromDeg(Double.NaN, -116.0)));
 		assertFalse(bbox.contains(LatLonGeo.fromDeg(Double.NaN, Double.NaN)));
 		assertTrue(bbox.contains(LatLonGeo.fromDeg(42.1, -116.0)));
 	}
	
    @Test 
    public void testInterpolation() {
    	
    	Velocity2d[][] testDataTime2 = new Velocity2d[][]{{new Velocity2d(1,1), new Velocity2d(1, 1), new Velocity2d(3, 3)}, 
    											     {new Velocity2d(0,2), new Velocity2d(0, 2), new Velocity2d(3, 3)}
    											  };
    	Velocity2d[][] testDataTime5 = new Velocity2d[][]{{new Velocity2d(1,1), new Velocity2d(1, 1), new Velocity2d(10, 10)}, 
     											     {new Velocity2d(0,5), new Velocity2d(0, 5), new Velocity2d(10, 10)}
     											  };
    	
	    Velocity2d[][] testDataTime2Copy = new Velocity2d[2][3];
	    testDataTime2Copy[0] = Arrays.copyOfRange(testDataTime2[0], 0, testDataTime2[0].length);
	    testDataTime2Copy[1] = Arrays.copyOfRange(testDataTime2[1], 0, testDataTime2[1].length);
     	assertTrue("Data for time 2 should not be modified", Arrays.equals(testDataTime2Copy[0], testDataTime2[0]));
	    								  
     	NavigableMap<Long, Velocity2d[][]>testDataByTime = new TreeMap<Long, Velocity2d[][]>();
     	testDataByTime.put(2l, testDataTime2);
     	testDataByTime.put(5l, testDataTime5);
     	TestDataSet testDataSet = new TestDataSet(testDataByTime);
     	
     	
     	Velocity2d[][] dataForTime4 = testDataSet.getData(4l);
     	// per comment in AbstractTimeBasedVectorData, weight for Time5 data should be 2/3, weight for Time2 data should be 1/3
     	// if we look at the [0][2] entry, for example, we'd expect: 
     	// 1/3 * 3 + 2/3 * 10 = 1 + 6.66666 = 7.66666 (for both north and east)
     	double sevenAndTwoThirds = (double) 7 + (double)2/(double) 3;
     	Velocity2d velocity02 = dataForTime4[0][2];
     	Assert.assertEquals(sevenAndTwoThirds, velocity02.east, .00001);
     	Assert.assertEquals(sevenAndTwoThirds, velocity02.north, .00001);
     	
     	  	
//     	System.out.println(Arrays.toString(dataForTime4[0]));
//     	System.out.println(Arrays.toString(dataForTime4[1]));
     	
     	// make sure we didn't change the data for time 2
     	// Arrays.equals seems to have trouble with a 2D array, but deepEquals works
     	assertTrue(Arrays.deepEquals(testDataTime2Copy, testDataTime2));

    }
    
    @Test
    public void testVelocity2d_UnknownValue() {
    	assertTrue(Velocity2d.UNKNOWN_VALUE.equals(Velocity2d.UNKNOWN_VALUE));
    	assertTrue(Velocity2d.UNKNOWN_VALUE.equals(new Velocity2d(Double.NaN, Double.NaN)));
    }
    
}
