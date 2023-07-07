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

package com.bbn.landsar.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class DateTimeUtilities {
	
	public static final long millisecsInDay =  24 * 60 * 60 * 1000;
	
	public static final long millisecInHour = 60 * 60 * 1000;

	public static final int millisecInMin = 60 * 1000;
	

	public static long getStartOfDay(long referenceTime, TimeZone timezone) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(referenceTime);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setTimeZone(timezone);
		return cal.getTimeInMillis();		
	}
	
	public static long getEndOfDay(long referenceTime, TimeZone timezone) {
		long startOfDay = getStartOfDay(referenceTime, timezone);
		return startOfDay + millisecsInDay;
	}
	
	public static long getMidnightForTimeZone(TimeZone timeZone) {
		return getStartOfDay(System.currentTimeMillis(), timeZone);
	}
	

	public static TimeZone getLocalTimeZone() {
		return TimeZone.getDefault();
	}
	
}
