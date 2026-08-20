package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ultraengine.benchmarks.BenchmarkResult
import com.example.ultraengine.benchmarks.BenchmarkRunner
import com.example.ultraengine.benchmarks.FullBenchmarkReport
import com.example.ultraengine.concurrency.BackoffWaitStrategy
import com.example.ultraengine.concurrency.BusySpinWaitStrategy
import com.example.ultraengine.concurrency.SleepingWaitStrategy
import com.example.ultraengine.concurrency.WaitStrategy
import com.example.ultraengine.concurrency.YieldingWaitStrategy
import com.example.ultraengine.core.EngineConfig
import com.example.ultraengine.core.EngineState
import com.example.ultraengine.events.EventBus
import com.example.ultraengine.events.EventLoop
import com.example.ultraengine.events.FastEvent
import com.example.ultraengine.examples.MarketDataSimulator
import com.example.ultraengine.examples.MarketQuote
import com.example.ultraengine.examples.Order
import com.example.ultraengine.examples.RealTimeOrderMatcher
import com.example.ultraengine.examples.SensorReading
import com.example.ultraengine.examples.Side
import com.example.ultraengine.examples.TelemetryPipeline
import com.example.ultraengine.examples.TradeMatch
import com.example.ultraengine.memory.ObjectPool
import com.example.ultraengine.platform.PlatformProvider
import com.example.ultraengine.scheduling.BackpressureController
import com.example.ultraengine.scheduling.BackpressureStrategy
import com.example.ultraengine.scheduling.EventPriority
import com.example.ultraengine.storage.AppendOnlyEventLog
import com.example.ultraengine.storage.EventReplayer
import com.example.ultraengine.storage.ReplayStats
import com.example.ultraengine.telemetry.EngineTelemetry
import com.example.ultraengine.telemetry.EngineTelemetrySnapshot
import com.example.ultraengine.tests.EngineVerificationSuite
import com.example.ultraengine.tests.TestSuiteResult
import com.example.ultraengine.timing.NanoClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class UltraEngineViewModel(application: Application) : AndroidViewModel(application) {

    // --- Core Engine & Telemetry State ---
    private val _engineState = MutableStateFlow(EngineState.RUNNING)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    private val _telemetrySnapshot = MutableStateFlow<EngineTelemetrySnapshot?>(null)
    val telemetrySnapshot: StateFlow<EngineTelemetrySnapshot?> = _telemetrySnapshot.asStateFlow()

    private val _waitStrategyName = MutableStateFlow("Yielding")
    val waitStrategyName: StateFlow<String> = _waitStrategyName.asStateFlow()

    private val _backpressureStrategy = MutableStateFlow(BackpressureStrategy.BLOCK_CONTROLLED)
    val backpressureStrategy: StateFlow<BackpressureStrategy> = _backpressureStrategy.asStateFlow()

    private val _targetIngestRate = MutableStateFlow(50_000) // events/sec
    val targetIngestRate: StateFlow<Int> = _targetIngestRate.asStateFlow()

    // --- Domain: Market Data State ---
    private val _marketQuotes = MutableStateFlow<List<MarketQuote>>(emptyList())
    val marketQuotes: StateFlow<List<MarketQuote>> = _marketQuotes.asStateFlow()

    private val _orderBookMatches = MutableStateFlow<List<TradeMatch>>(emptyList())
    val orderBookMatches: StateFlow<List<TradeMatch>> = _orderBookMatches.asStateFlow()

    // --- Domain: Sensor Telemetry State ---
    private val _sensorReadings = MutableStateFlow<List<SensorReading>>(emptyList())
    val sensorReadings: StateFlow<List<SensorReading>> = _sensorReadings.asStateFlow()

    // --- Benchmarks State ---
    private val _benchmarkReport = MutableStateFlow<FullBenchmarkReport?>(null)
    val benchmarkReport: StateFlow<FullBenchmarkReport?> = _benchmarkReport.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    private val _benchmarkProgress = MutableStateFlow(0f)
    val benchmarkProgress: StateFlow<Float> = _benchmarkProgress.asStateFlow()

    private val _benchmarkStatusText = MutableStateFlow("")
    val benchmarkStatusText: StateFlow<String> = _benchmarkStatusText.asStateFlow()

    // --- Verification & Tests State ---
    private val _testResults = MutableStateFlow<List<TestSuiteResult>>(emptyList())
    val testResults: StateFlow<List<TestSuiteResult>> = _testResults.asStateFlow()

    private val _isRunningTests = MutableStateFlow(false)
    val isRunningTests: StateFlow<Boolean> = _isRunningTests.asStateFlow()

    // --- Storage & Replay State ---
    private val _replayStats = MutableStateFlow<ReplayStats?>(null)
    val replayStats: StateFlow<ReplayStats?> = _replayStats.asStateFlow()

    // --- Engine Internals ---
    private val telemetry = EngineTelemetry()
    private val eventBus = EventBus(128)
    private var eventLoop: EventLoop = EventLoop(eventBus = eventBus)
    private val eventPool = ObjectPool(4096) { FastEvent() }
    private val backpressure = BackpressureController()
    private val marketSimulator = MarketDataSimulator()
    private val telemetryPipeline = TelemetryPipeline()
    private val orderMatcher = RealTimeOrderMatcher("BTC-USD")
    
    private val journalFile by lazy {
        File(getApplication<Application>().cacheDir, "ultra_journal.bin")
    }
    private var eventLog: AppendOnlyEventLog? = null

    private var telemetryPollingJob: Job? = null
    private var generatorJob: Job? = null
    private val isEngineActive = AtomicBoolean(true)

    init {
        setupEventHandlers()
        setupEventLog()
        startEngine()
    }

    private fun setupEventLog() {
        try {
            eventLog = AppendOnlyEventLog(journalFile, preallocateSizeBytes = 2 * 1024 * 1024L)
        } catch (_: Throwable) {}
    }

    private fun setupEventHandlers() {
        // EventType 100: Market Quote
        eventBus.register(100) { event ->
            val latencyNs = NanoClock.nowNanos() - event.timestampNanos
            telemetry.recordInProcessLatency(latencyNs)

            val quote = marketSimulator.parseQuote(event)
            val current = _marketQuotes.value
            _marketQuotes.value = (listOf(quote) + current).take(20)

            eventLog?.append(event)
            eventPool.release(event)
        }

        // EventType 200: Sensor Telemetry
        eventBus.register(200) { event ->
            val latencyNs = NanoClock.nowNanos() - event.timestampNanos
            telemetry.recordInProcessLatency(latencyNs)

            val reading = telemetryPipeline.process(event)
            val current = _sensorReadings.value
            _sensorReadings.value = (listOf(reading) + current).take(20)

            eventLog?.append(event)
            eventPool.release(event)
        }
    }

    fun startEngine() {
        if (_engineState.value == EngineState.RUNNING && generatorJob?.isActive == true) return

        _engineState.value = EngineState.RUNNING
        isEngineActive.set(true)
        eventLoop.start()

        // 1. Telemetry sampling loop (every 100ms)
        telemetryPollingJob?.cancel()
        telemetryPollingJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val snapshot = telemetry.sample(currentQueueDepth = eventLoop.ringBuffer.size)
                _telemetrySnapshot.value = snapshot
                delay(100)
            }
        }

        // 2. High-speed synthetic ingest stream
        generatorJob?.cancel()
        generatorJob = viewModelScope.launch(Dispatchers.Default) {
            var counter = 0L
            while (isActive && isEngineActive.get()) {
                val batchSize = 32
                val delayTimeMicros = (batchSize * 1_000_000L) / _targetIngestRate.value.coerceAtLeast(1000)

                for (b in 0 until batchSize) {
                    counter++
                    val qDepth = eventLoop.ringBuffer.size
                    val prio = if (counter % 20 == 0L) EventPriority.CRITICAL else EventPriority.NORMAL

                    if (backpressure.shouldAccept(qDepth, prio)) {
                        val event = if (counter % 2 == 0L) {
                            marketSimulator.generateTick()
                        } else {
                            telemetryPipeline.generateReading((counter % 8).toInt() + 1)
                        }

                        val queued = eventLoop.submit(event)
                        if (!queued) {
                            telemetry.recordDroppedEvent()
                            eventPool.release(event)
                        }
                    } else {
                        telemetry.recordDroppedEvent()
                    }
                }

                if (delayTimeMicros > 1000) {
                    delay(delayTimeMicros / 1000L)
                } else {
                    // Micro-pause
                    Thread.onSpinWait()
                }
            }
        }
    }

    fun pauseEngine() {
        _engineState.value = EngineState.PAUSED
        isEngineActive.set(false)
        generatorJob?.cancel()
    }

    fun stopEngine() {
        _engineState.value = EngineState.STOPPED
        isEngineActive.set(false)
        generatorJob?.cancel()
        eventLoop.stop()
    }

    fun resetTelemetry() {
        telemetry.reset()
    }

    fun setTargetIngestRate(rate: Int) {
        _targetIngestRate.value = rate.coerceIn(1_000, 500_000)
    }

    fun setWaitStrategy(name: String) {
        _waitStrategyName.value = name
        val strategy: WaitStrategy = when (name) {
            "BusySpin" -> BusySpinWaitStrategy()
            "Yielding" -> YieldingWaitStrategy()
            "Backoff" -> BackoffWaitStrategy()
            "Sleeping" -> SleepingWaitStrategy()
            else -> YieldingWaitStrategy()
        }
        eventLoop.stop()
        eventLoop = EventLoop(
            waitStrategy = strategy,
            eventBus = eventBus
        )
        if (_engineState.value == EngineState.RUNNING) {
            eventLoop.start()
        }
    }

    fun setBackpressureStrategy(strategy: BackpressureStrategy) {
        _backpressureStrategy.value = strategy
    }

    // --- Order Matching Interaction ---
    fun submitManualOrder(side: Side, price: Double, quantity: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val order = Order(
                orderId = System.currentTimeMillis(),
                side = side,
                price = price,
                quantity = quantity,
                timestampNanos = NanoClock.nowNanos()
            )
            val matches = orderMatcher.submitOrder(order)
            if (matches.isNotEmpty()) {
                val current = _orderBookMatches.value
                _orderBookMatches.value = (matches + current).take(20)
            }
        }
    }

    // --- Benchmark Execution ---
    fun runFullBenchmarkSuite() {
        if (_isBenchmarking.value) return
        _isBenchmarking.value = true
        _benchmarkProgress.value = 0f
        _benchmarkStatusText.value = "Starting Benchmark Suite..."

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val report = BenchmarkRunner.runFullSuite { status, progress ->
                    _benchmarkStatusText.value = status
                    _benchmarkProgress.value = progress
                }
                _benchmarkReport.value = report
                _benchmarkStatusText.value = "Benchmarks completed successfully!"
            } catch (e: Throwable) {
                _benchmarkStatusText.value = "Benchmark error: ${e.message}"
            } finally {
                _isBenchmarking.value = false
            }
        }
    }

    // --- Verification & Chaos Tests ---
    fun runVerificationTests() {
        if (_isRunningTests.value) return
        _isRunningTests.value = true
        _testResults.value = emptyList()

        viewModelScope.launch(Dispatchers.Default) {
            val results = mutableListOf<TestSuiteResult>()

            // 1. Concurrency Test
            results.add(EngineVerificationSuite.testConcurrencyIntegrity())
            _testResults.value = results.toList()
            delay(100)

            // 2. Backpressure Test
            results.add(EngineVerificationSuite.testBackpressureShedding())
            _testResults.value = results.toList()
            delay(100)

            // 3. Storage & Replay Test
            results.add(EngineVerificationSuite.testStorageReplayDeterminism(getApplication<Application>().cacheDir))
            _testResults.value = results.toList()
            delay(100)

            // 4. Soak Test
            results.add(EngineVerificationSuite.runSoakTest(250_000))
            _testResults.value = results.toList()

            _isRunningTests.value = false
        }
    }

    // --- Deterministic Replay Action ---
    fun triggerEventReplay() {
        viewModelScope.launch(Dispatchers.Default) {
            eventLog?.flush()
            val replayer = EventReplayer(journalFile)
            val stats = replayer.replay { /* replayed event */ }
            _replayStats.value = stats
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopEngine()
        eventLog?.close()
    }
}
