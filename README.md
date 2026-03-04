# nf-emoji

## Summary

A whimsical Nextflow plugin that adds emoji flair to your pipeline runs. Get seasonal greetings, themed progress tracking, completion summaries, and emoji-decorated channel operators - all without changing your pipeline code.

## Get Started

Add the plugin to your `nextflow.config`:

```groovy
plugins {
    id 'nf-emoji@0.1.0'
}
```

That's it! Your pipeline will now show seasonal greetings and a completion summary with emoji.

To customize, add an `emoji` block to your config:

```groovy
emoji {
    theme       = 'ocean'    // default, space, ocean, lab, food, pirate, animal
    progressBar = true       // show live progress bar
    greeting    = true       // show seasonal greeting
    summary     = true       // show completion summary
}
```

## Examples

### Automatic pipeline decoration

Just enable the plugin and your pipeline output gets emoji flair:

```
❄️ Freezing temps, blazing pipelines! ❄️

executor >  local (6)
[0b/56a76c] process > SAY_HELLO (3)     [100%] 3 of 3 ✔
[72/b1b2ee] process > COUNT_LETTERS (2) [100%] 3 of 3 ✔

🏖️ Pipeline complete!
🐟 6 succeeded | 🦈 0 failed | 🐚 0 cached
```

### Themes

Seven built-in themes change all emojis throughout the plugin:

| Theme | Succeeded | Failed | Cached | Progress |
|---------|-----------|--------|--------|----------|
| default | ✅ | ❌ | ♻️ | 🟩 |
| space | 🛸 | 💥 | 🌟 | 🚀 |
| ocean | 🐟 | 🦈 | 🐚 | 🌊 |
| lab | 🔬 | ☣️ | 📋 | 🧪 |
| food | 🍰 | 🔥 | 🥫 | 🍕 |
| pirate | 💰 | 🦜 | 🗺️ | 🏴‍☠️ |
| animal | 🦊 | 🦂 | 🐢 | 🐎 |

### Channel operators

**`emojiView`** - like `view()` but with an emoji prefix:

```nextflow
include { emojiView } from 'plugin/nf-emoji'

channel.of('Hello', 'World').emojiView(emoji: '🧬')
// 🧬 Hello
// 🧬 World
```

**`emojiDump`** - like `dump()` but with emoji-decorated tags:

```nextflow
include { emojiDump } from 'plugin/nf-emoji'

channel.of(1, 2, 3).emojiDump(tag: 'counts', emoji: '🔢')
// [🔢 counts] 1
// [🔢 counts] 2
// [🔢 counts] 3
```

### Seasonal greetings

The plugin detects the date and prints themed greetings:

- Mar 14: `🥧 3.14159... Pipeline is irrational! 🥧`
- Apr 25: `🧬 Happy DNA Day! Time to sequence some tasks! 🧬`
- Oct 31: `🎃 Something wicked this way computes! 🎃`
- Dec 24-25: `🎅 Santa is delivering your results! 🎅`
- Plus seasonal defaults for spring, summer, fall, and winter

## License

Copyright 2025, Friederike Hanssen. Licensed under the Apache License, Version 2.0.
