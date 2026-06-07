# VisionTest App - 项目配置指南

## 🐼 福宝特别提示

这是一个智能视力检查应用的完整 Android 项目。虽然代码已经生成，但还需要一些额外步骤才能编译运行。

---

## ⚠️ 重要注意事项

### 1. 当前手势识别的实现状态

**现状**: 
- 当前的 `CameraGestureDetector.kt` 中手势识别使用的是简化的图像分析方法（基于颜色/亮度特征）
- **这不是真正的 ML Kit 姿态识别**，需要集成完整的计算机视觉库才能获得准确结果

### 2. 需要补充的配置

#### a) 依赖项完整性检查

确认以下依赖在 `app/build.gradle.kts` 中都已正确添加：

```kotlin
// CameraX
implementation "androidx.camera:camera-core:${camerax_version}"
implementation "androidx.camera:camera-camera2:${camerax_version}"
implementation "androidx.camera:camera-lifecycle:${camerax_version}"
implementation "androidx.camera:camera-view:${camerax_version}"

// ML Kit Pose Detection (用于手势识别)
implementation 'com.google.mlkit:pose-detection:17.0.5'

// OpenCV (备用方案)
implementation 'org.opencv:opencv:4.9.0'
```

#### b) Gradle Wrapper 配置

需要在项目根目录创建 `gradle/wrapper/gradle-wrapper.properties`:

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

#### c) gradlew 可执行文件

Android Studio 会自动生成这些文件，如果手动创建项目，需要:
```bash
./gradlew wrapper
```

---

## 🔧 编译和运行步骤

### 方式一：使用 Android Studio（推荐）

1. **打开项目**
   ```bash
   cd /home/node/.openclaw/workspace/VisionTestApp
   # 然后双击 VisionTestApp 文件夹，或运行：
   android-studio .
   ```

2. **等待 Gradle 同步**
   - Android Studio 会自动下载依赖并配置项目
   - 首次可能需要几分钟

3. **连接设备或启动模拟器**
   ```bash
   # 查看已连接的设备
   adb devices
   
   # 如果没有设备，启动一个模拟器
   avdmanager list
   emulator -avd <your_avd_name>
   ```

4. **运行应用**
   - 点击工具栏的绿色播放按钮 ▶️
   - 或命令行列：
     ```bash
     ./gradlew installDebug
     adb shell am start -n com.fubao.visiontest/com.fubao.visiontest.MainActivity
     ```

### 方式二：命令行构建

```bash
cd /home/node/.openclaw/workspace/VisionTestApp

# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备（如果有连接）
adb install app/build/outputs/apk/debug/app-debug.apk

# 直接运行（Android Studio 调试模式更推荐）
adb shell am start -n com.fubao.visiontest/.MainActivity
```

---

## 📱 测试建议

### 功能验证清单

- [ ] **权限请求** - 相机权限是否正常工作？
- [ ] **E 字显示** - 视标大小是否随行数变化？
- [ ] **方向切换** - 横向/纵向模式是否正确旋转？
- [ ] **手势识别** - 是否能检测到简单的手势信号？（目前可能不够准确）
- [ ] **结果记录** - 测试结果是否正确保存和显示？
- [ ] **视力计算** - 最佳/最差视力值计算是否正确？

### 预期行为

1. **启动时**: 应用自动请求相机权限
2. **主界面**: 显示第 1 行的大号 E 字
3. **测试中**: 顶部显示"正在检测手势..."
4. **做对后**: 自动跳转到下一行（E 字变小）
5. **测试结束**: 显示最终视力总结

---

## 🛠️ 问题排查

### 常见问题

#### Q1: Gradle sync 失败
```
解决方法:
1. File -> Invalidate Caches / Restart
2. File -> Project Structure -> SDK Location
3. 确保 Android SDK Tools 已更新到最新
```

#### Q2: Camera 无法打开
```
原因分析:
- 没有授予相机权限
- 模拟器不支持相机
解决: 使用真机测试或在 AVD 中启用相机支持
```

#### Q3: 手势识别不准确
```
当前限制:
- 简化算法仅基于基础像素特征
改进建议:
- 集成完整的 ML Kit Pose Detection API
- 使用 TensorFlow Lite 模型
- 收集更多训练数据
```

#### Q4: 视标显示过大/过小
```
调整方法:
- 修改 EyeChartCalculator.kt 中的 calculateChartElementSize()
- 调整 viewingDistanceCm 参数
- 自定义屏幕尺寸比例
```

---

## 🎯 下一步优化方向

### 短期（MVP）
1. ✅ 完成基础 E 字显示
2. ✅ 实现简单的视图切换
3. ⚠️ 完善手势识别逻辑（需要真实 ML Kit 集成）
4. ✅ 记录并统计测试结果

### 中期
1. 添加双眼分别测试功能
2. 引入标准 LogMAR 视力表
3. 支持导出测试结果为 PDF
4. 添加历史趋势图表

### 长期
1. AR 校准功能（自动测量观看距离）
2. 多语言支持
3. 云端数据同步
4. 专业版（付费功能）

---

## 📞 技术支持

如果遇到任何问题：
1. 检查日志：`adb logcat | grep VisionTest`
2. 查看错误信息
3. 尝试清理重建：`./gradlew clean build`
4. 联系开发者

---

**最后提醒**: 此应用仅供娱乐和学习参考，不能作为医疗诊断依据。定期检查视力请咨询专业眼科医生 👨‍⚕️👁️
