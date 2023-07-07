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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.bbn.landsar.geospatial.GeographicDisk;
import com.bbn.landsar.geospatial.GeographicDrawable;
import com.bbn.landsar.geospatial.GeographicGeometry;
import com.metsci.glimpse.util.geo.LatLonGeo;

public class Waypoint implements GeographicDrawable {

	LatLonGeo location;
	String name;
	
	public Waypoint(LatLonGeo location, String name) {
		super();
		this.location = location;
		this.name = name;
	}
	
	public LatLonGeo getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public static List<Waypoint> readWaypointsFile(File file) throws IOException {

		List<Waypoint> waypoints = new ArrayList<Waypoint>();

		BufferedReader in = new BufferedReader(new FileReader(file));

		String line = in.readLine();

		// Line is comma separated and consists of an id, latitude(deg) and longitude(deg)
		while (line != null) {
			String[] s = line.split(",");
			String name = s[0];
			double latDeg = Double.parseDouble(s[1]);
			double lonDeg = Double.parseDouble(s[2]);
			LatLonGeo location = new LatLonGeo(latDeg, lonDeg);
			waypoints.add(new Waypoint(location, name));
			line = in.readLine();
		}
		in.close();

		return waypoints;
	}
	
	public static void writeWaypointsFile(File file, List<Waypoint> waypoints) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			
			for (int i = 0; i <  waypoints.size(); i++) {
				Waypoint waypoint = waypoints.get(i);
				if (i > 0) out.println();
				out.print(waypoint.name + "," + 
				      waypoint.location.getLatDeg() + "," + waypoint.location.getLonDeg());
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	double radiusMeters = 200;
//	public Renderable getDisplay() {
//		return ScenarioWorldWindDrawer.getDisplayPoint(this.getLocation(), 
//				Color.blue, radiusMeters);
//	}
	
	public List<GeographicGeometry> getGeographicGeometry() {
		List<GeographicGeometry> drawables = new ArrayList<GeographicGeometry>();
		drawables.add(new GeographicDisk(location, radiusMeters));
		return drawables;
	}

}
