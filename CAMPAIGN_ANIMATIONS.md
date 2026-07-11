# Campaign Animation Notes

This mod now renders the Stellar Resonance effect as a custom campaign entity instead of trying to draw it directly from the interaction dialog.

## What happens

1. `StarResonanceDialog` spawns a custom entity when Option 1 is chosen.
2. The entity type is defined in `data/config/custom_entities.json` as `star_resonance_beam`.
3. `ResonanceBeamEntityPlugin` renders the visual in the campaign map.
4. `Misc.fadeAndExpire(...)` removes the entity after a set duration.

## Why this works

Interaction dialogs are good for triggering behavior, but they are not a render surface. Campaign visuals should usually be done through a custom campaign entity plugin because that plugin gets an `advance()` method and a `render()` method each frame.

In this case the effect is drawn in the `STATIONS` campaign layer using OpenGL triangles. The plugin computes a line from the fleet to the star and draws three additive passes with different widths and alpha values. That is what gives the beam its bright core and softer outer glow.

## Pattern to reuse

If you want to make another campaign animation, the general pattern is:

- Add a custom entity id in `data/config/custom_entities.json`.
- Point it at a plugin class with `pluginClass`.
- In the plugin, update the entity in `advance()` if it needs to follow something.
- In `render()`, draw the effect with the campaign layer you want.
- Use `Misc.fadeAndExpire(entity, seconds)` or your own timer to clean it up.

## Useful files

- [StarResonanceDialog.java](src/starproject/dialogs/StarResonanceDialog.java)
- [ResonanceBeamEntityPlugin.java](src/starproject/entities/ResonanceBeamEntityPlugin.java)
- [custom_entities.json](data/config/custom_entities.json)

## Notes

- `STATIONS` in `custom_entities.json` is valid for Starsector even if VS Code marks it as a JSON warning.
- For beam-like visuals, a custom entity plugin is usually easier than trying to force a combat beam or particle effect into campaign space.
