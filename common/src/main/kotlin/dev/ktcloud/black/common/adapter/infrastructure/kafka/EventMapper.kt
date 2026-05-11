package dev.ktcloud.black.common.adapter.infrastructure.kafka

interface EventMapper<Message, Event> {
    fun toMessage(event: Event): Message
    fun toEvent(message: Message): Event

    fun toMessage(events: List<Event>): List<Message> = events.map { toMessage(it) }
    fun toEvent(messages: List<Message>): List<Event> = messages.map { toEvent(it) }
}