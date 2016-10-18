/*
 * Copyright (C) 2016 Marvin Ferber.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package arlocros;

import org.opencv.core.Point3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MarkerConfig {

    private final Map<String, Marker> map = new HashMap<>();
    private final float patterntSize;

    private MarkerConfig(String configfile, String patternDirectory) {
        float size = 0;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(configfile));
            String currentConfigurationLine = bufferedReader.readLine();
            while ((currentConfigurationLine = bufferedReader.readLine()) != null) {
                size = readMarkerSizeFromLine(currentConfigurationLine);
                readPatternFromLine(patternDirectory, size, currentConfigurationLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	if (bufferedReader != null) {
        		try {
        			bufferedReader.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        }

        patterntSize = size;
    }

    public static MarkerConfig createFromConfig(String configfile, String patternDirectory) {
        return new MarkerConfig(configfile, patternDirectory);
    }

    private void readPatternFromLine(String patternDirectory, float size, String line) {
        if (line.contains("patt")) {
            try {
                final String[] configurationLineTokens = line.split(" ");
                if (configurationLineTokens.length != 4) {
                	throw new RuntimeException("Problem in patterns configuration file, at line: " + line);
                } else {
	                final float x = Float.parseFloat(configurationLineTokens[0]);
	                final float y = Float.parseFloat(configurationLineTokens[1]);
	                final float z = Float.parseFloat(configurationLineTokens[2]);
	
	                final String patternFilename = patternDirectory + configurationLineTokens[3];
	                final Marker marker = Marker.builder()
	                        .patternFile(patternFilename)
	                        .upperleft(new Point3(x, y, z))
	                        .upperright(new Point3(x, y - size, z))
	                        .lowerright(new Point3(x, y - size, z - size))
	                        .lowerleft(new Point3(x, y, z - size))
	                        .build();
	
	                map.put(patternFilename, marker);
	                // patternlist.add(pattern);
	                // System.out.println("Adding pattern "+pattern);
                }
            } catch (NumberFormatException e) {
                return;
            }
        }
    }

    private static float readMarkerSizeFromLine(String line) {
        float markerSize = 0.0f;
        if (line.contains("markersize")) {
            markerSize = Float.parseFloat(line.split(" ")[1]);
        }
        return markerSize;
    }

    public List<Point3> create3dpointlist(String string) {
        if (map != null && map.containsKey(string)) {
            final Marker marker = map.get(string);
            final List<Point3> list = new ArrayList<>();
            list.add(marker.upperleft());
            list.add(marker.upperright());
            list.add(marker.lowerright());
            list.add(marker.lowerleft());
            return list;
        } else {
            return null;
        }
    }

    public List<Point3> getUnordered3DPointList() {
        if (map == null) {
            return null;
        }
        List<Point3> list = new ArrayList<>();
        for (String string : map.keySet()) {
            Marker m = map.get(string);
            list.add(m.upperleft());
            list.add(m.upperright());
            list.add(m.lowerright());
            list.add(m.lowerleft());

        }
        return list;
    }

    public float getMarkerSize() {
        return patterntSize;
    }

    public List<String> getPatternFileList() {
        List<String> patternlist = new ArrayList<>();
        for (String pattern : map.keySet()) {
            patternlist.add(map.get(pattern).patternFile());
        }
        return patternlist;
    }

}