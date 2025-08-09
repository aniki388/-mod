# 三国杀 Fabric Mod

## 项目简介

这是一个基于 **Minecraft Fabric 1.20.1** 开发的“三国杀”主题 Mod，
将经典桌游《三国杀》的玩法引入 Minecraft，玩家可以使用“杀、闪、桃、酒”等卡牌，
以及“无懈可击”“无中生有”等锦囊牌，甚至体验 BOSS 魏延等特殊武将的技能。
该 Mod 支持单人游戏与多人联机，并包含完整的战斗逻辑与特效。

## 功能特性

- **卡牌系统**：杀、闪、桃、酒等基础牌，顺手牵羊、过河拆桥等锦囊牌。
- **武将技能**：如无双、烈弓、苦肉、狂骨等技能机制。
- **BOSS 模式**：魏延等特殊 BOSS 行为与 AI。
- **动画与音效**：技能触发特效、音效提示。
- **多人交互**：支持多人对战、无懈可击链式响应等互动机制。

## 文档说明

目前本项目的 Javadoc 已覆盖核心逻辑类：

- `GeneralEntity.java`
- `CardGameManager.java`

其余类将在后续版本中逐步补充文档说明。

## 环境需求

- Minecraft **1.20.1**
- [Fabric Loader](https://fabricmc.net/) **>= 0.14.x**
- [Fabric API](https://modrinth.com/mod/fabric-api)

## 运行方法

1. 安装 Minecraft 1.20.1 客户端。
2. 安装 Fabric Loader 与 Fabric API。
3. 将本项目构建生成的 `sgsmod-x.x.x.jar` 文件放入 Minecraft 的 `mods` 文件夹。
4. 启动 Minecraft 并在 Mod 列表中确认已加载。

## 构建方法

本项目使用 **Gradle** 构建：

```bash
# 克隆项目
git clone https://github.com/你的用户名/sgsmod.git
cd sgsmod

# 构建
./gradlew build

# 构建完成后，在 build/libs/ 下找到 jar 文件
```

构建成功后，会在 `build/libs/` 目录下生成 `.jar` 文件，将其放入 Minecraft `mods` 文件夹即可使用。

## 贡献

欢迎提交 Issue 或 Pull Request 来完善此项目。  
如需重大改动，请先开启 Issue 讨论。

## 协议

本项目采用 **MIT License**，详细信息请见 [LICENSE](LICENSE) 文件。
