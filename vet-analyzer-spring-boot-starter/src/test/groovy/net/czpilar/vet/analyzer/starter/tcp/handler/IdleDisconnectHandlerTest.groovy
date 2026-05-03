package net.czpilar.vet.analyzer.starter.tcp.handler

import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import spock.lang.Specification

class IdleDisconnectHandlerTest extends Specification {

    def "IdleStateEvent closes the channel"() {
        given:
        def channel = new EmbeddedChannel(new IdleDisconnectHandler())
        assert channel.isOpen()

        when:
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.READER_IDLE_STATE_EVENT)

        then:
        !channel.isOpen()
    }

    def "non-IdleStateEvent is propagated and channel stays open"() {
        given:
        def channel = new EmbeddedChannel(new IdleDisconnectHandler())
        def someOtherEvent = new Object()

        when:
        channel.pipeline().fireUserEventTriggered(someOtherEvent)

        then:
        channel.isOpen()

        cleanup:
        channel.finish()
    }

    def "WRITER_IDLE_STATE_EVENT also closes channel"() {
        given:
        def channel = new EmbeddedChannel(new IdleDisconnectHandler())

        when:
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT)

        then:
        !channel.isOpen()
    }

    def "ALL_IDLE_STATE_EVENT also closes channel"() {
        given:
        def channel = new EmbeddedChannel(new IdleDisconnectHandler())

        when:
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.ALL_IDLE_STATE_EVENT)

        then:
        !channel.isOpen()
    }
}
