# Bob's Bunny: Carrot City — 3D Visual Production Contract

**Project:** Bob's Bunny: Carrot City, separate from the Kai-Jax franchise  
**Approval status:** Production direction established; final 3D model and artwork **not yet approved**  
**Integration authority:** Draft [PR #2](https://github.com/Bboy9090/Bobs-Bunny-Carrot-City/pull/2), branch `feat/bobs-bunny-3d-diorama`

## Art direction

An original **neo-retro 3D cartoon platformer**: approachable, expressive low-poly forms inspired by the readability of classic 3D handheld/console games, developed with modern lighting, color hierarchy, polished animation, and real diorama depth. Gameplay remains readable side-scrolling platforming. Do **not** move toward realistic fur, gritty combat, flat mobile illustration, or the separate Kai-Jax dark-fantasy identity.

The 3D Diorama is the target presentation. The original 2D renderer remains the supported functional fallback until 3D has feature parity and hardware qualification.

### Canonical Bob model

- Tall, lean, athletic rabbit silhouette; large expressive ears and blue eyes
- Golden-cream fur and warm-white muzzle, with clear front and 3/4 facial expression
- Aviator cap with **yellow goggles**; clearly visible strap, lenses, and rim
- Fluttering **yellow scarf** that conveys speed and directional motion
- Orange-and-white jacket / outerwear, midnight-navy pants, utility belt
- White gloves, white-and-yellow oversized cartoon sneakers, fluffy tail
- Expressive but controlled squash-and-stretch; readable run, jump, helicopter-ear glide, Burrow Blitz, dash, damage, and victory poses
- Protect the established proportions and palette; changes to silhouette or outfit require explicit owner review

The GLES3 procedural rig currently used in W1-1 is **a technical visual candidate only**. Commit `6c681823` improves the muzzle, smile, eye highlights, goggles, ear interiors, scarf layering, jacket trim and skyline detailing; it is **not** a final rigged mesh or a signed-off character design.

### W1-1 environment look

**Overgrown Outskirts** should feature recognizable Carrot City architecture, intentional foreground/background layering, clear playable surfaces, lively plants, storefronts, windows and distant structures. Prioritize playfield visibility, silhouettes, warm/cool palette grouping, and an unobstructed camera.

Avoid generic repeated blocks as final art, excessive dark foreground occlusion, dialogue overlays covering Bob, or decoration that makes platform boundaries unclear.

### Animation and implementation standards

1. Gameplay logic, stage collision, carrot and enemy progression, save data, and boss behavior remain in `CartoonStageEngine`. Visual upgrades must not fork physics.
2. 3D reads `DioramaFrameSnapshot` via the published frame bridge; **do not reintroduce direct reads of mutable engine lists from the GL thread**.
3. Maintain shader compile/link error validation, meaningful first-frame readiness, GLES3 capability detection and 2D fallback.
4. Prioritize a future **rigged GLB** candidate with documented skeletal hierarchy, eye/mouth and ear animation, consistent scale and facing, LOD, texture/material budgets, and deterministic animations. Do not call procedural primitives a final GLB.
5. Test all W1-1 mechanics and switching 3D → 2D → 3D through Android instrumentation. Retain the existing tests.
6. Set a mobile performance **target**, not an observed claim: aim for smooth 60 Hz rendering on suitable hardware (~16.7ms frame budget), with a documented lower-quality fallback. Verify measured FPS and thermal data on real Adreno/Mali phones before approval.
7. Capture stable screenshots of idle, sprint, jumping, helicopter glide, burrow, damage and goal completion, against the same stage. Compare with the approved design sheet when available.

## Acceptance gates, in order

- **Art review:** owner approves silhouette, facial expression, scarf/cap/eyes/outfit and color consistency using turnarounds and in-engine images
- **Vertical slice:** player completes W1-1 from start to goal, including carrots, stomps, checkpoints, recovery, rewards, save/reload and gameplay parity with 2D
- **Technical:** unit, instrumentation, Android CI, app resume/context recreation, GPU profiling, API 26 and physical Android test
- **World 1 expansion:** only after W1-1 becomes the production art template, continue W1-2 through W1-BOSS, including Don Foxone's Steamroller Juicer, preserving existing stage definitions
- **Commercial:** separate release package, Firebase and signing review; no publication before passing all gates

**Current decision:** Continue vertical-slice development. Keep PR #2 Draft, `main` and the 2D implementation unchanged until certification.
