# 移植验收方案（04）

> 目的：判断「移植是否做到了」——标准不是“能启动”，而是**与上游 1.3.0 行为一致**。
> 方法：双端对照（金标准）+ 逐项判定 + 边界用例 + 证据留存。

## 0. 三层判定

| 层        | 检查什么                                             | 通过标准                                          |
| --------- | ---------------------------------------------------- | ------------------------------------------------- |
| L0 加载层 | Mod 列表、版本、依赖、配置加载、Mixin 应用、日志     | 无 ERROR/崩溃；9 个 Mixin 全部应用；17 项配置齐全 |
| L1 功能层 | 7 大功能逐项行为（见 §3）                            | 每项与参考端一致（几何/换行点/翻转逻辑）          |
| L2 边界层 | 窗口缩放、GUI Scale、CJK、bundle、超长单行、边缘槽位 | 不崩溃、不越界、不遮挡鼠标关键区域                |

## 1. 双端对照环境（金标准）

| 端     | MC              | 加载器             | 依赖                                                  |
| ------ | --------------- | ------------------ | ----------------------------------------------------- |
| 参考端 | 1.20.1 / 1.20.2 | Fabric             | Fabric API + YACL 3.2.1 + Adaptive Tooltips **1.3.0** |
| 移植端 | 1.21.1          | NeoForge 21.1.218+ | YACL 3.8.2 + 本移植 jar                               |

对照要求：

- 同 GUI Scale（建议 2）、同窗口尺寸、同语言、同字体资源包；
- 同物品、同槽位、同鼠标相对位置；
- 截图后裁剪到 tooltip 区域，用看图工具 50% 透明度叠加对比。

允许差异：1.20.2 → 1.21.1 字形度量可能带来 ≤2px 的宽度差；**换行点、最大宽度、上下翻转、滚动边界必须一致**。

## 2. 测试素材

- 长名称 / 长 lore 的物品（直接用整合包里 tooltip 很长的 modded 物品最省事）；
- 大量附魔/描述的 tooltip（附魔书、装备）；
- 装满的收纳袋（bundle tooltip）；
- 屏幕最左/最右/最上/最下的物品栏槽位；
- GUI Scale 1 / 2 / 3 / 4；
- 窗口 1280×720、1920×1080、超宽屏；
- 中文 + emoji + 中英混排的长文本。

### 2.1 最快：命令生成（需作弊/创造）

在单人存档开启作弊，或对局域网世界开放作弊后执行。`custom_name` 是任意长度（不受铁砧 50 字符限制），`lore` 每项一行。

长单行 + 多行高度（换行 / SMART / 优先顶部 / 滚动都用它）：

```mcfunction
/give @s minecraft:diamond_sword[custom_name="这是一把名字特别特别长的测试剑用于验证自动换行和最大宽度限制一二三四五六七八九十",lore=["第二行说明文字，用来把提示框撑高一点。","第三行：短。","第四行：这是一段明显更长的文字，用来观察 SMART 模式如何按行宽差选择换行位置。","第五行：短。","第六行：又是一段很长的文字，继续测试换行。","第七行：短。","第八行：中等长度的文字。","第九行：短。","第十行：最后一行。","第十一行：额外一行。","第十二行：再额外一行。","第十三行：继续。","第十四行：继续。","第十五行：结束。"]] 1
```

宽字符（中文）换行：

```mcfunction
/give @s minecraft:paper[custom_name="宽字符测试壹贰叁肆伍陆柒捌玖拾佰仟万亿兆京垓秭穰沟涧正载极",lore=["短行","这是一段很长的中文说明文字，用于测试中文换行是否按字符正确切分，并且不会越出屏幕边界。","短行"]] 1
```

窄字符（英文）换行：

```mcfunction
/give @s minecraft:stick[custom_name="WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW",lore=["iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii","中英混排 mixed 文本 1234567890 测试"]] 1
```

> 语法依据：Minecraft Wiki `/give` 的 `lore=["Example"]` 示例（1.20.5+ 数据组件语法）。若你的版本对字符串组件解析报错，改用 JSON 形式：`custom_name='{"text":"..."}'`、`lore=['{"text":"..."}']`。

### 2.2 不用命令

- **铁砧改名**：任意物品改成 30–50 个中文字符（铁砧上限 50），足以触发换行；
- **附魔书叠 tooltip 行数**：创造模式取 6–8 本不同附魔书，在铁砧里两两合并，行数越多 tooltip 越高（测优先顶部/滚动）；
- **装满的潜影盒**：27 格塞不同物品，tooltip 会列出内容（多行）；
- **锻造模板**：原版自带多行说明；
- **整合包里的 modded 物品**：Apotheosis 词缀、Enchantment Descriptions 等通常自带长 tooltip，最省事。

## 3. 逐项判定表（L1）

| #   | 配置/功能                         | 操作                              | 预期（来自 1.3.0 算法）                                                     |
| --- | --------------------------------- | --------------------------------- | --------------------------------------------------------------------------- |
| 1   | `wrapText=SCREEN_WIDTH`           | 长 tooltip 放在屏幕右侧           | 最大行宽 = 屏宽 − 15；tooltip 不越出屏幕                                    |
| 2   | `REMAINING_WIDTH`                 | 同上，分别在左/右半屏             | 右侧：屏宽 − x − 15；若仍超宽则改用左侧剩余宽度                             |
| 3   | `HALF_SCREEN_WIDTH`               | 长 tooltip                        | 最大行宽 = 屏宽 / 2                                                         |
| 4   | `SMART`                           | 单行超长 / 多行长短不一           | 单行：上限 3/4 屏宽；多行：按相邻行宽差 ≤100px 的位置切分，硬上限 3/4 屏宽  |
| 5   | `OFF`                             | 超长单行                          | 完全不换行（允许超出屏幕）                                                  |
| 6   | `prioritizeTooltipTop`            | tooltip 高度 > 屏幕高度           | 顶部对齐 y=4，而不是底部贴边                                                |
| 7   | `bedrockCentering`                | 物品在屏幕左右边缘                | 水平居中于鼠标（clamp 6 ~ 屏宽−宽−6）；上方放不下时切换到下方（比较遮挡量） |
| 8   | `bestCorner` / `alwaysBestCorner` | tooltip 触边                      | 选“遮挡鼠标最少”的角落，边距 5px                                            |
| 9   | `preventVanillaClamping`          | 右侧长 tooltip                    | 关闭原版 `Math.max(...,4)` 的水平夹取                                       |
| 10  | `onlyRepositionHoverTooltips`     | 非悬浮位置器（如菜单 tooltip）    | 开启时只对 `DefaultTooltipPositioner` 重定位                                |
| 11  | `overwriteVanillaWrapping`        | 带 `Tooltip` 的控件（如按钮提示） | 跳过原版 170px 换行                                                         |
| 12  | 滚动                              | 按住 Alt 滚轮；Ctrl+Alt 横向      | 垂直/水平滚动；平滑动画；方向/reverse；灵敏度 5–20；切换物品后重置          |
| 13  | `tooltipTransparency`             | 调到 0.5                          | 背景与边框 alpha 减半，文字不透明                                           |
| 14  | `removeFirstLinePadding`          | 开关                              | 首行 2px 额外间距消失/恢复                                                  |
| 15  | `useYACLTooltipPositioner`        | 悬浮 GUI 按钮                     | 以控件中心定位，优先上方，放不下切下方，x clamp −4 ~ 屏宽−宽−4              |
| 16  | 配置页                            | Mods → Adaptive Tooltips → Config | 17 项全部可见、可改、保存后重进仍生效                                       |
| 17  | bundle 相等性                     | 两个内容相同的收纳袋之间切换      | 滚动位置保持；内容不同则重置                                                |
| 18  | 窗口缩放                          | 打开 tooltip 时拖动窗口           | 不崩溃，位置/换行实时正确                                                   |
| 19  | GUI Scale 切换                    | 1→3→4                             | 换行与定位按新屏宽计算                                                      |

## 4. L0 快速检查（自动化/半自动）

```powershell
# 1) 产物存在且版本正确
Get-ChildItem "D:\Lv\ModPort\ports\adaptive-tooltips\build\libs"

# 2) 日志：Mixin 应用 + 无错误（把 <实例> 换成整合包目录）
Select-String -Path "<实例>\logs\latest.log" -Pattern "adaptive-tooltips.mixins.json|Mixin apply failed|InvalidInjectionException|ERROR"
```

预期：

- 日志出现 `adaptive-tooltips.mixins.json:GuiGraphicsMixin`、`TooltipMixin`、`MouseMixin`、`GameRendererMixin`、`WidgetTooltipHolderMixin` 等应用行；
- 无 `Mixin apply failed` / `InvalidInjectionException` / `ERROR`；
- `config/adaptive-tooltips.json` 含 17 个键，默认值与 `docs/porting/00-inventory.md` §5 一致；
- Mods 列表显示 `Adaptive Tooltips 1.3.0+1.21.1`。

## 5. 5 分钟冒烟（先做这个）

1. 启动到主菜单 → Mods 列表能看到模组、能打开配置页；
2. 进创造世界，拿一个长 tooltip 物品：
   - 切 `SCREEN_WIDTH` / `OFF`，看是否变宽/不再换行；
   - 把物品移到屏幕最右，看是否仍完整显示；
3. 按住 Alt 滚轮，tooltip 内容滚动；
4. 透明度调 0.5，背景变淡；
5. 退出重进，配置保持。

全部通过 → 再做 §3 全表；任一项失败 → 记录「配置 + 操作 + 预期 + 实际 + 截图 + 日志」。

## 5.1 两个容易误判的点

- **聊天框 256 字符上限**：超长 `/give` 会被截断，典型报错是“字符串引号不成对”。长命令请用命令方块或数据包函数。
- **lore 纯数字**：`"10"`…`"30"` 会被解析成数字而不是文本组件，报“错误的 minecraft:lore 组件”。给每行加非数字前缀（如 `"a10"`）。
- **滚动**：只有 tooltip 高度 **大于屏幕 GUI 高度** 时才生效（GUI Scale 2 时 35 行左右）；且默认只对悬停型 tooltip 生效。配置界面按钮是“菜单型”定位器，需把“仅重定位悬停工具提示”关掉才会滚动/居中。
- **基岩版居中**：本移植的触发条件是 `x < 4` **或**右侧会溢出（`鼠标X + 12 + 宽度 > 屏宽`）；上游 1.3.0 只有前者。用中等宽度 tooltip 靠右悬浮即可看到居中，不再需要“宽到接近满屏”。

## 6. 证据与判定

- 截图命名：`at/<功能>/<before|after|on|off>.png`；
- 日志：`logs/latest.log`（崩溃则加 `crash-reports/`）；
- 判定：L0 全过 + L1 全过 + L2 无崩溃 = 通过；允许差异需在验收记录中写明。

## 7. 可选的自动化

- **测试数据包**：生成一批带超长名称/lore、不同文本宽度、中英混排的物品，免去手动找素材；
- **in-JVM playtest driver**：自动进世界、打开背包、悬浮指定槽位、截图并断言（需要额外的驱动工程，约 1–2 小时搭建）。

两者都可以按需生成，不影响当前手工验收。
