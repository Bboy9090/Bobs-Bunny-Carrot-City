# 🥕 Bobs Bunny: Carrot City — Road to the Golden World
**Master Game Design Document & Phased Implementation Roadmap (Mobile + Console)**

---

## 1. Executive Summary & Core Vision
* **Title:** *Bobs Bunny: Carrot City — Road to the Golden World*
* **Genre:** 2.5D Action-Adventure Platformer & 1v3 Asymmetric Party Multiplayer
* **Target Platforms:**
  * **Phase 1 & 2:** Android & iOS (Free-to-Play, Cosmetic-Only Monetization, 60 FPS Touch + Controller Support)
  * **Phase 3:** PC & Consoles (Enhanced Widescreen Presentation, Couch Co-Op, Phone-to-Console Asymmetric Party Play)
* **Core Inspiration:**
  * **Super Mario Bros. (Wonder / World / 3):** Tight, joyful jump arcs, secret exits, multi-hit visual power-up states, and stage-specific environmental gimmicks.
  * **Looney Tunes:** Playable slapstick physics, fourth-wall-breaking reactions, painted tunnel tricks, and villains whose elaborate traps backfire on themselves.
  * **Modern Streetwear Culture:** Lo-fi hip-hop meets big-band cartoon brass, fresh sneakers, hoodies, shades, and expressive style combos.

---

## 2. Story & World Progression (10 Worlds to the Golden World)
**The Premise:**
Bobs Bunny isn't trying to be a superhero—he just wants his carrots, his fresh kicks, and a peaceful afternoon in **New Carrot City**. When **Don Foxone** and the **Sneaky Fox Syndicate** steal the legendary **Golden Carrot Compass** on orders from cosmic tyrant **Empress Bun-Zilla**, Bobs sets off to get his stash back. Every time he tries to wrap things up and go home, a bigger, wackier crisis pulls him one world closer to the mythical **Golden World**.

### World Roster:
1. **World 1 — New Carrot City (Playable in Phase 1):** Enchanted forest outskirts, neon rooftops, underground subway burrows, high-altitude construction cranes, a hidden Golden Rift, and Don Foxone's Steamroller Juicer.
2. **World 2 — San Francis-Kale:** Foggy suspension bridges, steep cable-car rail grinds, and gusty wind tunnels.
3. **World 3 — Toky-Oat:** Neon cyber-rooftops, rhythm-synced platforms, and robotic Mecha-Foxes.
4. **World 4 — Rome-Aine:** Colos-Spud arenas, crumbling marble pillars, and chariot boss battles.
5. **World 5 — Lond-Onion:** Haunted libraries, shifting bookcases, and illusionist boss *The Great Furtini*.
6. **World 6 — Mount Vesuvi-Apple:** Rising molten salsa, fruit-shaped lava boulders, and runaway minecarts.
7. **World 7 — Rio de Jalapeño:** Tropical waterfalls, carnival parade floats, and vine-swinging chases.
8. **World 8 — Chicag-Artichoke:** High-wind steel girders, *Wrench Fox* contraptions, and precision assembly lines.
9. **World 9 — Cosmic Hare-Fortress:** Gravity-flipping space lanes and the grand showdown with *Empress Bun-Zilla*.
10. **World 10 — The Golden World (Endgame Expansion):** A full playable secret kingdom featuring 24-Karat transformations, Rainbow Highways, Golden Trials, and Encore Bosses.

---

## 3. Signature Gameplay Systems ("Cartoon Chaos")

### A. Core Movement & Playable Slapstick
* **Bunny Hop & Helicopter Ears:** Tap to jump/double-jump; hold in mid-air to spin Bobs's ears like helicopter blades to hover over gaps and catch updrafts.
* **"Don't Look Down" Cartoon Coyote Time:** Running off a ledge gives 0.45s of mid-air hang-time where Bobs pedals his feet in mid-air and glances at the camera before gravity kicks in—allowing clutch recovery jumps.
* **Burrow Blitz:** Dive underground to slip under low hazards, dodge overhead traps, and burst upward beneath foxes to launch them skyward.
* **Painted Tunnel Trick & Troll Counter:** Trigger a contextual prank at high Swag or paint a fake tunnel on solid walls—Bobs zips through into hidden stashes while pursuing foxes slam flat into accordions.
* **Cartoon Damage States (No Boring HP Bars):**
  * **Full Swag (2 Hits):** Bobs rocks his shades and glowing kicks.
  * **Scuffed (1 Hit):** Shades fly off on impact (can be recovered within 3 seconds!).
  * **Squashed / Accordion State:** Crushing traps flatten Bobs into a waddling pancake briefly instead of instant death.

### B. Streamlined Core 4 Power-Ups (+ Golden Transformation)
1. **Golden Wasabi Carrot:** Breathes cartoon fire and blasts forward at supersonic speed, smashing through obstacles and enemies.
2. **Swag Shades:** Slows time into stylish bullet-time with rainbow afterimages and automatic Troll Counters.
3. **Spring-Loaded Kicks:** Super-charged triple jumps and high-bouncing stomps.
4. **Bonk-O-Matic Mallet:** Smashes armored traps and flattens pursuing foxes into accordions.
5. **24-Karat Golden Bobs (Golden Rift / Golden World):** Invincible golden aura, 3x carrot magnet, and rainbow bridge creation.

### C. Hub Evolution: The Golden Plaza Statue
* Collecting **Golden Carrot Shards** in stages upgrades the **Golden Bobs Statue** in the New Carrot City Hub (from Wooden Crate → Bronze → Silver → 24K Gold → Portal to the Golden World).

---

## 4. Asymmetric Multiplayer: "Fox Hunt vs. Hare Stunt" (1 vs. 3)
* **The Runner (Bobs Bunny):** Collects 30 Golden Carrots and reaches the Safehouse Burrow before the clock expires using Burrow slides, decoys, and ear-glides.
* **The Trappers (3 Sneaky Foxes):** Control lanes and trigger street hazards (Anvil Drops, Net Cages, Grease Slicks, Subway Gates) on cooldowns to corner Bobs.
* **Playable Modes:** Run as Bobs against AI/Local Foxes, OR play as the Fox Trapper Commander dropping traps on an agile Bobs runner!

---

## 5. Complete Linear Roadmap: Phases 1, 2 & 3

### Phase 1 — The Playable Foundation (New Carrot City Vertical Slice)
**Objective:** Deliver a polished, installable Android game featuring New Carrot City, responsive cartoon physics, original characters, customization, and a complete 6-stage opening chapter.

1. **Gameplay & Technical Foundation**
   * Build the 60 FPS custom Jetpack Compose Canvas physics and cartoon rendering engine.
   * Implement "Don't Look Down" coyote time, jump buffering, Helicopter Ears, and Burrow Blitz.
   * Build the Cartoon Health states (Full Swag, Scuffed/Lost Shades, Pancake Squash).
   * Add responsive mobile arcade controls + hardware controller/keyboard support.
   * Persist carrots, Golden Shards, stars, and equipped Swag Shop items across app restarts.
2. **Characters & Visual Identity**
   * Render Bobs Bunny with dynamic ear-spin animations, shades, hoodies, sneakers, particle trails, and fourth-wall speech quips.
   * Implement 4 enemy types: *Sly Scout Foxes*, *Roller-Sweeper Foxes*, *Wrench Fox*, and boss *Don Foxone*.
3. **First Complete Adventure (World 1: New Carrot City — 6 Stages)**
   * **1-1 Overgrown Outskirts:** Lush forest edge, bouncy mushrooms, Sly Scout ambushes.
   * **1-2 Neon Rooftop Run:** Downtown skyline, bouncy awnings, roller-skating foxes.
   * **1-3 Metro Burrow Express:** Underground subway tracks, low ceilings requiring Burrow Blitz.
   * **1-4 Construction Sky-High:** Updraft wind currents, high girders, Wrench Fox traps.
   * **1-5 The Secret Golden Rift:** A playable glimpse of the Golden World with rainbow platforms and 24K rewards.
   * **1-6 Boss: Don Foxone's Steamroller Juicer:** Multi-phase slapstick boss battle.
4. **Swag Shop, Trophies & Asymmetric Party Mode**
   * Cosmetic-only Swag Shop (Headgear, Hoodies, Kicks, Trails) + Unlockable Gameplay Badges.
   * Playable "Fox Hunt vs. Hare Stunt" mode (Play as Bobs Runner OR Fox Trap Master).

---

### Phase 2 — The Great Carrot Adventure (Worlds 2–9 & Hub Expansion)
**Objective:** Expand into all nine Veggie Cosmos worlds, introduce playable allies, and deepen Carrot City's living hub.

1. **Worlds 2–9 Campaign**
   * Complete *San Francis-Kale*, *Toky-Oat*, *Rome-Aine*, *Lond-Onion*, *Mount Vesuvi-Apple*, *Rio de Jalapeño*, *Chicag-Artichoke*, and *Cosmic Hare-Fortress*.
   * Add biome-specific mechanics (cable cars, rhythm platforms, illusion walls, rising salsa, zero-gravity).
   * Build multi-phase boss encounters for *The Great Furtini*, *Wrench Fox*, and *Empress Bun-Zilla*.
2. **Carrot City Life & Playable Allies**
   * Expand the interactive hub with NPC side quests, arcade mini-games, and Bobs's Secret Underground Burrow network.
   * Add 2 new playable characters with distinct physics (a floor-smashing Badger bruiser and a grapple-hook Gadget Fox).
3. **Multiplayer & Replayability Polish**
   * Add 3-Star stage mastery challenges, Time Trials, and Heist Mode bonus chases.

---

### Phase 3 — The Golden World & Console Franchise Expansion
**Objective:** Unlock the secret tenth world, launch online multiplayer & community level creation, and bring the game to PC and Consoles.

1. **World 10: The Golden World**
   * Multi-stage postgame kingdom unlocked via Golden Carrot Shards.
   * Features Rainbow Carrot bridge mechanics, Encore Bosses, and the **24-Karat Crown Run**.
2. **Community & Online Party Scale**
   * Real-time networked 1v3 matchmaking and **Couch-to-Phone Cross-Play** (TV player as Bobs, mobile players as Fox Trappers).
   * **Cartoon Studio Level Creator** with stage sharing and validation.
3. **PC & Console Release**
   * 60–120 FPS controller-native builds, HD rumble feedback, and widescreen 2.5D shaders.
