package galaxyraiders.core.score

import java.util.Date

data class Score(
    var beginDate: Date = Date(),
    var finalScore: Double = 0.0,
    var asteroidsDestroyed: Int = 0
)
