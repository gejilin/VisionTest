package com.fubao.visiontest.data

import kotlin.math.pow
import kotlin.math.ln

object EyeChartCalculator {
    // 标准视力表参数
    private const val STANDARD_DISTANCE = 500f // 毫米，标准测试距离 5 米 = 5000mm
    
    // E 字视力表行数定义（从大到小）
    data class ChartRow(
        val index: Int,
        val snellen: String,
        val angleMinutes: Float, // 视角分 (arc minutes)
        val视力值: Double,
        val gapSize: Float // 最小分辨角对应的尺寸比例
    )
    
    // 标准中国 E 字视力表数据
    private val standardRows = listOf(
        ChartRow(0, "20/400", 30.0, 0.1, 30f),
        ChartRow(1, "20/200", 15.0, 0.2, 15f),
        ChartRow(2, "20/100", 7.5, 0.3, 7.5f),
        ChartRow(3, "20/80", 6.0, 0.4, 6f),
        ChartRow(4, "20/60", 4.8, 0.5, 4.8f),
        ChartRow(5, "20/50", 3.6, 0.6, 3.6f),
        ChartRow(6, "20/40", 3.0, 0.7, 3.0f),
        ChartRow(7, "20/30", 2.4, 0.8, 2.4f),
        ChartRow(8, "20/25", 1.92, 0.9, 1.92f),
        ChartRow(9, "20/20", 1.5, 1.0, 1.5f),
        ChartRow(10, "20/16", 1.2, 1.2, 1.2f),
        ChartRow(11, "20/12", 0.96, 1.5, 0.96f),
        ChartRow(12, "20/10", 0.72, 2.0, 0.72f)
    )
    
    /**
     * 计算指定行号在给定距离下的视标大小
     * @param rowIndex 行号 (0-12)
     * @param screenDiagonalInches 屏幕对角线英寸
     * @param viewingDistanceCm 观看距离 (厘米)
     */
    fun calculateChartElementSize(
        rowIndex: Int,
        screenDiagonalInches: Double,
        viewingDistanceCm: Double
    ): Float {
        require(rowIndex in 0 until standardRows.size)
        
        val row = standardRows[rowIndex]
        
        // 计算屏幕宽度
        val widthRatio = 16f / 9f // 假设 16:9 屏幕
        val screenWidthPx = (screenDiagonalInches * 2.54 * 25.4 * widthRatio / PPI).toFloat()
        
        // 根据视角公式计算：角度 = 2 * arctan(尺寸 / (2*距离))
        // 对于小角度：角度 ≈ 尺寸 / 距离 (弧度)
        // 转换为 mm: size_mm = distance_mm * angle_radians
        
        val viewingDistanceMm = (viewingDistanceCm * 10f).toFloat()
        val angleRadians = Math.toRadians(row.angleMinutes.toDouble() / 60.0)
        
        // 单个 E 字的宽度（包含间隙）
        val elementWidth = (viewingDistanceMm * angleRadians.toFloat()).coerceAtLeast(50f)
        
        return elementWidth
    }
    
    /**
     * 计算视力值（小数记录法）
     */
    fun calculateVisionValue(snellen: String): Double {
        return when (snellen) {
            "20/400" -> 0.1
            "20/200" -> 0.2
            "20/100" -> 0.3
            "20/80" -> 0.4
            "20/60" -> 0.5
            "20/50" -> 0.6
            "20/40" -> 0.7
            "20/30" -> 0.8
            "20/25" -> 0.9
            "20/20" -> 1.0
            "20/16" -> 1.2
            "20/12" -> 1.5
            "20/10" -> 2.0
            else -> 1.0
        }
    }
    
    /**
     * 根据视力值得到 Snellen 表示
     */
    fun snellenFromVisionValue(visionValue: Double): String {
        return when {
            visionValue <= 0.1 -> "20/400"
            visionValue <= 0.2 -> "20/200"
            visionValue <= 0.3 -> "20/100"
            visionValue <= 0.4 -> "20/80"
            visionValue <= 0.5 -> "20/60"
            visionValue <= 0.6 -> "20/50"
            visionValue <= 0.7 -> "20/40"
            visionValue <= 0.8 -> "20/30"
            visionValue <= 0.9 -> "20/25"
            visionValue <= 1.0 -> "20/20"
            visionValue <= 1.2 -> "20/16"
            visionValue <= 1.5 -> "20/12"
            else -> "20/10"
        }
    }
    
    /**
     * 获取指定视力值对应的行号
     */
    fun getRowIndexForVision(value: Double): Int {
        return standardRows.indexOfFirst { it.视力值 >= value }.takeIf { it >= 0 } 
            ?: standardRows.lastIndex
    }
    
    // 设备像素密度（每英寸点数），根据设备动态调整
    val PPI: Double
        get() {
            // 这里需要获取实际设备信息
            // 默认使用中等密度
            return 320.0
        }
}
