package com.github.rosjava_catkin_package_a.ARLocROS;

import com.google.auto.value.AutoValue;
import org.ros.message.Time;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class VelocityStamped {
  protected VelocityStamped() {}

  public static VelocityStamped create(Velocity velocity, Time timeStamp) {
    return new AutoValue_TwistStamped(velocity, timeStamp);
  }

  public abstract Velocity velocity();

  public abstract Time timeStamp();
}
