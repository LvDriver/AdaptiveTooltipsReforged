# Adaptive Tooltips Reforged

NeoForge 1.21.1 移植版 · `LGPL-3.0-or-later`

---

## 中文

### 简介

**Adaptive Tooltips Reforged** 是 isXander 的 Fabric 模组 **Adaptive Tooltips 1.3.0**（Minecraft 1.20.1 / 1.20.2）的 **NeoForge 1.21.1** 移植版。

目标是与 1.20.x 版本保持行为一致：相同的换行算法、定位规则、滚动逻辑与配置界面，只是重新适配到 Minecraft 1.21.1 与 NeoForge。**仅客户端生效**，不影响服务端，也不改变任何物品数据。

### 功能

- **文本换行**：关闭、屏幕宽度、剩余宽度、半屏宽度、智能
- **优先显示顶部**：tooltip 高于屏幕时优先保留顶部
- **基岩版居中**：tooltip 太宽、当前一侧放不下时，改为以鼠标为中心
- **角落对齐**：选择遮挡鼠标最少的角落，另有“始终对齐角落”模式
- **阻止原版夹取**：允许 tooltip 越过屏幕左缘后再重新定位
- **滚动**：按住左 Alt 滚轮上下滚动；同时按住左 Ctrl 水平滚动（均可改键，提示会显示当前绑定键）
- **透明度**：调整 tooltip 背景与边框的透明度
- **移除首行间距**
- **YACL 配置页**
- **语言**：简体中文、繁体中文、英语、俄语

### 环境要求

| 项目 | 要求 |
|---|---|
| Minecraft | **1.21.1** |
| 加载器 | **NeoForge 21.1.218+** |
| 前置 | **YetAnotherConfigLib (YACL) 3.8.2+1.21.1-neoforge**（必需） |
| Java | 21 |

### 安装

1. 安装 Minecraft 1.21.1 与 NeoForge 21.1.218 或更高版本。
2. 把本 mod 的 jar 和 YACL 的 jar 一起放进 `mods/`。
3. 启动客户端。配置页位于 **Mods → Adaptive Tooltips Reforged → Config**。

配置文件为 `config/adaptive-tooltips.json`（与上游同名，旧配置可直接沿用）。

### 从源码构建

本仓库是独立 Gradle 工程。需要 JDK 21。

```bash
./gradlew build
```

产物：`build/libs/adaptivetooltips-1.3.0-1.21.1.jar`。

- NeoForge 版本固定在 `gradle.properties` 的 `neo_version`，可按需修改。
- YACL 已放在 `libs/`，因为其 Maven 没有发布 1.21.1 NeoForge 构件。
- 开发客户端：`./gradlew runClient`。

### 与上游 1.3.0 的差异

| 项目 | 说明 |
|---|---|
| 加载器 | Fabric → NeoForge（入口、`neoforge.mods.toml`、`FMLPaths`、`IConfigScreenFactory`） |
| modId | `adaptive-tooltips` → `adaptivetooltips`（NeoForge modId 不允许连字符） |
| 透明度 | 适配 NeoForge 对 `TooltipRenderUtil` 的补丁：改为修改 9 参重载的 `RenderTooltipEvent.Color` |
| 基岩版居中 | 除“被推到 `x = 4` 以左”外，**右侧会溢出时也会居中**（体验增强，比上游更积极） |
| 语言 | 新增 `zh_tw`；按 YACL 3.x 键名重写 `zh_cn`；补齐按键选项；刷新 `ru_ru` |
| 滚动提示 | 文案中的按键名会随当前绑定动态显示 |

### 致谢与许可

- 原模组：**Adaptive Tooltips 1.3.0**，作者 **isXander** — <https://github.com/isXander/AdaptiveTooltips>
- NeoForge 1.21.1 移植：**LvDriver**
- 许可：**GNU Lesser General Public License v3.0 or later**（`LGPL-3.0-or-later`），见 [LICENSE](LICENSE)。

本移植是上游 LGPL-3.0-or-later 代码的衍生作品。本仓库包含该移植完整的对应源码；如果你分发修改后的构建，必须同时以相同许可证公开修改后的源码。

YACL 同样使用 LGPL-3.0；其 jar 放在 `libs/` 仅用于开发与构建，见 <https://github.com/isXander/YetAnotherConfigLib>。

---

## English

### Overview

**Adaptive Tooltips Reforged** is a **NeoForge 1.21.1** port of **Adaptive Tooltips 1.3.0**, originally a Fabric mod for Minecraft 1.20.1 / 1.20.2 by isXander.

The goal is behavior parity with the 1.20.x release: the same wrapping algorithms, positioning rules, scrolling logic and config screen, rebuilt against Minecraft 1.21.1 and NeoForge. It is **client-side only**; it does not affect the server and does not change item data.

### Features

- **Text wrapping**: Off, Screen width, Remaining width, Half screen width, Smart
- **Prioritize tooltip top** when a tooltip is taller than the screen
- **Bedrock-style centering**: center on the mouse when a tooltip is too wide for the current side
- **Align to corner**: pick the least obstructive corner, plus an "always" mode
- **Prevent vanilla clamping**: allow tooltips to leave the left edge and be repositioned
- **Scrolling**: hold Left Alt and scroll vertically; hold Left Ctrl as well to scroll horizontally (both rebindable, the hint shows the current keys)
- **Transparency**: adjust the tooltip background and border alpha
- **Remove first-line padding**
- **YACL config screen**
- **Languages**: English, Simplified Chinese, Traditional Chinese, Russian

### Requirements

| | |
|---|---|
| Minecraft | **1.21.1** |
| Loader | **NeoForge 21.1.218+** |
| Dependency | **YetAnotherConfigLib (YACL) 3.8.2+1.21.1-neoforge** (required) |
| Java | 21 |

### Installation

1. Install Minecraft 1.21.1 with NeoForge 21.1.218 or newer.
2. Put the mod jar and the YACL jar into `mods/`.
3. Launch the client. The config screen is available under **Mods → Adaptive Tooltips Reforged → Config**.

The config file is `config/adaptive-tooltips.json` (same filename as upstream, so existing settings keep working).

### Building from source

This is a standalone Gradle project. JDK 21 is required.

```bash
./gradlew build
```

Output: `build/libs/adaptivetooltips-1.3.0-1.21.1.jar`.

- NeoForge is pinned via `neo_version` in `gradle.properties`; adjust as needed.
- YACL is vendored in `libs/` because its Maven does not publish a 1.21.1 NeoForge artifact.
- Development client: `./gradlew runClient`.

### Differences from upstream 1.3.0

| | |
|---|---|
| Loader | Fabric → NeoForge (entrypoint, `neoforge.mods.toml`, `FMLPaths`, `IConfigScreenFactory`) |
| modId | `adaptive-tooltips` → `adaptivetooltips` (NeoForge mod IDs cannot contain `-`) |
| Transparency | Adapted to NeoForge's `TooltipRenderUtil` patch: modifies the 9-arg overload's `RenderTooltipEvent.Color` |
| Bedrock centering | Also centers when the tooltip **would overflow the right side**, not only when pushed past `x = 4` (a small usability enhancement) |
| Languages | Added `zh_tw`; rewrote `zh_cn` for YACL 3.x keys; added missing keybind labels; refreshed `ru_ru` |
| Scroll hint | The key names in the hint now follow the current bindings |

### Credits & License

- Original mod: **Adaptive Tooltips 1.3.0** by **isXander** — <https://github.com/isXander/AdaptiveTooltips>
- NeoForge 1.21.1 port: **LvDriver**
- License: **GNU Lesser General Public License v3.0 or later** (`LGPL-3.0-or-later`). See [LICENSE](LICENSE).

This port is a derivative work of the original LGPL-3.0-or-later code. This repository contains the complete corresponding source of the port; if you redistribute a modified build, you must also make the modified source available under the same license.

YACL is also LGPL-3.0; its jar is included under `libs/` for development and building only. See <https://github.com/isXander/YetAnotherConfigLib>.
