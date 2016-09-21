package arlocros;

import com.google.common.base.Optional;
import geometry_msgs.PoseStamped;

/** @author Hoang Tung Dinh */
public interface PoseEstimator {
  Optional<PoseStamped> getMostRecentPose();
}
