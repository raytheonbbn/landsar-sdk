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

import java.util.ArrayList;
import java.util.List;

public class ValidationInfo {
	
	
	
	List<String> errors = new ArrayList<>();
	List<String> warnings = new ArrayList<>();

	public void addError(String error) {
		errors.add(error);
		
	}
	
	public boolean isValid() {
		return errors.isEmpty();
	}

	public void addWarning(String string) {
		warnings.add(string);
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ValidationInfo [errors=");
		builder.append(errors);
		builder.append(", warnings=");
		builder.append(warnings);
		builder.append("]");
		return builder.toString();
	}

	public void add(ValidationInfo otherInfo) {
		this.errors.addAll(otherInfo.errors);
		this.warnings.addAll(otherInfo.warnings);
		
	}
	
	
}
