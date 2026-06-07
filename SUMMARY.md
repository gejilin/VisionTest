# VisionTest - 智能视力检查 App

## 📱 项目概述

这是一个**安卓手机上的智能视力检测应用**，具备以下核心功能：

### ✨ 已实现的功能

1. **动态 E 字视力表** 
   - 13 行标准视力表（从 0.1 到 2.0）
   - 支持横向↔和纵向↑↓两种显示模式切换
   - 基于视角原理自动计算视标大小

2. **屏幕尺寸自适应**
   - 根据设备屏幕对角线尺寸自动调整
   - 考虑观看距离优化显示效果
   - 使用 CameraX + DisplayMetrics 获取真实参数

3. **手势识别系统** (当前简化版本)
   - 🤏 **捏合手势** → 表示看到左右方向的 E
   - 👌 **OK 手势** → 表示看到上下方向的 E
   - ✊ **握拳** → 表示没看到或跳过
   - 🖐️ **张开手掌** → 辅助确认
   - ⚠️ *注：当前使用简化算法，需集成 ML Kit 提升准确性*

4. **结果记录与统计**
   - 实时记录每行测试结果
   - 自动计算最佳/最差视力值
   - 小数记录法 (1.0, 0.8) + Snellen(20/20)双格式
   - 保留最近 20 条历史记录

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────┐
│         Jetpack Compose UI          │
├─────────────────────────────────────┤
│         ViewModel (StateFlow)       │
├─────────────────────────────────────┤
│    CameraGestureDetector (ML Kit)   │
├─────────────────────────────────────┤
│        EyeChartCalculator           │
└─────────────────────────────────────┘
```

- **UI 层**: Jetpack Compose + Material Design 3
- **逻辑层**: MVVM 架构 + Coroutines
- **相机层**: CameraX Preview + ImageAnalysis
- **视觉识别**: ML Kit Pose Detection (预留接口)

---

## 📦 项目文件结构

```
VisionTestApp/
├── app/src/main/java/com/fubao/visiontest/
│   ├── MainActivity.kt              # 入口 Activity
│   ├── EyeChartScreen.kt            # 主界面（Compose）
│   ├── ViewModelHelper.kt           # ViewModel 管理
│   ├── data/
│   │   ├── EyeChartModel.kt         # 数据模型类
│   │   └── EyeChartCalculator.kt    # 计算器工具
│   ├── ui/
│   │   └── CameraGestureDetector.kt # 手势检测
│   └── util/
│       └── AppUtils.kt              # 通用工具函数
│
├── app/src/main/res/
│   ├── values/strings.xml           # 字符串资源
│   ├── values/colors.xml            # 颜色定义
│   └── values/themes.xml            # 主题配置
│
├── build.gradle.kts                 # 构建配置
├── AndroidManifest.xml              # 权限声明
└── README.md                        # 完整文档
```

---

## 🚀 如何运行

### 方式一：Android Studio（推荐）

```bash
cd /home/node/.openclaw/workspace/VisionTestApp
# 双击打开，或命令行启动
android-studio .
```

然后在 Android Studio 中：
1. 等待 Gradle 同步完成
2. 连接真机或启动模拟器
3. 点击绿色 ▶️ 按钮运行

### 方式二：命令行

```bash
cd /home/node/.openclaw/workspace/VisionTestApp

# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk

# 或直接启动
adb shell am start -n com.fubao.visiontest/com.fubao.visiontest.MainActivity
```

---

## 🎮 使用说明

### 测试流程

1. **准备阶段**
   - 授予相机权限
   - 将手机固定在距离眼睛约 **50cm** 处
   - 确保摄像头能清晰拍摄手部区域

2. **开始测试**
   - 点击「新测试」按钮
   - 查看屏幕上显示的 E 字开口方向
   - 根据看到的做对应手势：
     ```
     E 朝左/右  → 🤏 捏合手势
     E 朝上/下  → 👌 OK 手势
     看不到    → 保持静止或 ✊ 握拳
     ```

3. **查看结果**
   - 每次答对自动进入下一行（E 字变小）
   - 测试结束后查看底部"视力总结"卡片
   - 记录最佳和最差视力值

### 视力读数参考

| 数值 | 含义 | 建议 |
|------|------|------|
| ≥1.0 | 正常视力 | ✅ 继续保持 |
| 0.6-0.9 | 轻度下降 | 💡 注意用眼卫生 |
| 0.3-0.5 | 中度下降 | 🩺 建议眼科检查 |
| <0.3 | 严重下降 | 🚨 尽快就医 |

---

## 🔧 已知限制 & TODO

### ⚠️ 重要提示

1. **非医疗设备** - 仅供娱乐和学习，不能替代专业检查
2. **手势精度** - 当前使用简化像素分析，建议后续集成 ML Kit 完整方案

### 🛠️ 待完善功能

```markdown
[ ] 集成完整的 ML Kit Pose Detection API
[ ] 添加双眼分别测试模式  
[ ] 支持 LogMAR 视力表
[ ] 导出 PDF 报告
[ ] 历史趋势图表
[ ] AR 距离校准功能
```

---

## 📝 关键技术点

### 1. 视力表尺寸计算

```kotlin
// 基于视角公式：角度 ≈ 尺寸 / 距离 (弧度)
fun calculateChartElementSize(rowIndex: Int, screenDiagonalInches: Double, viewingDistanceCm: Double): Float {
    val row = standardRows[rowIndex]
    val viewingDistanceMm = viewingDistanceCm * 10f
    val angleRadians = Math.toRadians(row.angleMinutes.toDouble() / 60.0)
    return (viewingDistanceMm * angleRadians.toFloat()).coerceAtLeast(50f)
}
```

### 2. 手势状态流转

```
Idle → DetectingLeft/DetectingRight/DetectingUp/DetectingDown → Record Result → Next Line
```

### 3. 响应式 UI

使用 StateFlow + LaunchedEffect 实现实时数据更新：

```kotlin
LaunchedEffect(testResults) {
    if (testResults.isNotEmpty()) {
        val lastResult = testResults.last()
        if (lastResult.isCorrect) currentLineIndex++
    }
}
```

---

## 📄 许可证

MIT License - 仅供学习研究使用

---

**开发者**: Fubao (福宝) 🐼  
**版本**: v1.0.0  
**日期**: 2026-06-07  

<div align="center">
<i>愿你的每一双眼睛都明亮健康 👁️💙</i>
</div>
