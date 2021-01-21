package com.viartemev.thewhiterabbit.queue

import com.viartemev.thewhiterabbit.AbstractTestContainersTest
import com.viartemev.thewhiterabbit.channel.confirmChannel
import com.viartemev.thewhiterabbit.channel.publish
import com.viartemev.thewhiterabbit.exchange.ExchangeSpecification
import com.viartemev.thewhiterabbit.exchange.declareExchange
import com.viartemev.thewhiterabbit.utils.createMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueueTest : AbstractTestContainersTest() {

    @Test
    fun `declare a queue test`() {
        val queueName = "declare_queue_test"
        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                runBlocking {
                    channel.declareQueue(QueueSpecification(queueName))
                }
            }
        }
        assertNotNull(httpRabbitMQClient.getQueue(DEFAULT_VHOST, queueName))
    }

    @Test
    fun `delete a queue test`() {
        val queueName = "delete_queue_test"
        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                runBlocking {
                    channel.declareQueue(QueueSpecification(queueName))
                    assertNotNull(httpRabbitMQClient.getQueue(DEFAULT_VHOST, queueName))
                    channel.deleteQueue(DeleteQueueSpecification(queueName))
                    assertNull(httpRabbitMQClient.getQueue(DEFAULT_VHOST, queueName))
                }
            }
        }
    }

    @Test
    fun `purge a queue test`() {
        val queueName = "purge_queue_test"
        factory.newConnection().use { connection ->
            runBlocking {
                connection.confirmChannel {
                    declareQueue(QueueSpecification(queueName))
                    val queue = httpRabbitMQClient.getQueue(DEFAULT_VHOST, queueName)
                    assertNotNull(queue)
                    assertEquals(0, queue.totalMessages)

                    //publish { publishWithConfirmAsync(messages = (1..10).map { createMessage(queue = queueName, body = "") }) }
                    delay(5000)
                    val queueWithPublishedMessages = httpRabbitMQClient.getQueue(DEFAULT_VHOST, queueName)
                    assertNotNull(queueWithPublishedMessages)
                    assertEquals(10, queueWithPublishedMessages.totalMessages)

                    purgeQueue(PurgeQueueSpecification(queueName))
                    delay(5000)
                    val purgedQueue = httpRabbitMQClient.getQueue(DEFAULT_VHOST, queueName)
                    assertNotNull(purgedQueue)
                    assertEquals(0, purgedQueue.totalMessages)
                }
            }
        }
    }

    @Test
    fun `bind a queue test`() {
        val queueName = "purge_queue_test"
        val exchangeName = "new_exchange"
        factory.newConnection().use { connection ->
            runBlocking {
                connection.confirmChannel {
                    declareQueue(QueueSpecification(queueName))
                    declareExchange(ExchangeSpecification(exchangeName))
                    val queueBindingsBefore = httpRabbitMQClient.getQueueBindingsBetween(DEFAULT_VHOST, exchangeName, queueName)
                    assertTrue(queueBindingsBefore.isEmpty())

                    bindQueue(BindQueueSpecification(queueName, exchangeName))
                    val queueBindingsAfter = httpRabbitMQClient.getQueueBindingsBetween(DEFAULT_VHOST, exchangeName, queueName)
                    assertNotNull(queueBindingsAfter.isNotEmpty())
                }
            }
        }
    }

    @Test
    @Disabled("fixme")
    fun `unbind a queue test`() {
        val queueName = "purge_queue_test"
        val exchangeName = "new_exchange"
        val routingKey = "routing_key"
        factory.newConnection().use { connection ->
            runBlocking {
                connection.confirmChannel {
                    declareQueue(QueueSpecification(queueName))
                    declareExchange(ExchangeSpecification(exchangeName))
                    val queue = httpRabbitMQClient.getQueue(DEFAULT_VHOST, queueName)
                    assertNotNull(queue)
                    bindQueue(BindQueueSpecification(queueName, exchangeName, routingKey))
                    val binding = httpRabbitMQClient.getQueueBindingsBetween(DEFAULT_VHOST, exchangeName, queueName)
                    assertTrue(binding.isNotEmpty())

                    unbindQueue(UnbindQueueSpecification(queueName, exchangeName, routingKey))
                    delay(4000)
                    val bindingAfterUnbind = httpRabbitMQClient.getQueueBindingsBetween(DEFAULT_VHOST, exchangeName, queueName)
                    assertTrue(bindingAfterUnbind.isEmpty())
                }
            }
        }
    }
}
