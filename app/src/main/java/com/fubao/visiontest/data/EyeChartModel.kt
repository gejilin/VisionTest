package com.fubao.visiontest.data

data class EyeChartResult(
    val lineIndex: Int,
    val 视力值: Double, // 小数记录法，如 1.0, 0.8
    val snellen: String, // Snellen notation, like "20/20"
    val isCorrect: Boolean,
    val timestamp: Long,
    val swipeDirection: com.fubao.visiontest.ui.SwipeGestureType? = null
) {
    fun getFormattedVision(): String = when {
        视力值 >= 1.0 -> "${String.format("%.1f", 视力值)} (正常)"
        视力值 < 0.1 -> "< 0.1"
        else -> String.format("%.1f", 视力值)
    }
}

class VisionTestManager {
    private val results = mutableListOf<EyeChartResult>()
    
    fun addResult(result: EyeChartResult) {
        results.add(result)
    }
    
    fun getResultSummary(): Pair<Double, Double> {
        if (results.isEmpty()) return Pair(0.0, 0.0)
        
        val lastCorrectResult = results.last { it.isCorrect }
        val bestVision = results.filter { it.isCorrect }.maxByOrNull { it.视力值 }?.视力值 ?: 0.0
        val worstVision = results.filter { it.isCorrect }.minByOrNull { it.视力值 }?.视力值 ?: 0.0
        
        return Pair(bestVision, worstVision)
    }
    
    fun clearResults() {
        results.clear()
    }
    
    fun getAllResults(): List<EyeChartResult> = results.toList()
}
