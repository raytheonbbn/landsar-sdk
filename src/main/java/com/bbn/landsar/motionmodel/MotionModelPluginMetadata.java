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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A description of a motion model plugin, including its name and attributes.
 * OSPPRE clients can use this information to present users
 * with access to all the motion model plugins available on the server.
 *
 * @author Benjamin Toll
 * @since 0.0.4
 */
public class MotionModelPluginMetadata implements Serializable {
    private String name;
    private List<MotionModelAttributeDescription> motionModelParameters;
    private Set<GeospatialInputDescriptions> motionModelGeospatialDescriptions;
    private boolean stayOutOfWaterEnabled;
    private boolean landcoverMetadataEnabled;

    /**
     * Default constructor for serialization.
     */
    public MotionModelPluginMetadata() {}

    /**
     * Create a new metadata instance.
     * @param name a deployment-specific name for the plugin
     * @param motionModelParameters a list of motion model parameters
     * @param motionModelGeospatialDescriptions a list of geospatial motion
     *                                          model parameters
     */
    public MotionModelPluginMetadata(String name,
                                     Set<MotionModelAttributeDescription> motionModelParameters,
                                     Set<GeospatialInputDescriptions> motionModelGeospatialDescriptions,
                                     boolean stayOutOfWaterEnabled,
                                     boolean landcoverMetadataEnabled) {
        this.name = name;
        if (motionModelParameters != null) {
            Comparator<MotionModelAttributeDescription> comparator = Comparator.comparing(
                    MotionModelAttributeDescription::getName);
            this.motionModelParameters = motionModelParameters.stream().sorted(comparator).collect(Collectors.toCollection(
                    ArrayList::new));
        }else{
            this.motionModelParameters = null;
        }
        this.motionModelGeospatialDescriptions = motionModelGeospatialDescriptions;
        this.stayOutOfWaterEnabled = stayOutOfWaterEnabled;
        this.landcoverMetadataEnabled = landcoverMetadataEnabled;
    }

    /**
     * Returns the motion model plugin's deployment-specific name.
     * OSPPRE clients should display this name in their list of motion models.
     * @return the motion model plugin's deployment-specific name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>Returns any required or optional attributes that this
     * motion model plugin uses.
     * Examples might include distance, direction, or speed.
     * When users select a motion model plugin with custom attributes,
     * they should be presented with one or more forms in which to
     * enter values for the motion model's custom attributes.</p>
     *
     * <p>It is recommended, but not required, to modify or extend
     * the Set of default parameters:
     * ({see {@link MotionModelConstants#getDefaultMotionModelParameters()}.</p>
     *
     * @return any custom attributes that this motion model plugin uses.
     */
    public List<MotionModelAttributeDescription> getMotionModelParameters() {
        return motionModelParameters;
    }

    public void setMotionModelParameters(List<MotionModelAttributeDescription> motionModelParameters) {
        this.motionModelParameters = motionModelParameters;
    }

    /**
     * Returns any custom <b>geospatial</b> attributes that this motion model
     * plugin uses. Examples might include circular exclusion zones,
     * polygonal exclusion zones, or rendezvous points. This geospatial data
     * is traditionally user-entered and constrained to the bounding box.
     *
     * @return any custom geospatial attributes that this
     * motion model plugin uses.
     */
    public Set<GeospatialInputDescriptions> getMotionModelGeospatialDescriptions() {
        return motionModelGeospatialDescriptions;
    }

    public void setMotionModelGeospatialDescriptions(Set<GeospatialInputDescriptions> motionModelGeospatialDescriptions) {
        this.motionModelGeospatialDescriptions = motionModelGeospatialDescriptions;
    }

    public boolean isStayOutOfWaterEnabled() {
        return stayOutOfWaterEnabled;
    }

    public void setStayOutOfWaterEnabled(boolean stayOutOfWater) {
        this.stayOutOfWaterEnabled = stayOutOfWater;
    }

    public boolean isLandcoverMetadataEnabled() {
        return landcoverMetadataEnabled;
    }

    public void setLandcoverMetadataEnabled(boolean landcoverMetadataEnabled) {
        this.landcoverMetadataEnabled = landcoverMetadataEnabled;
    }
}
