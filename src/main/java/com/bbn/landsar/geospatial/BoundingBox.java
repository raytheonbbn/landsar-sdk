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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.units.Azimuth;


/**
 * Bounding Box specifies the Area of Operation for a Scenario/LPI, and specifies the bounds of geo-data to load for the corresponding AreaData instance.
 * 
 *
 */
public class BoundingBox implements GeographicDrawable, Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(BoundingBox.class);
	
	
	/**
     * 
     */
    private static final long serialVersionUID = -5745245809604038428L;
    
    private static final double earthAreaMetersSquare = 5.1e14;
    
    double northLatDeg;
	double southLatDeg;
	double eastLonDeg;
	double westLonDeg;
	
	public BoundingBox() {};
	
	public BoundingBox(double northLatDeg, double southLatDeg,
			double eastLonDeg, double westLonDeg) {
		super();
		this.northLatDeg = northLatDeg;
		this.southLatDeg = southLatDeg;
		this.eastLonDeg = eastLonDeg;
		this.westLonDeg = westLonDeg;
	}
	
	public BoundingBox(String boxString) {
		super();
		String[] t = boxString.split(",");
		this.southLatDeg = Double.parseDouble(t[0]);
		this.northLatDeg = Double.parseDouble(t[1]);
		this.westLonDeg = Double.parseDouble(t[2]);
		this.eastLonDeg = Double.parseDouble(t[3]);
	}

	public BoundingBox(LatLonGeo center, double nsExtentKm, double ewExtentKm) {
		this(
				center.displacedBy(1000 * nsExtentKm / 2, Azimuth.north).getLatDeg(),
				center.displacedBy(1000 * nsExtentKm / 2, Azimuth.south).getLatDeg(),
				center.displacedBy(1000 * ewExtentKm / 2, Azimuth.east).getLonDeg(),
				center.displacedBy(1000 * ewExtentKm / 2, Azimuth.west).getLonDeg()
			);
	}

	public LatLonGeo calcSWCorner() {
		return new LatLonGeo(southLatDeg, westLonDeg);
	}
	
	public LatLonGeo calcNECorner() {
		return new LatLonGeo(northLatDeg, eastLonDeg);
	}
	
	public LatLonGeo calcSECorner() {
		return new LatLonGeo(southLatDeg, eastLonDeg);
	}
	
	public LatLonGeo calcNWCorner() {
		return new LatLonGeo(northLatDeg, westLonDeg);
	}

	public boolean contains(LatLonGeo end) {
		final double latDeg = end.getLatDeg();
		final double lonDeg = end.getLonDeg();
		if (Double.isNaN(lonDeg) || Double.isNaN(latDeg)) {
			LOGGER.warn("Point {} has NaN coords: lat={} deg ({} rad), lon={} deg ({} rad)", end, latDeg, end.getLatRad(), lonDeg, end.getLonRad());
			return false;
		}
		if (latDeg > northLatDeg) return false;
		if (latDeg < southLatDeg) return false;
		if (lonDeg > eastLonDeg) return false;
		if (lonDeg < westLonDeg) return false;
		return true;
	}

	public double getNorthLatDeg() {
		return northLatDeg;
	}

	public double getSouthLatDeg() {
		return southLatDeg;
	}

	public double getEastLonDeg() {
		return eastLonDeg;
	}

	public double getWestLonDeg() {
		return westLonDeg;
	}
	
    public void setNorthLatDeg(double northLatDeg) {
        this.northLatDeg = northLatDeg;
    }
    public void setSouthLatDeg(double southLatDeg) {
        this.southLatDeg = southLatDeg;
    }   
    public void setWestLonDeg(double westLonDeg) {
        this.westLonDeg = westLonDeg;
    }
    public void setEastLonDeg(double eastLonDeg) {
        this.eastLonDeg = eastLonDeg;
    }

	@Override
	public List<GeographicGeometry> getGeographicGeometry() {
		List<GeographicGeometry> drawables = new ArrayList<GeographicGeometry>();
		
		List<LatLonGeo> pts = new ArrayList<LatLonGeo>();
		pts.add(new LatLonGeo(northLatDeg, westLonDeg));
		pts.add(new LatLonGeo(northLatDeg, eastLonDeg));
		pts.add(new LatLonGeo(southLatDeg, eastLonDeg));
		pts.add(new LatLonGeo(southLatDeg, westLonDeg));
		
		GeographicPolyPoints drawable = new GeographicPolyPoints(GeographicPolyPoints.Type.Polygon, pts);
		drawables.add(drawable);
		return drawables;
	} 
	
	public boolean expandToInclude(BoundingBox other) {
		boolean anyChange = false;
		if (other.northLatDeg > this.northLatDeg) {
			this.northLatDeg = other.northLatDeg;
			anyChange = true;
		}
		if (other.southLatDeg < this.southLatDeg) {
			this.southLatDeg = other.southLatDeg;
			anyChange = true;
		}
		if (other.westLonDeg > this.westLonDeg) {
			this.westLonDeg = other.westLonDeg;
			anyChange = true;
		}
		if (other.eastLonDeg < this.eastLonDeg) {
			this.eastLonDeg = other.eastLonDeg;
			anyChange = true;
		}
		return anyChange;		
	}

	
	public double calcApproximateAreaMetersSq() {
		double lonFactor = 
				Math.toRadians(Math.abs(westLonDeg - eastLonDeg));
		double latFactor = Math.abs(Math.cos(Math.toRadians(northLatDeg)) -
				Math.cos(Math.toRadians(southLatDeg)));
		double areaSterRadians = lonFactor * latFactor;
		
		return areaSterRadians * earthAreaMetersSquare / ( 4 * Math.PI);
	}
	
	public String toString() {
		return southLatDeg + "," + northLatDeg + "," + westLonDeg + "," + eastLonDeg;
	}
	

	public String createFileFriendlyString() {

		return "S" + String.format("%.3f", southLatDeg).replace('.', '_') + "_N" + String.format("%.3f", northLatDeg).replace('.', '_') 
				+ "_W" + String.format("%.3f", westLonDeg).replace('.', '_') + "_E" + String.format("%.3f", eastLonDeg).replace('.', '_');
	}


	public LatLonGeo calcCenter() {
		double centerLat = (southLatDeg + northLatDeg) / 2;
		double centerLon = (eastLonDeg + westLonDeg) / 2;
		
		return new LatLonGeo(centerLat, centerLon);
	}

    /**
     * @return distance in meters
     */
	public double calcNsExtent() {
		return calcNWCorner().getDistanceTo(calcSWCorner());
	}
	
	/**
	 * @return distance in meters
	 */
	public double calcEwExtent() {
		return calcNWCorner().getDistanceTo(calcNECorner());
	}

	/**
	 * 
	 * @param minExtentKm - Kilometers
	 * @return
	 */
	public BoundingBox expandToMinimum(double minExtentKm) {
		LatLonGeo oldCenter = calcCenter();
		double newNsExtentKm = Math.max(minExtentKm, calcNsExtent() / 1000);
		double newEwExtentKm = Math.max(minExtentKm, calcEwExtent() / 1000);
		
		return new BoundingBox(oldCenter, newNsExtentKm, newEwExtentKm);
	}
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(eastLonDeg);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(northLatDeg);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(southLatDeg);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(westLonDeg);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        BoundingBox other = (BoundingBox) obj;
        if (Double.doubleToLongBits(eastLonDeg) != Double.doubleToLongBits(other.eastLonDeg)) {
            return false;
        }
        if (Double.doubleToLongBits(northLatDeg) != Double.doubleToLongBits(other.northLatDeg)) {
            return false;
        }
        if (Double.doubleToLongBits(southLatDeg) != Double.doubleToLongBits(other.southLatDeg)) {
            return false;
        }
        if (Double.doubleToLongBits(westLonDeg) != Double.doubleToLongBits(other.westLonDeg)) {
            return false;
        }
        return true;
    }

    public static void main(String[] arg) {
		BoundingBox box = new BoundingBox(35,35.1,-112,-111.5);
		System.out.println(box);
		System.out.println(box.expandToMinimum(20));
	}

	public boolean contains(BoundingBox boundingBox) {
		if (!contains(boundingBox.calcNECorner())
			||!contains(boundingBox.calcSWCorner())){
			return false;
		}else{
			return true;
		}
	}

}
