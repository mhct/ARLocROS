package com.github.rosjava_catkin_package_a.ARLocROS;

import com.google.common.base.Optional;
import geometry_msgs.Point;
import geometry_msgs.PoseStamped;
import org.ros.message.Time;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.rosjava_geometry.Quaternion;

import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** @author Hoang Tung Dinh */
public final class FusedLocalization {
  private final PoseEstimator poseEstimator;
  private final VelocityEstimator velocityEstimator;
  private final Publisher<PoseStamped> posePublisher;
  private final ConnectedNode connectedNode;

  private FusedLocalization(
      PoseEstimator poseEstimator,
      VelocityEstimator velocityEstimator,
      Publisher<PoseStamped> posePublisher,
      double publishFrequency,
      ConnectedNode connectedNode) {
    this.poseEstimator = poseEstimator;
    this.velocityEstimator = velocityEstimator;
    this.posePublisher = posePublisher;
    this.connectedNode = connectedNode;

    final long publishRateInNanoSeconds = (long) (1.0E9 / publishFrequency);
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(
            new FuseAndPublishPose(), 0, publishRateInNanoSeconds, TimeUnit.NANOSECONDS);
  }

  public static FusedLocalization create(
      PoseEstimator poseEstimator,
      VelocityEstimator velocityEstimator,
      Publisher<PoseStamped> posePublisher,
      double publishFrequency,
      ConnectedNode connectedNode) {
    return new FusedLocalization(
        poseEstimator, velocityEstimator, posePublisher, publishFrequency, connectedNode);
  }

  private final class FuseAndPublishPose implements Runnable {
    @Nullable PoseStamped lastFusedPose;
    @Nullable PoseStamped lastRawPoseReceived;

    private FuseAndPublishPose() {}

    @Override
    public void run() {
      final Optional<PoseStamped> rawPoseStamped = poseEstimator.getMostRecentPose();
      final Optional<VelocityStamped> velocityStamped = velocityEstimator.getMostRecentVelocity();

      if (!rawPoseStamped.isPresent() || !velocityStamped.isPresent()) {
        return;
      }

      if (lastRawPoseReceived == null) {
        lastRawPoseReceived = rawPoseStamped.get();
      }

      if (lastFusedPose == null) {
        lastFusedPose = rawPoseStamped.get();
        posePublisher.publish(lastFusedPose);
        return;
      }

      if (!lastRawPoseReceived
          .getHeader()
          .getStamp()
          .equals(rawPoseStamped.get().getHeader().getStamp())) {
        lastRawPoseReceived = rawPoseStamped.get();
        lastFusedPose = lastRawPoseReceived;
      }

      final PoseStamped fusedPose = computeCurrentPose(velocityStamped.get());
      posePublisher.publish(fusedPose);
      lastFusedPose = fusedPose;
    }

    private PoseStamped computeCurrentPose(VelocityStamped velocityStamped) {
      final double lastYaw =
          EulerAngle.quaternionToEulerAngle(lastFusedPose.getPose().getOrientation()).angleZ();
      final Velocity inertialFrameVelocity =
          bodyFrameVelocityToInertialFrameVelocity(velocityStamped.velocity(), lastYaw);

      final Time currentTime = connectedNode.getCurrentTime();
      final double timeDeltaInSeconds =
          currentTime.subtract(lastFusedPose.getHeader().getStamp()).totalNsecs() / 1.0E09;

      final Point lastPosition = lastFusedPose.getPose().getPosition();
      final double newPosX = lastPosition.getX() + timeDeltaInSeconds * inertialFrameVelocity.x();
      final double newPosY = lastPosition.getY() + timeDeltaInSeconds * inertialFrameVelocity.y();
      final double newPosZ = lastPosition.getZ() + timeDeltaInSeconds * inertialFrameVelocity.z();
      final double newYaw = lastYaw + timeDeltaInSeconds * inertialFrameVelocity.yaw();

      final PoseStamped newPoseStamped = posePublisher.newMessage();
      newPoseStamped.getHeader().setFrameId("map");
      newPoseStamped.getHeader().setStamp(currentTime);

      newPoseStamped.getPose().getPosition().setX(newPosX);
      newPoseStamped.getPose().getPosition().setY(newPosY);
      newPoseStamped.getPose().getPosition().setZ(newPosZ);

      final geometry_msgs.Quaternion quaternion = newPoseStamped.getPose().getOrientation();
      newPoseStamped
          .getPose()
          .setOrientation(eulerToQuaternion(newYaw).toQuaternionMessage(quaternion));

      return newPoseStamped;
    }

    /**
     * Assume that roll and pitch are zero
     *
     * @param yaw
     * @return
     */
    public Quaternion eulerToQuaternion(double yaw) {
      return new Quaternion(0, 0, StrictMath.sin(yaw / 2), StrictMath.cos(yaw / 2));
    }

    public Velocity bodyFrameVelocityToInertialFrameVelocity(
        Velocity bodyFrameVelocity, double currentYaw) {
      // same linearZ
      final double linearZ = bodyFrameVelocity.z();
      // same angularZ
      final double angularZ = bodyFrameVelocity.yaw();

      final double theta = currentYaw;

      final double sin = StrictMath.sin(theta);
      final double cos = StrictMath.cos(theta);

      final double linearX = bodyFrameVelocity.x() * cos - bodyFrameVelocity.y() * sin;
      final double linearY = bodyFrameVelocity.x() * sin + bodyFrameVelocity.y() * cos;

      return Velocity.create(linearX, linearY, linearZ, angularZ);
    }
  }
}
