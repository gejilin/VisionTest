# 📦 VisionTest App 安装指南

> **重要提示**：本项目目前只有源代码，需要通过 Android Studio 或命令行工具构建为 APK 文件才能安装到手机上。

---

## 🎯 快速安装步骤（3 种方式任选）

### ⭐ 推荐方式 1：使用 Android Studio（最简单）

#### 前提条件
- 操作系统：Windows/macOS/Linux
- 已安装 [Android Studio](https://developer.android.com/studio)
- Java JDK 17+
- 网络连接（首次需要下载依赖）

#### 操作步骤
```bash
# 1. 将项目复制到本地电脑
# 如果是 Linux 系统，可以通过以下方式复制
scp -r /home/node/.openclaw/workspace/VisionTestApp user@your-pc:~/Documents/

# 或者从网络上传输（如果使用 NAS，可以挂载共享文件夹）
```

2. 打开 Android Studio → File → Open
3. 选择 `VisionTestApp` 文件夹
4. 等待 Gradle 自动同步（大约 5-10 分钟）
5. 连接手机（USB 调试开启）或启动模拟器
6. 点击 ▶️ Run 按钮
7. 应用会自动安装到设备

✅ **优点**：可视化操作，易于调试  
⏱️ **时间**：首次构建约 15-20 分钟

---

### 🔧 方式 2：命令行构建（高级用户）

#### 安装所需工具

**Ubuntu/Debian:**
```bash
# 安装 JDK
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

# 安装 Gradle
sudo apt-get install -y gradle

# 设置 ANDROID_HOME（如果你已有 Android SDK）
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
```

**macOS (Homebrew):**
```bash
brew install openjdk@17
brew install gradle
```

#### 构建 APK
```bash
cd /path/to/VisionTestApp

# 初始化 Gradle Wrapper
gradle wrapper --gradle-version 8.0

# 构建 Debug APK
./gradlew assembleDebug

# 成功标志
# app/build/outputs/apk/debug/app-debug.apk

# 通过 ADB 安装到手机
adb install app/build/outputs/apk/debug/app-debug.apk
```

✅ **优点**：适合 CI/CD 流水线  
⚠️ **缺点**：需要配置开发环境

---

### 📱 方式 3：我帮你编译（最快）

如果你没有开发环境，我可以：

1. **远程编译**：在支持 Android 构建的服务器上生成 APK
2. **网盘分发**：生成后通过阿里云盘/百度网盘提供下载链接
3. **直接传输**：通过飞书分享文件

**请告诉我你希望哪种方式！**

---

## 🔍 如何确认项目位置正确

检查以下关键文件是否存在：

```
/home/node/.openclaw/workspace/VisionTestApp/
├── app/build.gradle.kts      ✅ App 级构建配置
├── build.gradle.kts          ✅ 项目级构建配置
├── settings.gradle.kts       ✅ Gradle 设置
├── gradle.properties         ✅ Gradle 属性
└── app/src/main/
    ├── AndroidManifest.xml   ✅ 权限声明
    └── java/com/fubao/visiontest/
        ├── EyeChartScreen.kt         ✅ 主界面
        ├── ViewModelHelper.kt        ✅ 逻辑层
        ├── data/                     ✅ 数据层
        │   ├── EyeChartCalculator.kt
        │   ├── EyeChartModel.kt
        │   └── EyeChartResult.kt
        └── ui/                       ✅ UI 组件
            ├── DistanceAndGestureDetector.kt
            ├── SwipeGestureDetector.kt
            └── GestureStatus.kt
```

如果以上文件都存在，项目就是完整的！

---

## 📋 最小运行环境要求

### 手机端
- **Android 版本**: 7.0 (API 24) 或更高
- **屏幕尺寸**: 建议 5.5 英寸以上（视力测试需要足够大的显示区域）
- **摄像头**: 后置摄像头（用于手势检测）
- **存储空间**: 至少 100MB 可用空间

### 开发端（如果需要自己编译）
- **操作系统**: Windows/macOS/Linux
- **Java JDK**: 17+
- **Android Studio**: Arctic Fox (2020.3.1) 或更高版本
- **SDK Tools**: 
  - Android SDK Platform 24+
  - Build Tools 33.0.0+
  - Android Emulator（可选，用于测试）

---

## 🎁 预打包 APK 下载选项

### 选项 A：等待我生成（推荐新手）
```
优势：无需任何技术背景
流程：告诉我你的需求 → 我生成 APK → 通过飞书分享下载链接
时间：预计 10-15 分钟
```

### 选项 B：自行编译（适合开发者）
```
前提：拥有 Android Studio 开发环境
优势：可以自定义修改代码
流程：参考上方的"方式 1"或"方式 2"
时间：首次构建约 20 分钟
```

---

## 💡 常见问题 FAQ

### Q1: 为什么不能直接复制安装？
**A**: Android APK 是二进制可执行文件，需要从源代码编译生成。就像你不能直接复制 Excel 源代码让它变成可用的 .exe 文件一样，必须经过编译过程。

### Q2: 我的手机能装这个应用吗？
**A**: 检查你的手机：
- Android 版本：设置 → 关于手机 → Android 版本 ≥ 7.0
- 存储剩余：确保有至少 100MB 空间
- 如果是 iPhone/iPad，需要 iOS 移植版（暂未开发）

### Q3: 如果没有开发电脑怎么办？
**A**: 请选择"选项 A：等待我生成"，我会帮你在远程环境中编译并分享下载链接给你！

### Q4: 应用需要哪些权限？
**A**: 应用仅需以下权限：
```xml
<uses-permission android:name="android.permission.CAMERA"/> <!-- 用于手势检测 -->
<uses-permission android:name="android.permission.FLASHLIGHT"/> <!-- 辅助照明（可选） -->
```
不会读取你的通讯录、短信等敏感信息。

### Q5: 如何验证应用安全性？
**A**: 
1. 查看源码：所有代码都开源，可在 `/home/node/.openclaw/workspace/VisionTestApp/` 中查看
2. 权限审查：只需相机权限
3. 无网络请求：应用离线工作，不上传任何数据

---

## 📞 下一步行动

**请选择一个选项回复我：**

🅰️ **"帮我生成 APK"**
→ 我会立即开始远程编译，完成后通过飞书分享给你下载链接

🅱️ **"我自己用 Android Studio 编译"**
→ 你可以按照上方"方式 1"的步骤操作，有问题随时问我

🅾️ **"我想看看源码再决定"**
→ 我已经列出了所有文件路径，你可以检查后决定是否继续

---

**福宝 (Fubao) 🐼**  
*随时为你服务！*
