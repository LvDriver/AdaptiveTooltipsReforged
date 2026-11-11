# 1.20.2 → 1.21.1 API 差异对照（P3）

> 依据：MCModding-MCP（Parchment 1.21.1）签名查询 + `mcdev` 反编译 1.21.1（mojmap）源码 + YACL 3.8.2 / NeoForge 21.1.218 缓存构件 `javap` 核对。
> 结论只覆盖本模组实际触碰的 API；每条给出可执行改法。

## A. 原版（Minecraft）差异

| # | 1.3.0（1.20.2）用法 | 1.21.1 实况（证据） | 结论 / 改法 |
|---|---|---|---|
| A1 | `GuiGraphics#renderTooltipInternal(Font, List<ClientTooltipComponent>, int, int, ClientTooltipPositioner)` | 同名同签名仍在（`GuiGraphics.java:625`，Parchment 查询一致）；`pose()`（:109）、`guiWidth()`（:101）、`guiHeight()`（:105）均在 | ✅ Mixin 注入点全部保留：`@ModifyVariable(HEAD)`、`@WrapOperation(positionTooltip)`、`@Inject(popPose 前)`、`@ModifyConstant(2)` |
| A2 | `TooltipRenderUtil#renderTooltipBackground(GuiGraphics,int,int,int,int,int)` + 常量 `-267386864` / `1347420415` / `1344798847` | **NeoForge 打了补丁**：`GuiGraphics` 不再调用带常量的 6 参重载，而是调用 9 参重载并把 `RenderTooltipEvent.Color` 的四个颜色传进去（`TooltipRenderUtil` 6 参方法只做转发，常量路径是死代码） | ⚠️ 透明度 Mixin 改为对 9 参重载的四个颜色参数做 `@ModifyVariable`（index 6–9），否则完全无效 |
| A3 | `Tooltip#splitTooltip(Font, FormattedText)`，内部 `Font.split(text, 170)` | 方法改为 `splitTooltip(Minecraft, Component)`（`Tooltip.java:53`），内部仍是 `minecraft.font.split(message, 170)`；`Font.split(FormattedText,int)` 仍在（`Font.java:212`） | ✅ `@WrapOperation` 目标 `Font.split` 不变，注入方法名 `splitTooltip` 仍唯一 |
| A4 | `DefaultTooltipPositioner#positionTooltip(IILorg/joml/Vector2i;II)V` 的 `Math.max` | 私有重载仍在，签名完全一致，`Math.max` 在 `DefaultTooltipPositioner.java:21` | ✅ 原 Mixin 原样可用 |
| A5 | `ClientTextTooltip` 字段 `text`（`@Accessor getText()`） | 字段仍为 `private final FormattedCharSequence text`（`ClientTextTooltip.java:9`） | ✅ 不变 |
| A6 | `ClientBundleTooltip` 字段 `items`（`NonNullList<ItemStack>`，`@Accessor getItems()`） | **已重构**：字段为 `private final BundleContents contents`（`ClientBundleTooltip.java:17`），无 `items` | ⚠️ 改 accessor 为 `@Accessor("contents") BundleContents getContents()`；`BundleContents.items()` 返回 `Iterable<ItemStack>`（`BundleContents.java:68`），滚动相等性判断改为遍历该 Iterable + `ItemStack.matches` |
| A7 | `AbstractWidget#createTooltipPositioner()`（`@ModifyReturnValue`） | **方法已移除**；改到 `WidgetTooltipHolder#createTooltipPositioner(ScreenRectangle, boolean, boolean)`（`WidgetTooltipHolder.java:59`，private） | ⚠️ Mixin 改挂 `WidgetTooltipHolder`；`YACLTooltipPositioner` 构造参数由 `AbstractWidget` 改为 `ScreenRectangle`（`left()/top()/width()/height()`） |
| A8 | `GameRenderer#render(float, long, boolean)` | 改为 `render(DeltaTracker, boolean)`（`GameRenderer.java:985`） | ⚠️ 注入方法名不变，handler 签名改为 `(DeltaTracker, boolean, CallbackInfo)` |
| A9 | `MouseHandler#onScroll(long, double, double)` 内 `Screen.mouseScrolled(DDDD)Z` 的 `@WrapWithCondition` | 调用点不变，但 `mouseScrolled` 返回 boolean，而 MixinExtras 0.5.0 的 `@WrapWithCondition` 只允许 void 目标 | ⚠️ 改为 `@WrapOperation`，用目标调用自带的 `horizontalAmount/verticalAmount`，窗口句柄改从 `Minecraft.getInstance().getWindow()` 取 |
| A10 | `Minecraft#getDeltaFrameTime()` | **已移除**；`Minecraft#getTimer()` 返回 `DeltaTracker`（`Minecraft.java:2553`），帧增量用 `getRealtimeDeltaTicks()`（`DeltaTracker.java:13`） | ⚠️ `ScrollTracker` 改 `Minecraft.getInstance().getTimer().getRealtimeDeltaTicks()` |
| A11 | `ScreenRectangle`（新用） | 记录类：`left()/top()/right()/bottom()/width()/height()`（`ScreenRectangle.java:72-86,5`） | ✅ YACL 定位器用 `left()/top()/width()/height()` |

## B. 加载器 / 依赖差异

| # | 1.3.0 | 1.21.1 NeoForge 实况 | 改法 |
|---|---|---|---|
| B1 | `FabricLoader.getInstance().getConfigDir()` | `FMLPaths.CONFIGDIR.get()`（loader 4.0.42 `javap` 确认 `CONFIGDIR` 存在） | 配置路径改 `FMLPaths.CONFIGDIR.get().resolve("adaptive-tooltips.json")` |
| B2 | `fabric.mod.json` + `client` entrypoint | `META-INF/neoforge.mods.toml` + `@Mod` | `@Mod(value = "adaptivetooltips", dist = Dist.CLIENT)`（`@Mod` 含 `Dist[] dist()`，`javap` 确认）；构造器内 `HANDLER.load()`、`OptionFactory.register(...)`、注册配置页 |
| B3 | ModMenu `ConfigScreenFactory` | NeoForge `IConfigScreenFactory`：`container.registerExtensionPoint(IConfigScreenFactory.class, ...)`（官方 1.21.1 文档 `misc/config` 片段） | 用 lambda `(modContainer, parent) -> AdaptiveTooltipConfig.HANDLER.generateGui().generateScreen(parent)`；不需要 ModMenu |
| B4 | Fabric API（代码零引用） | — | 直接删除依赖 |
| B5 | MixinExtras 0.2.0-rc.4（自带） | NeoForge 21.1.218 内置 **0.5.0**（`META-INF/jarjar/mixinextras-neoforge-0.5.0.jar`） | `WrapOperation`/`ModifyReturnValue`/`ModifyExpressionValue` 包名不变；`WrapWithCondition` 新旧包都在，建议改用 `com.llamalad7.mixinextras.injector.v2.WrapWithCondition` |
| B6 | Mixin 配置 `compatibilityLevel: JAVA_17` | Java 21 运行时 | 改 `JAVA_21`（Mixin 0.8.5+ 支持） |
| B7 | YACL 3.2.1 Fabric | YACL **3.8.2 NeoForge 1.21.1**（jar 已下载，`javap` 核对） | `config.v2` API 全部保留：`ConfigClassHandler.createBuilder`、`generateGui()`、`OptionFactory.register`、`SimpleOptionFactory#createController`、`GsonConfigSerializerBuilder#setJson5`、`ValueFormatters$PercentFormatter`（record，无参构造）、`ControllerWidget` 三参构造、`Controller` 三方法 |

## C. 结论

- **9 个 Mixin 中 4 个原样可用**（A1/A3/A4/A5），5 个需改目标/签名/注解（A2/A6/A7/A8/A9）。
- 业务代码（换行、定位、滚动、透明度）**逻辑无需重写**，只改外围 API 调用（`FMLPaths`、`DeltaTracker`、`BundleContents`、`ScreenRectangle`）。
- 无 1.21.1 不存在的功能；不需要 AT/AW；不需要额外 Mixin。

## D. 移植后的行为差异（相对上游 1.3.0）

| 差异 | 说明 |
|---|---|
| 基岩版居中触发条件 | 上游只在 `x < 4`（被推到左边缘外）时居中；本移植额外在**右侧会溢出**（`mouseX + 12 + width > screenWidth`）时也居中，避免“tooltip 贴左但差几像素不居中”的观感问题 |
| modId | `adaptive-tooltips` → `adaptivetooltips`（NeoForge modId 不允许连字符） |
| 语言 | 新增 `zh_tw`；`zh_cn` 按 YACL 3.x 键名重写；`en_us` 补齐两个按键选项键 |

> 若需要严格 1:1，可把“右侧溢出即居中”做成配置开关（见待办）。
