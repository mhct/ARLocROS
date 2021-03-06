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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.opencv.core.Point3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MarkerConfig {

  private static final Logger logger = LoggerFactory.getLogger(MarkerConfig.class);

  private final Map<String, Marker> map = new HashMap<>();
  private final float patterntSize;

  private MarkerConfig(String configfile, String patternDirectory) {
    float size = 0;
    try {
      final Yaml yaml = new Yaml();
      final InputStream input = new FileInputStream(new File(configfile));
      final Map<String, Object> config = (Map<String, Object>) yaml.load(input);
      input.close();

      size = (float) (double) config.get("marker_size");
      logger.info("Markers size is: {}", size);
      final Map<String, List<Double>> relativeCornerPosition =
          (Map<String, List<Double>>) config.get("relative_corner_position");
      final Map<String, List<Double>> markers = (Map<String, List<Double>>) config.get("markers");

      for (final Map.Entry<String, List<Double>> entry : markers.entrySet()) {
        final String pattern = patternDirectory + entry.getKey();
        final List<Double> pos = entry.getValue();
        final Marker marker =
            Marker.builder()
                .patternFile(pattern)
                .upperleft(
                    new Point3(
                        pos.get(0) + relativeCornerPosition.get("upper_left").get(0),
                        pos.get(1) + relativeCornerPosition.get("upper_left").get(1),
                        pos.get(2) + relativeCornerPosition.get("upper_left").get(2)))
                .upperright(
                    new Point3(
                        pos.get(0) + relativeCornerPosition.get("upper_right").get(0),
                        pos.get(1) + relativeCornerPosition.get("upper_right").get(1),
                        pos.get(2) + relativeCornerPosition.get("upper_right").get(2)))
                .lowerright(
                    new Point3(
                        pos.get(0) + relativeCornerPosition.get("lower_right").get(0),
                        pos.get(1) + relativeCornerPosition.get("lower_right").get(1),
                        pos.get(2) + relativeCornerPosition.get("lower_right").get(2)))
                .lowerleft(
                    new Point3(
                        pos.get(0) + relativeCornerPosition.get("lower_left").get(0),
                        pos.get(1) + relativeCornerPosition.get("lower_left").get(1),
                        pos.get(2) + relativeCornerPosition.get("lower_left").get(2)))
                .build();

        map.put(pattern, marker);
      }
    } catch (IOException e) {
      logger.info("Exception while reading marker configs.", e);
    }

    patterntSize = size;
  }

  public static MarkerConfig createFromConfig(String configfile, String patternDirectory) {
    return new MarkerConfig(configfile, patternDirectory);
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
