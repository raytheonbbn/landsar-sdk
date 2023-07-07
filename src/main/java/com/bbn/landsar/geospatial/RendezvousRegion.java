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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.util.DistanceAzimuth;

public class RendezvousRegion extends RendezvousSite implements GeographicDrawable {

	double majorAxisMeters;
	double minorAxisMeters;
	double orientationNavDeg;
	double orientationNavRad;
	
	public RendezvousRegion(LatLonGeo loc) {
		super(loc);
		majorAxisMeters = 0;
		minorAxisMeters = 0;
		orientationNavDeg = 0;
	}

	public RendezvousRegion(LatLonGeo loc, double majorAxisMeters,
			double minorAxisMeters, double orientationNavDeg) {
		super(loc);
		this.majorAxisMeters = majorAxisMeters;
		this.minorAxisMeters = minorAxisMeters;
		this.orientationNavDeg = orientationNavDeg;
		this.orientationNavRad = Math.toRadians(this.orientationNavDeg);
	}
	
    public boolean pointAtSite(LatLonGeo pt) {
    	DistanceAzimuth distAz = location.getDistanceAzimuthTo(pt);
    	double azimuth = distAz.getAzimuth();
    	double dist = distAz.getDistance();
    	double x = Math.abs(dist * Math.cos(azimuth));
    	double y = Math.abs(dist * Math.sin(azimuth));
//    	return (x <= majorAxisMeters) && (y <= minorAxisMeters);
    	return (y <= majorAxisMeters) && (x <= minorAxisMeters);
    }
    
	public LatLonGeo getRandomLocation(Random rs) {
		
		double majorOffset = (2 * rs.nextDouble() - 1) * majorAxisMeters;
		double minorOffset = (2 * rs.nextDouble() - 1) * minorAxisMeters;
		double distance = Math.hypot(majorOffset, minorOffset);
//		double azimuth = orientationNavRad + Math.atan2(majorOffset, minorOffset);
		double azimuth = (Math.PI - orientationNavRad) + Math.atan2(majorOffset, minorOffset);
		DistanceAzimuth distAzimuth = new DistanceAzimuth(distance, azimuth);
		LatLonGeo randomLoc = location.displacedBy(distAzimuth);
		return randomLoc;
	}
	
//	public AbstractSurfaceShape getDisplay() {
//		Angle aLat = Angle.fromDegreesLatitude(location.getLatDeg());
//		Angle aLon = Angle.fromDegreesLongitude(location.getLonDeg());
//		
//		BasicShapeAttributes attr = new BasicShapeAttributes();
//		attr.setDrawInterior(false);
//		attr.setDrawOutline(true);
//		
// //       Material matExt = new Material(Color.BLACK);
//        Material matExt = new Material(Color.GREEN);
//        Material matInt = new Material(Color.WHITE);
//        attr.setOutlineMaterial(matExt);
//		attr.setInteriorMaterial(matInt);
//
//		SurfaceQuad boundary =  new SurfaceQuad(attr, new LatLon(aLat, aLon), 
//				2 * majorAxisMeters, 2 * minorAxisMeters, Angle.fromDegrees(90 - orientationNavDeg));
//		return boundary;
//	}
	
	public List<GeographicGeometry> getGeographicGeometry() {
		List<GeographicGeometry> drawables = new ArrayList<GeographicGeometry>();
		
		List<LatLonGeo> corners = new ArrayList<LatLonGeo>();
		
		double ort = Math.toRadians(90 - orientationNavDeg);
		
		LatLonGeo p0 = location.displacedBy(majorAxisMeters, ort);
		p0 = p0.displacedBy(minorAxisMeters, ort + Math.PI / 2);
		corners.add(p0);
		LatLonGeo p1 = location.displacedBy(majorAxisMeters, ort);
		p1 = p1.displacedBy(minorAxisMeters, ort - Math.PI / 2);
		corners.add(p1);
		LatLonGeo p2 = location.displacedBy(-majorAxisMeters, ort);
		p2 = p2.displacedBy(minorAxisMeters, ort - Math.PI / 2);
		corners.add(p2);
		LatLonGeo p3 = location.displacedBy(-majorAxisMeters, ort);
		p3 = p3.displacedBy(minorAxisMeters, ort + Math.PI / 2);
		corners.add(p3);

		corners.add(p0);
		GeographicGeometry drawable = new GeographicPolyPoints(GeographicPolyPoints.Type.Polyline, corners);
		drawables.add(drawable);
		return drawables;
	}

}
