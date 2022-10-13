# Customizable XP drops
A plugin which allows you to customize XP drops in more ways than the default OSRS XP drops allow.

# Features
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

# Change log
- v1.6.1 - Added options to customize text background. Added option to color predicted hit separately.
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
- v1.5 - Added XP Tracker. This minimalistic widget replaces the in-game XP tracker. Credit to `@TylerHarding` for the initial implementation.
- v1.4.1 - Added filter for predicted hits.
- v1.4 - Add attach to NPC option. Add icon size override. Add attach overlay offset config options. Default font now follow italic and bold style settings.
- v1.3.3 - Show predicted hit when Hitpoints skill is filtered. Allow for larger default(Runescape) font size.
- v1.3.2 - Make XP drop speed independent to fps. 
- v1.3.1 - Add prefix and suffix settings for predicted hit. Add xp multiplier for predicted hits.
- v1.3 - Add damage prediction based on XP drop.
- v1.2 - Add config options for prefix and suffix for XP drop text.
- v1.1 - Updated positioning logic and make sure shortly subsequent XP drops are evenly spaced.
- v1.0 - Initial release.

# Screenshots
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

# Installing custom fonts
When installing custom fonts on Windows make sure to use "Install for all users" otherwise the plugin might not be able to find the font.
Once installed, enter the name of the font under Font in the plugin settings. This field is case-insensitive.
Fonts that are shipped with Windows should not need to be installed.\
![installforallusers](https://i.imgur.com/MXzOKjH.png)
