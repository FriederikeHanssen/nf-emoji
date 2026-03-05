/*
 * Copyright 2025, Friederike Hanssen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rike.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.trace.TraceObserver
import nextflow.processor.TaskHandler
import nextflow.trace.TraceRecord

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.time.LocalDate
import java.time.temporal.ChronoUnit
/**
 * Implements an observer that allows implementing custom
 * logic on nextflow execution events.
 */
@Slf4j
@CompileStatic
class EmojiObserver implements TraceObserver {

    // Theme definitions: each theme maps emoji roles to characters
    static final Map<String, Map<String, String>> THEMES = [
        'default': [filled: '🟩', empty: '⬜', completed: '✅', cached: '♻️', failed: '❌', summary: '🏁', error: '☠️'],
        'space'  : [filled: '🚀', empty: '🌑', completed: '🛸', cached: '🌟', failed: '💥', summary: '🌌', error: '☄️'],
        'ocean'  : [filled: '🌊', empty: '⬜', completed: '🐟', cached: '🐚', failed: '🦈', summary: '🏖️', error: '🌀'],
        'lab'    : [filled: '🧪', empty: '⬜', completed: '🔬', cached: '📋', failed: '☣️', summary: '🧬', error: '⚠️'],
        'food'   : [filled: '🍕', empty: '⬜', completed: '🍰', cached: '🥫', failed: '🔥', summary: '🍽️', error: '🤮'],
        'pirate' : [filled: '🏴‍☠️', empty: '🏳️', completed: '💰', cached: '🗺️', failed: '🦜', summary: '⚓', error: '☠️'],
        'animal' : [filled: '🐎', empty: '⬜', completed: '🦊', cached: '🐢', failed: '🦂', summary: '🦁', error: '🐛'],
        'nf-core': [filled: '🍏', empty: '⬜', completed: '🍏', cached: '🌿', failed: '🍎', summary: '🌳', error: '🍎'],
        'spring' : [filled: '🌷', empty: '⬜', completed: '🌸', cached: '🌱', failed: '🥀', summary: '🌻', error: '🌧️'],
        'summer' : [filled: '☀️', empty: '⬜', completed: '🏖️', cached: '🧊', failed: '🌪️', summary: '🌴', error: '⛈️'],
        'fall'   : [filled: '🍂', empty: '⬜', completed: '🎃', cached: '🍄', failed: '💨', summary: '🍁', error: '🌫️'],
        'winter' : [filled: '❄️', empty: '⬜', completed: '⛄', cached: '🧣', failed: '🥶', summary: '🎄', error: '🌨️'],
    ] as Map<String, Map<String, String>>

    // Active theme
    Map<String, String> theme = THEMES.get('default')

    // Feature toggles (all on by default, confetti off by default)
    boolean showProgressBar = true
    boolean showGreeting = true
    boolean showSummary = true
    boolean showConfetti = false

    // Custom greeting message (null = use seasonal greeting)
    String customGreeting = null

    // Track per-process counts
    ConcurrentHashMap<String, AtomicInteger> completed = new ConcurrentHashMap<>()
    ConcurrentHashMap<String, AtomicInteger> cached = new ConcurrentHashMap<>()
    ConcurrentHashMap<String, AtomicInteger> failed = new ConcurrentHashMap<>()

    // Track overall progress
    AtomicInteger submitted = new AtomicInteger(0)
    AtomicInteger done = new AtomicInteger(0)
    AtomicInteger retries = new AtomicInteger(0)

    // Background timer to keep progress bar visible
    Timer progressTimer

    @Override
    void onFlowBegin() {
        if (showProgressBar) {
            progressTimer = new Timer('emoji-progress', true)
            progressTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                void run() {
                    printProgress()
                }
            }, 0L, 1000L)
        }
    }

    @Override
    void onFlowCreate(Session session) {
        // Read theme from nextflow.config: emoji { theme = 'space' }
        String themeName = session.config.navigate('emoji.theme', 'default') as String
        if (themeName == 'seasonal') {
            int month = LocalDate.now().monthValue
            if (month >= 3 && month <= 5) themeName = 'spring'
            else if (month >= 6 && month <= 8) themeName = 'summer'
            else if (month >= 9 && month <= 11) themeName = 'fall'
            else themeName = 'winter'
        }
        if (THEMES.containsKey(themeName)) {
            theme = THEMES.get(themeName)
        } else {
            log.warn "Unknown emoji theme '${themeName}', using default. Available: ${THEMES.keySet()}"
        }
        // Read feature toggles
        showProgressBar = session.config.navigate('emoji.progressBar', true) as boolean
        showSummary = session.config.navigate('emoji.summary', true) as boolean
        showConfetti = session.config.navigate('emoji.confetti', false) as boolean

        // Greeting: true/false for seasonal, or a custom string
        Object greetingConfig = session.config.navigate('emoji.greeting', true)
        if (greetingConfig instanceof String) {
            showGreeting = true
            customGreeting = greetingConfig as String
        } else {
            showGreeting = greetingConfig as boolean
        }

        if (showGreeting) {
            String greeting = customGreeting ?: getSeasonalGreeting()
            System.err.println("\n" + greeting + "\n")
        }
    }

    @Override
    void onFlowError(TaskHandler handler, TraceRecord trace) {
        System.err.println "${theme.get('error')}"
    }

    @Override
    void onFlowComplete() {
        if (progressTimer != null) {
            progressTimer.cancel()
        }
        if (showSummary) {
            // Use a shutdown hook to print after the ANSI renderer is done
            Runtime.getRuntime().addShutdownHook(new Thread({
                int totalCompleted = 0
                int totalCached = 0
                int totalFailed = 0
                for (AtomicInteger v : completed.values()) totalCompleted += v.get()
                for (AtomicInteger v : cached.values()) totalCached += v.get()
                for (AtomicInteger v : failed.values()) totalFailed += v.get()

                if (showConfetti && totalFailed == 0) {
                    launchConfetti()
                }

                System.err.println "\n${theme.get('summary')} Pipeline complete!"
                System.err.println "${theme.get('completed')} ${totalCompleted} succeeded | ${theme.get('failed')} ${totalFailed} failed | ${theme.get('cached')} ${totalCached} cached"
            }))
        }
    }

    private void launchConfetti() {
        try {
            Process which = new ProcessBuilder('which', 'cli-confetti').start()
            if (which.waitFor() != 0) return

            ProcessBuilder pb = new ProcessBuilder('cli-confetti', '-d', '1')
            pb.inheritIO()
            Process p = pb.start()
            p.waitFor()
        } catch (Exception e) {
            // cli-confetti not found or failed, silently skip
        }
    }

    @Override
    void onProcessSubmit(TaskHandler handler, TraceRecord trace) {
        submitted.incrementAndGet()
    }

    @Override
    void onProcessStart(TaskHandler handler, TraceRecord trace) {
    }

    @Override
    void onProcessCached(TaskHandler handler, TraceRecord trace) {
        increment(cached, handler.task.processor.name)
        submitted.incrementAndGet()
        done.incrementAndGet()
        if (showProgressBar) printProgress()
    }

    @Override
    void onProcessComplete(TaskHandler handler, TraceRecord trace) {
        String name = handler.task.processor.name
        if (handler.task.exitStatus == 0) {
            increment(completed, name)
        } else {
            increment(failed, name)
        }
        done.incrementAndGet()
        if (showProgressBar) printProgress()
    }

    private void increment(ConcurrentHashMap<String, AtomicInteger> map, String key) {
        map.putIfAbsent(key, new AtomicInteger(0))
        map.get(key).incrementAndGet()
    }

    private void printProgress() {
        int total = submitted.get()
        int finished = done.get()
        int percent = total > 0 ? Math.round(finished / (float) total * 100) as int : 0
        int filled = Math.round(percent / 10.0f) as int
        int empty = 10 - filled
        String bar = "${theme.get('filled').multiply(filled)}${theme.get('empty').multiply(empty)} ${percent}% (${finished}/${total})"
        System.err.print "\r${bar}   "
        System.err.flush()
    }

    // Festive events: [month, day, emoji, name]
    static final List<List<Object>> FESTIVE_DAYS = [
        [1,  1,  '🍀', 'New Year\'s Day'],
        [2,  14, '💕', 'Valentine\'s Day'],
        [3,  14, '🥧', 'Pi Day'],
        [4,  22, '🌍', 'Earth Day'],
        [4,  25, '🧬', 'DNA Day'],
        [10, 31, '🎃', 'Halloween'],
        [12, 25, '🎅', 'Christmas'],
        [12, 31, '🎆', 'New Year\'s Eve'],
    ] as List<List<Object>>

    String getSeasonalGreeting(LocalDate today = LocalDate.now()) {
        int month = today.monthValue
        int day = today.dayOfMonth

        // Special days
        if (month == 1 && day == 1) return "🍀 Happy New Year! 🍀"
        if (month == 2 && day == 14) return "💕 Love is in the air...! 💕"
        if (month == 3 && day == 14) return "🥧 Happy Pi Day! 🥧"
        if (month == 4 && day == 22) return "🌍 Happy Earth Day! 🌍"
        if (month == 4 && day == 25) return "🧬 Happy DNA Day! 🧬"
        if (month == 10 && day == 31) return "🎃 Something wicked this way computes! 🎃"
        if (month == 12 && (day == 24 || day == 25)) return "🎅 'Twas the night before deployment... 🎅"
        if (month == 12 && day == 31) return "🎆 Happy New Year! 🎆"

        // Seasonal greeting + countdown to next festive day
        String seasonal
        if (month >= 3 && month <= 5) seasonal = "🌱 Happy spring! 🌱"
        else if (month >= 6 && month <= 8) seasonal = "☀️ Happy summer! ☀️"
        else if (month >= 9 && month <= 11) seasonal = "🍂 Happy fall! 🍂"
        else seasonal = "❄️ Happy winter! ❄️"

        String countdown = getCountdown(today)
        return countdown != null ? "${seasonal}\n${countdown}" : seasonal
    }

    String getCountdown(LocalDate today) {
        int year = today.year
        long minDays = Long.MAX_VALUE
        String nextEmoji = null
        String nextName = null

        for (List<Object> event : FESTIVE_DAYS) {
            int m = event.get(0) as int
            int d = event.get(1) as int
            String emoji = event.get(2) as String
            String name = event.get(3) as String

            LocalDate eventDate = LocalDate.of(year, m, d)
            if (!eventDate.isAfter(today)) {
                // Try next year
                eventDate = LocalDate.of(year + 1, m, d)
            }
            long days = ChronoUnit.DAYS.between(today, eventDate)
            if (days > 0 && days < minDays) {
                minDays = days
                nextEmoji = emoji
                nextName = name
            }
        }

        if (nextEmoji != null) {
            return "${nextEmoji} ${minDays} day${minDays == 1L ? '' : 's'} until ${nextName}!"
        }
        return null
    }

}
