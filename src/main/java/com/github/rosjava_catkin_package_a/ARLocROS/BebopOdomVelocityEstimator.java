package com.github.rosjava_catkin_package_a.ARLocROS;

import com.google.common.base.Optional;
import geometry_msgs.Quaternion;
import nav_msgs.Odometry;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A velocity estimator using odometry of bebop drone. The linear velocity is got from the linear
 * twist message from bebop odom. The yaw velocity is calculated using two most recent poses got
 * from odom.
 *
 * @author Hoang Tung Dinh
 */
public final class BebopOdomVelocityEstimator
    implements VelocityEstimator, MessageObserver<Odometry> {

  private static final double DEFAULT_YAW = -9999;

  private final AtomicReference<VelocityStamped> mostRecentVelocity;
  @Nullable private Odometry lastOdom;

  private BebopOdomVelocityEstimator() {
    mostRecentVelocity = new AtomicReference<>();
  }

  public static BebopOdomVelocityEstimator create() {
    return new BebopOdomVelocityEstimator();
  }

  @Override
  public void onNewMessage(Odometry message) {
    if (lastOdom == null) {
      lastOdom = message;
      return;
    }

    final double yawVelocity = getYawVelocity(lastOdom, message);
    final Velocity velocity =
        Velocity.create(
            message.getTwist().getTwist().getLinear().getX(),
            message.getTwist().getTwist().getLinear().getY(),
            message.getTwist().getTwist().getLinear().getZ(),
            yawVelocity);
    final VelocityStamped velocityStamped =
        VelocityStamped.create(velocity, message.getHeader().getStamp());
    mostRecentVelocity.set(velocityStamped);
    lastOdom = message;
  }

  private static double getYawVelocity(Odometry lastOdom, Odometry currentOdom) {
    final double lastYaw = getYaw(lastOdom.getPose().getPose().getOrientation());
    final double currentYaw = getYaw(currentOdom.getPose().getPose().getOrientation());
    final double timeDelta =
        currentOdom.getHeader().getStamp().toSeconds()
            - lastOdom.getHeader().getStamp().toSeconds();

    return (currentYaw - lastYaw) / timeDelta;
  }

  private static double getYaw(Quaternion orientation) {
    return EulerAngle.quaternionToEulerAngle(orientation).angleZ();
  }

  @Override
  public Optional<VelocityStamped> getMostRecentVelocity() {
    final VelocityStamped velocityStamped = mostRecentVelocity.get();
    if (velocityStamped == null) {
      return Optional.absent();
    } else {
      return Optional.of(velocityStamped);
    }
  }
}
