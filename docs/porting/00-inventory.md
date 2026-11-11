# Adaptive Tooltips 1.3.0 盘点（P1）

> 参考源：`reference/AdaptiveTooltips-1.3.0/`（GitHub tag `1.3.0`，commit `9a54090728703f9ee8baa2ead9bc02b4c21cf149`，tarball 已校验可解压）
> 用途：只读对照，不参与构建。目标：MC 1.21.1 / NeoForge。

## 1. 元数据

| 项 | 值 |
|---|---|
| mod id | `adaptive-tooltips`（fabric.mod.json 中为模板变量 `${id}`，gradle.properties 定义 `modId=adaptive-tooltips`；资源包目录用 `adaptivetooltips`） |
| mod 名称 | Adaptive Tooltips |
| 作者 | isXander |
| 许可证 | **LGPL-3.0-or-later**（tag 内 `LICENSE` 为 LGPL v3 全文；`fabric.mod.json` 显式声明） |
| 环境 | client-only |
| 基线构建 | Minecraft **1.20.2**，Quilt Mappings build 2 + Mojang official 叠加，Loom `1.3.+` |
| 发布兼容 | 1.20.1 / 1.20.2，Fabric / Quilt |
| 冲突声明 | `tooltipfix: *` |

## 2. 依赖与实际使用

| 依赖 | 坐标/版本 | 代码中是否实际使用 |
|---|---|---|
| Fabric Loader | `net.fabricmc:fabric-loader:0.14.22` | **是**，仅 1 处：`FabricLoader.getInstance().getConfigDir()`（配置路径） |
| Fabric API | `net.fabricmc.fabric-api:fabric-api:0.89.1+1.20.2` | **否**：全库无 `net.fabricmc.fabric.*` import；元数据里 `fabric-api: "*"` 属模板残留，移植时可去掉 |
| YACL | `dev.isxander.yacl:yet-another-config-lib-fabric:3.2.1+1.20.2` | **是**：`yacl3.config.v2` 自动生成配置（`ConfigClassHandler`、`@AutoGen`、`@SerialEntry`、`SimpleOptionFactory`、`NameableEnum`、`ValueFormatters`） |
| ModMenu | `com.terraformersmc:modmenu:8.0.0` | **是**：仅 `ModMenuIntegration`（ConfigScreenFactory） |
| MixinExtras | `com.github.llamalad7.mixinextras:mixinenextras-fabric:0.2.0-rc.4`（`include`） | **是**：`WrapOperation`、`WrapWithCondition`、`ModifyReturnValue`、`ModifyExpressionValue` |

## 3. 类清单（24 个 Java 文件）

| 包 | 文件 | 作用 |
|---|---|---|
| 根 | `AdaptiveTooltips` | 客户端入口：`HANDLER.load()` + 注册 `KeyCode` 自定义 OptionFactory |
| config | `AdaptiveTooltipConfig` | YACL v2 配置定义（15 个字段）+ Gson/JSON5 序列化 |
| config | `ScrollDirection` / `WrapTextBehaviour` | `NameableEnum` 枚举 |
| config.gui | `KeyCode` / `KeyCodeController` | 自定义注解 + YACL 控件（等待按键） |
| config.modmenu | `ModMenuIntegration` | ModMenu 配置页入口 |
| helpers | `TooltipWrapper` | 换行核心：文本行 → 按策略 `Font.split`，再包回 `ClientTextTooltip` |
| helpers | `ScrollTracker` | 滚动目标/动画/边界 clamp/组件相等性判断 |
| helpers | `YACLTooltipPositioner` | 按钮工具提示的 YACL 风格定位 |
| helpers.positioner | `TooltipPositionModule` + `BedrockCentering` / `BestCorner` / `PrioritizeTooltipTop` | 三种定位策略 |
| mixins | 9 个（见下表） | 注入原版渲染/输入 |
| utils | `TextUtil` | `FormattedCharSequence` ↔ `MutableComponent` 转换 |

## 4. Mixin 明细（9 个，全部在 `client` 列表）

| Mixin | 目标类 | 注入点 | 目的 |
|---|---|---|---|
| `GuiGraphicsMixin` | `net.minecraft.client.gui.GuiGraphics` | `renderTooltipInternal`：`@ModifyVariable(HEAD, argsOnly)`、`@WrapOperation`（`ClientTooltipPositioner;positionTooltip(IIIIII)`）、`@Inject`（`PoseStack.popPose` 前）、`@ModifyConstant(intValue=2)` | 核心：替换 tooltip 组件列表（换行）、改写位置、注入矩阵栈、去掉首行 2px padding |
| `TooltipMixin` | `net.minecraft.client.gui.components.Tooltip` | `splitTooltip` 中 `Font.split(FormattedText,int)` 的 `@WrapOperation` | 关闭原版 170px 换行 |
| `TooltipRenderUtilMixin` | `...inventory.tooltip.TooltipRenderUtil` | `renderTooltipBackground` 中 3 个颜色常量的 `@ModifyExpressionValue` | 透明度调节（改 alpha） |
| `MouseMixin` | `net.minecraft.client.MouseHandler` | `onScroll` 中 `Screen.mouseScrolled(DDDD)` 的 `@WrapWithCondition` | 拦截滚轮，喂给 `ScrollTracker` |
| `ClickableWidgetMixin` | `net.minecraft.client.gui.components.AbstractWidget` | `createTooltipPositioner` 的 `@ModifyReturnValue` | 替换为 `YACLTooltipPositioner` |
| `GameRendererMixin` | `net.minecraft.client.renderer.GameRenderer` | `render` 的 `@Inject(RETURN)` | 一帧未渲染 tooltip 则重置滚动 |
| `HoveredTooltipPositionerMixin` | `...tooltip.DefaultTooltipPositioner` | `positionTooltip` 中 `Math.max` 的 `@ModifyArg` | 关闭原版水平 clamp |
| `BundleTooltipComponentAccessor` | `...tooltip.ClientBundleTooltip` | `@Accessor getItems()` | 读取 bundle 内容用于滚动相等性判断 |
| `ClientTextTooltipAccessor` | `...tooltip.ClientTextTooltip` | `@Accessor getText()` | 读取文本用于换行/比较 |

## 5. 配置项（15 个，默认值）

| 字段 | 类型/注解 | 默认值 |
|---|---|---|
| `wrapText` | `WrapTextBehaviour` / `@EnumCycler` | `SCREEN_WIDTH` |
| `overwriteVanillaWrapping` | boolean / `@TickBox` | `false` |
| `prioritizeTooltipTop` | boolean | `true` |
| `bedrockCentering` | boolean | `true` |
| `bestCorner` | boolean / `@MasterTickBox("alwaysBestCorner")` | `false` |
| `alwaysBestCorner` | boolean / `@MasterTickBox({...}, invert=true)` | `false` |
| `preventVanillaClamping` | boolean / `@MasterTickBox("bedrockCentering")` | `true` |
| `onlyRepositionHoverTooltips` | boolean | `true` |
| `useYACLTooltipPositioner` | boolean | `false` |
| `scrollKeyCode` | int / `@KeyCode` | `KEY_LALT` |
| `horizontalScrollKeyCode` | int / `@KeyCode` | `KEY_LCONTROL` |
| `smoothScrolling` | boolean | `true` |
| `scrollDirection` | `ScrollDirection` | macOS `NATURAL`，其余 `REVERSE` |
| `verticalScrollSensitivity` | int `[5,20]` | `10` |
| `horizontalScrollSensitivity` | int `[5,20]` | `10` |
| `tooltipTransparency` | float `[0,1]`、百分比格式 | `1.0` |
| `removeFirstLinePadding` | boolean | `true` |

（注：`scrollingGuide` 为 `@Label` 说明文字，不计入可配置字段。）

## 6. 资源

- `assets/adaptivetooltips/lang/en_us.json`（7.4 KB）、`ru_ru.json`、`zh_cn.json`
- `icon.png`（15 KB）
- `adaptive-tooltips.mixins.json`：`compatibilityLevel: JAVA_17`，9 个 client mixin

## 7. 对移植的直接影响（预判，待 P3 核实）

1. **Fabric API 可整体移除**（代码零引用），只需处理 `FabricLoader.getConfigDir()`。
2. **ModMenu → NeoForge `IConfigScreenFactory`**，配置页生成代码（`HANDLER.generateGui().generateScreen(parent)`）可复用。
3. **入口**：`onInitializeClient()` → `@Mod(distribution = Dist.CLIENT)` 构造器，`HANDLER.load()` 与 `OptionFactory.register` 保留。
4. **MixinExtras**：1.3.0 用 0.2.0-rc.4；NeoForge 1.21.1 自带版本较新，`WrapWithCondition` 等注解包名可能迁移（如 `injector.v2`），需按实际版本核对。
5. **9 个 Mixin 的目标签名**是最大风险面：`GuiGraphics#renderTooltipInternal`、`TooltipRenderUtil#renderTooltipBackground`、`Tooltip#splitTooltip`、`ClientBundleTooltip` 等需要在 1.21.1 上逐个核对（P3）。
6. **YACL**：1.21.1 有 NeoForge 构建（Modrinth `3.8.2+1.21.1-neoforge`），但 3.2.1 → 3.8.x 的 v2 config API 是否兼容需核实。

## 8. P3 待查清单（输入）

- [ ] `GuiGraphics`：`renderTooltipInternal` 是否仍存在、签名、`pose()`/`guiWidth()`/`guiHeight()`
- [ ] `ClientTooltipPositioner` / `DefaultTooltipPositioner`：`positionTooltip` 签名与 `Math.max` 调用点
- [ ] `ClientTextTooltip` / `ClientBundleTooltip`：字段名与类型
- [ ] `TooltipRenderUtil`：方法名/签名/3 个颜色常量值
- [ ] `Tooltip`：`splitTooltip` 与 `Font.split`
- [ ] `AbstractWidget#createTooltipPositioner`
- [ ] `MouseHandler#onScroll` 与 `Screen#mouseScrolled`
- [ ] `GameRenderer#render` 签名
- [ ] NeoForge：`IConfigScreenFactory`、客户端专属 `@Mod` 写法、`FMLPaths.CONFIGDIR`
- [ ] YACL 3.8.2 NeoForge：`config.v2` API 与 `SimpleOptionFactory`/`OptionFactory.register` 是否保留
- [ ] MixinExtras：NeoForge 1.21.1 自带版本及注解包名
