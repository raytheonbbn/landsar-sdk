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

import java.util.Objects;

import com.bbn.landsar.motionmodel.Unit;

public class GeospatialMetadata {
	
	protected Object dataFetcher;
	protected Unit dataUnit;
	/**
	 * protected double spatialResolutionMeters = 0.0; // currently, this is calculated so it varies slightly on each instance. This is more "for our information" than actually useful. 
	 * approximate resolution at surface level, in meters
	 */
	protected double spatialResolutionMeters;
	protected String dataType;
	
	public GeospatialMetadata(Object dataFetcher, Unit dataUnit, double spatialResolutionMeters, String dataType) {
		super();
		this.dataFetcher = dataFetcher;
		this.dataUnit = dataUnit;
		this.spatialResolutionMeters = spatialResolutionMeters;
		this.dataType = dataType;
	}

	public Object getDataFetcher() {
		return dataFetcher;
	}

	public void setDataFetcher(Object dataFetcher) {
		this.dataFetcher = dataFetcher;
	}

	public Unit getDataUnit() {
		return dataUnit;
	}

	public void setDataUnit(Unit dataUnit) {
		this.dataUnit = dataUnit;
	}

	public double getSpatialResolutionMeters() {
		return spatialResolutionMeters;
	}

	public void setSpatialResolutionMeters(double spatialResolutionMeters) {
		this.spatialResolutionMeters = spatialResolutionMeters;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataFetcher, dataType, dataUnit, spatialResolutionMeters);
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
		GeospatialMetadata other = (GeospatialMetadata) obj;
		return Objects.equals(dataFetcher, other.dataFetcher) && Objects.equals(dataType, other.dataType)
				&& dataUnit == other.dataUnit && Double.doubleToLongBits(spatialResolutionMeters) == Double
						.doubleToLongBits(other.spatialResolutionMeters);
	}
	
	
	
}
