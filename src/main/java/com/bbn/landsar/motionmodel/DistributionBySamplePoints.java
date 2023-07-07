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

import java.util.Collections;
import java.util.List;

import com.metsci.glimpse.util.geo.LatLonGeo;
/**
 * More fine-grained representation of lost person state than Probability Distribution or ContainmentMap. 
 */
public class DistributionBySamplePoints {

	final List<LatLonGeo> points;
	final List<Double> weights;
	
	public DistributionBySamplePoints(List<LatLonGeo> points, 
			List<Double> weights) {
		this.points = Collections.unmodifiableList(points);
		this.weights = Collections.unmodifiableList(weights);
	}

	public List<LatLonGeo> getPoints() {
		return points;
	}
	
	public List<Double> getWeights() {
		return weights;
	}
}
