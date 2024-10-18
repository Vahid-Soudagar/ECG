package com.vcreate.ecg.data.model

data class Segment(
    val end_time: Double,
    val p: P,
    val q: Q,
    val r: R,
    val s: S,
    val start_time: Double,
    val t: T,
    val valid: Boolean
)