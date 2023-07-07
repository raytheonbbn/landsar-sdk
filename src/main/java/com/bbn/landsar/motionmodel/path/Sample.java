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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.bbn.landsar.utils.LandsarUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metsci.glimpse.util.geo.LatLonGeo;

/**
 * Would "SamplePath" be a more accurate name for this class? 
 *<br>
 *A Note on Sample Path Generation: 
 *<ol>
 *<li>OSPPRE first generates the 5k Paths, each of which has an ordered list of locations,
 * without times associated, based on a Movement Model and geo-data</li>
 * <li>Each Path is traversed (on an IpTraversalThread) according to the Path, 
 * an AbstractIsolatedPerson, and a Movement Schedule. 
 * There is one instance of AbstractIsolatedPerson (or the static instance is reset)
 *  for each Path Traversal. One Movement Schedule is shared among all Path Traversals. 
 *  A time-delta of 5 minutes is used to create a Sample Path as traversed over time.</li>
 *  <li>This class encapsulates the "result" from step 2, which can be represented as a list
 *  of LatLonGeos; and includes time information based 
 *  on the time delta, that each point in the list occurs <i>time delta</i> after the previous point.
 *   In this way, a SamplePath can be completely represented by a list of locations.</li>
 *   </ol> 
 */
public class Sample implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3390872889038770480L;
	private long startTime;
	private long endTime;
	private long timeDelta;
	private long quittingTime = Long.MAX_VALUE;
	
	private List<LatLonGeo> pts;
	private List<Double> resourceLevelChange;
	
	public Sample() {
	    //JSON constructor
	}
	
	public Sample(long startTime, long timeDelta, List<LatLonGeo> pts, 
			List<Double> resourceLevelChange) {
		this.startTime = startTime;
		this.timeDelta = timeDelta;
		this.pts = pts;
		this.resourceLevelChange = resourceLevelChange;
		endTime = startTime + (pts.size()-1) * timeDelta;
	}
	
	/*
	 * To be used by JSON serialization only
	 */
    public String getPtsString() {
        return LandsarUtils.getStringForPts(pts);
    }
    
	/*
	 * To be used by JSON serialization only
	 */
    public void setPtsString(String ptsString) {
        pts = LandsarUtils.stringToPtsList(ptsString);
    }
	
	public long getEndTime() {
		return endTime;
//		return Math.min(endTime, quittingTime);
	}
	
	@JsonIgnore
	public List<LatLonGeo> getPoints(){
		return new ArrayList<>(this.pts);
	}
	
	/*
	 * To be used by JSON serialization only
	 */
	public void setEndTime(long endTime) {
	    this.endTime = endTime;
	}

	
	public long getStartTime() {
		return startTime;
	}
	
	/*
	 * To be used by JSON serialization only
	 */
	public void setStartTime(long startTime) {
	    this.startTime = startTime;
	}

	public long getTimeDelta() {
		return timeDelta;
	}
	
	/*
	 * To be used by JSON serialization only
	 */
	public void setTimeDelta(long timeDelta) {
	    this.timeDelta = timeDelta;
	}

	/*
	 * To be used by JSON serialization only
	 */
	public void setQuittingTimeRelativeToStart(long relativeQuittingTime) {
		quittingTime = startTime + relativeQuittingTime;
	}
	
	public List<Double> getResourceLevelChange() {
		return resourceLevelChange;
	}
	
	/**
	 * To be used by JSON serialization only
	 */
	public void setResourceLevelChange(List<Double> rlc) {
	    this.resourceLevelChange = rlc;
	}
	
	public LatLonGeo getLocation(long time) {
	
		if (time > quittingTime) time = quittingTime;

		if (time <= startTime) return pts.get(0);
		if (time >= endTime) return pts.get(pts.size() - 1);
				
		int indx0 = (int)((time - startTime) / timeDelta);
		int indx1 = Math.min(indx0 + 1, pts.size() - 1);
		
		double beta = (double)((time - startTime) % timeDelta) / timeDelta;
		
		LatLonGeo pt0 = pts.get(indx0);
		LatLonGeo pt1 = pts.get(indx1);
		
		double lat = (1 - beta) * pt0.getLatDeg() + beta * pt1.getLatDeg();
		double lon = (1 - beta) * pt0.getLonDeg() + beta * pt1.getLonDeg();
		
		return LatLonGeo.fromDeg(lat, lon);		
	}
	
	// Returns the locations at regular intervals over time
	public List<LatLonGeo> getLocations(long startTime, long endTime, long interval) {
		List<LatLonGeo> locations = new ArrayList<LatLonGeo>();
		
		for (long time = startTime; time <= endTime; time += interval) {
			locations.add(getLocation(time));
		}
		return locations;
	}
	
	public List<LatLonGeo> getLocations(List<Long> times) {
		List<LatLonGeo> locations = new ArrayList<>();
		
		for (long time : times) {
			locations.add(getLocation(time));
		}
		return locations;		
	}

	public boolean isMoving(double time) {
		if (time <= startTime) return false;
		if (time >= endTime) return false;
		
		int indx0 = (int)((time - startTime) / timeDelta);
		int indx1 = Math.min(indx0 + 1, pts.size() - 1);
				
		LatLonGeo pt0 = pts.get(indx0);
		LatLonGeo pt1 = pts.get(indx1);	
		
		if (pt0.getLatDeg() != pt1.getLatDeg()) return true;
		if (pt0.getLonDeg() != pt1.getLonDeg()) return true;
		
		return false;
	}
	
	@JsonIgnore
	public double getSpeed(double time) {
		if (time <= startTime) return 0;
		if (time >= endTime) return 0;
		
		int indx0 = (int)((time - startTime) / timeDelta);
		int indx1 = Math.min(indx0 + 1, pts.size() - 1);
				
		LatLonGeo pt0 = pts.get(indx0);
		LatLonGeo pt1 = pts.get(indx1);	
		
		return pt0.getDistanceTo(pt1) / timeDelta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (endTime ^ (endTime >>> 32));
		result = prime * result + ((pts == null) ? 0 : pts.hashCode());
		result = prime * result + (int) (quittingTime ^ (quittingTime >>> 32));
		result = prime * result + ((resourceLevelChange == null) ? 0 : resourceLevelChange.hashCode());
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		result = prime * result + (int) (timeDelta ^ (timeDelta >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Sample other = (Sample) obj;
		if (endTime != other.endTime) {
			return false;
		}
		if (pts == null) {
			if (other.pts != null) {
				return false;
			}
		} else if (!pts.equals(other.pts)) {
			return false;
		}
		if (quittingTime != other.quittingTime) {
			return false;
		}
		if (resourceLevelChange == null) {
			if (other.resourceLevelChange != null) {
				return false;
			}
		} else if (!resourceLevelChange.equals(other.resourceLevelChange)) {
			return false;
		}
		if (startTime != other.startTime) {
			return false;
		}
		if (timeDelta != other.timeDelta) {
			return false;
		}
		return true;
	}

}
