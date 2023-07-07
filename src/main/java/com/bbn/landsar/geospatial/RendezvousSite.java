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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metsci.glimpse.util.geo.LatLonGeo;

public class RendezvousSite implements GeographicDrawable {
	
	private static double locationTolerance = 200; //50;
	
	LatLonGeo location;	
	
	@Deprecated
	static List<RendezvousSite> sites;
	
//	static {
//		sites = new ArrayList<RendezvousSite>();
//		sites.add(new RendezvousSite(new LatLonGeo( 48.76168, -117.86666)));
//		sites.add(new RendezvousSite(new LatLonGeo( 48.152190, -117.23966)));
//		sites.add(new RendezvousSite(new LatLonGeo( 48.015029, -118.027020 )));
//	}
	
	public RendezvousSite() {
	}
	
	public RendezvousSite(LatLonGeo loc) {
		this.location = new LatLonGeo(loc.getLatDeg(), loc.getLonDeg());
	}
	
	@JsonIgnore
	// There is only one location, so this method is trivial
    public LatLonGeo getRandomLocation(Random rs) {
        return this.location;
    }

    public LatLonGeo getLocation() {
        return location;
    }
    
    public void setLocation(LatLonGeo location) {
        this.location = location;
    }
	
    double getDistance(LatLonGeo loc) {
    	return this.location.getDistanceTo(loc);
    }
    
    @Deprecated
    public static List<RendezvousSite> getRendezvousSites() {
    	return sites;
    }
    
    
    public boolean pointAtSite(LatLonGeo pt) {
    	return pt.getDistanceTo(location) < locationTolerance;
    }
    
	/*
	 * The format for the input file is CSV with first entry the latitude in degrees
	 * (N positive) and the second entry longitude in degrees(E positive).
	 */
	public static void setRendezvousSitesFromFile(File zoneFile) {
		sites = new ArrayList<RendezvousSite>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(zoneFile));
			String line = in.readLine();
			while (line != null) {
				String[] t = line.split(",");
				double lat = Double.parseDouble(t[0]);
				double lon = Double.parseDouble(t[1]);
				LatLonGeo center = new LatLonGeo(lat, lon);
				if (t.length > 2) {
					double majorAxisMeters = Double.parseDouble(t[2]);
					double minorAxisMeters = Double.parseDouble(t[3]);
					double orientationNavDeg = Double.parseDouble(t[4]);
					sites.add(new RendezvousRegion(center, majorAxisMeters, minorAxisMeters, orientationNavDeg));
				}
				else {
					sites.add(new RendezvousSite(center));
				}
				line = in.readLine();
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<RendezvousSite> getRendezvousSites(File zoneFile) throws IOException {
		List<RendezvousSite> newSites = new ArrayList<RendezvousSite>();
			BufferedReader in = new BufferedReader(new FileReader(zoneFile));
			String line = in.readLine();
			while (line != null) {
				String[] t = line.split(",");
				double lat = Double.parseDouble(t[0]);
				double lon = Double.parseDouble(t[1]);
				LatLonGeo center = new LatLonGeo(lat, lon);
				if (t.length > 2) {
					double majorAxisMeters = Double.parseDouble(t[2]);
					double minorAxisMeters = Double.parseDouble(t[3]);
					double orientationNavDeg = Double.parseDouble(t[4]);
					newSites.add(new RendezvousRegion(center, majorAxisMeters, minorAxisMeters, orientationNavDeg));
				}
				else {
					newSites.add(new RendezvousSite(center));
				}
				line = in.readLine();
			}
			in.close();
			return newSites;
	}
	
	// See comment before readExclusionZones
	public static void writeRendezvousSites(File zoneFile) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(zoneFile)));
			
			for (int i = 0; i <  sites.size(); i++) {
				RendezvousSite site = sites.get(i);
				if (i > 0) out.println();
				out.print(site.location.getLatDeg() + "," + site.location.getLonDeg());
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void writeRendezvousSites(File zoneFile, List<LatLonGeo> pts) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(zoneFile)));
			
			for (int i = 0; i <  pts.size(); i++) {
				RendezvousSite site = new RendezvousSite(pts.get(i));
				if (i > 0) out.println();
				out.print(site.location.getLatDeg() + "," + site.location.getLonDeg());
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	// WHY IS IT A PROBLEM TO HAVE THE TWO METHODS ABOVE AND BELOW HAVE THE SAME NAME???
	public static void writeRendezvousSites2(File zoneFile, List<RendezvousSite> sites) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(zoneFile)));
			
			for (int i = 0; i <  sites.size(); i++) {
				RendezvousSite site = sites.get(i);
				if (i > 0) out.println();
				out.print(site.location.getLatDeg() + "," + site.location.getLonDeg());
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
//	public AbstractSurfaceShape getDisplay() {
//		Angle aLat = Angle.fromDegreesLatitude(location.getLatDeg());
//		Angle aLon = Angle.fromDegreesLongitude(location.getLonDeg());
//		
//		BasicShapeAttributes attr = new BasicShapeAttributes();
//		attr.setDrawInterior(true);
//		attr.setDrawOutline(true);
//		attr.setOutlineWidth(2);
//		
//        Material matExt = new Material(Color.WHITE);
////        Material matInt = new Material(Color.BLACK);
//        Material matInt = new Material(Color.GREEN);
//        attr.setOutlineMaterial(matExt);
//		attr.setInteriorMaterial(matInt);
//		
////		return new SurfaceCircle(attr, new LatLon(aLat,aLon), 200);
//		return new SurfaceQuad(attr, new LatLon(aLat, aLon), 1500, 1500, Angle.fromDegrees(45));
//	}
	
	public List<GeographicGeometry> getGeographicGeometry() {
		List<GeographicGeometry> drawables = new ArrayList<GeographicGeometry>();
		
		double size = 500;
		List<LatLonGeo> corners = new ArrayList<LatLonGeo>();
		for (int i = 0; i < 4; i++) {
			double azimuth = Math.PI * i / 2.0;
		    corners.add(location.displacedBy(size, azimuth));
		}
		GeographicGeometry drawable = new GeographicPolyPoints(GeographicPolyPoints.Type.Polygon, corners);
		drawables.add(drawable);
		return drawables;
	}
 
	public static void main(String[] args) {
		writeRendezvousSites(new File("RendezvousSites.csv"));
		
		setRendezvousSitesFromFile(new File("RendezvousSites.csv"));
	}
}
