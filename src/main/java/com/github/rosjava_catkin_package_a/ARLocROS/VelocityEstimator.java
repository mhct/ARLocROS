package com.github.rosjava_catkin_package_a.ARLocROS;

import com.google.common.base.Optional;

/** @author Hoang Tung Dinh */
public interface VelocityEstimator {
  Optional<VelocityStamped> getMostRecentVelocity();
}
