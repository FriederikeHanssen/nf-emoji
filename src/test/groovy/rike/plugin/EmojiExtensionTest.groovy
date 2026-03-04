package rike.plugin

import nextflow.Session
import spock.lang.Specification

class EmojiExtensionTest extends Specification {

    def 'should create extension point'() {
        given:
        def ext = new EmojiExtension()

        when:
        ext.init(Mock(Session))

        then:
        noExceptionThrown()
    }

    def 'should print emoji via function'() {
        given:
        def ext = new EmojiExtension()
        def output = new ByteArrayOutputStream()
        System.setOut(new PrintStream(output))

        when:
        ext.printEmoji('🚀')

        then:
        output.toString().trim() == '🚀'

        cleanup:
        System.setOut(System.out)
    }
}
