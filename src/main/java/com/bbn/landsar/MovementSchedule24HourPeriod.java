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

package com.bbn.landsar;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.utils.DateTimeUtilities;

/**
 * Note that this version of a schedule assumes that every 24 hour period is identical
 * 
 * Implementation note: TimeZones are stored as part of the MovementSchedule because in future implementations, 
 * we may change the schedule to be dependent on timezone, or time zone/location related data, like sunrise/sunset. 
 * 
 * @author Stephen Anderson, crock
 * 
 *
 */
public class MovementSchedule24HourPeriod implements MovementSchedule {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovementSchedule24HourPeriod.class);

	private static final long millisecInDay = DateTimeUtilities.millisecsInDay;
	
	private List<TimeInterval> movingIntervals;
	
	private String name;
	
	private long timeMovingPerDay;

	private TimeZone timeZone;
	
    public enum MovementStatus {Resting, Moving};
	
	public MovementSchedule24HourPeriod() {
	    //JSON constructor
	}
	

	MovementSchedule24HourPeriod(long[] statusChangeTimes, MovementStatus referenceStatus, String name, TimeZone timezone ) {
		this(statusChangeTimes, referenceStatus, name);
		this.timeZone = timezone;
	}
	
	private MovementSchedule24HourPeriod(long[] statusChangeTimes, MovementStatus referenceStatus, String name ) {
		this.name = name;
		
		// Set the time moving per day
		timeMovingPerDay = 0;
		boolean moving = (referenceStatus == MovementStatus.Moving);
		if (moving) timeMovingPerDay += statusChangeTimes[0];
		moving = !moving;
		for (int i = 1; i < statusChangeTimes.length; i++) {
			if (moving) timeMovingPerDay += (statusChangeTimes[i] - statusChangeTimes[i-1]);
			moving = !moving;
		}
		if (moving) timeMovingPerDay += (millisecInDay - statusChangeTimes[statusChangeTimes.length - 1]);
		
		// Build 48 hours of moving time intervals
		movingIntervals = new ArrayList<TimeInterval>();
		moving = referenceStatus == MovementStatus.Moving;
		if (moving) movingIntervals.add(new TimeInterval(0, statusChangeTimes[0]));
		moving = !moving;
		int totalEntries = statusChangeTimes.length;
		for (int i = 1; i < totalEntries; i++) {
			if (moving) {
				movingIntervals.add(new TimeInterval(statusChangeTimes[i-1], statusChangeTimes[i]));
			}
			moving = !moving;
		}
		if (moving) movingIntervals.add(
				new TimeInterval(statusChangeTimes[statusChangeTimes.length - 1], millisecInDay));
		
		// Add the next 24 hours
		int totalIntervals = movingIntervals.size();
		for (int i = 0; i < totalIntervals; i++) {
			TimeInterval interval = movingIntervals.get(i);
			movingIntervals.add(new TimeInterval(interval.t0 + millisecInDay, interval.t1 + millisecInDay));
		}
	}
	
	public void printMovingIntervals() {
		System.out.print(movingIntervalsDescription());
	}
	
	public String movingIntervalsDescription() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.name);
		for (TimeInterval interval : movingIntervals){
			builder.append(" [").append((long) interval.getT0()/millisecInDay).append(", ").append(interval.getT1()/millisecInDay).append("]");
		}
		builder.append('\n');
		return builder.toString();
	}
	
	
	// Quick hack to obtain functionality 
	@Override
	public boolean isMoving(long time) {
		return timeSpentMoving(time, time + 1) > 0;
	}
	
	// Returns the amount of time spent moving during the specified interval
	@Override
	public long timeSpentMoving(long startTime, long endTime) {

		if (startTime >  endTime) {
			throw new IllegalArgumentException("End time before start time");
		}
		
		// Determine the number of complete days in the interval
		long numWholeDays = (endTime - startTime) / millisecInDay;
		long  timeMoving = timeMovingPerDay * numWholeDays;
		
		// Determine how long is left with the complete days removed.  
		// It will be less than a day
		long remainingTime = (endTime - startTime) - numWholeDays * millisecInDay;
		
		// absolute value of timezone Offset is between 0 and 24 hours, startTime is between 0 and 24 hours
		// hence t0 is obtained by "modding" a positive number and is between 0 and 24 hours
		long t0 = (millisecInDay + startTime + this.getTimeZone().getOffset(startTime)) %  millisecInDay;
		
		// t1 must be between 0 and 48 hours
		long t1 = t0 + remainingTime;
		TimeInterval interval = new TimeInterval(t0, t1);
		
		// The "moving" intervals cover the time from 0 to 48 hours by construction
		for (TimeInterval t : movingIntervals) {
			timeMoving += t.calcIntersectionDuration(interval);
		}
		return timeMoving;
	}

	@Override
	public List<long[]> determineMovingIntervals(long minTime, long maxTime) {
		
		List<long[]> intervals = new ArrayList<long[]>();
		
		long t0 = (millisecInDay + minTime + this.getTimeZone().getOffset(minTime)) %  millisecInDay;
		long t1 = t0 + (maxTime - minTime);
		long offset = minTime - t0;
		for (TimeInterval moveInteval : movingIntervals) {
			
			long a0 = moveInteval.t0;
			long a1 = moveInteval.t1;
			while (a0 < t1) {
				
				long b0 = Math.max(a0,  t0);
				long b1 = Math.min(a1,  t1);
				
				if (b0 < b1) {
					long[] interval = {b0 + offset,b1 + offset};
					intervals.add(interval);
				}
				a0 += millisecInDay;
				a1 += millisecInDay;
			}
		}
		return intervals;
	}

    @Override
	public String getName() {
        return name;
    }
    
    /**
     * Gets the time zone for this MovementSchedule
     */
    @Override
	public TimeZone getTimeZone() {
    	if (this.timeZone == null) {
    		this.timeZone = TimeZone.getDefault();
    		LOGGER.debug("Using default time zone: {}", timeZone);
    	}
    	return this.timeZone;
    }
    /**
     * For JSON
     * @param timeZone
     */
    public void setTimeZone(TimeZone timeZone) {
    	this.timeZone = timeZone;
    }

	public List<TimeInterval> getMovingIntervals() {
		return movingIntervals;
	}

	public void setMovingIntervals(List<TimeInterval> movingIntervals) {
		this.movingIntervals = movingIntervals;
	}


	@Override
	public String getDescription() {
		return movingIntervalsDescription() + "This movement schedule is identical over all 24-hour periods (except for daylight savings, if applicable).";
	}
}
