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

package com.bbn.landsar.motionmodel.example;

import com.bbn.landsar.motionmodel.InternalModel;


/**
 * Maintain Internal-to-the-Motion-Model State for each Lost Person Instance
 * @author crock
 *
 */
public class ExampleInternalModel extends InternalModel {
	private double direction;
	private double distanceKm;
	private double speed;

	public ExampleInternalModel(){
		// Json constructor
	}
	
	ExampleInternalModel(Double direction, Double distanceKm, Double speed) {
		this.direction=direction;
		this.setDistanceKm(distanceKm);
		this.setSpeed(speed);
	}


	public double getDirection() {
		return direction;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public double getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(double distanceKm) {
		this.distanceKm = distanceKm;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
}
