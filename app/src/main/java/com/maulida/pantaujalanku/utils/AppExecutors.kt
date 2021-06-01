package com.maulida.pantaujalanku.utils

import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.logging.Handler

class AppExecutors private constructor(
    val diskIO : Executor,
    val networkIO : Executor,
    val mainThread : Executor
) {

    companion object{
        const val THREAD_COUNT = 3
    }

    constructor() : this(
        Executors.newSingleThreadExecutor(),
        Executors.newFixedThreadPool(THREAD_COUNT),
        MainThreadExecutors()
    )

    fun diskIO() : Executor = diskIO

    fun networkIO() : Executor = networkIO

    fun mainThread() : Executor = mainThread

    private class MainThreadExecutors() : Executor {
        private val mainThreadHandler = android.os.Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

}