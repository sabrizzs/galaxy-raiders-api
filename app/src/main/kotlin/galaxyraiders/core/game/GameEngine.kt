package galaxyraiders.core.game

import galaxyraiders.Config
import galaxyraiders.ports.RandomGenerator
import galaxyraiders.ports.ui.Controller
import galaxyraiders.ports.ui.Controller.PlayerCommand
import galaxyraiders.ports.ui.Visualizer
import kotlin.system.measureTimeMillis

//2.2
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import galaxyraiders.core.score.Score
import java.io.File
//import java.util.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.LocalDate
import java.util.Date

const val MILLISECONDS_PER_SECOND: Int = 1000

object GameEngineConfig {
  private val config = Config(prefix = "GR__CORE__GAME__GAME_ENGINE__")

  val frameRate = config.get<Int>("FRAME_RATE")
  val spaceFieldWidth = config.get<Int>("SPACEFIELD_WIDTH")
  val spaceFieldHeight = config.get<Int>("SPACEFIELD_HEIGHT")
  val asteroidProbability = config.get<Double>("ASTEROID_PROBABILITY")
  val coefficientRestitution = config.get<Double>("COEFFICIENT_RESTITUTION")
  val msPerFrame: Int = MILLISECONDS_PER_SECOND / this.frameRate
}

@Suppress("TooManyFunctions")
class GameEngine(
  val generator: RandomGenerator,
  val controller: Controller,
  val visualizer: Visualizer,
) {
  val field = SpaceField(
    width = GameEngineConfig.spaceFieldWidth,
    height = GameEngineConfig.spaceFieldHeight,
    generator = generator
  )

  var playing = true
	
  var mapper = ObjectMapper()
  var newPlayer = true
	
  fun execute() {
    while (true) {
      val duration = measureTimeMillis { this.tick() }

      Thread.sleep(
        maxOf(0, GameEngineConfig.msPerFrame - duration)
      )
    }
  }

  fun execute(maxIterations: Int) {
    repeat(maxIterations) {
      this.tick()
    }
  }

  fun tick() {
    this.processPlayerInput()
    this.updateSpaceObjects()
    this.renderSpaceField()
  }

  fun processPlayerInput() {
    this.controller.nextPlayerCommand()?.also {
      when (it) {
        PlayerCommand.MOVE_SHIP_UP ->
          this.field.ship.boostUp()
        PlayerCommand.MOVE_SHIP_DOWN ->
          this.field.ship.boostDown()
        PlayerCommand.MOVE_SHIP_LEFT ->
          this.field.ship.boostLeft()
        PlayerCommand.MOVE_SHIP_RIGHT ->
          this.field.ship.boostRight()
        PlayerCommand.LAUNCH_MISSILE ->
          this.field.generateMissile()
        PlayerCommand.PAUSE_GAME ->
          this.playing = !this.playing
      }
    }
  }

  fun updateSpaceObjects() {
    if (!this.playing) return
    this.handleCollisions()
    this.moveSpaceObjects()
    this.trimSpaceObjects()
    this.generateAsteroids()
    this.updateScoreBoard() //2.2
    this.updateLeaderboard() //2.2
  }
  
  //2.2
  fun updateLeaderboard() {
    val scoreboardFile = File("./src/main/kotlin/galaxyraiders/core/score/Scoreboard.json")
    val leaderboardFile = File("./src/main/kotlin/galaxyraiders/core/score/Leaderboard.json")
    
    var scoreList: MutableList<Score> = mapper.readValue(scoreboardFile, mapper.typeFactory.constructCollectionType(MutableList::class.java, Score::class.java))
    
    scoreList.sortByDescending{it.score}

    val leaderboardList: List<Score> = if(scoreList.size > 3){
        scoreList.take(3)
    }else{
        scoreList
    }
    mapper.writeValue(leaderboardFile, leaderboardList)
  }

  //2.2
  fun updateScoreBoard() {
    val scoreboardFile = File("./src/main/kotlin/galaxyraiders/core/score/Scoreboard.json")
    
    var scoreList: MutableList<Score> = mapper.readValue(scoreboardFile, mapper.typeFactory.constructCollectionType(MutableList::class.java, Score::class.java))

    if(newPlayer){
      val currentScore = Score(Date(), this.field.score, this.field.explodedAsteroids)
      scoreList.add(currentScore)
      newPlayer = false
    }else{
      val lastScore = scoreList.last()
        lastScore.apply {
            score = field.score
            asteroidsDestroyed = field.explodedAsteroids + 1
        }
    }
    mapper.writeValue(scoreboardFile, scoreList)
  }

  fun handleCollisions() {
    this.field.spaceObjects.forEachPair {
        (first, second) ->
      missileCollidesWithAsteroid(first, second)
    }
  }

  private fun missileCollidesWithAsteroid(first: SpaceObject, second: SpaceObject) {
    var missile: Missile
    var asteroid: Asteroid
    if (!first.impacts(second)) return
    first.collideWith(second, GameEngineConfig.coefficientRestitution)
    if (first is Missile && second is Asteroid) {
      missile = first
      asteroid = second
      this.field.generateExplosion(missile, asteroid)
    }
    if (first is Asteroid && second is Missile) {
      missile = second
      asteroid = first
      this.field.generateExplosion(missile, asteroid)
    }
  }

  fun moveSpaceObjects() {
    this.field.moveShip()
    this.field.moveAsteroids()
    this.field.moveMissiles()
  }

  fun trimSpaceObjects() {
    this.field.trimAsteroids()
    this.field.trimMissiles()
    this.field.trimExplosions()
  }

  fun generateAsteroids() {
    val probability = generator.generateProbability()

    if (probability <= GameEngineConfig.asteroidProbability) {
      this.field.generateAsteroid()
    }
  }

  fun renderSpaceField() {
    this.visualizer.renderSpaceField(this.field)
  }
}

fun <T> List<T>.forEachPair(action: (Pair<T, T>) -> Unit) {
  for (i in 0 until this.size) {
    for (j in i + 1 until this.size) {
      action(Pair(this[i], this[j]))
    }
  }
}
