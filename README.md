# Customizable XP drops

A plugin which allows you to customize XP drops in more ways than the default OSRS XP drops allow.

### Features
- Show XP drops grouped or ungrouped, as well as configuring the delay between subsequent ungrouped XP drops.
- Customize the font, style and size of the XP drop text.
- Customize the color of the XP drop text.
- Customize the location, direction and speed of the XP drop.
- Fade out the XP drop before it disappears.
- Color the XP drop based on active offensive prayers.
- Group fake (also known as blocked) XP drops with regular XP drops.
- Clear icons even at small size.
- Customize which skills to filter from showing XP drops at all.
- Attach XP drop location to in game player model.
- Add a custom prefix and suffix to the XP drop text.
- Show predicted damage from XP drop.
- Replace the XP tracker widget with a minimalistic one.

#### Change log
- v1.9.0 - Add xp multiplier config.
- v1.8.16 - Add titans xp bonus.
- v1.8.15.1 - Fix to account for Runelite api change. - `@DapperMickie`
- v1.8.15 - Add xp bonuses for Hueycoatl and Amoxliatl. - `@wavesy`
- v1.8.14 - Add xp bonuses for new npcs.
- v1.8.13 - Fix duke xp bonus again.
- v1.8.12 - Added voidwaker variations, and fixed a few xp bonuses. - `@umer-rs`, `@Tylanderr`
- v1.8.11 - Fix whisperer xp bonus.
- v1.8.10 - Fix keris partisan attack style and duke xp bonus.
- v1.8.9 - Update NPC stats.
- v1.8.8 - Fix attach to target ungrouped xp drop draw location.
- v1.8.7 - Fixed predicted hits on Awakened DT2 bosses, Kalphite Queen P2, and added Scurrius. - `@umer-rs`
- v1.8.6 - Update plugin to work with the Varlamore: Part One game update. - `@umer-rs`
- v1.8.5 - Fixed a bug where the interacting mob was incorrect. (e.g. Tekton, Vanguards)
- v1.8.4 - Fix Chambers of Xeric predicted hits with respect to the [2023-11-29 game update](https://secure.runescape.com/m=news/chambers-of-xeric-changes?oldschool=1). - `@umer-rs`
- v1.8.3 - Fix warden xp multiplier on first attack after skull phase.
- v1.8.2 - Text spacing of the xp tracker is now consistent, Added RP prayer support, Changed to varbits for toa raid level and party size so the widgets are no longer needed and other plugins that interfere with those widgets will not interfere with this plugin.
- v1.8.1 - Added coloring of xp drop for offensive melee prayer when using special attack with voidwaker.
- v1.8.0 - Added a new settings category called Miscellaneous. This category has the following new settings: `Hide vanilla xp drops`, `Hide vanilla xp tracker`, `Force xp drops to be centered`, `Center xp drop on`, `Xp drop overlay priority`, `Xp tracker overlay priority`. `Xp drop overlay priority` and `Xp tracker overlay priority` can be used to sort the respective overlays after other overlays. For example to sort the xp drops below the xp globes overlay.
- v1.7.5 - Added setting to customize the border color of the xp tracker. - `@iipom`
- v1.7.4 - Update NPC stats with dt2 bosses.
- v1.7.3.2 - Fixes to account for Runelite api change.
- v1.7.3.1 - Fix recolor xp drop to match up better with server. - `@hoheps`
- v1.7.3 - Update NPC stats to account for Jagex xp bonus change.
- v1.7.2.3 - Update for RuneLite api change.
- v1.7.2.2 - Set overlay priority explicitly to force correct ordering.
- v1.7.2.1 - Change xp drop overlay layer to UNDER_WIDGETS. - `@Jbleezy`
- v1.7.2 - Add alpha channel to color pickers.
- v1.7.1 - Fix bug where xp drop icons were not properly loaded. Xp tracker most recent will now respect the filtered skills.
- v1.7.0.1 - Update xp bonuses file for wilderness boss update.
- v1.7.0 - Now using xp bonus formulae for monsters in CoX, ToB and ToA. Predicted hits should be more accurate for these monsters now.
- v1.6.3 - Fix XP drop overlapping issue by properly calculating delay based on previous XP drops.
- v1.6.2 - Add customization of predicted hit icon.
- v1.6.1.2 - Fix XP drops and predicted hit color encoding when using prefix and suffix settings.
- v1.6.1.1 - Fix XP drops color encoding bug for in scene overlay.
- v1.6.1.0 - Added options to customize text background. Added option to color predicted hit separately.
- v1.6.0 - Added progress bar to the xp tracker similar to the vanilla xp tracker progress bar using the xp tracker plugin(must be enabled). This feature is disabled by default.
- v1.5.10 - Using `Group XP drops = False` and `Never group predicted hit = False` now groups the predicted hit with every XP drop (previously `Never group predicted hit` had no effect in this configuration). To revert to old behaviour with `Group XP drops = False` simply switch `Never group predicted hit` to `True`.
- v1.5.9 - Fixed bug where xp tracker would sometimes not work correctly.
- v1.5.8 - Changed OSRS bold font to the in-game bold font.
- v1.5.7 - Recolor xp drop based on attack style. Update npc multipliers. Recolor predicted hit according to prayer even if the skill is filtered.
- v1.5.6 - Fixed xp drop recoloring when long range or defensive cast styles are used. Updated README with info about installing custom fonts.
- v1.5.5 - Fixed fake xp drops being split in ungrouped settings.
- v1.5.4 - Allow icons to be overridden by other plugins such as resource packs. Show/Hide xp drops based on the in-game setting (following the xp orb button next to the minimap).
- v1.5.3 - Bugfix for target attached overlay without having predicted hits enabled. Load old overlay position when swapping between attached or detached overlay setting.
- v1.5.2 - Fix another overlay location bug introduced by `v1.5.1`.
- v1.5.1 - Bugfix for target or player attached overlay location.
- v1.5.0 - Added XP Tracker. This minimalistic widget replaces the in-game XP tracker. Credit to `@TylerHarding` for the initial implementation.
- v1.4.1 - Added filter for predicted hits.
- v1.4.0 - Add attach to NPC option. Add icon size override. Add attach overlay offset config options. Default font now follow italic and bold style settings.
- v1.3.3 - Show predicted hit when Hitpoints skill is filtered. Allow for larger default(Runescape) font size.
- v1.3.2 - Make XP drop speed independent to fps.
- v1.3.1 - Add prefix and suffix settings for predicted hit. Add xp multiplier for predicted hits.
- v1.3.0 - Add damage prediction based on XP drop.
- v1.2.0 - Add config options for prefix and suffix for XP drop text.
- v1.1.0 - Updated positioning logic and make sure shortly subsequent XP drops are evenly spaced.
- v1.0.0 - Initial release.

### Known issues

#### OSRS Font Scaling
The Old school RuneScape fonts are bitmap fonts. 
This means they do not scale well in size and have ugly edges and artifacts when using a font size that is not a multiple of 16.
It is recommended to specify a font in the settings when using such font size.

#### Predicted hits
Predicted hits will never be 100% accurate. Predicted hits are calculated from the hitpoints experience gained. 
The amount of hitpoints experience gained correlates with the amount of damage done however it is not exact.
Round of experience gained happens on the server before it is sent to the client. 
Furthermore, some monsters in OSRS give bonus experience in the form of a multiplier over the experience gained.
This plugin tries to use the same multipliers per monster but the list is incomplete leading to incorrect predicted hits.

#### XP drops are delayed
Try increasing the `Vertical XP drop speed` and/or lowering the `XP drop delay` settings. 
The plugin uses these settings to determine by how much consecutive XP drops should be delayed to prevent overlapping.
If these settings are configured in such way that xp drops do not clear the screen fast enough for new XP drops the queue of XP drops will keep backing up resulting in a delay.

#### There is a gap between my overlays / XP drops overlay is sorted before x overlay (even when no xp drops are visible)
This has to do with the sorting of the overlays that are in the same overlay group.
Tune `Xp drop overlay priority` to a lower value until this is not a problem anymore OR do not snap the overlay to an overlay group (the boxes when you hold the alt key).

#### The XP drops are off center
When `Attach to player` and `Attach to target` are disabled you can enable `Force xp drops to be centered` under `Miscellaneous` and tune `Center xp drop on` to your liking.\
When `Attach to player` or `Attach to target` is enabled the xp drops are centered on the position of the player or target on your screen. 
You can use 'Center xp drop on', 'Attach to x offset' and 'Attach to y offset' to fine tune the position of the xp drop with relation to the player or target.

### Installing custom fonts
When installing custom fonts on Windows make sure to use "Install for all users" otherwise the plugin might not be able to find the font.
Once installed, enter the name of the font under Font in the plugin settings. This field is case-insensitive.
Fonts that are shipped with Windows should not need to be installed.\
![installforallusers](https://i.imgur.com/MXzOKjH.png)

### Vanilla XP drops
This plugin replaces vanilla XP drops however using (close to) the following settings they can easily be mimicked if you play on fixed client layout.
The vanilla xp drops speed depends on the size of the game canvas therefore the defaults given below only match vanilla behaviour on fixed layout client size.
Note that you can view both customizable xp drops and vanilla xp drops at the same time by setting `Hide vanilla xp drops: false` under the Miscellaneous tab in the plugin settings for easy comparing.
Note that when using ungrouped xp drops the `XP drop delay` setting is also important and depends on size and speed.\
Vanilla XP drop Size smallest:  `Font style: Default`, `Background: Shadow`, `Font size: 12`, `Icon size override: 0`.\
Vanilla XP drop Size medium:    `Font style: Default`, `Background: Shadow`, `Font size: 16`, `Icon size override: 25`.\
Vanilla XP drop Size largest:   `Font style: Bold`, `Background: Shadow`, `Font size: 16`, `Icon size override: 25`.\
Vanilla XP drop Speed slower:   `Vertical XP drop speed: 44`.\
Vanilla XP drop Speed default:  `Vertical XP drop speed: 60`.\
Vanilla XP drop Speed faster:   `Vertical XP drop speed: 90`.

### Screenshots
Default in game XP drops (left) compared to this plugin with similar settings (right). Settings: Font size 12, Font style default, icon size override 0.\
![comparison](https://i.imgur.com/UV0b0dJ.png)

Icons turned off. Attach to player selected. Custom font, size and style. "+" prefix and " xp" suffix.\
![oldschool](https://i.imgur.com/u6sO5QK.png)

Damage prediction never grouped(left) and grouped(right).\
![damageprediction](https://i.imgur.com/MPdFFvy.png)

Customizable XP tracker (Settings: Font size 16, Font style default, icon size override 0).\
![xptracker](https://i.imgur.com/4UKHxPe.png)

With optional progress bar (Settings: Font size 16, Font style bold, icon size override 25).\
![xptrackerprogress](https://i.imgur.com/d9lKvQK.png)

From https://github.com/ruchir90 \
![pic1](https://i.imgur.com/8W9zE8g.png)
![pic2](https://i.imgur.com/ZFpgKa1.png)
![pic3](https://i.imgur.com/sSabp2c.gif)

Feel free to open an issue or pull request with your screenshot or gif of XP drop if you want it added here (preferably a link to imgur).

