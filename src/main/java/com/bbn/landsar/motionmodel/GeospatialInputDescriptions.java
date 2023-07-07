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

package com.bbn.landsar.motionmodel;

import java.io.Serializable;
import java.util.Objects;

public class GeospatialInputDescriptions implements Serializable {
    private UserEnteredGeospatialData.GeospatialDataType geospatialDataType;
    private boolean isRequired;

    public GeospatialInputDescriptions(){
    }

    public GeospatialInputDescriptions(UserEnteredGeospatialData.GeospatialDataType geospatialDataType, boolean isRequired) {
        this.geospatialDataType = geospatialDataType;
        this.isRequired = isRequired;
    }

    public UserEnteredGeospatialData.GeospatialDataType getGeospatialDataType() {
        return geospatialDataType;
    }

    public void setGeospatialDataType(UserEnteredGeospatialData.GeospatialDataType geospatialDataType) {
        this.geospatialDataType = geospatialDataType;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String toString() {
        return "<geospatialDataType: " + this.geospatialDataType + ", isRequired: " + this.isRequired + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeospatialInputDescriptions that = (GeospatialInputDescriptions) o;
        return isRequired == that.isRequired && geospatialDataType == that.geospatialDataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(geospatialDataType, isRequired);
    }
}
