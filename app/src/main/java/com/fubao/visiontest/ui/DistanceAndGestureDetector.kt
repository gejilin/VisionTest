package com.fubao.visiontest.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 📏 距离估算类型
 */
enum class DistanceEstimate {
    TOO_CLOSE,      // 太近（< 40cm）
    OPTIMAL,        // 最佳距离（40-60cm）
    TOO_FAR,        // 太远（> 70cm）
    UNKNOWN         // 无法判断
}

/**
 * 手部跟踪数据
 */
data class HandTrackingData(
    val centerX: Float,
    val centerY: Float,
    val confidence: Float,
    val timestamp: Long,
    val handSize: Float // 手掌在图像中的大小（像素）
)

/**
 * 挥手检测状态
 */
enum class WaveStatus {
    IDLE,           // 未检测到挥手
    WAVING_LEFT,    // 向左挥动
    WAVING_RIGHT,   // 向右挥动
    WAVING_BOTH     // 双向挥动（完整挥手）
}

/**
 * 滑动检测参数
 */
data class SwipeParameters(
    val thresholdX: Float = 25f,      // X 轴最小位移阈值（像素）
    val thresholdY: Float = 25f,      // Y 轴最小位移阈值（像素）
    val waveThreshold: Float = 50f,   // 挥手动阈值（像素）
    val minWaveDuration: Long = 300,  // 最小挥手持续时间（毫秒）
    val maxWaveVelocity: Float = 800f // 最大挥手速度（像素/秒）
)

class DistanceAndGestureDetector(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    // 距离检测相关
    private var lastHandSize: Float = 0f
    private var distanceEstimate: DistanceEstimate = DistanceEstimate.UNKNOWN
    
    // 挥手检测相关
    private var waveStartTime: Long = 0
    private var lastWaveDirection: Int = 0 // -1:左，1:右
    private var waveHistory = mutableListOf<SwipePoint>()
    
    // 滑动检测相关
    private var gestureStartX: Float? = null
    private var gestureStartY: Float? = null
    private var lastValidGesture: SwipeGestureType = SwipeGestureType.NONE
    
    private val _distanceDetected = MutableStateFlow<DistanceEstimate>(DistanceEstimate.UNKNOWN)
    val distanceDetected: StateFlow<DistanceEstimate> = _distanceDetected.asStateFlow()
    
    private val _waveStatus = MutableStateFlow<WaveStatus>(WaveStatus.IDLE)
    val waveStatus: StateFlow<WaveStatus> = _waveStatus.asStateFlow()
    
    private val _gestureDetected = MutableStateFlow<GestureStatus>(GestureStatus.Idle)
    val gestureDetected: StateFlow<GestureStatus> = _gestureDetected.asStateFlow()
    
    enum class GestureStatus {
        Idle, DetectingLeft, DetectingRight, DetectingUp, DetectingDown, Error
    }
    
    /**
     * 获取当前检测到的滑动手势
     */
    fun getCurrentSwipeGesture(): SwipeGestureType {
        return lastValidGesture
    }
    
    /**
     * 获取当前挥手状态
     */
    fun getCurrentWaveStatus(): WaveStatus {
        return _waveStatus.value
    }
    
    /**
     * 更新距离估计
     */
    fun getDistanceEstimate(): DistanceEstimate {
        return distanceEstimate
    }
    
    /**
     * 获取基于距离的视标缩放系数
     * @return 缩放系数，越大表示需要更大的视标
     */
    fun getScaleFactorForDistance(): Float {
        return when (distanceEstimate) {
            DistanceEstimate.TOO_CLOSE -> 0.7f   // 调小视标
            DistanceEstimate.OPTIMAL -> 1.0f     // 标准大小
            DistanceEstimate.TOO_FAR -> 1.3f     // 调大视标
            else -> 1.0f
        }
    }
    
    /**
     * 分析图像并更新所有检测状态
     */
    fun analyzeImage(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxy.toBitmap()
            val centerX = bitmap.width / 2f
            val centerY = bitmap.height / 2f
            
            // TODO: 实际应用中应使用 ML Kit 或 OpenCV 检测手部位置和大小
            // 这里模拟从图像中提取手部信息
            
            // 提取手部中心坐标（placeholder）
            val handData = extractHandFromImage(bitmap, centerX, centerY)
            
            // 更新距离估计
            updateDistanceEstimate(handData.handSize)
            
            // 检查挥手动作
            checkWaveAction(handData.centerX, handData.centerY, System.currentTimeMillis())
            
            // 检查滑动动作
            updateSwipePosition(Pair(handData.centerX, handData.centerY))
            
        } finally {
            imageProxy.close()
        }
    }
    
    /**
     * 从图像中提取手部信息（模拟实现）
     */
    private fun extractHandFromImage(bitmap: Bitmap, centerX: Float, centerY: Float): HandTrackingData {
        // TODO: 集成 ML Kit Pose Detection 或 MediaPipe Hands
        // 这里使用简化的中心区域检测
        
        val detectionConfidence = 0.85f
        val handWidth = estimateHandWidth(bitmap)
        val handHeight = bitmap.height / 4
        
        return HandTrackingData(
            centerX = centerX,
            centerY = centerY,
            confidence = detectionConfidence,
            timestamp = System.currentTimeMillis(),
            handSize = (handWidth + handHeight) / 2f
        )
    }
    
    /**
     * 估算手掌宽度（基于图像分析）
     */
    private fun estimateHandWidth(bitmap: Bitmap): Float {
        // TODO: 实际的计算机视觉算法应该：
        // 1. 检测到手部轮廓
        // 2. 计算手掌的边界框
        // 3. 返回手掌的最大宽度
        
        // 简化版：假设手掌占据屏幕中心的某个比例
        val centralRegionSize = minOf(bitmap.width, bitmap.height) * 0.3f
        return centralRegionSize
    }
    
    /**
     * 更新距离估计
     */
    private fun updateDistanceEstimate(handPixelSize: Float) {
        lastHandSize = handPixelSize
        
        // 基于手掌在图像中的大小估算距离
        // 真实的手掌宽度约 8-10cm
        // 距离越远，图像中的手掌越小
        
        val referenceHandSize = 100f // 假设标准距离下的参考像素值
        
        if (handPixelSize > referenceHandSize * 1.5) {
            // 手掌很大 → 太近了
            distanceEstimate = DistanceEstimate.TOO_CLOSE
        } else if (handPixelSize < referenceHandSize * 0.5) {
            // 手掌很小 → 太远了
            distanceEstimate = DistanceEstimate.TOO_FAR
        } else {
            // 正常范围
            distanceEstimate = DistanceEstimate.OPTIMAL
        }
        
        _distanceDetected.value = distanceEstimate
    }
    
    /**
     * 检查挥手动作（左右摆动手掌）
     */
    private fun checkWaveAction(currentX: Float, currentY: Float, timestamp: Long) {
        val startX = gestureStartX ?: currentX
        val startY = gestureStartY ?: currentY
        
        val deltaX = currentX - startX
        val deltaY = currentY - startY
        
        val absDeltaX = abs(deltaX)
        val absDeltaY = abs(deltaY)
        
        // 如果主要是水平移动且超过阈值，可能是挥手
        if (absDeltaX > 50f && absDeltaX > absDeltaY * 2) {
            val currentDirection = if (deltaX > 0) 1 else -1
            
            if (lastWaveDirection != 0 && lastWaveDirection != currentDirection) {
                // 方向改变，记录挥手开始时间
                waveStartTime = timestamp
                lastWaveDirection = currentDirection
                
                // 如果是双向挥动（至少切换一次方向），视为有效挥手
                if (waveHistory.isNotEmpty()) {
                    _waveStatus.value = WaveStatus.WAVING_BOTH
                } else {
                    _waveStatus.value = if (currentDirection > 0) 
                        WaveStatus.WAVING_RIGHT 
                    else 
                        WaveStatus.WAVING_LEFT
                }
            } else if (lastWaveDirection == 0) {
                // 新的挥手开始
                waveStartTime = timestamp
                lastWaveDirection = currentDirection
                waveHistory.clear()
                
                _waveStatus.value = if (currentDirection > 0) 
                    WaveStatus.WAVING_RIGHT 
                else 
                    WaveStatus.WAVING_LEFT
            }
            
            // 记录波形数据
            waveHistory.add(SwipePoint(currentX, currentY, timestamp))
            
            // 限制历史记录数量
            if (waveHistory.size > 20) {
                waveHistory.removeAt(0)
            }
            
            // 重置滑动起始点
            gestureStartX = currentX
            gestureStartY = currentY
        } else if (timestamp - waveStartTime > 500) {
            // 长时间没有变化，结束挥手检测
            if (_waveStatus.value != WaveStatus.IDLE) {
                _waveStatus.value = WaveStatus.IDLE
            }
            lastWaveDirection = 0
            waveHistory.clear()
        }
    }
    
    /**
     * 更新滑动位置（用于检测水平/垂直滑动）
     */
    fun updateSwipePosition(currentHandPosition: Pair<Float, Float>) {
        val (currentX, currentY) = currentHandPosition
        
        // 如果没有设置起始点，先设置
        if (gestureStartX == null || gestureStartY == null) {
            gestureStartX = currentX
            gestureStartY = currentY
            return
        }
        
        val trackingData = SwipeTrackingData(
            points = emptyList(),
            startX = gestureStartX!!,
            startY = gestureStartY!!,
            endX = currentX,
            endY = currentY,
            startTime = gestureStartX!! .toLong(), // placeholder
            endTime = System.currentTimeMillis()
        )
        
        val direction = trackingData.detectDirection(25f, 25f)
        
        // 只有在非挥手状态下才处理滑动
        if (direction != SwipeGestureType.NONE && _waveStatus.value == WaveStatus.IDLE) {
            if (direction != lastValidGesture) {
                lastValidGesture = direction
                
                // 映射到 UI 状态
                _gestureDetected.value = mapGestureToUIStatus(direction)
            }
        }
        
        // 更新起始点以便后续检测
        gestureStartX = currentX
        gestureStartY = currentY
    }
    
    /**
     * 将滑动手势映射到 UI 显示状态
     */
    private fun mapGestureToUIStatus(gesture: SwipeGestureType): GestureStatus {
        return when (gesture) {
            SwipeGestureType.SWIPE_UP -> GestureStatus.DetectingUp
            SwipeGestureType.SWIPE_DOWN -> GestureStatus.DetectingDown
            SwipeGestureType.SWIPE_LEFT -> GestureStatus.DetectingLeft
            SwipeGestureType.SWIPE_RIGHT -> GestureStatus.DetectingRight
            SwipeGestureType.NONE -> GestureStatus.Idle
        }
    }
    
    /**
     * 重置所有检测状态
     */
    fun resetAll() {
        gestureStartX = null
        gestureStartY = null
        lastValidGesture = SwipeGestureType.NONE
        lastHandSize = 0f
        distanceEstimate = DistanceEstimate.UNKNOWN
        waveStartTime = 0
        lastWaveDirection = 0
        waveHistory.clear()
        
        _distanceDetected.value = DistanceEstimate.UNKNOWN
        _waveStatus.value = WaveStatus.IDLE
        _gestureDetected.value = GestureStatus.Idle
    }
    
    fun startCamera(previewView: PreviewView, imageAnalysis: ImageAnalysis) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    androidx.camera.view.Preview.Builder().build().also { preview ->
                        previewView.preview = it
                    },
                    imageAnalysis
                )
            }, ContextCompat.getMainExecutor(context))
            
        } catch (e: Exception) {
            _gestureDetected.value = GestureStatus.Error
        }
    }
    
    fun stopCamera(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()
    }
}
