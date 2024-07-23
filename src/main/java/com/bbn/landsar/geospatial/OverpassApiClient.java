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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class OverpassApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(OverpassApiClient.class.getName());
    private final String OVERPASS_API_URL;
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int NUM_ATTEMPTS = 3;

    public OverpassApiClient(String overpassApiUrl) {
        OVERPASS_API_URL = overpassApiUrl;
    }

    /**
     * Queries Overpass API with input query
     *
     * Example input query:
     * 
     * <pre>
     *  "waterway=river"
     *  [out:json][timeout:25];
     *  (
     *    // query part for: “waterway=river"
     *    node["waterway"="river"]({{bbox}});
     *    way["waterway"="river"]({{bbox}});
     *    relation["waterway"="river"]({{bbox}});
     *  );
     *  // print results
     *  out body;
     *  >;
     *  out skel qt;"
     * </pre>
     *
     * @param query required - the json query to use
     *
     * @return json response
     */
    public String queryData(String query) {
        LOGGER.debug("Sending request {}", query);
        for(int i=0; i<NUM_ATTEMPTS; i++) {
            try {
                URL url = new URL(OVERPASS_API_URL + "interpreter?data=" + URLEncoder.encode(query, "utf-8"));
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(DEFAULT_TIMEOUT);
                StringBuilder sb = new StringBuilder();
                for (int ch; (ch = con.getInputStream().read()) != -1; ) {
                    sb.append((char) ch);
                }
                return sb.toString();
            } catch (IOException e) {
                LOGGER.warn("IO Exception querying OverPass with query = {}", query, e);
            }
        }
        return null;
    }

    /**
     * Queries Overpass API for river way traversal data given bounding area
     *
     * @param boundingBox required - the bounding area to restrict search
     * @return River network json
     */
    public String getRiverDataViaOverpass(BoundingBox boundingBox) {
        String formattedBoundingBox = boundingBox.getSouthLatDeg() + "," + boundingBox.getWestLonDeg()
                + "," + boundingBox.getNorthLatDeg() + "," + boundingBox.getEastLonDeg();
        String formattedRequest = "[out:json]\n"
                + "[timeout:25]\n"
                + ";\n"
                + "(\n"
                + "  node\n"
                + "    [\"waterway\"=\"river\"]\n"
                + "    (" + formattedBoundingBox + ");\n"
                + "  way\n"
                + "    [\"waterway\"=\"river\"]\n"
                + "    (" + formattedBoundingBox + ");\n"
                + "  relation\n"
                + "    [\"waterway\"=\"river\"]\n"
                + "    (" + formattedBoundingBox + ");\n"
                + ");\n"
                + "out;\n"
                + ">;\n"
                + "out skel qt;";
        return queryData(formattedRequest);
    }
}
