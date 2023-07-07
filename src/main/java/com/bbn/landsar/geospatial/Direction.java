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

package com.bbn.landsar.geospatial;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit vectors representing each of the four cardinal directions (N, S, E, W)
 * and four ordinal directions (NE, NW, SE, SW).
 */
public enum Direction {
    NORTH("N", new Velocity2d(0, 1)),
    SOUTH("S", new Velocity2d(0, -1)),
    EAST("E", new Velocity2d(1, 0)),
    WEST("W", new Velocity2d(-1, 0)),
    NORTHEAST("NE", new Velocity2d(Math.sqrt(2)/2, Math.sqrt(2)/2)),
    NORTHWEST("NW", new Velocity2d(-Math.sqrt(2)/2, Math.sqrt(2)/2)),
    SOUTHEAST("SE", new Velocity2d(Math.sqrt(2)/2, -Math.sqrt(2)/2)),
    SOUTHWEST("SW", new Velocity2d(-Math.sqrt(2)/2, -Math.sqrt(2)/2));

    private final Velocity2d directionVector;
    private final String name;
    
    final static Map<String, Direction> lookupMap = new HashMap<>();

    Direction(String name, Velocity2d directionVector) {
        this.name = name;
        this.directionVector = directionVector;
    }

    public Velocity2d getDirectionVector() {
        return directionVector;
    }

    public String getName() {
        return name;
    }
    
    static {
    	lookupMap.put(Direction.NORTH.getName(), Direction.NORTH);
    	lookupMap.put(Direction.SOUTH.getName(), Direction.SOUTH);
    	lookupMap.put(Direction.EAST.getName(), Direction.EAST);
    	lookupMap.put(Direction.WEST.getName(), Direction.WEST);
    	lookupMap.put(Direction.NORTHEAST.getName(), Direction.NORTHEAST);
    	lookupMap.put(Direction.NORTHWEST.getName(), Direction.NORTHWEST);
    	lookupMap.put(Direction.SOUTHEAST.getName(), Direction.SOUTHEAST);
    	lookupMap.put(Direction.SOUTHWEST.getName(), Direction.SOUTHWEST);
    }
    
    public static Direction getDirectionFromString(String str) {
    	try {
    		return Direction.valueOf(str);
    	} catch (IllegalArgumentException e) {
    		if (str != null) {
        		Direction dir = lookupMap.get(str.toUpperCase());
        		if (dir != null) {
        			return dir;
        		}
    		}
    		throw e;
    	}
    }
}
