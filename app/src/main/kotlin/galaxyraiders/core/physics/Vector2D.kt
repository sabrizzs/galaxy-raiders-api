@file:Suppress("UNUSED_PARAMETER") // <- REMOVE
package galaxyraiders.core.physics

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties("unit", "normal", "degree", "magnitude")
data class Vector2D(val dx: Double, val dy: Double) {
  override fun toString(): String {
    return "Vector2D(dx=$dx, dy=$dy)"
  }

  val magnitude: Double
    get() = Math.sqrt(this.dx*this.dx+this.dy*this.dy)

  val radiant: Double
    get() {
      val tg = this.dy / this.dx
      val result = when (this.dx > 0) {
        true -> Math.atan(tg)
        else -> when (this.dy > 0) {
          true -> Math.atan(tg) + Math.PI
          else -> Math.atan(tg) - Math.PI
        }
      }
      return result
    }

  val degree: Double
    get() = Math.toDegrees(this.radiant)

  val unit: Vector2D
    get() = this/this.magnitude

  val normal: Vector2D
    get() = Vector2D(this.dy, -this.dx).unit

  operator fun times(scalar: Double): Vector2D {
    return Vector2D(dx*scalar, dy*scalar)
  }

  operator fun div(scalar: Double): Vector2D {
    return Vector2D(dx/scalar, dy/scalar)
  }

  operator fun times(v: Vector2D): Double {
    return this.dx * v.dx + this.dy * v.dy
  }

  operator fun plus(v: Vector2D): Vector2D {
    return Vector2D(this.dx + v.dx, this.dy + v.dy)
  }

  operator fun plus(p: Point2D): Point2D {
    return Point2D(this.dx + p.x, this.dy + p.y)
  }

  operator fun unaryMinus(): Vector2D {
    return Vector2D(-this.dx, -this.dy)
  }

  operator fun minus(v: Vector2D): Vector2D {
    return Vector2D(this.dx - v.dx, this.dy - v.dy)
  }

  fun scalarProject(target: Vector2D): Double {
    return (target * this)/target.magnitude
  }

  fun vectorProject(target: Vector2D): Vector2D {
    return this.scalarProject(target) * target.unit
  }
}

operator fun Double.times(v: Vector2D): Vector2D {
  return Vector2D(v.dx * this, v.dy * this)
}
