package com.example.ultraengine.core

/**
 * Marks a code path as latency-critical (HOT PATH).
 * Hot paths must strictly avoid:
 * - Object allocations
 * - Logging & string concatenations
 * - Reflection & dynamic virtual dispatch
 * - Blocking calls & context switching
 * - Synchronization / heavy locking
 * - Filesystem & database access
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class HotPath(val description: String = "")

/**
 * Marks initialization, configuration, or cold-start paths.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ColdPath(val description: String = "")

/**
 * Marks control-plane, administration, or monitoring paths outside latency bounds.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ControlPath(val description: String = "")

/**
 * Marks asynchronous offloaded I/O paths (e.g. disk flush, network async read).
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class IoPath(val description: String = "")

/**
 * Indicates that the field or struct uses cache-line padding (typically 64 or 128 bytes)
 * to prevent CPU false-sharing across cores.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class CacheLinePad
