# nf-emoji

## Summary

A whimsical Nextflow plugin that adds emoji flair to your pipeline runs. Get seasonal greetings, themed progress tracking, completion summaries, and emoji-decorated channel operators - all without changing your pipeline code.

## Get Started

Add the plugin to your `nextflow.config`:

```groovy
plugins {
    id 'nf-emoji@0.2.0'
}
```

That's it! Your pipeline will now show seasonal greetings and a completion summary with emoji.

To customize, add an `emoji` block to your config:

```groovy
emoji {
    theme       = 'ocean'              // default, space, ocean, lab, food, pirate, animal, nf-core, seasonal
    progressBar = true                 // show live progress bar
    greeting    = true                 // true/false, or a custom string
    summary     = true                 // show completion summary
    confetti    = false                // launch confetti on success (requires cli-confetti)
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

Twelve built-in themes (or use `seasonal` to auto-select by time of year):

| Theme | Succeeded | Failed | Cached | Progress |
|---------|-----------|--------|--------|----------|
| default | ✅ | ❌ | ♻️ | 🟩 |
| space | 🛸 | 💥 | 🌟 | 🚀 |
| ocean | 🐟 | 🦈 | 🐚 | 🌊 |
| lab | 🔬 | ☣️ | 📋 | 🧪 |
| food | 🍰 | 🔥 | 🥫 | 🍕 |
| pirate | 💰 | 🦜 | 🗺️ | 🏴‍☠️ |
| animal | 🦊 | 🦂 | 🐢 | 🐎 |
| nf-core | 🍏 | 🍎 | 🌿 | 🍏 |
| spring | 🌸 | 🥀 | 🌱 | 🌷 |
| summer | 🏖️ | 🌪️ | 🧊 | ☀️ |
| fall | 🎃 | 💨 | 🍄 | 🍂 |
| winter | ⛄ | 🥶 | 🧣 | ❄️ |

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

### Custom greeting

Set `greeting` to a string to use your own message instead of the seasonal default:

```groovy
emoji {
    greeting = '🧪 Welcome to the variant calling pipeline!'
}
```

### Seasonal greetings

The plugin detects the date and prints themed greetings:

- Jan 1: `🍀 Happy New Year! 🍀`
- Feb 14: `💕 Love is in the air...! 💕`
- Mar 14: `🥧 Happy Pi Day! 🥧`
- Apr 22: `🌍 Happy Earth Day! 🌍`
- Apr 25: `🧬 Happy DNA Day! 🧬`
- Oct 31: `🎃 Something wicked this way computes! 🎃`
- Dec 24-25: `🎅 'Twas the night before deployment... 🎅`
- Dec 31: `🎆 Happy New Year! 🎆`
- Plus seasonal defaults and a countdown to the next festive day

### Confetti

Enable confetti to celebrate successful pipeline runs. This requires [`cli-confetti`](https://github.com/IonicaBizau/cli-confetti) to be installed.

Then enable it in your config:

```groovy
emoji {
    confetti = true
}
```

## License

Copyright 2025, Friederike Hanssen. Licensed under the Apache License, Version 2.0.
