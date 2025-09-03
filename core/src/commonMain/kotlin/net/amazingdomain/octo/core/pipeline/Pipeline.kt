package net.amazingdomain.octo.core.pipeline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.amazingdomain.octo.core.networking.Listener
import net.amazingdomain.octo.core.networking.SinkListener
import net.amazingdomain.octo.core.networking.SourceListener

/**
 * A Pipeline orchestrates the unidirectional flow of data from a [SourceListener],
 * through a processing [Listener], to a [SinkListener].
 *
 * The data flow is as follows:
 * 1. Data is read from the `source`.
 * 2. The data is written to the `processor`.
 * 3. The processed data is read from the `processor`.
 * 4. The processed data is written to the `sink`.
 *
 * The pipeline runs these flows concurrently in the background.
 *
 * @param source The source of the data.
 * @param processor The component that processes the data.
 * @param sink The destination for the processed data.
 * @param scope The [CoroutineScope] to run the pipeline jobs in.
 */
class Pipeline(
    private val source: SourceListener,
    private val processor: Listener,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val logger = KotlinLogging.logger {}
    var pipelineJob: Job? = null
        private set

    /**
     * Starts the pipeline.
     * This will start the source listener and begin processing data.
     * This function is non-blocking.
     */
    suspend fun start() {
        if (pipelineJob?.isActive == true) {
            logger.warn { "Pipeline is already running." }
            return
        }

        logger.info { "Starting pipeline..." }
        source.start()

        // TODO introduce enum of statuses: init, started, closed(with bitwise for read, and for write)
        pipelineJob = scope.launch(scope.coroutineContext) {
            val sourceToProcessor = launch(scope.coroutineContext) {
                logger.debug { "Starting source-to-processor flow." }
                try {
                    while (isActive && source.isOpen()) {

//                        processor.write(input)
                    }
                   // logger.warn { "source.isOpenForRead() = ${source.isOpenForRead()} / scope isActive = $isActive" }
                } catch (e: Exception) {
                    // No op, means no more data from source
                    // TODO shut down the pipeline
                } finally {
                    logger.info { "Source-to-processor flow finished." }
                    stop()
                }
            }

            logger.info { "Pipeline started." }
        }
    }

    /**
     * Stops the pipeline and releases all resources.
     */
    suspend fun stop() {
        logger.info { "Stopping pipeline..." }
        pipelineJob?.cancelAndJoin()
        pipelineJob = null

        source.close()
        processor.close()
        logger.info { "Pipeline stopped." }
    }

}