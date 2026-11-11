# Adaptive Tooltips Reforged

A **NeoForge 1.21.1** port of [Adaptive Tooltips](https://github.com/isXander/AdaptiveTooltips) **1.3.0**, originally a Fabric mod for Minecraft 1.20.1/1.20.2 by [isXander](https://github.com/isXander).

The goal is behavior parity with the 1.20.x release: same wrapping algorithms, same positioning rules, same scrolling, same config screen — rebuilt against Minecraft 1.21.1 and NeoForge.

> Client-side only. The original mod and this port only affect tooltip rendering on the client.

## Features

- **Text wrapping** — `Off`, `Screen width`, `Remaining width`, `Half screen width`, `Smart`
- **Prioritize tooltip top** when a tooltip is taller than the screen
- **Bedrock-style centering** when a tooltip is too wide for the current side
- **Align to corner** (least obstructive corner), with an "always" mode
- **Prevent vanilla clamping** so tooltips can leave the left edge and be repositioned
- **Tooltip scrolling** — hold `Left Alt` and scroll vertically; hold `Left Ctrl` as well to scroll horizontally (both rebindable, the hint shows your current keys)
- **Tooltip transparency** for the background and border
- **Remove first-line padding**
- **YACL config screen**
- Languages: English, Simplified Chinese, Traditional Chinese, Russian

## Requirements

| | |
|---|---|
| Minecraft | **1.21.1** |
| Loader | **NeoForge 21.1.218+** |
| Dependency | **YetAnotherConfigLib (YACL) 3.8.2+1.21.1-neoforge** (required) |
| Java | 21 |

## Installation

1. Install Minecraft 1.21.1 with NeoForge 21.1.218 or newer.
2. Put the mod jar and the YACL jar into `mods/`.
3. Launch the client. The config screen is available under **Mods → Adaptive Tooltips Reforged → Config**.

The config file is stored at `config/adaptive-tooltips.json` (same filename as upstream, so existing settings keep working).

## Building

JDK 21 is required.

```bash
./gradlew build
```

Output: `build/libs/adaptivetooltips-1.3.0-1.21.1.jar`.

- NeoForge 21.1.218 is pinned in `gradle.properties`; adjust if needed.
- YACL is vendored in `libs/` because its Maven does not publish a 1.21.1 NeoForge artifact.
- Run a development client with `./gradlew runClient`.

## Differences from upstream 1.3.0

| | |
|---|---|
| Loader | Fabric → NeoForge (entrypoint, `neoforge.mods.toml`, `FMLPaths`, `IConfigScreenFactory`) |
| modId | `adaptive-tooltips` → `adaptivetooltips` (NeoForge mod IDs cannot contain `-`) |
| Transparency | Adapted to NeoForge's patched `TooltipRenderUtil` 9-arg overload (`RenderTooltipEvent.Color`) |
| Bedrock centering | Also triggers when the tooltip **would overflow the right side**, not only when it is pushed past `x = 4` (small usability enhancement) |
| Languages | Added `zh_tw`; rewrote `zh_cn` for YACL 3.x keys; added missing keybind labels; refreshed `ru_ru` |

Porting notes (API delta / acceptance criteria / verification) are under [`docs/porting/`](docs/porting/).

## 中文简介

**Adaptive Tooltips Reforged** 是 isXander 的 Fabric 模组 *Adaptive Tooltips* **1.3.0**（Minecraft 1.20.1/1.20.2）的 **NeoForge 1.21.1** 移植版。

- 功能：tooltip 自动换行（5 种策略）、优先顶部、基岩版居中、角落对齐、Alt+滚轮滚动、透明度、首行间距、YACL 配置页
- 环境：Minecraft 1.21.1 + NeoForge 21.1.218+，需要同时安装 **YACL 3.8.2+1.21.1-neoforge**
- 客户端模组；配置文件为 `config/adaptive-tooltips.json`
- 构建：`./gradlew build`

## Credits & License

- Original mod: **Adaptive Tooltips 1.3.0** by **isXander** — <https://github.com/isXander/AdaptiveTooltips>
- NeoForge 1.21.1 port: **LvDriver**
- License: **GNU Lesser General Public License v3.0 or later** (`LGPL-3.0-or-later`). See [LICENSE](LICENSE).

This port is a derivative work of the original LGPL-3.0-or-later code. The complete corresponding source of this port is this repository. If you redistribute a modified build, you must also make the modified source available under the same license.

YACL is licensed under LGPL-3.0; its jar is included under `libs/` for development convenience. See <https://github.com/isXander/YetAnotherConfigLib>.
