package arlocros;

import com.google.auto.value.AutoValue;
import org.ros.message.Time;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class VelocityStamped {
  protected VelocityStamped() {}

  public static VelocityStamped create(Velocity velocity, Time timeStamp) {
    return new AutoValue_VelocityStamped(velocity, timeStamp);
  }

  public abstract Velocity velocity();

  public abstract Time timeStamp();
}
