package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D
import java.util.Timer
import java.util.TimerTask

const val EXPLOSIONDURATION: Long = 2000

class Explosion(
  initialPosition: Point2D,
  initialVelocity: Vector2D,
  radius: Double,
  mass: Double
) : SpaceObject("Explosion", '*', initialPosition, initialVelocity, radius, mass) {
  
  var isTriggered: Boolean = true
  
  private var timer: Timer = Timer()

  init {
    timer.schedule(object : TimerTask() {
        isTriggered = false
        timer.cancel()
    }, EXPLOSIONDURATION)
  }
}
