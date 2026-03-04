package rike.plugin

import nextflow.Session
import nextflow.processor.TaskProcessor
import nextflow.processor.TaskHandler
import nextflow.processor.TaskRun
import nextflow.trace.TraceRecord
import spock.lang.Specification

import java.time.LocalDate

class EmojiObserverTest extends Specification {

    // --- Factory ---

    def 'should create the observer instance'() {
        given:
        def factory = new EmojiFactory()
        when:
        def result = factory.create(Mock(Session))
        then:
        result.size() == 1
        result.first() instanceof EmojiObserver
    }

    // --- Configuration ---

    def 'should use default configuration values'() {
        given:
        def observer = new EmojiObserver()
        expect:
        observer.showProgressBar == true
        observer.showGreeting == true
        observer.showSummary == true
        observer.showConfetti == false
        observer.theme == EmojiObserver.THEMES.get('default')
    }

    def 'should configure theme from session config'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [emoji: [theme: 'space']]

        when:
        observer.onFlowCreate(session)

        then:
        observer.theme == EmojiObserver.THEMES.get('space')
    }

    def 'should fall back to default for unknown theme'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [emoji: [theme: 'nonexistent']]

        when:
        observer.onFlowCreate(session)

        then:
        observer.theme == EmojiObserver.THEMES.get('default')
    }

    def 'should configure feature toggles from session config'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [emoji: [
            progressBar: false,
            greeting: false,
            summary: false,
            confetti: true,
        ]]

        when:
        observer.onFlowCreate(session)

        then:
        observer.showProgressBar == false
        observer.showGreeting == false
        observer.showSummary == false
        observer.showConfetti == true
    }

    def 'should use defaults when emoji config block is missing'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [:]

        when:
        observer.onFlowCreate(session)

        then:
        observer.theme == EmojiObserver.THEMES.get('default')
        observer.showProgressBar == true
        observer.showGreeting == true
        observer.showSummary == true
        observer.showConfetti == false
    }

    // --- Themes ---

    def 'should have all expected themes'() {
        expect:
        EmojiObserver.THEMES.keySet() == ['default', 'space', 'ocean', 'lab', 'food', 'pirate', 'animal', 'nfcore'] as Set
    }

    def 'each theme should have all required keys'() {
        given:
        def requiredKeys = ['filled', 'empty', 'completed', 'cached', 'failed', 'summary', 'error'] as Set

        expect:
        EmojiObserver.THEMES.each { name, theme ->
            assert theme.keySet() == requiredKeys : "Theme '${name}' missing keys"
        }
    }

    def 'should select theme: #themeName'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [emoji: [theme: themeName, greeting: false]]

        when:
        observer.onFlowCreate(session)

        then:
        observer.theme == EmojiObserver.THEMES.get(themeName)

        where:
        themeName << ['default', 'space', 'ocean', 'lab', 'food', 'pirate', 'animal', 'nfcore']
    }

    def 'should pick a valid theme when random is selected'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [emoji: [theme: 'random', greeting: false]]

        when:
        observer.onFlowCreate(session)

        then:
        EmojiObserver.THEMES.values().contains(observer.theme)
    }

    // --- Custom greeting ---

    def 'should use custom greeting when greeting is a string'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [emoji: [greeting: 'Hello from the lab!']]

        when:
        observer.onFlowCreate(session)

        then:
        observer.showGreeting == true
        observer.customGreeting == 'Hello from the lab!'
    }

    def 'should use seasonal greeting when greeting is true'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [emoji: [greeting: true]]

        when:
        observer.onFlowCreate(session)

        then:
        observer.showGreeting == true
        observer.customGreeting == null
    }

    def 'should disable greeting when greeting is false'() {
        given:
        def observer = new EmojiObserver()
        def session = Mock(Session)
        session.config >> [emoji: [greeting: false]]

        when:
        observer.onFlowCreate(session)

        then:
        observer.showGreeting == false
    }

    // --- Process tracking ---

    def 'should track completed processes'() {
        given:
        def observer = new EmojiObserver()
        observer.showProgressBar = false
        def handler = mockHandler('HELLO', 0)

        when:
        observer.onProcessComplete(handler, Mock(TraceRecord))

        then:
        observer.completed.get('HELLO')?.get() == 1
        observer.done.get() == 1
    }

    def 'should track failed processes'() {
        given:
        def observer = new EmojiObserver()
        observer.showProgressBar = false
        def handler = mockHandler('HELLO', 1)

        when:
        observer.onProcessComplete(handler, Mock(TraceRecord))

        then:
        observer.failed.get('HELLO')?.get() == 1
        observer.done.get() == 1
    }

    def 'should track cached processes'() {
        given:
        def observer = new EmojiObserver()
        observer.showProgressBar = false
        def handler = mockHandler('HELLO', 0)

        when:
        observer.onProcessCached(handler, Mock(TraceRecord))

        then:
        observer.cached.get('HELLO')?.get() == 1
        observer.done.get() == 1
        observer.submitted.get() == 1
    }

    def 'should track submitted processes'() {
        given:
        def observer = new EmojiObserver()
        def handler = mockHandler('HELLO', 0)

        when:
        observer.onProcessSubmit(handler, Mock(TraceRecord))

        then:
        observer.submitted.get() == 1
    }

    def 'should track multiple processes independently'() {
        given:
        def observer = new EmojiObserver()
        observer.showProgressBar = false
        def handler1 = mockHandler('HELLO', 0)
        def handler2 = mockHandler('WORLD', 0)
        def handler3 = mockHandler('HELLO', 1)

        when:
        observer.onProcessComplete(handler1, Mock(TraceRecord))
        observer.onProcessComplete(handler2, Mock(TraceRecord))
        observer.onProcessComplete(handler3, Mock(TraceRecord))

        then:
        observer.completed.get('HELLO')?.get() == 1
        observer.completed.get('WORLD')?.get() == 1
        observer.failed.get('HELLO')?.get() == 1
        observer.done.get() == 3
    }

    // --- Seasonal greetings ---

    def 'should return special greeting for #name'() {
        given:
        def observer = new EmojiObserver()

        expect:
        def greeting = observer.getSeasonalGreeting(LocalDate.of(2025, month, day))
        greeting.contains(expectedEmoji)

        where:
        month | day | expectedEmoji | name
        1     | 1   | '🍀'         | "New Year's Day"
        2     | 14  | '💕'         | "Valentine's Day"
        3     | 14  | '🥧'         | 'Pi Day'
        4     | 22  | '🌍'         | 'Earth Day'
        4     | 25  | '🧬'         | 'DNA Day'
        10    | 31  | '🎃'         | 'Halloween'
        12    | 25  | '🎅'         | 'Christmas'
        12    | 31  | '🎆'         | "New Year's Eve"
    }

    def 'should return seasonal greeting for #season'() {
        given:
        def observer = new EmojiObserver()

        expect:
        def greeting = observer.getSeasonalGreeting(LocalDate.of(2025, month, day))
        greeting.contains(expectedEmoji)

        where:
        month | day | expectedEmoji | season
        4     | 15  | '🌱'         | 'spring'
        7     | 15  | '☀️'         | 'summer'
        10    | 15  | '🍂'         | 'fall'
        1     | 15  | '❄️'         | 'winter'
    }

    // --- Countdown ---

    def 'should calculate countdown to next festive day'() {
        given:
        def observer = new EmojiObserver()
        def today = LocalDate.of(2025, 10, 29)

        when:
        def countdown = observer.getCountdown(today)

        then:
        countdown != null
        countdown.contains('2 days until Halloween')
    }

    def 'should wrap to next year for countdown'() {
        given:
        def observer = new EmojiObserver()
        // Dec 31 is a festive day itself, so countdown should look to next year
        def today = LocalDate.of(2025, 12, 31)

        when:
        def greeting = observer.getSeasonalGreeting(today)

        then:
        // Should get the New Year's Eve greeting, not a countdown
        greeting.contains('🎆')
    }

    def 'should show singular day in countdown'() {
        given:
        def observer = new EmojiObserver()
        def today = LocalDate.of(2025, 10, 30)

        when:
        def countdown = observer.getCountdown(today)

        then:
        countdown.contains('1 day until Halloween')
        !countdown.contains('days')
    }

    // --- Flow lifecycle ---

    def 'should cancel progress timer on flow complete'() {
        given:
        def observer = new EmojiObserver()
        observer.showSummary = false
        observer.progressTimer = new Timer('test', true)

        when:
        observer.onFlowComplete()

        then:
        noExceptionThrown()
    }

    def 'should handle flow complete with no timer'() {
        given:
        def observer = new EmojiObserver()
        observer.showSummary = false
        observer.progressTimer = null

        when:
        observer.onFlowComplete()

        then:
        noExceptionThrown()
    }

    // --- Festive days data ---

    def 'festive days should be well-formed'() {
        expect:
        EmojiObserver.FESTIVE_DAYS.each { event ->
            assert event.size() == 4
            assert event.get(0) instanceof Integer
            assert event.get(1) instanceof Integer
            assert event.get(2) instanceof String
            assert event.get(3) instanceof String
            def month = event.get(0) as int
            def day = event.get(1) as int
            assert month >= 1 && month <= 12
            assert day >= 1 && day <= 31
        }
    }

    // --- Helper ---

    private TaskHandler mockHandler(String processName, int exitStatus) {
        def processor = Mock(TaskProcessor)
        processor.name >> processName

        def task = Mock(TaskRun)
        task.processor >> processor
        task.exitStatus >> exitStatus

        def handler = Mock(TaskHandler)
        handler.task >> task
        return handler
    }
}
