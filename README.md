# nf-emoji

A whimsical Nextflow plugin that adds emoji flair to your pipeline runs.

## Features

### Seasonal Greetings

Automatically prints a seasonal or holiday greeting when your pipeline starts:

```
❄️ Freezing temps, blazing pipelines! ❄️
```

Special greetings on Pi Day (Mar 14), DNA Day (Apr 25), Halloween (Oct 31), Christmas, and more.

### Pipeline Summary

Prints a themed summary when your pipeline completes:

```
🏖️ Pipeline complete!
🐟 6 succeeded | 🦈 0 failed | 🐚 0 cached
```

### Progress Bar

A live emoji progress bar that updates as tasks complete:

```
🌊🌊🌊🌊🌊🌊⬜⬜⬜⬜ 60% (3/5)
```

### Themes

Five built-in themes that change all emojis throughout the plugin:

| Theme | Succeeded | Failed | Cached | Progress |
|---------|-----------|--------|--------|----------|
| default | ✅ | ❌ | ♻️ | 🟩 |
| space | 🛸 | 💥 | 🌟 | 🚀 |
| ocean | 🐟 | 🦈 | 🐚 | 🌊 |
| lab | 🔬 | ☣️ | 📋 | 🧪 |
| food | 🍰 | 🔥 | 🥫 | 🍕 |

### Channel Operators

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

## Configuration

Add to your `nextflow.config`:

```groovy
plugins {
    id 'nf-emoji@0.1.0'
}

emoji {
    theme       = 'ocean'    // default, space, ocean, lab, food
    progressBar = true       // show live progress bar
    greeting    = true       // show seasonal greeting
    summary     = true       // show completion summary
}
```
