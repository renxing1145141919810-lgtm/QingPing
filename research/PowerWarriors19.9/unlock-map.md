# 19.9 解锁入口与 RVA 记录

## 构建信息
- 游戏：Power Warriors
- 版本：19.9（由用户提供 APK 文件名确认）
- 引擎：Unity IL2CPP
- 架构：ARM64 / AArch64；libil2cpp.so 为 Android ELF64
- Metadata Version：分析日志显示 31；Il2CppDumper 曾提示文件可能受保护，随后按版本 29 完成导出。版本识别存在异常，需以原始二进制和工具输出复核。
- 当前证据：dump(1).cs、global-metadata.dat、script.zip、libil2cpp.so；当前工作目录没有原始 APK。

## ProfileManager
以下 RVA 来自 dump.cs，并已在当前 libil2cpp.so 中核对关键位置：

| 功能 | 方法 | RVA |
|---|---|---:|
| 解锁角色 | ProfileManager.UnlockCharacter(int characterID) | 0x8C21C4 |
| 查询角色状态 | GetCharacterUnlockStatus(int characterID) | 0x8BC23C |
| 获取角色状态列表 | GetCharactersUnlockStatus() | 0x8C2230 |
| 重置角色状态 | ResetCharactersUnlockStatus() | 0x8C23F0 |
| 查询 Saga 关卡状态 | GetSagaLevelUnlockStatus(int sagaID, int levelID) | 0x8C2580 |
| 解锁 Saga 关卡 | UnlockSagaLevel(int sagaID, int levelID) | 0x8C2720 |
| 查询 Mission 关卡状态 | GetMissionLevelUnlockStatus(int missionID, int levelID) | 0x8C2730 |
| 解锁 Mission 关卡 | UnlockMissionLevel(int missionID, int levelID) | 0x8C27D0 |
| 查询 Challenge 等级 | GetChallengeLevel(int challengeID) | 0x8BF740 |
| 设置 Challenge 等级 | SetChallengeLevel(int challengeID, int level) | 0x8C2884 |
| 重置 Saga 解锁状态 | ResetAllSagaLevelsUnlockStatus() | 0x8C29DC |
| 名为 UnlockAll 的入口 | UnlockAll() | 0x8C2AE0 |
| 重置全部状态 | ResetAll() | 0x8C2B28 |

### 已完成的机器码核对
script.json 将 0x8C2AE0 标记为 ProfileManager.UnlockAll。AArch64 反汇编显示，该函数把字符串键 UnlockAll 和整数 1 传给 SPrefs.SetInt（RVA 0x843C84）。因此它设置的是一个单独的偏好/存档标志，而不是在此函数内逐个遍历角色、Saga 和 Mission。它可能代表商店的“全部解锁”购买状态，但尚未确认所有读取点和具体覆盖内容。

SPrefs 类包含独立的字符串、整数、浮点数和布尔值存取函数，并定义了混淆存储盐值。不要把它简单等同于 Unity PlayerPrefs 的明文键值存储。

0x8C29DC 的 ResetAllSagaLevelsUnlockStatus() 则出现了循环，并调用 SetSagaLevel(sagaID, levelID, 0) 一类写入路径；不要调用它来实现解锁。

### 解释边界
- ProfileManager 确实提供独立的角色、Saga、Mission、Challenge 状态接口。
- UnlockAll() 已确认设置 SPrefs 中的 UnlockAll 键为 1；该标志是否直接控制全部角色和关卡仍待追踪。
- GetUnlockStatus()（0x8C2BCC）当前反汇编表现为直接返回 0；需结合调用点和方法元数据进一步确认其实际用途。
- dump.cs 不含函数体；单凭方法名不能推断覆盖范围。
- ResetAll() 可能影响进度，不能在未反汇编前调用或修改。

## 商店与主界面入口
| 功能 | 方法 | RVA |
|---|---|---:|
| 显示解锁状态 | StoreSceneManager.DisplayUnlockAllStatus() | 0xBD1E6C |
| 显示已解锁角色 | DisplayUnlockedCharacters() | 0xBD1FF0 |
| 购买角色 | BuyCharacterPressed(GameObject) | 0xBD2708 |
| 确认购买 | ConfirmBuyCharacterPressed(GameObject) | 0xBD2CDC |
| 商店全部解锁按钮 | UnlockAllButtonPressed() | 0xBD3194 |
| 刷新商店状态 | RefreshStatus() | 0xBD32D4 |
| 主界面 UnlockAll 按钮 | HomeSceneManager.UnlockAllPressed() | 0xBC4324 |

主界面 UnlockAllPressed 的反汇编会调用 ProfileManager.UnlockAll，因此能确认该标志有明确的 UI 入口。商店按钮函数仍需继续检查完整逻辑，不能仅凭名称认定它直接完成角色解锁。

## 竞技场 / Tournament
目前从 dump.cs 已看到以下 Tournament 配置/状态接口：
- GetTournamentRandomCPU() / SetTournamentRandomCPU(bool)
- GetTournamentCPUOnly() / SetTournamentCPUOnly(bool)
- GetTournamentNumOfPlayers() / SetTournamentNumOfPlayers(int)
- GetTournamentScore() / SetTournamentScore(int)
- GetTournamentRewardCompleted(int tournamentCharID)
- SetTournamentRewardCompleted(int tournamentCharID, bool completed)

这些名称说明存在 Tournament 设置与奖励状态，但尚不能证明有“竞技场解锁”字段。下一步应从模式选择 UI、Tournament 奖励流程及 ProfileManager 相关存档字段追踪，不应把所有竞技模式一概视为 Arena。

## 下一步分析顺序
1. 追踪 SPrefs.GetInt / GetBool 对 UnlockAll 键的读取点，确认该标志实际影响哪些菜单和角色。
2. 反汇编 UnlockCharacter、UnlockSagaLevel、UnlockMissionLevel 和 Challenge 写入路径，确认数据结构及参数范围。
3. 检查 StoreSceneManager 的按钮事件绑定和完整函数体，区分音效、广告、付费与本地状态逻辑。
4. 追踪 Tournament / Battle Survival / Challenge 的解锁判定与存档键。
5. 只有确认数据结构与写入路径后，才制作可回滚的本地补丁。

## 回滚与测试要求
- 修改前复制原 APK 和游戏存档。
- 每次只改一个逻辑点，保留原始文件与补丁差异。
- 先在离线/本地环境验证，不上传修改版游戏 APK。
- 验证角色选择、Saga、Mission、Challenge、Tournament 页面均能正常加载；同时确认通关记录与设置未被清空。