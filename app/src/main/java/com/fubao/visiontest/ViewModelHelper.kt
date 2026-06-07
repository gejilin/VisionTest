package com.fubao.visiontest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fubao.visiontest.data.EyeChartCalculator
import com.fubao.visiontest.data.EyeChartResult
import com.fubao.visiontest.data.VisionTestManager
import com.fubao.visiontest.ui.DistanceEstimate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModelHelper : ViewModel() {
    private val manager = VisionTestManager()
    
    private val _testResults = MutableStateFlow<List<EyeChartResult>>(emptyList())
    val testResults: StateFlow<List<EyeChartResult>> = _testResults.asStateFlow()
    
    // 当前测试行号
    private val _currentLineIndex = MutableStateFlow(0)
    val currentLineIndex: StateFlow<Int> = _currentLineIndex.asStateFlow()
    
    init {
        require(_currentLineIndex.value in 0..12) { "Line index must be between 0 and 12" }
    }
    
    /**
     * 添加测试结果（当用户滑动表示看到了 E 字）
     */
    fun addResult(result: EyeChartResult) {
        viewModelScope.launch {
            manager.addResult(result)
            _testResults.value = manager.getAllResults()
            
            // 答对了，进入下一行
            if (result.isCorrect && _currentLineIndex.value < 12) {
                _currentLineIndex.value++
            }
        }
    }
    
    /**
     * 记录挥手操作（当用户左右摆动手掌表示没看到）
     */
    fun recordHandWave(lineIndex: Int, distanceStatus: DistanceEstimate) {
        val snellen = EyeChartCalculator.snellenFromVisionValue(calculateVisionForLine(lineIndex))
        
        val result = EyeChartResult(
            lineIndex = lineIndex,
            视力值 = calculateVisionForLine(lineIndex),
            snellen = snellen,
            isCorrect = false, // 挥手表明没看到
            timestamp = System.currentTimeMillis(),
            swipeDirection = null
        )
        
        viewModelScope.launch {
            manager.addResult(result)
            _testResults.value = manager.getAllResults()
            
            // 挥手不代表完成该行，不自动进入下一行
            // 用户可以重新尝试这一行
        }
    }
    
    /**
     * 计算指定行的视力值
     */
    private fun calculateVisionForLine(rowIndex: Int): Double {
        return when (rowIndex) {
            0 -> 0.1   // 第 1 行（最大）
            1 -> 0.2
            2 -> 0.3
            3 -> 0.4
            4 -> 0.5
            5 -> 0.6
            6 -> 0.7
            7 -> 0.8
            8 -> 0.9
            9 -> 1.0
            10 -> 1.2
            11 -> 1.5
            12 -> 2.0  // 第 13 行（最小）
            else -> 1.0
        }
    }
    
    /**
     * 获取总结信息
     */
    fun getResultSummary(): Pair<Double, Double> {
        return manager.getResultSummary()
    }
    
    /**
     * 清除所有结果并重置
     */
    fun clearResults() {
        viewModelScope.launch {
            manager.clearResults()
            _testResults.value = emptyList()
            _currentLineIndex.value = 0
        }
    }
    
    /**
     * 获取当前行信息
     */
    fun getCurrentLineInfo(rowIndex: Int): String {
        val rowSize = 100 / (rowIndex + 1).coerceAtLeast(1)
        return "第 ${rowIndex + 1} 行 - E 字大小：${rowSize}%"
    }
}
