package arlocros;

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
    final Parameter parameter = Parameter.createFrom(connectedNode.getParameterTree());
    final Publisher<PoseStamped> markerPosePubliser =
        connectedNode.newPublisher(parameter.markerPoseTopicName(), PoseStamped._TYPE);

    final PoseEstimator poseEstimator =
        ArMarkerPoseEstimator.create(connectedNode, parameter, markerPosePubliser);

    final BebopOdomVelocityEstimator velocityEstimator = BebopOdomVelocityEstimator.create();

    final MessagesSubscriberService<Odometry> odomSubscriber =
        MessagesSubscriberService.create(
            connectedNode.<Odometry>newSubscriber("/bebop/odom", Odometry._TYPE));
    odomSubscriber.registerMessageObserver(velocityEstimator);

    final Publisher<PoseStamped> fusedPosePublisher =
        connectedNode.newPublisher(parameter.fusedPoseTopicName(), PoseStamped._TYPE);

    final FusedLocalization fusedLocalization =
        FusedLocalization.create(
            poseEstimator, velocityEstimator, fusedPosePublisher, 40, connectedNode);
  }
}
