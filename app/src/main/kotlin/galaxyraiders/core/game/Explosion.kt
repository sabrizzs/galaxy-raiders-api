package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D

class Explosion(
  initialPosition: Point2D,
  initialVelocity: Vector2D,
  radius: Double,
  mass: Double,
  var isTriggered: Int = 0,
  var limit: Int = 50,
) : SpaceObject("Explosion", '*', initialPosition, initialVelocity, radius, mass) {

  fun desapeareExplosion(): Boolean {
    isTriggered += 1
    return isTriggered < limit
  }
}
