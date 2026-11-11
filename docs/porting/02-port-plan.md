# Adaptive Tooltips → NeoForge 1.21.1 移植方案（P4 待确认）

> 状态：已确认并执行完毕（P2–P6 完成），验证记录见 [`03-verification.md`](03-verification.md)。
>
> 基线：上游 tag `1.3.0`（Fabric 1.20.2，兼容 1.20.1），许可证 **LGPL-3.0-or-later**。
> 目标：MC 1.21.1 / NeoForge 21.1.x / 客户端模组，功能 1:1，不做额外重构。

## 1. 技术基线（与已缓存环境对齐）

| 项 | 选择 | 依据 |
|---|---|---|
| 构建插件 | ModDevGradle **2.0.140** | Gradle 缓存已有 `net.neoforged.moddev:2.0.140` |
| NeoForge | **21.1.218** | 缓存已有 `neoforge-21.1.218`（含 sources/userdev） |
| Java | **21**（Temurin 21.0.11） | 本机已装 |
| Mappings | Parchment `2024.11.17` | 缓存 `parchment-1.21.1-2024.11.17.zip` |
| YACL | **3.8.2+1.21.1-neoforge**（本地 jar 随工程 `libs/`） | Maven 坐标无 1.21.1 版本；Modrinth jar 已下载并 `javap` 核对 API |
| MixinExtras | NeoForge 内置 0.5.0，不额外引入 | 已从 universal jar 解出确认 |

> ⚠️ 环境事实：`maven.neoforged.net` 当前 TCP 不可达；但上述 NeoForge/NeoForm/MDG/Parchment 构件均已缓存，Mojang 主机可达。**优先 `--offline` 构建**；若缺件再报你处理（代理/镜像），不擅自改网络配置。

## 2. 工程结构（新增 1 个 Mixin 类，其余按包平移）

```
build.gradle.kts / settings.gradle.kts / gradle.properties
libs/yet_another_config_lib_v3-3.8.2+1.21.1-neoforge.jar
src/main/java/dev/isxander/adaptivetooltips/
  AdaptiveTooltips.java                 # @Mod(dist=CLIENT) + 配置/扩展点
  config/AdaptiveTooltipConfig.java     # FMLPaths.CONFIGDIR
  config/ScrollDirection.java
  config/WrapTextBehaviour.java
  config/gui/KeyCode.java
  config/gui/KeyCodeController.java
  helpers/TooltipWrapper.java
  helpers/ScrollTracker.java            # BundleContents + DeltaTracker
  helpers/YACLTooltipPositioner.java    # ScreenRectangle 构造
  helpers/positioner/*.java             # 4 个，原样
  mixins/GuiGraphicsMixin.java          # 原样（6 处注入全部命中）
  mixins/TooltipMixin.java              # 原样（splitTooltip 内部 Font.split 未变）
  mixins/TooltipRenderUtilMixin.java    # 原样（常量未变）
  mixins/MouseMixin.java                # 仅 WrapWithCondition 改 v2 包
  mixins/DefaultTooltipPositionerMixin.java  # 原 HoveredTooltipPositionerMixin，原样
  mixins/GameRendererMixin.java         # render(DeltaTracker, boolean)
  mixins/WidgetTooltipHolderMixin.java  # 新：替代 ClickableWidgetMixin
  mixins/ClientBundleTooltipAccessor.java    # contents → BundleContents
  mixins/ClientTextTooltipAccessor.java      # 原样
  utils/TextUtil.java
src/main/resources/
  META-INF/neoforge.mods.toml
  adaptive-tooltips.mixins.json         # compatibilityLevel JAVA_21
  assets/adaptivetooltips/lang/{en_us,ru_ru,zh_cn}.json
  icon.png
LICENSE / HEADER                        # 保留上游 LGPL 文本与文件头
```

## 3. 关键改造点

1. **modId 改为 `adaptivetooltips`**：Fabric 允许 `adaptive-tooltips`，NeoForge 的 modId 不允许连字符；资源命名空间本来就是 `adaptivetooltips`，改动面最小。
2. **入口**：`@Mod(value="adaptivetooltips", dist=Dist.CLIENT)`，构造器依次 `HANDLER.load()` → `OptionFactory.register(KeyCode.class, new KeyCode.KeyCodeImpl())` → `container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> HANDLER.generateGui().generateScreen(parent))`。
3. **配置路径**：`FMLPaths.CONFIGDIR.get().resolve("adaptive-tooltips.json")`，文件名不变，老配置文件可直接复用。
4. **YACL 位置器**：`YACLTooltipPositioner(ScreenRectangle)`；`WidgetTooltipHolderMixin` 用 `@ModifyReturnValue` 挂 `createTooltipPositioner`。
5. **滚动帧增量**：`Minecraft.getTimer().getRealtimeDeltaTicks()` 替代 `getDeltaFrameTime()`，平滑动画参数不变。
6. **Bundle 相等性**：`ClientBundleTooltipAccessor#getContents()` → `BundleContents.items()` 遍历 + `ItemStack.matches`，保持“忽略数量、比较物品+组件”的原语义。
7. **GameRenderer**：`@Inject(method="render", at=@At("RETURN"))`，handler `(DeltaTracker, boolean, CallbackInfo)`。
8. **Mixins JSON**：9 个 client mixin（`ClickableWidgetMixin` 换名 `WidgetTooltipHolderMixin`），`compatibilityLevel: JAVA_21`。
9. **mods.toml**：`modLoader="javafml"`、`license="LGPL-3.0-or-later"`、`[[mixins]] config="adaptive-tooltips.mixins.json"`；依赖 `neoforge [21.1,)`、`minecraft [1.21.1,1.21.2)`、`yet_another_config_lib_v3 [3.8,)`，`side="CLIENT"`。

## 4. 执行顺序（确认后）

1. **P2 脚手架**：`mcskill activate_platform_pack`（neoforge 1.21.1 规则树）→ `download_official_mdk`（ModDevGradle，先 dry-run 预览）→ 落盘 → 对齐到 21.1.218 / MDG 2.0.140 → `diagnose_gradle`。
2. **P5 编码**（每批可编译）：
   - 批 1：Gradle + mods.toml + 入口 + 配置 + YACL 接线；
   - 批 2：`TooltipWrapper` + 4 个 positioner + `TextUtil`；
   - 批 3：`GuiGraphicsMixin` / `TooltipRenderUtilMixin` / `TooltipMixin` / `DefaultTooltipPositionerMixin`；
   - 批 4：`MouseMixin` / `ScrollTracker` / `GameRendererMixin` / `WidgetTooltipHolderMixin`；
   - 批 5：资源、语言、许可证文件、清理。
3. **P6 校验**：`gradlew compileJava` → `validate_project` → `mixin_analyze`（deep）→ `gradlew build`；失败走 `analyze_build_log`。
4. **P7 验收**：`runClient` + 7 项功能清单逐项截图/记录；崩溃走 `crash_analyze`。

## 5. 验收清单（功能 1:1）

- [ ] 换行 5 模式：`OFF` / `SCREEN_WIDTH` / `REMAINING_WIDTH` / `HALF_SCREEN_WIDTH` / `SMART`
- [ ] 优先顶部（超屏 tooltip 从顶部开始）
- [ ] Bedrock 居中（含左右越界时的上下切换）
- [ ] 角落对齐（`bestCorner` / `alwaysBestCorner`）
- [ ] 关闭原版 170px 换行（`overwriteVanillaWrapping`）
- [ ] 滚动：垂直/水平、平滑动画、方向、灵敏度、Alt/Ctrl 组合键
- [ ] 透明度（背景+边框 alpha）
- [ ] 首行 2px padding 去除
- [ ] YACL 配置页可从 NeoForge Mods 界面打开并保存
- [ ] 不再声明 TooltipFix 冲突（NeoForge 侧无此模组）

## 6. 风险与应对

| 风险 | 应对 |
|---|---|
| `maven.neoforged.net` 不可达导致构建缺件 | 先 `--offline`；缺件时列出清单给你，不擅自改代理 |
| `WidgetTooltipHolder` 私有方法注入失败 | `mixin_analyze` 预检；失败则改用 `Screen#setTooltipForNextRenderPass` 侧的注入，行为等价 |
| `@ModifyConstant(2)` 误伤 | 1.21.1 中该方法只有两处 `2`（首行 padding），已核对；`mixin_analyze` 再验 |
| YACL 3.8.2 运行时行为差异（配置页/滚动） | 3.8.2 的更新日志明确修复了 1.21.9 以下滚动问题；本地 jar 为 1.21.1 专用构建 |
| 客户端专属加载 | `@Mod(dist=CLIENT)` + `side="CLIENT"` 依赖声明 |

## 7. 许可证与署名

- 1.3.0 为 **LGPL-3.0-or-later**（当前 main 已改 MPL-2.0，本移植以 1.3.0 为准）。
- 保留 `LICENSE`、源码文件头 `HEADER`、作者署名与仓库链接。
- 仅自用无额外义务；**若分发**，修改后的源码需以 LGPL-3.0-or-later 提供。
