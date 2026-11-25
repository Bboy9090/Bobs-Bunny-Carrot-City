import Foundation

@main
struct VerifyBobsBunnyApplePort {
    static func main() {
        precondition(PlayableHero.allCases.count == 6, "Expected 6 canonical heroes")
        precondition(CarrotCityCatalog.stages.count == 20, "Expected 20 stages across 11 worlds")
        precondition(Set(CarrotCityCatalog.stages.map(\.worldNumber)).count == 11, "Expected all 11 worlds")
        precondition(CarrotLegendMove.allCases.count == 5, "Expected 5 Carrot Legend Moves")

        let varied = CarrotStyleComboEngine()
        _ = varied.recordMove(.earCopterSpin, swagMeter: 0.5)
        _ = varied.recordMove(.turboBurrowPop, swagMeter: 0.5)
        let r3 = varied.recordMove(.carrotRailGrind, swagMeter: 0.5)

        let spammed = CarrotStyleComboEngine()
        _ = spammed.recordMove(.earCopterSpin, swagMeter: 0.5)
        _ = spammed.recordMove(.earCopterSpin, swagMeter: 0.5)
        _ = spammed.recordMove(.earCopterSpin, swagMeter: 0.5)

        precondition(varied.score > spammed.score && r3.varietyCount == 3, "Variety must outscore spam")

        let engine = CarrotStyleComboEngine()
        let blocked = engine.recordMove(.carrotCrownFinale, swagMeter: 1.0)
        precondition(!blocked.accepted, "24K Carrot Finale must block below Rank A")

        for i in 0..<7 {
            let mv: CarrotLegendMove = i % 2 == 0 ? .carrotRailGrind : .quakeCarrotSlam
            _ = engine.recordMove(mv, swagMeter: 0.5)
        }
        precondition(engine.rank >= .a, "Expected Rank A+ after chained combo")

        let blockedSwag = engine.recordMove(.carrotCrownFinale, swagMeter: 0.4)
        precondition(!blockedSwag.accepted, "24K Carrot Finale must block below 85% swag")

        let finale = engine.recordMove(.carrotCrownFinale, swagMeter: 0.92)
        precondition(finale.accepted && finale.bonusCarrots == 15 && engine.rank == .legend, "Expected LEGEND finale")

        let peak = engine.peakRank
        engine.decay(rawDt: Double.nan)
        engine.decay(rawDt: -1.0)
        precondition(engine.score > 0, "Invalid dt must not decay score")
        engine.decay(rawDt: 30.0)
        precondition(engine.score == 0 && engine.rank == .d && engine.peakRank == peak, "Full decay must preserve peakRank")

        print("PASS: Bob's Bunny: Carrot City Apple Swift Engine & 20-Stage Catalog Verified")
    }
}
