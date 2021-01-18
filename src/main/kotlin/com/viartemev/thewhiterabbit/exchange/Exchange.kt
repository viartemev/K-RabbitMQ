package com.viartemev.thewhiterabbit.exchange

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.viartemev.thewhiterabbit.common.resourceManagementDispatcher
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Declare an exchange following the specification on resource management dispatcher context.
 * @see com.viartemev.thewhiterabbit.exchange.ExchangeSpecification
 * @return a declare-confirm method to indicate the exchange was successfully declared.
 * @throws java.io.IOException if an error is encountered
 */
suspend fun Channel.declareExchange(exchangeSpecification: ExchangeSpecification): AMQP.Exchange.DeclareOk {
    val channel = this
    val declaration = AMQP.Exchange.Declare.Builder()
        .exchange(exchangeSpecification.name)
        .type(exchangeSpecification.type.type)
        .durable(exchangeSpecification.durable)
        .autoDelete(exchangeSpecification.autoDelete)
        .internal(exchangeSpecification.internal)
        .arguments(exchangeSpecification.arguments)
        .build()

    return withContext(resourceManagementDispatcher) {
        channel.asyncCompletableRpc(declaration).await().method as AMQP.Exchange.DeclareOk
    }
}

/**
 * Delete an exchange following the specification on resource management dispatcher context.
 * @see com.viartemev.thewhiterabbit.exchange.DeleteExchangeSpecification
 * @return a deletion-confirm method to indicate the exchange was successfully deleted
 * @throws java.io.IOException if an error is encountered
 */
suspend fun Channel.deleteExchange(specification: DeleteExchangeSpecification): AMQP.Exchange.DeleteOk {
    val channel = this
    val deleteDeclaration = AMQP.Exchange.Delete.Builder()
        .exchange(specification.name)
        .ifUnused(specification.ifUnused)
        .nowait(specification.noWait)
        .build()

    return withContext(resourceManagementDispatcher) {
        channel.asyncCompletableRpc(deleteDeclaration).await().method as AMQP.Exchange.DeleteOk
    }
}

/**
 * Bind an exchange following the specification on resource management dispatcher context.
 * @see com.viartemev.thewhiterabbit.exchange.BindExchangeSpecification
 * @return a binding-confirm method if the binding was successfully created
 * @throws java.io.IOException if an error is encountered
 */
suspend fun Channel.bindExchange(specification: BindExchangeSpecification): AMQP.Exchange.BindOk {
    val channel = this
    val bindDeclaration = AMQP.Exchange.Bind.Builder()
        .source(specification.source)
        .destination(specification.destination)
        .nowait(specification.noWait)
        .routingKey(specification.routingKey)
        .arguments(specification.arguments)
        .build()

    return withContext(resourceManagementDispatcher) {
        channel.asyncCompletableRpc(bindDeclaration).await().method as AMQP.Exchange.BindOk
    }
}

/**
 * Unbind an exchange following the specification on resource management dispatcher context.
 * @see com.viartemev.thewhiterabbit.exchange.UnbindExchangeSpecification
 * @return a binding-confirm method if the binding was successfully created
 * @throws java.io.IOException if an error is encountered
 */
suspend fun Channel.unbindExchange(specification: UnbindExchangeSpecification): AMQP.Exchange.UnbindOk {
    val channel = this
    val unbindDeclaration = AMQP.Exchange.Unbind.Builder()
        .source(specification.source)
        .destination(specification.destination)
        .nowait(specification.noWait)
        .routingKey(specification.routingKey)
        .arguments(specification.arguments)
        .build()

    return withContext(resourceManagementDispatcher) {
        channel.asyncCompletableRpc(unbindDeclaration).await().method as AMQP.Exchange.UnbindOk
    }
}
