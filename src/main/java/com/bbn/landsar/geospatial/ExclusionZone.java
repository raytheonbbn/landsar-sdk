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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.vector.Vector2d;


public class ExclusionZone implements Serializable, GeographicDrawable {
    private static final long serialVersionUID = 2463561099126873157L;
    private LatLonGeo center;
    private double radius;
    boolean known; // Indicates that the exclusion zone was known to IP before incident

    @JsonIgnore
    private transient TangentPlane tangentPlane;
    private Vector2d c;
    private Double westLon = null;
    private Double eastLon = null;
    private Double northLat = null;
    private Double southLat = null;

    public ExclusionZone() {
        //JSON Constructor
    }

    public ExclusionZone(LatLonGeo center, double radius, boolean known) {
        this.setCenter(center);
        this.setRadius(radius);
        this.known = known;

        this.eastLon = tangentPlane.unproject(c.getX() + radius, c.getY()).getLonDeg();
        this.westLon = tangentPlane.unproject(c.getX() - radius, c.getY()).getLonDeg();
        this.northLat = tangentPlane.unproject(c.getX(), c.getY() + radius).getLatDeg();
        this.southLat = tangentPlane.unproject(c.getX(), c.getY() - radius).getLatDeg();
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }

    /*
     * The format for the input file is CSV with first entry the latitude in degrees
     * (N positive), the second entry longitude in degrees(E positive) and the third
     * entry the radius in meters.
     *
     * The fourth entry is optional.  It has a "0" if the exclusion zone is "known".
     * It has a "1" if the exclusion zone is "discovered".  The default in the
     * absence of a value is "known"
     */
    public static List<ExclusionZone> readExclusionZones(File zoneFile) throws IOException {
        List<ExclusionZone> zones = new ArrayList<ExclusionZone>();
        if (zoneFile == null) return zones;

        BufferedReader in = new BufferedReader(new FileReader(zoneFile));
        String line = in.readLine();
        while (line != null) {
            String[] t = line.split(",");
            double lat = Double.parseDouble(t[0]);
            double lon = Double.parseDouble(t[1]);
            double radius = Double.parseDouble(t[2]);
            LatLonGeo center = new LatLonGeo(lat, lon);

            // Look for exclusion zone type information
            boolean known = true;
            if (t.length > 3) {
                if (t[3].equals("1")) known = false;
            }

            zones.add(new ExclusionZone(center, radius, known));

            line = in.readLine();
        }
        in.close();
        return zones;
    }

    // See comment before readExclusionZones
    public static void writeExclusionZones(File zoneFile, List<ExclusionZone> zones) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(zoneFile)));

            for (int i = 0; i <  zones.size(); i++) {
                ExclusionZone zone = zones.get(i);
                if (i > 0) out.println();
                out.print(zone.getCenter().getLatDeg() + "," + zone.getCenter().getLonDeg() + "," + zone.getRadius());
                String typeString = "";
                if (zone.known) typeString = "0";
                if (!zone.known) typeString = "1";
                out.print("," + typeString);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public boolean isInside(LatLonGeo pt) {

        if (eastLon == null || westLon == null || northLat == null || southLat == null) {
            eastLon = tangentPlane.unproject(c.getX() + radius, c.getY()).getLonDeg();
            westLon = tangentPlane.unproject(c.getX() - radius, c.getY()).getLonDeg();
            northLat = tangentPlane.unproject(c.getX(), c.getY() + radius).getLatDeg();
            southLat = tangentPlane.unproject(c.getX(), c.getY() - radius).getLatDeg();
        }


        // Quick screening is much faster
        if (pt.getLatDeg() > northLat) return false;
        if (pt.getLatDeg() < southLat) return false;
        if (pt.getLonDeg() > eastLon) return false;
        if (pt.getLonDeg() < westLon) return false;

        return this.getCenter().getDistanceTo(pt) < getRadius();
    }

    public LatLonGeo closestBoundaryPoint(LatLonGeo latLon) {

        Vector2d pt = tangentPlane.project(latLon);

        pt = pt.scaledBy(1.01 * getRadius() / pt.norm());

        return tangentPlane.unproject(pt.getX(), pt.getY());
    }

    LatLonGeo getAvoidanceWaypoint(LatLonGeo aLatLon, LatLonGeo bLatLon) {

        Vector2d a = tangentPlane.project(aLatLon);
        Vector2d b = tangentPlane.project(bLatLon);

        Vector2d waypoint = GeometricUtilities.getCirleAvoidanceWaypoint(a, b, this.c, 1.01 * this.getRadius());

        return tangentPlane.unproject(waypoint.getX(), waypoint.getY());
    }

//	public SurfaceCircle getDisplay() {
//		Angle aLat = Angle.fromDegreesLatitude(center.getLatDeg());
//		Angle aLon = Angle.fromDegreesLongitude(center.getLonDeg());
//
//		BasicShapeAttributes attr = new BasicShapeAttributes();
//		attr.setDrawInterior(false);
//		attr.setDrawOutline(true);
//
// //       Material mat = new Material(Color.CYAN);
//        Material mat = new Material(Color.RED);
//        if (!known) mat = new Material(Color.pink);
//        attr.setOutlineMaterial(mat);
//
//		return new SurfaceCircle(attr, new LatLon(aLat,aLon), radius);
//	}

    /*public List<GeographicGeometry> getGeographicGeometry() {
        List<GeographicGeometry> geoDrawables = new ArrayList<GeographicGeometry>();
        geoDrawables.add(new GeographicDisk(getCenter(), getRadius()));
        return geoDrawables;
    }*/

    public List<GeographicGeometry> getGeographicGeometry() {
        List<GeographicGeometry> geoDrawables = new ArrayList<GeographicGeometry>();
        geoDrawables.add(new GeographicDisk(getCenter(), getRadius()));
        return geoDrawables;
    }

    public LatLonGeo getCenter() {
        return center;
    }

    public void setCenter(LatLonGeo center) {
        this.center = center;
        this.tangentPlane = new TangentPlane(center);
        this.c = tangentPlane.project(center);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

//	public String getKML() {
//
//		String kmlString = "<Placemark>\n";
//		kmlString += kmlStyle;
//		kmlString += KMLUtilities.getCirclePolygon(center, radius);
//		kmlString += "</Placemark>";
//		return kmlString;
//	}
//
//	static String kmlStyle =
////			"<Style><LineStyle><color>ffffff00</color><width>2</width></LineStyle><PolyStyle><outline>1</outline><fill>0</fill></PolyStyle></Style>";
//	"<Style><LineStyle><color>ff0000ff</color><width>2</width></LineStyle><PolyStyle><outline>1</outline><fill>0</fill></PolyStyle></Style>";
}
