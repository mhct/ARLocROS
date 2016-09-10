package com.github.rosjava_catkin_package_a.ARLocROS;

import com.google.auto.value.AutoValue;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class Velocity {

  protected Velocity() {}

  public static Velocity create(double x, double y, double z, double yaw) {
    return new AutoValue_Velocity(x, y, z, yaw);
  }

  abstract double x();

  abstract double y();

  abstract double z();

  abstract double yaw();
}
