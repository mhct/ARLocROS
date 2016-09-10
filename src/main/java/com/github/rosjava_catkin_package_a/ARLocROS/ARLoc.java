package com.github.rosjava_catkin_package_a.ARLocROS;

import geometry_msgs.PoseStamped;
import nav_msgs.Odometry;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/** @author Hoang Tung Dinh */
public class ARLoc extends AbstractNodeMain {
  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava/imshow");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
    final Parameter parameter = Parameter.createFromParameterTree(connectedNode.getParameterTree());
    final Publisher<PoseStamped> posePublisher =
        connectedNode.newPublisher(parameter.poseTopicName(), PoseStamped._TYPE);

    final PoseEstimator poseEstimator =
        ArMarkerPoseEstimator.create(connectedNode, parameter, posePublisher);

    final BebopOdomVelocityEstimator velocityEstimator = BebopOdomVelocityEstimator.create();

    final MessagesSubscriberService<Odometry> odomSubscriber =
        MessagesSubscriberService.create(
            connectedNode.<Odometry>newSubscriber("/bebop/odom", Odometry._TYPE));
    odomSubscriber.registerMessageObserver(velocityEstimator);

    final FusedLocalization fusedLocalization =
        FusedLocalization.create(
            poseEstimator, velocityEstimator, posePublisher, 40, connectedNode);
  }
}
