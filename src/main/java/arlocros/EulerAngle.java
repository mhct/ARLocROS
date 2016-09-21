package arlocros;

import com.google.auto.value.AutoValue;
import geometry_msgs.Quaternion;

/**
 * A value class which stores the euler angle in three dimensions.
 *
 * @author Hoang Tung Dinh
 */
@AutoValue
public abstract class EulerAngle {

  EulerAngle() {}

  /**
   * Gets the builder of this class.
   *
   * @return a builder instance
   */
  public static Builder builder() {
    return new AutoValue_EulerAngle.Builder();
  }

  /**
   * Computes the distance between two angles. The distance will be in range [-pi, pi]. The distance
   * is negative if the {@code secondAngle} is on the left of the {@code firstAngle}.
   *
   * @param firstAngle the first angle
   * @param secondAngle the second angle
   * @return the distance between the two angles
   */
  public static double computeAngleDistance(double firstAngle, double secondAngle) {
    double distance = secondAngle - firstAngle;

    while (distance < -Math.PI) {
      distance += 2 * Math.PI;
    }

    while (distance > Math.PI) {
      distance -= 2 * Math.PI;
    }

    return distance;
  }

  public static EulerAngle quaternionToEulerAngle(Quaternion quaternion) {
    final double q0 = quaternion.getW();
    final double q1 = quaternion.getX();
    final double q2 = quaternion.getY();
    final double q3 = quaternion.getZ();

    final double eulerX = StrictMath.atan2(2 * (q0 * q1 + q2 * q3), 1 - 2 * (q1 * q1 + q2 * q2));
    final double eulerY = StrictMath.asin(2 * (q0 * q2 - q3 * q1));
    final double eulerZ = StrictMath.atan2(2 * (q0 * q3 + q1 * q2), 1 - 2 * (q2 * q2 + q3 * q3));

    return builder().setAngleX(eulerX).setAngleY(eulerY).setAngleZ(eulerZ).build();
  }

  /**
   * Gets the angle of the X rotation.
   *
   * @return the angle of the X rotation
   */
  public abstract double angleX();

  /**
   * Gets the angle of the Y rotation.
   *
   * @return the angle of the Y rotation
   */
  public abstract double angleY();

  /**
   * Gets the angle of the Z rotation.
   *
   * @return the angle of the Z rotation
   */
  public abstract double angleZ();

  /** The builder of the {@link EulerAngle} value class. */
  @AutoValue.Builder
  public abstract static class Builder {
    /**
     * Sets the angle of the X rotation.
     *
     * @param value the value to set
     * @return a reference to this Builder
     */
    public abstract Builder setAngleX(double value);

    /**
     * Sets the angle of the Y rotation.
     *
     * @param value the value to set
     * @return a reference to this Builder
     */
    public abstract Builder setAngleY(double value);

    /**
     * Sets the angle of the Z rotation.
     *
     * @param value the value to set
     * @return a reference to this Builder
     */
    public abstract Builder setAngleZ(double value);

    /**
     * Builds an {@link EulerAngle} instance.
     *
     * @return an {@link EulerAngle} instance
     */
    public abstract EulerAngle build();
  }
}
