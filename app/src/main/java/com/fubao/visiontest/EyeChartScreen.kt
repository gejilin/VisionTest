package com.fubao.visiontest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fubao.visiontest.data.EyeChartCalculator
import com.fubao.visiontest.data.EyeChartResult
import com.fubao.visiontest.ui.DistanceAndGestureDetector
import com.fubao.visiontest.ui.DistanceEstimate
import com.fubao.visiontest.ui.SwipeGestureType
import com.fubao.visiontest.ui.WaveStatus
import com.fubao.visiontest.ui.theme.VisionTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisionTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EyeChartScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeChartScreen(
    modifier: Modifier = Modifier
) {
    var currentLineIndex by remember { mutableIntStateOf(0) }
    var results by remember { mutableStateOf(emptyList<EyeChartResult>()) }
    var visionSummary by remember { mutableStateOf(Pair(0.0, 0.0)) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detector = remember { DistanceAndGestureDetector(context, lifecycleOwner) }
    var swipeDirection by remember { mutableStateOf<SwipeGestureType?>(null) }
    var waveDetected by remember { mutableStateOf(false) }
    var distanceStatus by remember { mutableStateOf<DistanceEstimate>(DistanceEstimate.UNKNOWN) }

    val viewModel = remember { ViewModelHelper() }
    val testResults = viewModel.testResults

    LaunchedEffect(swipeDirection) {
        if (swipeDirection != null && !waveDetected) {
            when (swipeDirection) {
                SwipeGestureType.SWIPE_UP, SwipeGestureType.SWIPE_DOWN,
                SwipeGestureType.SWIPE_LEFT, SwipeGestureType.SWIPE_RIGHT -> {
                    viewModel.addResult(EyeChartResult(
                        lineIndex = currentLineIndex,
                        视力值 = calculateCurrentVisionValue(currentLineIndex),
                        snellen = EyeChartCalculator.snellenFromVisionValue(calculateCurrentVisionValue(currentLineIndex)),
                        isCorrect = true,
                        timestamp = System.currentTimeMillis(),
                        swipeDirection = swipeDirection
                    ))
                    currentLineIndex = viewModel.currentLineIndex.value
                }
            }
            swipeDirection = null
            waveDetected = false
            visionSummary = viewModel.getResultSummary()
        }
    }

    LaunchedEffect(waveDetected) {
        if (waveDetected) {
            viewModel.recordHandWave(
                lineIndex = currentLineIndex,
                distanceStatus = distanceStatus
            )
            waveDetected = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBarWithSettings(
            onReset = {
                currentLineIndex = 0
                results = emptyList()
                viewModel.clearResults()
                detector.resetAll()
            },
            onStartNewTest = {
                currentLineIndex = 0
                viewModel.clearResults()
                detector.resetAll()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        DistanceStatusCard(distanceEstimate = distanceStatus, scaleFactor = detector.getScaleFactorForDistance())
        Spacer(modifier = Modifier.height(16.dp))
        WaveStatusCard(waveStatus = if (waveDetected) WaveStatus.WAVING_BOTH else WaveStatus.IDLE, onResetWave = { waveDetected = false })
        Spacer(modifier = Modifier.height(32.dp))
        CurrentLineInfo(lineIndex = currentLineIndex, totalLines = 13)
        Spacer(modifier = Modifier.height(32.dp))
        EyeChartDisplay(rowIndex = currentLineIndex, sizeScale = detector.getScaleFactorForDistance())
        Spacer(modifier = Modifier.height(48.dp))
        GestureInstructionCard()
        Spacer(modifier = Modifier.height(32.dp))
        ResultsHistoryCard(results = testResults.take(20))
        Spacer(modifier = Modifier.height(16.dp))
        VisionSummaryCard(summary = visionSummary)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun calculateCurrentVisionValue(rowIndex: Int): Double = when (rowIndex) {
    0 -> 0.1; 1 -> 0.2; 2 -> 0.3; 3 -> 0.4; 4 -> 0.5
    5 -> 0.6; 6 -> 0.7; 7 -> 0.8; 8 -> 0.9; 9 -> 1.0
    10 -> 1.2; 11 -> 1.5; 12 -> 2.0; else -> 1.0
}

@Composable
fun TopBarWithSettings(onReset: () -> Unit, onStartNewTest: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("👁️ 智能视力检查", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Fubao AI 版 \u00b7 v1.2.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onStartNewTest) { Icon(Icons.Default.Refresh, contentDescription = "新测试") }
                IconButton(onClick = onReset) { Icon(Icons.Default.Clear, contentDescription = "重置") }
            }
        }
    }
}

@Composable
fun DistanceStatusCard(distanceEstimate: DistanceEstimate, scaleFactor: Float) {
    val (text, icon, color) = when (distanceEstimate) {
        DistanceEstimate.TOO_CLOSE -> Triple("请远离屏幕一点！当前太近", "⚠️", MaterialTheme.colorScheme.error)
        DistanceEstimate.OPTIMAL -> Triple("✓ 距离适中", "😌", MaterialTheme.colorScheme.primary)
        DistanceEstimate.TOO_FAR -> Triple("请靠近一点！当前太远", "⚠️", MaterialTheme.colorScheme.tertiary)
        else -> Triple("等待距离检测...", "🔍", MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = icon, fontSize = 24.sp)
                Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "视标已根据距离自动调整 (${"%.1f".format(scaleFactor)}倍)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WaveStatusCard(waveStatus: WaveStatus, onResetWave: () -> Unit) {
    val (text, icon, showButton) = when (waveStatus) {
        WaveStatus.IDLE -> Triple("未检测到挥手", "✋", false)
        WaveStatus.WAVING_BOTH -> Triple("检测到挥手！已记录为没看到", "🎉", true)
        else -> Triple("正在检测挥手动作...", "🤔", false)
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (showButton) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = icon, fontSize = 24.sp)
                Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (showButton) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showButton) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onResetWave, modifier = Modifier.widthIn(min = 120.dp)) { Text("确认已记录") }
            }
        }
    }
}

@Composable
fun CurrentLineInfo(lineIndex: Int, totalLines: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("第 ${lineIndex + 1} / $totalLines 行", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("看清 E 字的开口方向，然后按对应方向滑动手指", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun EyeChartDisplay(rowIndex: Int, sizeScale: Float = 1f) {
    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size((180.dp * sizeScale).coerceAtMost(300.dp))) {
            drawEye(scale = sizeScale)
        }
    }
}

@Composable
fun GestureInstructionCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("🎯 手势操作指南", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InstructionSection("✅ 看到 E 字时", listOf(
                    InstructionItem("E 口朝上", "向上滑动"), InstructionItem("E 口朝下", "向下滑动"),
                    InstructionItem("E 口朝左", "向左滑动"), InstructionItem("E 口朝右", "向右滑动")
                ))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                InstructionSection("❌ 没看到/看不清时", listOf(InstructionItem("左右摆动手掌", "像打招呼一样左右摇手")))
            }
            Spacer(modifier = Modifier.height(16.dp))
            AlertMessageBox("💡 提示：确保手机摄像头能看到您的手部，并保持适当的光线。")
        }
    }
}

@Composable
fun InstructionSection(title: String, instructions: List<InstructionItem>) {
    Column {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        instructions.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${item.label}: ${item.action}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

data class InstructionItem(val label: String, val action: String)

@Composable
fun AlertMessageBox(text: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("💡", fontSize = 20.sp)
            Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

@Composable
fun ResultsHistoryCard(results: List<EyeChartResult>) {
    if (results.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("📝 测试记录", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            results.reversed().forEach { result ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("行${result.lineIndex + 1}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (!result.isCorrect) "❌ 挥手（没看到）" else if (result.swipeDirection != null) "✓ 正确" else "?", fontSize = 12.sp, color = if (!result.isCorrect) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun VisionSummaryCard(summary: Pair<Double, Double>) {
    if (summary.first == 0.0 && summary.second == 0.0) return
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("📊 视力总结", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("最佳视力:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    Text("${"%.1f".format(summary.first)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Column {
                    Text("最差视力:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    Text("${"%.1f".format(summary.second)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = { summary.first.toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp), trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("建议定期（每 6-12 个月）进行专业视力检查。本应用仅供娱乐参考。", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f), fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
        }
    }
}

fun DrawScope.drawEye(scale: Float = 1f) {
    val s = size.minDimension
    val t = s * 0.08f * scale
    val c = Color.Black
    val spineX = s * 0.2f
    drawRect(color = c, topLeft = Offset(spineX, s * 0.1f), size = Size(t, s * 0.8f))
    drawRect(color = c, topLeft = Offset(spineX, s * 0.1f), size = Size(s * 0.55f, t))
    drawRect(color = c, topLeft = Offset(spineX, s * 0.5f - t / 2), size = Size(s * 0.4f, t))
    drawRect(color = c, topLeft = Offset(spineX, s * 0.9f - t), size = Size(s * 0.55f, t))
}
