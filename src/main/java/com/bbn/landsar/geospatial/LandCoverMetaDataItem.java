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
import java.util.Arrays;

public class LandCoverMetaDataItem implements Comparable<LandCoverMetaDataItem>, 
                                                     Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int lcCode;
	private int cost;
	private float soaFactor;
	private String shortDescription;
	private int[] rgbColor;
	private String detailedDescription;
	private float terrainResourceParameter;
	

	
	public LandCoverMetaDataItem() {
		
	}
	
	public LandCoverMetaDataItem(int lcCode, String shortDescription,  
			float soaFactor, int cost, float terrainResourceParameter,
			int red, int green, int blue, 
			String detailedDescription) {
		
		super();
		this.lcCode = lcCode;
		this.cost = cost;
		this.soaFactor = soaFactor;
		this.shortDescription = shortDescription;
		this.rgbColor = new int[] {red, green, blue};
		this.detailedDescription = detailedDescription;
		this.terrainResourceParameter = terrainResourceParameter;
	}

	public LandCoverMetaDataItem(String line) {
		String[] s = line.split("\t");
		lcCode = Integer.parseInt(s[0]);
		shortDescription = s[1];
		soaFactor = (float) Double.parseDouble(s[2]);
		cost = Integer.parseInt(s[3]);
		terrainResourceParameter = (float) Double.parseDouble(s[4]);
		detailedDescription = "";
		if (s.length > 7) {
			int red = Integer.parseInt(s[5]);
			int green = Integer.parseInt(s[6]);
			int blue = Integer.parseInt(s[7]);
			rgbColor = new int[] {red, green, blue};
			if (s.length > 8) {
				detailedDescription = s[8];
			}
		}
	}
	
	public String toString() {
		return lcCode + AbstractLandCoverData.T + 
				shortDescription + AbstractLandCoverData.T +
				soaFactor + AbstractLandCoverData.T + 
				cost + AbstractLandCoverData.T + 
				terrainResourceParameter + AbstractLandCoverData.T + 
				rgbColor[0] + AbstractLandCoverData.T +
				rgbColor[1] + AbstractLandCoverData.T +
				rgbColor[2] + AbstractLandCoverData.T +
				detailedDescription;
	}
    		
	public int getLcCode() {
		return lcCode;
	}

	public int getCost() {
		return cost;
	}

	public float getSoaFactor() {
		return soaFactor;
	}

	public String getShortDescription() {
		return shortDescription;
	}
	
	public int[] getRgbColor() {
		return rgbColor;
	}

	public String getDetailedDescription() {
		return detailedDescription;
	}

	public float getTerrainResourceParameter() {
		return terrainResourceParameter;
	}

	@Override
	public int compareTo(LandCoverMetaDataItem o) {
		if (lcCode == o.lcCode) return 0;
		if (lcCode < o.lcCode) return -1;
		return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cost;
		result = prime * result + ((detailedDescription == null) ? 0 : detailedDescription.hashCode());
		result = prime * result + lcCode;
		result = prime * result + Arrays.hashCode(rgbColor);
		result = prime * result + ((shortDescription == null) ? 0 : shortDescription.hashCode());
		result = prime * result + Float.floatToIntBits(soaFactor);
		result = prime * result + Float.floatToIntBits(terrainResourceParameter);
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
		LandCoverMetaDataItem other = (LandCoverMetaDataItem) obj;
		if (cost != other.cost) {
			return false;
		}
		if (detailedDescription == null) {
			if (other.detailedDescription != null) {
				return false;
			}
		} else if (!detailedDescription.equals(other.detailedDescription)) {
			return false;
		}
		if (lcCode != other.lcCode) {
			return false;
		}
		if (!Arrays.equals(rgbColor, other.rgbColor)) {
			return false;
		}
		if (shortDescription == null) {
			if (other.shortDescription != null) {
				return false;
			}
		} else if (!shortDescription.equals(other.shortDescription)) {
			return false;
		}
		if (Float.floatToIntBits(soaFactor) != Float.floatToIntBits(other.soaFactor)) {
			return false;
		}
		if (Float.floatToIntBits(terrainResourceParameter) != Float.floatToIntBits(other.terrainResourceParameter)) {
			return false;
		}
		return true;
	}	
}
