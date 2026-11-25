import Foundation
import SwiftUI

public enum CarrotStyleRank: String, CaseIterable, Codable, Sendable, Comparable {
    case d = "D"
    case c = "C"
    case b = "B"
    case a = "A"
    case s = "S"
    case legend = "LEGEND"

    public var code: String { rawValue }

    public var title: String {
        switch self {
        case .d: return "Warmup Hop"
        case .c: return "Crisp Bounce"
        case .b: return "Groovy Dash"
        case .a: return "Carrot Ace"
        case .s: return "Golden Swag"
        case .legend: return "Carrot City Legend"
        }
    }

    public var minScore: Int {
        switch self {
        case .d: return 0
        case .c: return 45
        case .b: return 100
        case .a: return 175
        case .s: return 275
        case .legend: return 420
        }
    }

    public var bonusMultiplier: Double {
        switch self {
        case .d: return 1.00
        case .c: return 1.05
        case .b: return 1.10
        case .a: return 1.18
        case .s: return 1.25
        case .legend: return 1.35
        }
    }

    private var orderIndex: Int {
        switch self {
        case .d: return 0
        case .c: return 1
        case .b: return 2
        case .a: return 3
        case .s: return 4
        case .legend: return 5
        }
    }

    public static func < (lhs: CarrotStyleRank, rhs: CarrotStyleRank) -> Bool {
        lhs.orderIndex < rhs.orderIndex
    }

    public static func fromScore(_ score: Int) -> CarrotStyleRank {
        let clamped = max(0, min(1000, score))
        return allCases.last(where: { clamped >= $0.minScore }) ?? .d
    }
}

public enum CarrotLegendMove: String, CaseIterable, Codable, Identifiable, Sendable {
    case earCopterSpin = "ear_copter_spin"
    case turboBurrowPop = "turbo_burrow_pop"
    case carrotRailGrind = "carrot_rail_grind"
    case quakeCarrotSlam = "quake_carrot_slam"
    case carrotCrownFinale = "carrot_crown_finale"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .earCopterSpin: return "Ear-Copter Spin"
        case .turboBurrowPop: return "Turbo Burrow Pop"
        case .carrotRailGrind: return "Carrot Rail Grind"
        case .quakeCarrotSlam: return "Quake Carrot Slam"
        case .carrotCrownFinale: return "24K Carrot Finale"
        }
    }

    public var basePoints: Int {
        switch self {
        case .earCopterSpin: return 20
        case .turboBurrowPop: return 30
        case .carrotRailGrind: return 40
        case .quakeCarrotSlam: return 55
        case .carrotCrownFinale: return 110
        }
    }

    public var bonusCarrots: Int {
        switch self {
        case .earCopterSpin: return 2
        case .turboBurrowPop: return 3
        case .carrotRailGrind: return 4
        case .quakeCarrotSlam: return 6
        case .carrotCrownFinale: return 15
        }
    }
}

public struct StyleMoveResult: Equatable, Sendable {
    public let accepted: Bool
    public let move: CarrotLegendMove
    public let pointsEarned: Int
    public let bonusCarrots: Int
    public let totalScore: Int
    public let rank: CarrotStyleRank
    public let varietyCount: Int
    public let reason: String
}

public final class CarrotStyleComboEngine {
    public private(set) var score: Int = 0
    public private(set) var rank: CarrotStyleRank = .d
    public private(set) var peakRank: CarrotStyleRank = .d
    public private(set) var streak: Int = 0
    public private(set) var decayTimerSeconds: Double = 0
    public private(set) var lastMove: CarrotLegendMove? = nil
    public private(set) var finaleCount: Int = 0
    private var recentMoves: [CarrotLegendMove] = []

    public init() {}

    public func canTriggerFinale(swagMeter: Double) -> Bool {
        rank >= .a && swagMeter.isFinite && swagMeter >= 0.85
    }

    @discardableResult
    public func recordMove(_ move: CarrotLegendMove, swagMeter: Double = 0.5) -> StyleMoveResult {
        if move == .carrotCrownFinale && !canTriggerFinale(swagMeter: swagMeter) {
            return StyleMoveResult(
                accepted: false,
                move: move,
                pointsEarned: 0,
                bonusCarrots: 0,
                totalScore: score,
                rank: rank,
                varietyCount: Set(recentMoves).count,
                reason: "Requires Rank A+ and 85%+ Swag Meter for 24K Carrot Finale."
            )
        }

        var repeatCount = 0
        for prev in recentMoves.reversed() {
            if prev == move {
                repeatCount += 1
            } else {
                break
            }
        }

        if recentMoves.count >= 4 {
            recentMoves.removeFirst()
        }
        recentMoves.append(move)
        let varietyCount = Set(recentMoves).count

        let freshnessMult: Double
        if repeatCount >= 2 {
            freshnessMult = 0.30
        } else if repeatCount == 1 {
            freshnessMult = 0.55
        } else if varietyCount >= 3 {
            freshnessMult = 1.50
        } else if varietyCount >= 2 {
            freshnessMult = 1.25
        } else {
            freshnessMult = 1.00
        }

        let pointsEarned = max(1, Int(Double(move.basePoints) * freshnessMult))
        score = max(0, min(1000, score + pointsEarned))
        rank = CarrotStyleRank.fromScore(score)
        if rank > peakRank {
            peakRank = rank
        }
        streak += 1
        decayTimerSeconds = 3.5
        lastMove = move
        if move == .carrotCrownFinale {
            finaleCount += 1
        }

        return StyleMoveResult(
            accepted: true,
            move: move,
            pointsEarned: pointsEarned,
            bonusCarrots: move.bonusCarrots,
            totalScore: score,
            rank: rank,
            varietyCount: varietyCount,
            reason: ""
        )
    }

    public func decay(rawDt: Double) {
        guard rawDt.isFinite && rawDt > 0 else { return }
        if score == 0 && decayTimerSeconds <= 0 { return }

        if decayTimerSeconds > rawDt {
            decayTimerSeconds -= rawDt
            return
        }

        let excessDt = rawDt - decayTimerSeconds
        decayTimerSeconds = 0
        let drain = max(excessDt > 0 ? 1 : 0, Int(excessDt * 50.0))
        score = max(0, score - drain)
        rank = CarrotStyleRank.fromScore(score)
        if score == 0 {
            streak = 0
            recentMoves.removeAll()
            lastMove = nil
            rank = .d
        }
    }

    public func reset() {
        score = 0
        rank = .d
        peakRank = .d
        streak = 0
        decayTimerSeconds = 0
        lastMove = nil
        finaleCount = 0
        recentMoves.removeAll()
    }
}

public enum PlayableHero: String, CaseIterable, Identifiable, Codable, Sendable {
    case bobsBunny = "hero_bobs"
    case brickBadger = "hero_brick_badger"
    case roxySparks = "hero_roxy_fox"
    case goldenBobsChampion = "hero_golden_bobs"
    case cosmicEmpressBunzilla = "hero_empress_bunzilla"
    case reformedDonFoxone = "hero_don_foxone"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .bobsBunny: return "Bobs Bunny"
        case .brickBadger: return "Brick the Badger"
        case .roxySparks: return "Roxy Sparks"
        case .goldenBobsChampion: return "24K Golden Bobs"
        case .cosmicEmpressBunzilla: return "Redeemed Bun-Zilla"
        case .reformedDonFoxone: return "Don Foxone (VIP Ally)"
        }
    }

    public var specialMoveLabel: String {
        switch self {
        case .bobsBunny: return "TROLL DASH"
        case .brickBadger: return "QUAKE SLAM"
        case .roxySparks: return "GRAPPLE ZIP"
        case .goldenBobsChampion: return "RAINBOW BRIDGE"
        case .cosmicEmpressBunzilla: return "STARLIGHT NOVA"
        case .reformedDonFoxone: return "STEAMROLLER BLITZ"
        }
    }

    public var accentColor: Color {
        switch self {
        case .bobsBunny: return Color(red: 1.0, green: 0.43, blue: 0.0)
        case .brickBadger: return Color(red: 1.0, green: 0.84, blue: 0.0)
        case .roxySparks: return Color(red: 0.0, green: 0.90, blue: 1.0)
        case .goldenBobsChampion: return Color(red: 1.0, green: 0.84, blue: 0.0)
        case .cosmicEmpressBunzilla: return Color(red: 0.88, green: 0.25, blue: 0.98)
        case .reformedDonFoxone: return Color(red: 0.0, green: 0.90, blue: 0.46)
        }
    }
}

public struct CarrotStageDescriptor: Identifiable, Hashable, Sendable {
    public let id: Int
    public let worldNumber: Int
    public let code: String
    public let title: String
    public let parTimeSeconds: Int
    public let isBossStage: Bool
}

public enum CarrotCityCatalog {
    public static let stages: [CarrotStageDescriptor] = [
        CarrotStageDescriptor(id: 1, worldNumber: 1, code: "W1-1", title: "Overgrown Outskirts", parTimeSeconds: 38, isBossStage: false),
        CarrotStageDescriptor(id: 2, worldNumber: 1, code: "W1-2", title: "Neon Rooftop Run", parTimeSeconds: 42, isBossStage: false),
        CarrotStageDescriptor(id: 3, worldNumber: 1, code: "W1-3", title: "Metro Burrow Express", parTimeSeconds: 45, isBossStage: false),
        CarrotStageDescriptor(id: 4, worldNumber: 1, code: "W1-4", title: "Construction Sky-High", parTimeSeconds: 48, isBossStage: false),
        CarrotStageDescriptor(id: 5, worldNumber: 1, code: "W1-RIFT", title: "The Secret Golden Rift", parTimeSeconds: 42, isBossStage: false),
        CarrotStageDescriptor(id: 6, worldNumber: 1, code: "W1-BOSS", title: "Don Foxone's Steamroller Juicer", parTimeSeconds: 36, isBossStage: true),
        CarrotStageDescriptor(id: 7, worldNumber: 2, code: "W2-1", title: "San Francis-Kale Bay Bridge", parTimeSeconds: 42, isBossStage: false),
        CarrotStageDescriptor(id: 8, worldNumber: 3, code: "W3-1", title: "Toky-Oat Neon Beat", parTimeSeconds: 44, isBossStage: false),
        CarrotStageDescriptor(id: 9, worldNumber: 4, code: "W4-1", title: "Rome-Aine Colos-Spud", parTimeSeconds: 45, isBossStage: false),
        CarrotStageDescriptor(id: 10, worldNumber: 5, code: "W5-BOSS", title: "Lond-Onion & The Great Furtini", parTimeSeconds: 40, isBossStage: true),
        CarrotStageDescriptor(id: 11, worldNumber: 6, code: "W6-1", title: "Mount Vesuvi-Apple Eruption", parTimeSeconds: 44, isBossStage: false),
        CarrotStageDescriptor(id: 12, worldNumber: 7, code: "W7-1", title: "Rio de Jalapeño Carnival", parTimeSeconds: 43, isBossStage: false),
        CarrotStageDescriptor(id: 13, worldNumber: 8, code: "W8-BOSS", title: "Chicag-Artichoke & Chief Wrench Fox", parTimeSeconds: 42, isBossStage: true),
        CarrotStageDescriptor(id: 14, worldNumber: 9, code: "W9-FINAL", title: "Cosmic Hare-Fortress: Empress Bun-Zilla", parTimeSeconds: 50, isBossStage: true),
        CarrotStageDescriptor(id: 15, worldNumber: 10, code: "W10-1", title: "Golden Forest & Rainbow Highway", parTimeSeconds: 42, isBossStage: false),
        CarrotStageDescriptor(id: 16, worldNumber: 10, code: "W10-2", title: "Palace of the Ancient Carrot", parTimeSeconds: 45, isBossStage: false),
        CarrotStageDescriptor(id: 17, worldNumber: 10, code: "W10-CROWN", title: "The 24-Karat Crown Run", parTimeSeconds: 52, isBossStage: true),
        CarrotStageDescriptor(id: 18, worldNumber: 10, code: "W10-RUSH", title: "Grand Boss Rush Pantheons", parTimeSeconds: 75, isBossStage: true),
        CarrotStageDescriptor(id: 19, worldNumber: 11, code: "W11-1", title: "Inkwell Studio & Mirror Dimension", parTimeSeconds: 43, isBossStage: false),
        CarrotStageDescriptor(id: 20, worldNumber: 11, code: "W11-OMEGA", title: "The Animator's Pencil & Omega Foxtron", parTimeSeconds: 55, isBossStage: true),
    ]
}
