package io.github.hejcz

import kotlinx.coroutines.*
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import java.lang.Runnable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun main() {
    example3()
}

private fun example1() {
    val executor = Executors.newSingleThreadExecutor()
    Dsl.asyncHttpClient().use {
        runBlocking {
            val dispatcher = executor.asCoroutineDispatcher()
            CoroutineScope(dispatcher).launch {
                println("1st starts")
                val runner = async { callGoogle(it, executor) }
                println("1st after call") // will print this before actual call
                println(runner.await())
            }
            CoroutineScope(dispatcher).launch {
                println("2nd starts")
                delay(2000)
                println("2nd after delay")
            }
            println("Main waits")
            delay(4000)
        }
    }
    executor.shutdown()
}

private fun example2() {
    val executor = Executors.newSingleThreadExecutor()
    Dsl.asyncHttpClient().use {
        runBlocking {
            val dispatcher = executor.asCoroutineDispatcher()
            CoroutineScope(dispatcher).launch {
                println("1st starts")
                val runner = callGoogle(it, executor)
                println("1st after call") // will print this after call
                println(runner)
            }
            CoroutineScope(dispatcher).launch {
                println("2nd starts")
                delay(2000)
                println("2nd after delay")
            }
            println("Main waits")
            delay(4000)
        }
    }
    executor.shutdown()
}

// runs on main thread
private fun example3() {
    Dsl.asyncHttpClient().use {
        runBlocking {
            launch {
                println("First starts")
                val runner = callGoogle(it, null)
                println(runner)
            }
            launch {
                println("Second starts")
                delay(2000)
                println("Second ends")
            }
        }
    }
}

private suspend fun callGoogle(
    httpClient: AsyncHttpClient,
    executor: ExecutorService?
): Response =
    suspendCancellableCoroutine { uCont: CancellableContinuation<Response> ->
        println("Call starts")
        val request: ListenableFuture<Response> =
            httpClient.executeRequest(Dsl.get("http://httpstat.us/200?sleep=1000").build())
        request.addListener(
            Runnable { println("Response received"); uCont.resumeWith(Result.success(request.get())); },
            executor
        )
        println("Call executed")
    }

