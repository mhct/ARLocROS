package com.github.rosjava_catkin_package_a.ARLocROS;

import com.google.common.base.Optional;
import geometry_msgs.PoseStamped;

/** @author Hoang Tung Dinh */
public interface StateEstimator {
  Optional<PoseStamped> getMostRecentPose();
}
