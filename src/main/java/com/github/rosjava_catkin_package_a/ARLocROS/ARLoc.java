package com.github.rosjava_catkin_package_a.ARLocROS;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;

/**
 * @author Hoang Tung Dinh
 */
public class ARLoc extends AbstractNodeMain {
  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava/imshow");
  }
}
