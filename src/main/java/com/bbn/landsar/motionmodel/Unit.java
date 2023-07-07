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

public enum Unit {
	KILOMETERS, // Imperial Equivalent is miles
	METERS, // Imperial Equivalent is yards
	CENTIMETERS, 
	DEGREES, 
	RADIANS,
	KILOMETERS_PER_HOUR, // Imperial Equivalent is yards per hour
	METERS_PER_SECOND, // Imperial Equivalent is feet per second
	CELSIUS, // Imperial Equivalent is Fahrenheit 
	KILOGRAMS,
	KNOTS, // doesn't really fit with metric or Imperial. Knot = one nautical mile per hour, exactly 1.852 km/h (approximately 1.151 mph or 0.514 m/s
	
	NONE // for attributes that don't involve units
}
