#!/usr/bin/env nextflow

// Import custom function from our plugin
include { emojiView; emojiDump } from 'plugin/nf-emoji'

process SAY_HELLO {
    input:
    val greeting

    output:
    stdout

    script:
    """
    sleep 3
    echo "${greeting}"
    """
}

process COUNT_LETTERS {
    input:
    val word

    output:
    stdout

    script:
    """
    sleep 3
    echo "${word}" | wc -c
    """
}

workflow {

    // Create a channel to drive some processes
    // (the observer will print emojis on process start/complete)
    greetings = Channel.of('Hello', 'Hola', 'Bonjour')
    words     = Channel.of('Nextflow', 'Emoji', 'Plugin')

    SAY_HELLO(greetings)
    //SAY_HELLO.out.emojiView(emoji: '🦠')

    COUNT_LETTERS(words)
    //COUNT_LETTERS.out.emojiDump(tag: 'letter-count', emoji: '🔢')
}
