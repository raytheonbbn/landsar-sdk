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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.bbn.roger.config.AttributeDescription;

/**
 * Holds a description of user-entered data to configure a model with. 
 */
public class MotionModelAttributeDescription implements Serializable {
    private String name;
    private String description;
    private boolean required;
    private Class<?> type;

    /*
     * Allows UI to show user-preferred units (ie. imperial/metric) but send units back to server converted to unit
     * specified here
     */
    private Unit dataUnit;

	public MotionModelAttributeDescription(){
    }

    public MotionModelAttributeDescription(String name, String description, boolean required, Class<?> type,
    		Unit dataUnit) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.type = type;
        this.dataUnit = dataUnit;
    }
    
    public MotionModelAttributeDescription(String name, String description, boolean required, Class<?> type) {
    	this(name, description, required, type, Unit.NONE);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isRequired() {
        return this.required;
    }


    public Class<?> getType() {
        return this.type;
    }

    public static Set<String> toNames(Set<com.bbn.roger.config.AttributeDescription> anyMetadata) {
        Set<String> names = new HashSet<>();

        for (AttributeDescription each : anyMetadata) {
            names.add(each.getName());
        }

        return names;
    }
    
    public Unit getDataUnit() {
		return dataUnit;
	}

	public void setDataUnit(Unit dataUnit) {
		this.dataUnit = dataUnit;
	}

    public String toString() {
        return "<name: " + this.name + ", desc: " + this.description + ", type: " + this.type + ", required: "
                + this.required + ", units: " +this.dataUnit + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MotionModelAttributeDescription that = (MotionModelAttributeDescription) o;
        return required == that.required && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(type, that.type) && dataUnit == that.dataUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, required, type, dataUnit);
    }
}
