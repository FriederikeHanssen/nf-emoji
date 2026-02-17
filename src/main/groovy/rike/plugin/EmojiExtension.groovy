/*
 * Copyright 2025, Seqera Labs
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
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Channel
import nextflow.Session
import nextflow.extension.CH
import nextflow.extension.DataflowHelper
import nextflow.plugin.extension.Function
import nextflow.plugin.extension.Operator
import nextflow.plugin.extension.PluginExtensionPoint

@CompileStatic
class EmojiExtension extends PluginExtensionPoint {

    @Override
    protected void init(Session session) {
    }

    @Function
    void printEmoji(String emoji) {
        println "${emoji}"
    }

    @Operator
    DataflowWriteChannel emojiView(DataflowReadChannel source, Map opts) {
        String prefix = (opts?.emoji as String) ?: '🔍'
        final DataflowWriteChannel output = CH.createBy(source)
        DataflowHelper.subscribeImpl(source, [
            onNext: { Object value ->
                println "${prefix} ${value}"
                output.bind(value)
            },
            onComplete: {
                output.bind(Channel.STOP)
            }
        ])
        return output
    }

    @Operator
    DataflowWriteChannel emojiDump(DataflowReadChannel source, Map opts) {
        String tag = (opts?.tag as String) ?: 'debug'
        String emoji = (opts?.emoji as String) ?: '🔖'
        final DataflowWriteChannel output = CH.createBy(source)
        DataflowHelper.subscribeImpl(source, [
            onNext: { Object value ->
                println "[${emoji} ${tag}] ${value}"
                output.bind(value)
            },
            onComplete: {
                output.bind(Channel.STOP)
            }
        ])
        return output
    }
}
