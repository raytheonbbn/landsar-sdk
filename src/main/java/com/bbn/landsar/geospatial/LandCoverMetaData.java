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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;


/**
 * Represents LandCoverMetaData - which parameters (cost, SOA, etc) go with each type of land cover (water, grass, etc). 
 * 
 * 
 * OSPPRE Developer Note: 
 *  October 2022: added ability to add default landcover data via configuration back.
     * Now, there's two different definitions of "default landcover": the default loaded statically by LandCoverMetadata, as well as the default set by configuration. 
     * The offline data cache is part of the configuration and sets the default landcover during OsppreConfiguration's initialization. 
     * When a default is set by configuration it overrides the statically-loaded default, which can be retrieved with LandCoverMetaData.reloadDefaultLandcoverMetadataFromDisk()
     * 
 * TODO use the writeToFile/readFromFile methods for serialization
 *
 */


public class LandCoverMetaData implements Serializable {


	public static final short MISSING_DATA_LC_CODE = 0;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(LandCoverMetaData.class);

	private static final ConcurrentMap<String, LandCoverMetaData> landCoverTypeToMetadata = new ConcurrentHashMap<>();

	static {
		initializeDefaultMetadata();
	}


	String name = "";

	private ConcurrentMap<Integer, LandCoverMetaDataItem> metaDataItems = new ConcurrentHashMap<Integer, LandCoverMetaDataItem>();
	
	@JsonGetter("metaDataItems")
    public List<LandCoverMetaDataItem> getMetaDataItemsAsList() {
        return metaDataItems.values().stream().collect(Collectors.<LandCoverMetaDataItem>toList());
    }

    @JsonSetter("metaDataItems")
    public void setMetaDataItemsAsList(List<LandCoverMetaDataItem> metaDataItems) {
        ConcurrentMap<Integer, LandCoverMetaDataItem> deserializedMetaDataItems = metaDataItems.stream().collect(Collectors.toConcurrentMap(LandCoverMetaDataItem::getLcCode, car -> car));
        this.metaDataItems = deserializedMetaDataItems;
    }
	
	public LandCoverMetaData() {
	}
	
	public LandCoverMetaData(String name, ConcurrentHashMap<Integer, LandCoverMetaDataItem> metaDataItems) {
		this.name = name;
		this.metaDataItems = metaDataItems;
	}
	
    public static LandCoverMetaData getMetadataFor(String landcoverType) {
		return landCoverTypeToMetadata.get(landcoverType);
    }
    
    public static void setMetadataFor(String landcoverType, LandCoverMetaData metadata) {
    	LandCoverMetaData previous = landCoverTypeToMetadata.put(landcoverType, metadata);
    	if (LOGGER.isDebugEnabled() && previous == null) {
    		LOGGER.debug("Set landcover data for landcover type: {}", landcoverType);
    	}
    }
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public int getSize() {
		return metaDataItems.size();
	}
	
	public void addMetaDataItem(LandCoverMetaDataItem metaDataItem) {
		metaDataItems.put(metaDataItem.lcCode, metaDataItem);
	}
	
	public LandCoverMetaDataItem getMetaDataItem(int landCoverCode) {
		return metaDataItems.get(landCoverCode);
	}
	
	public List<Short> getCodes() {
		ArrayList<Integer> codes = new ArrayList<Integer>();
		
		ConcurrentHashMap<Integer, LandCoverMetaDataItem> map = (ConcurrentHashMap<Integer, LandCoverMetaDataItem>) metaDataItems;
		
		Enumeration<Integer> codeValues = map.keys();

		while (codeValues.hasMoreElements()) codes.add(codeValues.nextElement());
		
		Collections.sort(codes);
		
		ArrayList<Short> shortCodes = new ArrayList<Short>();
		for (int code : codes) {
			shortCodes.add((short)code);
		}
		return shortCodes;
	} 
	
	public void writeToFile(File outputFile) throws IOException {
		outputFile.getParentFile().mkdirs();
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))){
			
			boolean firstLine = true;
		    for (Short landCoverCode : getCodes()) {
				if (firstLine) {
					firstLine = false;
				}
				else {
					out.println();
				}
				out.print(getMetaDataItem(landCoverCode));
			}
			out.flush();
		}
	}
	
	/**
	 * If there isn't already a default for this type of landcover and the loaded metadata is not null, it is set as the static default for this landcoverType
	 * @param metaDataFilename
	 * @param landcoverType
	 * @return
	 */
	public static LandCoverMetaData loadLandcoverMetaData(String metaDataFilename, String landcoverType) {
		LandCoverMetaData metadata;
		try {
			metadata = loadLandcoverMetaData(metaDataFilename);
		} catch (NumberFormatException | IOException e) {
			LOGGER.error("Error loading LandCoverMetaData '{}' ", metaDataFilename, e);
			return null;
		}
		if (metadata != null) {
			landCoverTypeToMetadata.putIfAbsent(landcoverType, metadata);
		}
		return metadata;
	}
	
	public static LandCoverMetaData loadLandcoverMetaData(File metadataFile) {
		if (metadataFile == null) {
			return null;
		}
		LandCoverMetaData metaData = new LandCoverMetaData();
		try (BufferedReader in = new BufferedReader(new FileReader(metadataFile))){
			String line = in.readLine();
			while (line != null) {
				LandCoverMetaDataItem data = new LandCoverMetaDataItem(line);
				metaData.addMetaDataItem(data);
				line = in.readLine();
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			return null;
		}

		return metaData;
	}

	public static LandCoverMetaData loadLandcoverMetaData(String metaDataFilename) throws IOException, NumberFormatException {
		InputStream inputStream = LandCoverMetaData.class.getClassLoader().getResourceAsStream(metaDataFilename);
		if (inputStream == null) {
			return loadLandcoverMetaData(new File(metaDataFilename));
		}
		LandCoverMetaData metaData = new LandCoverMetaData();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))){
			String line = in.readLine();
			while (line != null) {
				LandCoverMetaDataItem data = new LandCoverMetaDataItem(line);
				metaData.addMetaDataItem(data);
				line = in.readLine();
			}
		}

		return metaData;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metaDataItems == null) ? 0 : metaDataItems.hashCode());
		return result;
	}

	/**
	 * The "name" of the LandCoverMetaData object as a whole is not considered when comparing instances of LandCoverMetaData for equality 
	 */
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
		LandCoverMetaData other = (LandCoverMetaData) obj;
		if (metaDataItems == null) {
			if (other.metaDataItems != null) {
				return false;
			}
		} else if (!metaDataItems.equals(other.metaDataItems)) {
			return false;
		}
		return true;
	}
	
    @Override
	public String toString() {
    	if ("".equals(name)) {
    		return "LandCoverMetaData [metaDataItems hashcode=" + metaDataItems.hashCode() + "]";
    	}else {
    		return "LandCoverMetaData [name= " + name + "]";
    	}
	}
	
    // this gets initialized before we set the server default from configuration, in case there is no server-specific metadata.
    // most Raytheon-run servers aren't expected to set server-specific landcover metadata, 
    // but other users of the SDK will likely want to set server-specific landcover metadata since LandSAR BASE has redacted defaults. 
    private static void initializeDefaultMetadata() {
		landCoverTypeToMetadata.put(AbstractLandCoverData.NLCDB, reloadDefaultLandcoverMetadataFromDisk());
    }
    
    /**
     * This re-loads and re-sets the default (NLCDB) landcover metadata, from disk. 
     * This overwrites any LandSAR gateway configuration-defined metadata
     * @return
     */
    public static LandCoverMetaData reloadDefaultLandcoverMetadataFromDisk() {
    	String filename = "NLCDB_default_landcover_empty.txt";
    	LandCoverMetaData metaData;
		try {
//		lcCode, String shortDescription, soaFactor, cost, terrainResourceParameter, red, green, blue, detailedDescription
			metaData = loadLandcoverMetaData(filename);
			metaData.setName("Default NLCDB - SAR");
		} catch (IOException | NumberFormatException | NullPointerException e) {
			LOGGER.error("Error loading NLCDB metadata...from file: {}. You may need to download this file, contact the developers of the SDK for it", filename, e);
			return new LandCoverMetaData();
		}


		if (!landCoverTypeToMetadata.containsKey(AbstractLandCoverData.NLCDB)) {
			landCoverTypeToMetadata.put(AbstractLandCoverData.NLCDB, metaData);
		}
		return metaData;

    }
    
}
