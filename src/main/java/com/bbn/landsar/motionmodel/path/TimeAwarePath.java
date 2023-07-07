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

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.bbn.landsar.motionmodel.ProbabilityDistribution;
import com.metsci.glimpse.util.geo.LatLonGeo;

public class TimeAwarePath extends Path {

	private List<Long> times;
	private List<LatLonGeo> points;

	public TimeAwarePath(List<LatLonGeo> anchorPoints, List<Long> times) {
		super(anchorPoints);
		this.times = times;
		this.points = anchorPoints;
	}

	public LatLonGeo pointAtTime(long time) {
		// TODO use a more efficient data structure so this look up is faster, and/or use interpolation (used in Motion Model SDK for getting ocean current value)
		long delta = Long.MAX_VALUE;
		long latestTime = Long.MIN_VALUE;
		
		// closest time after desired time
		Long bestTime = null;
		
		for (Long distTime: this.times) {
			if (distTime > latestTime) {
				latestTime = distTime;
			}
			long distDelta = distTime - time;
			if (distDelta > 0 && distDelta < delta) {
				delta = distDelta;
				bestTime = distTime;
			}
		}
		// maybe we want to create a map of time--> point in the constructor...
		// found closest time after desired time
		return points.get(times.indexOf(bestTime));
	}

}
