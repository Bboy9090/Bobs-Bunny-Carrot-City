import SwiftUI
#if os(macOS)
import AppKit
#endif

private enum CarrotSoundFX {
    static func play(muted: Bool, isFinale: Bool = false) {
        guard !muted else { return }
        #if os(macOS)
        let soundName = NSSound.Name(isFinale ? "Glass" : "Pop")
        NSSound(named: soundName)?.play()
        #endif
    }
}

@main
struct BobsBunnyCarrotCityAppleApp: App {
    var body: some Scene {
        WindowGroup("Bob's Bunny: Carrot City — Apple Edition") {
            BobsBunnyAppleRootView()
                .preferredColorScheme(.dark)
        }
    }
}

struct BobsBunnyAppleRootView: View {
    @AppStorage("bb_carrots_v1") private var totalCarrots: Int = 95
    @AppStorage("bb_shards_v1") private var totalShards: Int = 3
    @AppStorage("bb_peak_rank_v1") private var savedPeakRank: String = "D"
    @AppStorage("bb_sfx_muted_v1") private var sfxMuted: Bool = false

    @State private var selectedHero: PlayableHero = .bobsBunny
    @State private var selectedStage: CarrotStageDescriptor = CarrotCityCatalog.stages[0]
    @State private var styleEngine = CarrotStyleComboEngine()
    @State private var swagMeter: Double = 0.55
    @State private var stageProgress: Double = 0.25
    @State private var runnerBobPhase: Double = 0
    @State private var lastActionBanner: String = "Tap any Legend Move or press buttons to chain combos & fill Swag!"
    @State private var styleScoreDisplay: Int = 0
    @State private var styleRankCodeDisplay: String = "D"
    @State private var styleRankTitleDisplay: String = "Warmup Hop"

    private var finaleReady: Bool {
        styleEngine.canTriggerFinale(swagMeter: swagMeter)
    }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color(red: 0.08, green: 0.05, blue: 0.17), Color(red: 0.03, green: 0.04, blue: 0.09)],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    headerHUD
                    stageCanvasCard
                    heroAndWorldSelectors
                    legendStyleDeck
                }
                .padding(16)
            }
        }
        #if os(macOS)
        .frame(minWidth: 960, minHeight: 680)
        #endif
    }

    private var headerHUD: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("BOB'S BUNNY: CARROT CITY")
                        .font(.system(size: 18, weight: .black, design: .rounded))
                        .foregroundStyle(Color(red: 1.0, green: 0.84, blue: 0.0))
                    Text("\(selectedStage.code) • \(selectedStage.title) (World \(selectedStage.worldNumber))")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(Color(red: 0.0, green: 0.90, blue: 1.0))
                }
                Spacer()
                HStack(spacing: 10) {
                    badgePill("🥕 \(totalCarrots)", tint: .orange)
                    badgePill("🌟 \(totalShards)", tint: .yellow)
                    badgePill("\(styleRankCodeDisplay) • \(styleScoreDisplay) PTS", tint: .purple)
                    badgePill("Peak \(savedPeakRank)", tint: .mint)
                    Button {
                        sfxMuted.toggle()
                        lastActionBanner = sfxMuted ? "Sound FX muted (M)." : "Sound FX enabled (M)."
                        CarrotSoundFX.play(muted: sfxMuted)
                    } label: {
                        Text(sfxMuted ? "🔇 SFX OFF (M)" : "🔊 SFX ON (M)")
                            .font(.system(size: 10.5, weight: .bold, design: .monospaced))
                            .padding(.horizontal, 8).padding(.vertical, 4)
                            .background((sfxMuted ? Color.gray : Color.cyan).opacity(0.24), in: Capsule())
                            .foregroundStyle(sfxMuted ? .white.opacity(0.75) : .cyan)
                    }
                    .buttonStyle(.plain)
                    .keyboardShortcut("m", modifiers: [])
                    .accessibilityLabel("Toggle sound effects mute")
                }
            }
            Text(lastActionBanner)
                .font(.system(size: 11.5, design: .monospaced))
                .foregroundStyle(.white.opacity(0.85))
                .accessibilityLabel("Stage status: \(lastActionBanner)")
        }
        .padding(12)
        .background(Color.black.opacity(0.45), in: RoundedRectangle(cornerRadius: 12))
    }

    private var stageCanvasCard: some View {
        VStack(spacing: 8) {
            TimelineView(.animation(minimumInterval: 1.0 / 30.0)) { timeline in
                let t = timeline.date.timeIntervalSinceReferenceDate
                Canvas { ctx, size in
                    let sky = Gradient(colors: [
                        Color(red: 0.11, green: 0.25, blue: 0.56),
                        Color(red: 0.42, green: 0.17, blue: 0.54)
                    ])
                    ctx.fill(
                        Path(CGRect(origin: .zero, size: size)),
                        with: .linearGradient(sky, startPoint: .zero, endPoint: CGPoint(x: 0, y: size.height))
                    )

                    let groundY = size.height * 0.76
                    ctx.fill(
                        Path(CGRect(x: 0, y: groundY, width: size.width, height: size.height - groundY)),
                        with: .color(Color(red: 0.28, green: 0.18, blue: 0.14))
                    )
                    ctx.fill(
                        Path(CGRect(x: 0, y: groundY, width: size.width, height: 8)),
                        with: .color(Color(red: 0.0, green: 0.90, blue: 0.46))
                    )

                    let hopY = abs(sin(t * 4.5 + runnerBobPhase)) * 22.0
                    let heroX = size.width * CGFloat(max(0.12, min(0.86, stageProgress)))
                    let heroRect = CGRect(x: heroX - 18, y: groundY - 46 - hopY, width: 36, height: 42)
                    ctx.fill(Path(roundedRect: heroRect, cornerRadius: 10), with: .color(selectedHero.accentColor))

                    let ear1 = CGRect(x: heroX - 10, y: heroRect.minY - 18, width: 8, height: 18)
                    let ear2 = CGRect(x: heroX + 2, y: heroRect.minY - 18, width: 8, height: 18)
                    ctx.fill(Path(roundedRect: ear1, cornerRadius: 4), with: .color(.yellow))
                    ctx.fill(Path(roundedRect: ear2, cornerRadius: 4), with: .color(.yellow))
                }
                .frame(height: 185)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }

            HStack(spacing: 12) {
                Text("SWAG \(Int(swagMeter * 100))% • \(styleRankTitleDisplay)")
                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                    .foregroundStyle(.white.opacity(0.9))
                ProgressView(value: swagMeter)
                    .tint(finaleReady ? .yellow : .cyan)
                Button("Hop Boost (Space)") {
                    swagMeter = min(1.0, swagMeter + 0.12)
                    stageProgress = min(0.86, stageProgress + 0.06)
                    runnerBobPhase += 1.2
                    CarrotSoundFX.play(muted: sfxMuted)
                    syncState("Turbo hop boost engaged! Swag \(Int(swagMeter * 100))%.")
                }
                .buttonStyle(.borderedProminent)
                .keyboardShortcut(.space, modifiers: [])
                .accessibilityLabel("Hop boost forward")

                Button("Reset Run") {
                    styleEngine.reset()
                    swagMeter = 0.55
                    stageProgress = 0.25
                    syncState("Style chain reset for fresh run.")
                }
                .buttonStyle(.bordered)
                .accessibilityLabel("Reset current style run")
            }
        }
        .padding(12)
        .background(Color.black.opacity(0.35), in: RoundedRectangle(cornerRadius: 12))
    }

    private var heroAndWorldSelectors: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("SELECT HERO (6 CANONICAL ALLIES)")
                .font(.system(size: 10.5, weight: .heavy, design: .monospaced))
                .foregroundStyle(.yellow)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(PlayableHero.allCases) { hero in
                        let picked = selectedHero == hero
                        Button(hero.displayName) {
                            selectedHero = hero
                            lastActionBanner = "\(hero.displayName) active — Special: \(hero.specialMoveLabel)"
                        }
                        .buttonStyle(.plain)
                        .font(.system(size: 11, weight: .bold))
                        .padding(.horizontal, 10).padding(.vertical, 6)
                        .background(picked ? hero.accentColor : Color.white.opacity(0.10), in: Capsule())
                        .foregroundStyle(picked ? .black : .white)
                        .accessibilityLabel("Select Hero \(hero.displayName)")
                    }
                }
            }

            Text("11 WORLDS / 20 STAGES")
                .font(.system(size: 10.5, weight: .heavy, design: .monospaced))
                .foregroundStyle(.cyan)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(CarrotCityCatalog.stages) { stage in
                        let active = selectedStage.id == stage.id
                        Button("\(stage.code) \(stage.title)") {
                            selectedStage = stage
                            stageProgress = 0.18
                            lastActionBanner = "Entered \(stage.code): \(stage.title) (Par \(stage.parTimeSeconds)s)"
                        }
                        .buttonStyle(.plain)
                        .font(.system(size: 10.5, weight: .bold))
                        .padding(.horizontal, 9).padding(.vertical, 5)
                        .background(active ? Color.cyan : Color.white.opacity(0.08), in: Capsule())
                        .foregroundStyle(active ? .black : .white)
                    }
                }
            }
        }
        .padding(12)
        .background(Color.black.opacity(0.35), in: RoundedRectangle(cornerRadius: 12))
    }

    private var legendStyleDeck: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("CARROT-STYLE LEGEND MOVE DECK")
                .font(.system(size: 10.5, weight: .heavy, design: .monospaced))
                .foregroundStyle(.purple)

            HStack(spacing: 8) {
                ForEach(CarrotLegendMove.allCases) { move in
                    legendStyleButton(for: move)
                }
            }
        }
        .padding(12)
        .background(Color.black.opacity(0.35), in: RoundedRectangle(cornerRadius: 12))
    }

    private func handleStyleMove(_ move: CarrotLegendMove) {
        let isFinale = move == .carrotCrownFinale
        let res = styleEngine.recordMove(move, swagMeter: swagMeter)
        if res.accepted {
            CarrotSoundFX.play(muted: sfxMuted, isFinale: isFinale)
            totalCarrots += res.bonusCarrots
            if isFinale { totalShards += 1 }
            swagMeter = min(1.0, swagMeter + 0.10)
            stageProgress = min(0.86, stageProgress + 0.08)
            runnerBobPhase += 0.9
            if styleEngine.peakRank.rawValue != savedPeakRank &&
                styleEngine.peakRank >= (CarrotStyleRank(rawValue: savedPeakRank) ?? .d) {
                savedPeakRank = styleEngine.peakRank.rawValue
            }
            syncState("\(move.displayName)! +\(res.pointsEarned) PTS, +\(res.bonusCarrots) 🥕 -> Rank \(res.rank.code)")
        } else {
            syncState(res.reason)
        }
    }

    private func moveShortcutKey(_ move: CarrotLegendMove) -> KeyEquivalent {
        switch move {
        case .earCopterSpin: return "1"
        case .turboBurrowPop: return "2"
        case .carrotRailGrind: return "3"
        case .quakeCarrotSlam: return "4"
        case .carrotCrownFinale: return "5"
        }
    }

    private func legendStyleButton(for move: CarrotLegendMove) -> some View {
        let isFinale = move == .carrotCrownFinale
        let bgColor: Color = isFinale
            ? (finaleReady ? Color.yellow : Color.gray.opacity(0.35))
            : Color.purple.opacity(0.35)
        let fgColor: Color = (isFinale && finaleReady) ? .black : .white
        let idxLabel = moveShortcutKey(move).character

        return Button {
            handleStyleMove(move)
        } label: {
            VStack(spacing: 3) {
                Text("\(move.displayName) [\(String(idxLabel))]")
                    .font(.system(size: 11, weight: .bold))
                Text("+\(move.bonusCarrots) 🥕")
                    .font(.system(size: 9.5, design: .monospaced))
            }
            .padding(.horizontal, 10).padding(.vertical, 8)
            .frame(maxWidth: .infinity)
            .background(bgColor, in: RoundedRectangle(cornerRadius: 9))
            .foregroundStyle(fgColor)
        }
        .buttonStyle(.plain)
        .keyboardShortcut(moveShortcutKey(move), modifiers: [])
        .disabled(isFinale && !finaleReady)
        .accessibilityLabel("Trigger \(move.displayName)")
    }

    private func badgePill(_ label: String, tint: Color) -> some View {
        Text(label)
            .font(.system(size: 11, weight: .bold, design: .monospaced))
            .padding(.horizontal, 8).padding(.vertical, 4)
            .background(tint.opacity(0.22), in: Capsule())
            .foregroundStyle(tint)
    }

    private func syncState(_ message: String) {
        styleScoreDisplay = styleEngine.score
        styleRankCodeDisplay = styleEngine.rank.code
        styleRankTitleDisplay = styleEngine.rank.title
        lastActionBanner = message
    }
}
