package com.bbn.landsar.geospatial;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoadsAndTrailsMetaData {
        public static final int ROAD = 1;
        public static final int TRAIL = 2;
        public static final int ROAD_OR_TRAIL = 3;
        public static final int NOT_ROAD_OR_TRAIL = 0;

        private String name;

        private final List<AbstractRoadsAndTrails.RoadsAndTrailsMetaDataItem> metaData;

        public RoadsAndTrailsMetaData() {
                super();
                metaData = new ArrayList<>();
        }

        public RoadsAndTrailsMetaData(String name) {
                super();
                this.name = name;
                metaData = new ArrayList<>();
        }

        public void addMetaDataItem(AbstractRoadsAndTrails.RoadsAndTrailsMetaDataItem item) {
                metaData.add(item);
        }

        public AbstractRoadsAndTrails.RoadsAndTrailsMetaDataItem getItem(int rtCode) {
                for (AbstractRoadsAndTrails.RoadsAndTrailsMetaDataItem item : metaData) {
                        if (item.rtCode == rtCode) return item;
                }
                return null;
        }

        public List<Integer> getRtCodes() {
                List<Integer> rtCodes = new ArrayList<Integer>();

                for (AbstractRoadsAndTrails.RoadsAndTrailsMetaDataItem item : metaData) {
                        rtCodes.add(item.rtCode);
                }
                return rtCodes;
        }

        public static RoadsAndTrailsMetaData getDefaultMetaData() {
                RoadsAndTrailsMetaData metaData =
                        new RoadsAndTrailsMetaData("Default");

                AbstractRoadsAndTrails.RoadsAndTrailsMetaDataItem item =
                        new AbstractRoadsAndTrails.RoadsAndTrailsMetaDataItem(NOT_ROAD_OR_TRAIL,
                                //							new Color(0), "Not road or trail");
                                255,255,255, "Not road or trail");
                metaData.addMetaDataItem(item);
                item = new AbstractRoadsAndTrails.RoadsAndTrailsMetaDataItem(ROAD_OR_TRAIL,
                       168,168,168, "Road or trail");
                metaData.addMetaDataItem(item);

                return metaData;
        }

        public void writeToFile(File outputFile) throws IOException {
                outputFile.getParentFile().mkdirs();
                try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))){

                        boolean firstLine = true;
                        for (Integer code : getRtCodes()) {
                                if (firstLine) {
                                        firstLine = false;
                                }
                                else {
                                        out.println();
                                }
                                out.print(getItem(code));
                        }
                        out.flush();
                }
        }
}
