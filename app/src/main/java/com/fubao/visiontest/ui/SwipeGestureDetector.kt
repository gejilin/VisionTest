package com.fubao.visiontest.ui

data class SwipePoint(
    val x: Float,
    val y: Float,
    val timestamp: Long
)

data class SwipeTrackingData(
    val points: List<SwipePoint>,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val startTime: Long,
    val endTime: Long
) {
    fun getDeltaX(): Float = endX - startX
    fun getDeltaY(): Float = endY - startY
}

data class SwipeEvent(
    val direction: SwipeGestureType,
    val velocity: Float,
    val displacement: Float,
    val timestamp: Long
)
