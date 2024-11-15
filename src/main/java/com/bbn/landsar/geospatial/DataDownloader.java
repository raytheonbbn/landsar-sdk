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

import java.util.Set;

/**
 * The Data Downloader provides a Downloader for a specific data source.
 *
 * TODO: we should eventually break this down into DataDownloader and DataLoader, or something similar
 * to support both internet and data cache modes in LandSAR
 */
public interface DataDownloader {
	// Type of AreaData that this Downloader can download. When possible, please reuse constants from com.bbn.landsar.motionmodel.AreaDataType
    String getAreaDataType();
    AdditionalData getData(BoundingBox bbox);
}
