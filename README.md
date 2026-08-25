# CinematicScenes

A Forge 1.20.1 / Java 17 vertical slice of a story-cutscene engine. Run `./gradlew runClient`, enter a world, and use `/cutscene demo`.

## What the demo does
It freezes the local player, fades from black into a smooth wide-to-close camera move focused on a scene actor target placed ahead of the player, displays a typewriter dialogue and an interactive three-way choice, then runs a distinct camera branch before fading out and restoring state. `/cutscene skip` advances dialogue/choice stages; `/cutscene stop` always restores the client state.

## Architecture
`scene` owns deterministic event timelines and branches; `camera` owns interpolation, look-at, orbit, follow, path and additive shake; `gui` renders overlays and captures choice input; `actor` wraps temporary entities; `network` deliberately keeps the server command thin and sends client-only playback instructions.

## Blockbuster reference and licensing
The requested Blockbuster repository could not be fetched in this build environment (GitHub tunnel returned 403). This project contains no Blockbuster source, assets, or copied implementation, and therefore does not incorporate GPL-3.0 code. Its design is an independent implementation of general cinematography concepts: keyframed interpolation, saved/restored camera state, and actor-targeted shots. Before importing or adapting any Blockbuster source in a future change, review its GPL-3.0 LICENSE and preserve required notices/copyright attribution.
