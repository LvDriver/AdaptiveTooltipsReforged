# 移植验证记录（P6 / P7）

日期：2026-11-05
目标：Minecraft 1.21.1 / NeoForge 21.1.218 / 客户端

## 1. 构建

| 检查 | 结果 | 证据 |
|---|---|---|
| `gradlew compileJava` | ✅ | 第一次即通过（MDG 用缓存构件本地重建 NeoForge） |
| `gradlew build` | ✅ | `BUILD SUCCESSFUL`，产物 `ports/adaptive-tooltips/build/libs/adaptivetooltips-1.3.0+1.21.1.jar`（61 KB） |
| jar 内容 | ✅ | `META-INF/neoforge.mods.toml`、`adaptive-tooltips.mixins.json`、`icon.png`、三份 lang、全部 class |
| `validate_project` | ✅ | passed；入口已是 `@Mod + IEventBus` 构造 |
| `mixin_analyze`（非 deep） | ⚠️ 工具误报 | 其 1.21.1 映射表查不到 `renderTooltipInternal` 等（但 `mcdev` 反编译源码确认存在）；不采信为失败 |
| `mixin_analyze`（deep） | ⚠️ 工具误报 | 对 `ports/adaptive-tooltips/build/moddev/artifacts/neoforge-21.1.218-merged.jar` 报“目标类不存在”，实际 `tar -tf` 可见 `GuiGraphics.class`；不采信 |

## 2. 运行期（runClient）

启动日志关键证据：

- `Found valid mod file main with {adaptivetooltips} mods - versions {1.3.0+1.21.1}`
- `Creating FMLModContainer instance for [dev.isxander.adaptivetooltips.AdaptiveTooltips]`
- `Config file 'ports/adaptive-tooltips/run/config/adaptive-tooltips.json' does not exist. Creating it with default values.`（默认值 17 项全部正确）
- Mixin 成功应用：`TooltipMixin → Tooltip`、`MouseMixin → MouseHandler`、`GameRendererMixin → GameRenderer`、`WidgetTooltipHolderMixin → WidgetTooltipHolder`、`GuiGraphicsMixin → GuiGraphics`
- 资源重载包含 `mod/adaptivetooltips`
- `latest.log` 无 `ERROR` / `FATAL` / `InvalidInjectionException`
- 客户端到达主菜单（Sound engine started / 纹理图集创建完成）

## 3. 运行期修复记录

| 问题 | 原因 | 修复 |
|---|---|---|
| 首次启动崩溃于 `MouseHandler` | MixinExtras 0.5.0 的 `@WrapWithCondition` 只允许 **void** 目标，而 `Screen.mouseScrolled` 返回 boolean；且旧代码依赖“捕获外层参数” | 改为 `@WrapOperation`，用目标调用自带的 `horizontalAmount/verticalAmount`，窗口句柄改从 `Minecraft.getInstance().getWindow()` 取 |
| 元数据模板解析失败 | 注释里出现 `${...}` 被 Groovy 模板引擎当作表达式 | 注释去掉 `${...}` |
| `validate_project` 入口检查失败 | 构造器只有 `ModContainer` | 改为 `(IEventBus, ModContainer)`（与官方 MDK 一致） |

## 4. 仍需人工游戏内验收（P7 剩余）

启动方式：仓库根目录执行 `.\gradlew :ports:adaptive-tooltips:runClient`（或 `.\scripts\run-client.ps1 adaptive-tooltips`）。YACL 已在 classpath，无需额外装模组。

| # | 功能 | 操作 |
|---|---|---|
| 1 | 换行 5 模式 | 打开配置页切换 `wrapText`，用长名称物品对比 |
| 2 | 优先顶部 | 超长 tooltip（如带大量附魔/描述）观察从顶部开始 |
| 3 | Bedrock 居中 | 物品在屏幕左右边缘时观察居中/上下切换 |
| 4 | 角落对齐 | 开启 `bestCorner` / `alwaysBestCorner` |
| 5 | 关闭原版换行 | 开启 `overwriteVanillaWrapping` |
| 6 | 滚动 | 按住 Alt 滚轮（Ctrl+Alt 横向），平滑动画与灵敏度 |
| 7 | 透明度 | 调 `tooltipTransparency` 观察背景与边框 |
| 8 | 首行 padding | 开关 `removeFirstLinePadding` |
| 9 | 配置页 | Mods → Adaptive Tooltips → Config（YACL 页面） |
| 10 | 崩溃/日志 | 检查 `ports/adaptive-tooltips/run/logs/latest.log` 无 ERROR |

> 注：日志中 YACL 自身的 `Class version 65 ... JAVA_17` 提示是 YACL 的 mixin 配置声明较旧所致，不影响运行；本模组 mixin 配置已声明 `JAVA_21`。

## 5. 工具限制记录（供后续会话参考）

- `mcskill activate_platform_pack` 对 **neoforge 1.21.1** 返回 `PACK_NOT_FOUND`（无规则树）；API 查证改用 `search_neoforge_docs` + `mcdev` 反编译源码。
- `mcskill download_official_mdk` 写用户工程需要 `MC_SKILL_ALLOW_WRITE` / `MC_SKILL_PROJECT_ROOT` 环境变量（当前会话未设置），因此采用“下载到缓存 + 手动复制”的官方脚手架路径。
- `maven.neoforged.net` 不可达；本地 Gradle 缓存含 NeoForge 21.1.218 / MDG / Parchment，构建可正常完成。

## 6. 多模块布局（方案 B）验证

工程已迁移为根多模块构建：

```
ModPort/settings.gradle          include(':ports:adaptive-tooltips')
ModPort/build.gradle             plugins { id 'base'; id 'net.neoforged.moddev' version '2.0.144' apply false }
ModPort/ports/adaptive-tooltips/ 子模块（无独立 wrapper，无独立 settings.gradle）
```

验证结果：

| 检查 | 结果 |
|---|---|
| 根 `.\gradlew build` | ✅ `BUILD SUCCESSFUL`，执行 `:ports:adaptive-tooltips:build` |
| 根 `.\gradlew :ports:adaptive-tooltips:runClient` | ✅ 到达主菜单，游戏目录为 `ports/adaptive-tooltips/run` |
| 子模块 plugin 版本 | 由根 `apply false` 统一，子模块只写 `id 'net.neoforged.moddev'` |
| 子模块 wrapper | 已删除，统一使用根 wrapper（Gradle 9.2.1） |

注意：同一 build 仅适用于同 MC + 同加载器的端口；跨大版本必须拆分独立 build。

## 7. 收尾修复（2026-11-11，人工实测后）

| 问题 | 根因 | 修复 |
|---|---|---|
| 配置界面中文显示原始键/旧键名 | `zh_cn.json` 仍是旧版 YACL 键（`adaptivetooltips.opt.*`），1.21.1 YACL 实际查 `yacl3.config.adaptivetooltips:config.*` | 按正确键名重写 `zh_cn.json`；新增 `zh_tw.json`；`en_us` 补 `scrollKeyCode` / `horizontalScrollKeyCode` |
| 透明度完全无效 | NeoForge 补丁让 `GuiGraphics` 调用 `TooltipRenderUtil` 的 **9 参重载**（颜色来自 `RenderTooltipEvent.Color`）；原 Mixin 改的 6 参常量方法无人调用 | Mixin 改为对 9 参重载的四个颜色参数 `@ModifyVariable`（index 6–9） |
| 基岩版居中“看起来没生效” | 上游只在 `x < 4` 触发，tooltip 宽度不够时即使贴右也不会进入该分支 | 触发条件扩展为 `x < 4 || 右侧会溢出`；这是相对上游的行为增强，已记录在 01 文档 §D |
| 聊天框 `/give` 报错 | 命令超过 256 字符被截断；`lore` 纯数字字符串被当作数字 | 拆短命令、lore 加非数字前缀（见 04 文档 §5.1） |

清理：已移除诊断用的 `[AT]` INFO 日志，最终 jar 无调试输出。
