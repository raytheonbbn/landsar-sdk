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

/**
 * A vector created to represent a velocity in two dimensions (ignoring vertical / Z component) 
 * @author crock
 *
 */
public class Velocity2d {
	double east;
	double north;
	
	public static final Velocity2d UNKNOWN_VALUE = new Velocity2d(Double.NaN, Double.NaN);
	
	
	public Velocity2d(double east, double north) {
		this.east = east;
		this.north = north;
	}

	public double getEast() {
		return east;
	}

	public double getNorth() {
		return north;
	}
	
	/**
	 * Adds other to this and returns this, like v += other;
	 * @param other
	 * @return this (modified)
	 */
	public Velocity2d plusEquals(Velocity2d other) {
		this.east+= other.east;
		this.north+=other.north;
		return this;
	}
	
	/**
	 * multiplies each component by the scalar and returns a new Velocity2d
	 * @param factor
	 * @return new Velocity2d
	 */
	public Velocity2d scalarFactor(double factor) {
		return new Velocity2d(this.east*factor, this.north*factor);
	}
	
	/**
	 * <heading> (from KML documentation)
    		Rotation about the z axis (normal to the Earth's surface). A value of 0 (the default) equals North. 
    		A positive rotation is clockwise around the z axis and specified in degrees from 0 to 360.
    		
	 * @return
	 */
	public Double getHeading() {
		if (hasUnknownValue()) {
			// use the default heading value
			return 0d;
		}
		if (this.east == 0) {
			// 0 for north and 180 for south
			if (this.north >= 0.0) {
				return 0d;
			} else return 180.0;
		}
		else{
    		// see https://math.stackexchange.com/questions/2106751/how-to-convert-north-south-and-east-west-velocities-into-a-compass-heading-degre
			Double headingDeg = Math.toDegrees(Math.atan2(east, north));
			headingDeg = (headingDeg + 360.0) % 360.0;
			return headingDeg;
		}
	}
	
	/** 
	 * Implementation note: two velocity2ds may be unequal but both have an unknown value, if they each have an unknown value for different component vectors
	 * @return true if at least one of the component vectors of this Velocity2d is NaN. 
	 */
	public boolean hasUnknownValue() {
		return Double.isNaN(east) || Double.isNaN(north);
	}
	
	/**
	 * Returns magnitude of the vector. 
	 * Returns -1 if the vector contains an unknown value
	 */
	public Double getMagnitude() {
		if (hasUnknownValue()) {
			return -1d;
		}
		return Math.sqrt((east*east) + (north*north));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Velocity2d [east=");
		builder.append(east);
		builder.append(", north=");
		builder.append(north);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} 
		
		if (obj instanceof Velocity2d) {
			Velocity2d other = (Velocity2d) obj;					
			if (Double.isNaN(east) && Double.isNaN(north) && Double.isNaN(other.east) && Double.isNaN(other.north)) {
				return true;
			}
			else if (this.hasUnknownValue() && other.hasUnknownValue()) {
				// if they have the same unknown component vector and their other vector is equivalent, then it makes sense for them to be equal. 
				// I don't know how having one NaN component vector would happen, though -Colleen
				if (Double.isNaN(east) && Double.isNaN(other.east) && north == other.north) {
					return true; 
				}
				if (Double.isNaN(north) && Double.isNaN(other.north) && east == other.east) {
					return true; 
				}
				//In these two cases, we would get false with the below logic, because Double.NaN != Double.NaN
			}
			// regular case with known values
			return this.east == other.east && this.north == other.north;
		}
		return super.equals(obj);
	}
}
