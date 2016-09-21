package arlocros;

import com.google.common.base.Optional;

/** @author Hoang Tung Dinh */
public interface VelocityEstimator {
  Optional<VelocityStamped> getMostRecentVelocity();
}
