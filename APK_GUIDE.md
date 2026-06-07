# 📦 VisionTest App - APK 下载指南

## ❌ 当前状况说明

很抱歉，当前运行的服务器（极空间 NAS）没有安装完整的 Android 开发环境（Java JDK + Gradle + Android SDK），所以**无法直接在这个机器上编译出 APK**。

Android 应用编译需要：
- ✅ Java JDK 17+
- ✅ Gradle 8.0+
- ✅ Android SDK (API 24+)
- ✅ 至少 2GB 磁盘空间

极空间 NAS 主要设计用于文件存储和轻量服务，不适合重型编译任务。

---

## ✅ 最佳解决方案（推荐）

### 🎯 方案 1：使用本地电脑 + Android Studio（最快上手）

#### 步骤 1：准备项目文件
```bash
# 方法 A：通过飞书传输（推荐）
1. 登录飞书 PC 版
2. 创建一个临时云文档或网盘文件夹
3. 将 /home/node/.openclaw/workspace/VisionTestApp/ 整个文件夹上传到飞书网盘
4. 分享链接给你的本地电脑，下载到桌面

# 方法 B：如果 NAS 支持 FTP/SMB
1. 在极空间开启 FTP/SMB 共享
2. 在本地电脑的网络位置访问 NAS
3. 复制 VisionTestApp 文件夹到本地硬盘
```

#### 步骤 2：安装开发工具
```
下载地址：https://developer.android.com/studio

1. 下载并安装 Android Studio（约 1GB）
   - Windows: exe 安装包
   - macOS: dmg 镜像文件
   - Linux: tar.gz 压缩包

2. 首次启动会自动下载：
   - Android SDK Platform 24+
   - Gradle Wrapper
   - 所有依赖库

⏱️ 预计时间：5-15 分钟（取决于网络速度）
```

#### 步骤 3：打开并构建项目
```
1. 打开 Android Studio
2. File → Open
3. 选择你下载好的 VisionTestApp 文件夹
4. 等待自动同步（进度条顶部显示 "Syncing..."）
5. 连接手机或启动模拟器：
   - 真机：设置 → 关于手机 → 连续点击"版本号"7 次开启开发者模式 → 
     返回设置 → 开发者选项 → 开启"USB 调试" → 连接电脑
   - 模拟器：Device Manager → Create Device → 选择 Pixel 系列 → 下载系统镜像
   
6. 点击 ▶️ Run 按钮（绿色三角形图标）
7. 等待构建完成...
8. APK 会自动安装到设备！🎉
```

✅ **成功标志**：应用启动，显示"👁️ 智能视力检查"界面

---

### 🚀 方案 2：GitHub Actions 自动编译（免本地环境）

如果你有 GitHub 账号，这是最优雅的方案！

#### 步骤 1：创建 GitHub 仓库
```bash
# 在你的电脑上操作
git init
git add .
git commit -m "VisionTest App v1.2.0"
git branch -M main
git remote add origin git@github.com:你的用户名/visiontest.git
git push -u --force origin main
```

#### 步骤 2：添加自动编译配置
在你的仓库根目录创建 `.github/workflows/android-build.yml`：

```yaml
name: Build APK

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x ./gradlew
      working-directory: app  # 注意：Gradle 在 app 目录下可能需要调整路径
    
    - name: Build Debug APK
      run: ./gradlew assembleDebug
      working-directory: .
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: visiontest-apk
        path: app/build/outputs/apk/debug/app-debug.apk
```

#### 步骤 3：获取 APK
1. 推送到 GitHub 后，等待约 5-10 分钟
2. 进入 GitHub 仓库页面 → Actions 标签页
3. 找到最新的运行记录
4. 展开 Artifacts 部分
5. 点击 `visiontest-apk.zip` 下载
6. 解压后得到 APK 文件

#### 步骤 4：安装到手机
```
1. 通过微信/QQ/飞书发送到手机
2. 在手机上点击下载的 APK
3. 允许"来自未知来源的安装"
4. 安装完成！
```

✅ **优点**：无需任何本地软件，完全云端编译  
⏱️ **时间**：总共约 15 分钟

---

### 🌐 方案 3：在线 Android 编译器（第三方服务）

有一些网站可以直接编译 Android 项目，但需要注意隐私安全：

| 服务名称 | 网址 | 是否免费 | 备注 |
|---------|------|---------|------|
| CloudBuild | https://cloudbuild.dev | ✅ 免费 | 支持 Gradle 项目 |
| Codacy | https://www.codacy.com | ✅ 免费 | 需要 GitHub 账号 |
| Bitrise | https://www.bitrise.io | ⚠️ 有免费额度 | 专业 CI/CD 平台 |

**操作流程：**
1. 注册账号（通常用 GitHub 登录）
2. 连接你的项目代码仓库（GitHub/GitLab）
3. 选择预设模板：Kotlin/Android
4. 配置 Gradle 版本和 API 级别
5. 点击 Build
6. 等待完成后下载 APK

⚠️ **注意**：上传源码前请确认服务商的隐私政策

---

## 💡 方案对比表

| 方案 | 难度 | 时间 | 成本 | 安全性 | 推荐度 |
|-----|------|------|------|--------|--------|
| **本地 Android Studio** | ⭐⭐ | 30 分钟 | 免费 | ✅ 最高 | ⭐⭐⭐⭐⭐ |
| **GitHub Actions** | ⭐⭐⭐ | 20 分钟 | 免费 | ✅ 高 | ⭐⭐⭐⭐⭐ |
| **在线编译器** | ⭐⭐ | 15 分钟 | 部分收费 | ⚠️ 中 | ⭐⭐⭐ |
| **本地手动配置 Gradle** | ⭐⭐⭐⭐⭐ | 60+ 分钟 | 免费 | ✅ 高 | ⭐⭐ |

---

## 🆘 如果你还是不确定...

### 问自己几个问题：

**Q1: 你有 Windows/Mac/Linux 个人电脑吗？**
- ✅ 有 → 选择方案 1（最简单直接）
- ❌ 没有 → 继续看 Q2

**Q2: 你有 GitHub 账号吗？**
- ✅ 有 → 选择方案 2（云端编译）
- ❌ 没有 → 继续看 Q3

**Q3: 你能花 5 分钟注册一个免费的 GitHub 账号吗？**
- ✅ 能 → 强烈建议这样做（长期有用）
- ❌ 不能 → 考虑借用朋友电脑或使用在线编译器

---

## 📝 我的建议总结

### 🥇 最佳方案：本地 Android Studio（90% 用户的选择）

**为什么？**
1. 一次安装，永久可用
2. 不仅限于这个项目，以后任何 Android 开发都能用
3. 可视化调试，出现问题容易排查
4. 完全免费，无风险

**适合人群：**
- 有个人电脑的学生/开发者
- 可能继续学习 Android 开发的人
- 想要完全控制代码的用户

---

### 🥈 次优方案：GitHub Actions（技术友好型用户）

**为什么？**
1. 零本地环境配置
2. 自动化流程，可重复使用
3. 适合团队协作场景

**适合人群：**
- 熟悉 Git 操作的技术用户
- 不想安装大型软件的轻量级需求
- 想要学习 CI/CD 概念的人

---

### 🥉 备选方案：在线编译器（应急情况）

**为什么？**
1. 最快的方式（如果你急用一个测试版本）
2. 不需要任何准备工作

**适合人群：**
- 急用且没有其他选择的紧急情况
- 不愿意安装任何东西的懒人模式

---

## 🤔 还需要帮助吗？

请告诉我你的实际情况，我会给出更精准的建议：

```
【请勾选】
[ ] 我有个人电脑（Windows/Mac/Linux）
[ ] 我愿意安装 Android Studio
[ ] 我想学习 Android 开发
[ ] 我没有电脑，只能用手机

【或者告诉我你的困难】
例如：
- "我不会用 Git"
- "我的电脑太老了跑不动"
- "我不懂编程想快速用上"
- "我只有 iPad，怎么办？"
```

我会根据你的具体情况提供最合适的解决方案！😊

---

**福宝 (Fubao) 🐼**  
*你的贴心助手，随时解答疑问！*
