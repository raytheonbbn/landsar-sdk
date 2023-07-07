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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Most current movement schedules are the same for any 24-hour period, but this interface allows the Movement Schedules and types of Movement Schedules to be extended.
 * 
 * @author crock
 *
 */
public interface MovementSchedule {
	
	public static List<String> PREFERRED_US_TIMEZONES = Collections.unmodifiableList(Arrays.asList(new String[] {
			"US/Eastern", "US/Pacific", "US/Mountain", "US/Central"
	}));
	
	@JsonIgnore
	String getDescription();

    boolean isMoving(long time);

    /**
     *     Returns the amount of time spent moving during the specified interval
      */
    long timeSpentMoving(long startTime, long endTime);

    List<long[]> determineMovingIntervals(long minTime, long maxTime);

    /**
     * This should not return null
     * @return
     */
    String getName();

    TimeZone getTimeZone();

    public class TimeInterval implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -3215714313703829003L;
        long t0;
        long t1;


        /**
         * Json constructor
         */
        public TimeInterval() {

        }

        public TimeInterval(long t0, long t1) {
            super();
            this.t0 = t0;
            this.t1 = t1;
        }

        public long getT0() {
            return t0;
        }

        public void setT0(long t0) {
            this.t0 = t0;
        }

        public long getT1() {
            return t1;
        }

        public void setT1(long t1) {
            this.t1 = t1;
        }

        double calcIntersectionDuration(TimeInterval interval) {
            long a0 = Math.max(interval.t0, t0);
            long a1 = Math.min(interval.t1, t1);
            return Math.max(0, a1 - a0);
        }
    }
}
