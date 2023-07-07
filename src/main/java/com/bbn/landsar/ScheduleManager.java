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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.landsar.MovementSchedule24HourPeriod.MovementStatus;
import com.bbn.landsar.utils.DateTimeUtilities;

/**
 * static class to manage movement schedules
 * 
 * Schedules are managed by their names
 * @author crock
 *
 */
public class ScheduleManager {
    private static final String NAME_SEPERATOR_CHAR = "-";

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleManager.class);

	public static final String CONTINUOUS = "Continuous";
	
	public static enum StandardSchedule {Night, Day, Continuous}
	
	/**
	 * This is the name of the default movement schedule - moving at all times appended with default time zone
	 */
	public static final String DEFAULT_SCHEDULE_NAME = nameConvention(StandardSchedule.Continuous, TimeZone.getDefault());
	
	private static final List<MovementSchedule> availableSchedules = new ArrayList<>();
	
	
	public static List<String> PREFERRED_US_TIMEZONES = Collections.unmodifiableList(Arrays.asList(new String[] {
			"US/Eastern", "US/Pacific", "US/Mountain", "US/Central"
	}));
	

	
	static {
		instantiateStandardMovementSchedules(TimeZone.getDefault());
	}
	
	private static void instantiateStandardMovementSchedules(TimeZone timeZone) {

		/// Always moving
		double[] changeHours = {12,12}; 
		addMovementSchedule(
				createMovementSchedule(MovementStatus.Moving, changeHours, 
						nameConvention(StandardSchedule.Continuous, timeZone), timeZone));

		// Night time movement
		// break 11pm-1am, moving 1am-7am, resting 7am-5pm, moving 5pm to 11pm
		double[] changeHours2 = {1, 7, 17, 23};
		addMovementSchedule(
				createMovementSchedule(MovementStatus.Resting, changeHours2,
						nameConvention(StandardSchedule.Night, timeZone), timeZone));

		/// Day time movement
		//Resting midnight-5am, moving 5am-11am, break 11-1pm, moving 1pm-7pm, rest 7pm-midnight
		double[] changeHours3 = {5, 11, 13, 19};
		addMovementSchedule(
				createMovementSchedule(MovementStatus.Resting, changeHours3,
						nameConvention(StandardSchedule.Day, timeZone), timeZone));

	}
	
	/**
	 * 
	 * @param movementSchedule
	 * @throws IllegalArgumentException - if the provided movementschedule's name is null or we already have a movement schedule with the same name
	 */
	private static void addMovementSchedule(MovementSchedule movementSchedule) {
		if (movementSchedule.getName() == null) {
			throw new IllegalArgumentException("Movement Schedule name must not be null");
		}		
		synchronized (ScheduleManager.class) {
			if (getScheduleNames().contains(movementSchedule.getName())){
				LOGGER.error("Already have a movement schedule with name {}", movementSchedule.getName());
			}else {
				availableSchedules.add(movementSchedule);
			}
		}
	}
	
	
	/**
	 * Used by ScheduleManager to deserialize schedules 
	 * @param scheduleName
	 * @return
	 */
	protected static MovementSchedule getOrRecreateScheduleForName(String scheduleName) {
		
		synchronized (ScheduleManager.class) {
			MovementSchedule schedule = getScheduleForName(scheduleName);
		
			if (schedule != null){
				return schedule;
			} else {
				LOGGER.info("Recreating Movement Schedule for name '{}'", scheduleName);
				int splitIndex = scheduleName.indexOf(NAME_SEPERATOR_CHAR);
				String name;
				TimeZone timeZone;
				if (splitIndex == -1) {
					name = scheduleName;
					timeZone = TimeZone.getDefault();
				} else {
					name = scheduleName.substring(0, splitIndex);
					timeZone  = TimeZone.getTimeZone(scheduleName.substring(splitIndex + 1, scheduleName.length()));
				}
				
				StandardSchedule standardSchedule;
				try {
					standardSchedule = StandardSchedule.valueOf(name);
				} catch (IllegalArgumentException e) {
					LOGGER.warn("Using default 'Continuous' movement schedule for unrecognized ScheduleType '{}'", name);
					standardSchedule = StandardSchedule.Continuous;
				}
				return getOrCreateScheduleForTimeZone(standardSchedule, timeZone);
			}
		}
	}
	
	public static MovementSchedule getScheduleForName(String scheduleName) {
		for (MovementSchedule schedule : availableSchedules) {
			if (schedule.getName().equals(scheduleName)) {
				return schedule;
			}
		}
		
		return null;
	}
	
	public static List<String> getScheduleNames() {
		
		List<String> scheduleNames = new ArrayList<String>();
		for (MovementSchedule schedule : availableSchedules) {
			scheduleNames.add(schedule.getName());
		}
		return scheduleNames;
	}
	
	/**
	 * Gets the specified StandardSchedule for this time zone; or creates it
	 * @param scheduleType
	 * @param timeZone
	 * @return MovementSchedule
	 */
	public static MovementSchedule getOrCreateScheduleForTimeZone(StandardSchedule scheduleType, TimeZone timeZone) {
			
		String nameConvention = nameConvention(scheduleType, timeZone);
		
		for (MovementSchedule schedule : availableSchedules) {
			if (schedule.getName().equals(nameConvention)) {
				if (schedule.getTimeZone().hasSameRules(timeZone)) {
					return schedule;
				}
			}
		}
		// if we haven't found it, try making schedules for this time zone
		instantiateStandardMovementSchedules(timeZone);
		// and then try again: 
		for (MovementSchedule schedule : availableSchedules) {
			if (schedule.getName().equals(nameConvention)) {
				if (schedule.getTimeZone().hasSameRules(timeZone)) {
					return schedule;
				}
			}
		}
		LOGGER.debug("No movement schedule for name='{}', timezone='{}'. Have schedules: {}", nameConvention, timeZone, availableSchedules);
		return null;
	}
	
	public static String nameConvention(StandardSchedule scheduleType, TimeZone timeZone) {
		return scheduleType.toString() + NAME_SEPERATOR_CHAR + timeZone.getID();
	}

	private static MovementSchedule createMovementSchedule(
			MovementStatus referenceStatus, double[] statusChangeHours, String name, TimeZone timezone) {
		long[] statusChangeTimes = new long[statusChangeHours.length];
		for (int i = 0; i < statusChangeTimes.length; i++) {
			statusChangeTimes[i] = (long)(statusChangeHours[i] * DateTimeUtilities.millisecInHour);
		}

		MovementSchedule movementSchedule = 
				new MovementSchedule24HourPeriod(statusChangeTimes, referenceStatus, name, timezone);
		
		return movementSchedule;
	}
	
	/**
	 * This uses the default time zone
	 * @param restIntervals
	 * @param name 
	 * @return
	 */
	public static MovementSchedule createNew24HourSchedule(List<double[]> restIntervals, String name) {
		List<Double> statusChangeHours = new ArrayList<Double>();
		
		for (double[] interval : restIntervals) {
			statusChangeHours.add(interval[0]);
			statusChangeHours.add(interval[1]);
		}
		
		Collections.sort(statusChangeHours);
		
		MovementStatus initialStatus = MovementStatus.Moving;
		if (statusChangeHours.get(0) == 0.0) {
			initialStatus = MovementStatus.Resting;
			statusChangeHours.remove(0);
		}
		
		long[] statusChangeTimes = new long[statusChangeHours.size()];
		for (int i = 0; i < statusChangeTimes.length; i++) {
			statusChangeTimes[i] = (long)(statusChangeHours.get(i) * DateTimeUtilities.millisecInHour);
		}
		
		MovementSchedule movementSchedule = 
				new MovementSchedule24HourPeriod(statusChangeTimes, initialStatus, name, TimeZone.getDefault());
		return movementSchedule;
	}

}
