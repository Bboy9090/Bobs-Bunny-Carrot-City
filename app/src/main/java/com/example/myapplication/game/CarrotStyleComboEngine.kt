package com.example.myapplication.game

enum class CarrotStyleRank(
    val code: String,
    val title: String,
    val bonusMultiplier: Float,
    val minScore: Int
) {
    D("D", "Warmup Hop", 1.0f, 0),
    C("C", "Crisp Bounce", 1.05f, 45),
    B("B", "Groovy Dash", 1.10f, 100),
    A("A", "Carrot Ace", 1.18f, 175),
    S("S", "Golden Swag", 1.25f, 275),
    LEGEND("LEGEND", "Carrot City Legend", 1.35f, 420);

    companion object {
        fun fromScore(score: Int): CarrotStyleRank {
            val clamped = score.coerceIn(0, 1000)
            return entries.lastOrNull { clamped >= it.minScore } ?: D
        }
    }
}

enum class CarrotLegendMove(
    val id: String,
    val displayName: String,
    val basePoints: Int,
    val minRank: CarrotStyleRank,
    val bonusCarrots: Int
) {
    EAR_COPTER_SPIN("ear_copter_spin", "Ear-Copter Spin", 20, CarrotStyleRank.D, 2),
    TURBO_BURROW_POP("turbo_burrow_pop", "Turbo Burrow Pop", 30, CarrotStyleRank.C, 3),
    CARROT_RAIL_GRIND("carrot_rail_grind", "Carrot Rail Grind", 40, CarrotStyleRank.B, 4),
    QUAKE_CARROT_SLAM("quake_carrot_slam", "Quake Carrot Slam", 55, CarrotStyleRank.A, 6),
    CARROT_CROWN_FINALE("carrot_crown_finale", "24K Carrot Finale", 110, CarrotStyleRank.A, 15)
}

data class StyleMoveResult(
    val accepted: Boolean,
    val move: CarrotLegendMove,
    val pointsEarned: Int,
    val bonusCarrots: Int,
    val totalScore: Int,
    val rank: CarrotStyleRank,
    val varietyCount: Int,
    val reason: String = ""
)

class CarrotStyleComboEngine {
    var score: Int = 0
        private set
    var rank: CarrotStyleRank = CarrotStyleRank.D
        private set
    var peakRank: CarrotStyleRank = CarrotStyleRank.D
        private set
    var streak: Int = 0
        private set
    var decayTimerSeconds: Float = 0f
        private set
    var lastMove: CarrotLegendMove? = null
        private set
    var finaleCount: Int = 0
        private set

    private val recentMoves = ArrayDeque<CarrotLegendMove>(4)

    fun canTriggerFinale(swagMeter: Float): Boolean {
        return rank.ordinal >= CarrotStyleRank.A.ordinal &&
            swagMeter.isFinite() &&
            swagMeter >= 0.85f
    }

    fun recordMove(move: CarrotLegendMove, swagMeter: Float = 0.5f): StyleMoveResult {
        if (move == CarrotLegendMove.CARROT_CROWN_FINALE && !canTriggerFinale(swagMeter)) {
            return StyleMoveResult(
                accepted = false,
                move = move,
                pointsEarned = 0,
                bonusCarrots = 0,
                totalScore = score,
                rank = rank,
                varietyCount = recentMoves.toSet().size,
                reason = "Requires Rank A+ and 85%+ Swag Meter for 24K Carrot Finale."
            )
        }

        var repeatCount = 0
        for (i in recentMoves.indices.reversed()) {
            if (recentMoves[i] == move) {
                repeatCount++
            } else {
                break
            }
        }

        if (recentMoves.size >= 4) {
            recentMoves.removeFirst()
        }
        recentMoves.addLast(move)
        val varietyCount = recentMoves.toSet().size

        val freshnessMult = when {
            repeatCount >= 2 -> 0.30f
            repeatCount == 1 -> 0.55f
            varietyCount >= 3 -> 1.50f
            varietyCount >= 2 -> 1.25f
            else -> 1.0f
        }

        val pointsEarned = (move.basePoints * freshnessMult).toInt().coerceAtLeast(1)
        score = (score + pointsEarned).coerceIn(0, 1000)
        rank = CarrotStyleRank.fromScore(score)
        if (rank.ordinal > peakRank.ordinal) {
            peakRank = rank
        }
        streak += 1
        decayTimerSeconds = 3.5f
        lastMove = move
        if (move == CarrotLegendMove.CARROT_CROWN_FINALE) {
            finaleCount += 1
        }

        return StyleMoveResult(
            accepted = true,
            move = move,
            pointsEarned = pointsEarned,
            bonusCarrots = move.bonusCarrots,
            totalScore = score,
            rank = rank,
            varietyCount = varietyCount
        )
    }

    fun decay(rawDt: Float) {
        if (!rawDt.isFinite() || rawDt <= 0f) return
        if (score == 0 && decayTimerSeconds <= 0f) return

        if (decayTimerSeconds > rawDt) {
            decayTimerSeconds -= rawDt
            return
        }

        val excessDt = rawDt - decayTimerSeconds
        decayTimerSeconds = 0f
        val drain = (excessDt * 50f).toInt().coerceAtLeast(if (excessDt > 0f) 1 else 0)
        score = (score - drain).coerceAtLeast(0)
        rank = CarrotStyleRank.fromScore(score)
        if (score == 0) {
            streak = 0
            recentMoves.clear()
            lastMove = null
            rank = CarrotStyleRank.D
        }
    }

    fun reset() {
        score = 0
        rank = CarrotStyleRank.D
        peakRank = CarrotStyleRank.D
        streak = 0
        decayTimerSeconds = 0f
        lastMove = null
        finaleCount = 0
        recentMoves.clear()
    }
}
