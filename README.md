# Ashen Arena

A single-player Minecraft Java combat arena with directional dodges, a guarding katana, and an ogre boss that grabs and kicks.

**Early alpha: 1.0.0-alpha.1.** Requires Minecraft Java **26.3**, Fabric Loader **0.20.1 or newer**, Fabric API **0.166.0+26.3**, and Java **25 or newer**. Dedicated servers, multiplayer, and LAN play are unsupported.

Download the alpha from this repository's **Releases** tab. Use **Issues** to report bugs.

![The Ashen Hall](gallery/ashen-hall.png)

## What you get

- A protected 49 by 65 block hall with towering pillars, red standards, gold windows, and a throne.
- An Ashen Ogre with 140 health, a boss bar, and distinct grab and kick cues. It tracks you during attack windups, with a short commitment window for late dodges.
- A directional quickstep using your held movement keys, or your last movement key when none is held.
- An Ashen Katana that blocks frontal attacks while guarding. The ogre's grab bypasses guard; kicks can be blocked.
- Boss music on aggro, using Minecraft's installed Ender Dragon track. The mod amplifies that quiet track during playback and respects Music and Master volume.
- A replacement katana after death if you have none; Keep Inventory retains your existing sword.

The hall is inspired by dark fantasy boss arenas, including Messmer's arena. No Elden Ring assets are included.

## Install

1. Back up any world you want to use. This alpha permanently adds blocks to the world.
2. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft **26.3**.
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) **0.166.0+26.3** and the Ashen Arena JAR from this repository's Releases page.
4. Put both JARs in your launcher's Minecraft `mods` folder. Remove older Ashen Arena JARs first so only one version is present.
5. Launch the Fabric profile and open a single-player world. Cheats are not required.

The release ZIP is an optional bundle of the same mod JAR, instructions, and license. Extract it first; put only the mod JAR in `mods`. Minecraft, Fabric Loader, and Fabric API are separate downloads.

## Enter the arena

Stand on solid ground in an open area and press **F8**. The entrance needs a clear space 7 blocks wide, 7 deep, and 8 high, seven blocks east of you. Wait for the hall-ready chat message, approach the golden glass, and press **E**. Inside, approach the entrance behind you and press **E** to return. E opens inventory normally elsewhere.

The ogre appears automatically. **Dodge the red grab cue late. Face the gold kick cue and guard, or dodge out of range.** Leave and re-enter after a victory for a fresh fight. Creative visitors see an idle boss; combat targets Survival and Adventure players.

| Control | Action |
| --- | --- |
| F8 | Place the world's entrance, or report its location |
| E near the gate | Enter or leave |
| Left Shift + movement | Dodge; without held movement, use the last movement key |
| Left Ctrl | Crouch; crouching prevents dodging |
| Right Ctrl | Sprint; double-tap forward also works |
| Left-click | Attack |
| Hold right-click with katana | Guard; release before dodging |
| F5 | Cycle Minecraft's first/third-person views |

Dodge can be rebound in Minecraft's key settings. The mod sets crouch and sprint defaults once, preserving a backup of options. Gate interaction keys E/F8 are fixed in this alpha. The quickstep has no invulnerability frames, stamina, or custom player animation.

![The Ashen Ogre](gallery/ashen-ogre.png)

![Ashen Katana in third person](gallery/ashen-katana.png)

## World behavior and limitations

- One entrance and one hall per world. The hall is built in batches in a secluded sky area of the Overworld around Y=256. First construction and chunk generation can cause pauses.
- The entire gate and hall region is protected against breaking, building, explosions, fire, fluids, and pistons, including in Creative.
- The boss and its bar clear when you die or leave. Arena chunks are not kept loaded.
- Removing the mod leaves the arena's blocks behind and removes its protection. Leave the hall and back up the world before uninstalling.
- Other mods, resource packs, shaders, custom dimensions, multiplayer, and dedicated servers have not been validated. Resource packs replacing the dragon track may change music loudness.
- No posture system, timed parry, custom fog effect, or custom ogre model is included. The ogre uses a scaled Minecraft husk with custom poses.

For bugs, include Minecraft/Fabric versions, steps to reproduce, other installed mods, and relevant log excerpts with personal details removed.

## Development, rights, and disclosure

This prototype's implementation and documentation were produced primarily with AI coding assistance under the owner's direction. Gallery images are actual Minecraft gameplay captures; they are not AI-generated images. No AI service runs in the mod during gameplay.

The mod contains original code and model definitions that reference Minecraft resources. It does **not** bundle Minecraft music, textures, game code, Fabric API, or personal worlds. Music comes from the player's own Minecraft installation and is amplified during playback.

All rights reserved; see [LICENSE](LICENSE). This public repository hosts release information and downloads; it is not an open-source release.

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT. Minecraft belongs to its respective owners. Elden Ring and Sekiro belong to their respective owners; this project is not affiliated with them.
