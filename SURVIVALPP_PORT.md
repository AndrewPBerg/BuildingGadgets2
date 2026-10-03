# Survival++ Minecraft 26.2 port

This branch starts from Direwolf20-MC/BuildingGadgets2 `21.6` at `f669d20cebe287202421d2d83ddb9906baafeeab` (Building Gadgets 2 1.4.6, Minecraft 26.1.2). The upstream MIT license and attribution remain intact. The target is Minecraft 26.2, NeoForge 26.2.0.75, and Java 25.

The port moves the build to ModDevGradle 2.0.144, uses the packaged Curios 16 runtime, migrates 26.2 screen and render APIs, bounds imported templates to 100,000 positions, validates palette indexes and server-imported block states, and caps per-frame ghost rendering at 2,048 non-air blocks. The render code uses Minecraft's 26.2 submit-node pipeline; this replaces upstream's retained GPU mesh path and needs user-led in-world performance review.

Survival++ keeps Building and Exchanging Gadgets and the material- and FE-accounted Copy Paste Gadget. Upstream's replacement paste discards existing blocks, Destruction Gadget voids drops, and Cut Paste handles block-entity data. These actions are Creative-only here, and the two destructive gadgets have no survival recipes. The fuel-powered Charging Gadgets port supplies FE in Survival++.

Build with the pinned Java 25 toolchain and one Gradle worker:

```sh
../custom-survival-starter/gradlew --offline --no-daemon --max-workers=1 -Dorg.gradle.jvmargs=-Xmx1536m jar
```

The isolated fork reached Java compilation and jar packaging. Before the final survival-policy edits, its dedicated-server load and a combined Survival++ server load reached `Done`; an isolated client loaded through resource atlases under a virtual display. The user will do the final in-world and multiplayer checks. See `../../docs/release-1.4.1.md`.
