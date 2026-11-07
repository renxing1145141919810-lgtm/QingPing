# 19.9 解锁入口与 RVA 记录

## 构建信息
- 游戏：Power Warriors
- 版本：19.9（由用户提供 APK 文件名确认）
- 引擎：Unity IL2CPP
- 架构：ARM64 / AArch64；libil2cpp.so 为 Android ELF64
- Metadata Version：分析日志显示 31；Il2CppDumper 曾提示文件可能受保护，随后按版本 29 完成导出。版本识别存在异常，需以原始二进制和工具输出复核。
- 当前证据：dump(1).cs、global-metadata.dat、script.zip、libil2cpp.so；本仓库不存放第三方原版 APK 或完整反编译资源。

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
对 0x8C2AE0 附近进行 AArch64 反汇编后，发现 UnlockAll() 的主体最终调用 PlayerPrefs.SetInt(string key, int value)，传入的 value 为 1。也就是说，它写入的是一个单独的整数偏好项，而不是在该函数中逐个遍历角色、Saga 和 Mission。该 key 的实际字符串内容尚未从静态数据中可靠解析，因此不能把这个方法直接当作“解锁全部内容”的实现。

0x8C29DC 的 ResetAllSagaLevelsUnlockStatus() 则出现了循环，并调用 SetSagaLevel(sagaID, levelID, 0) 一类写入路径；不要调用它来实现解锁。

### 解释边界
- ProfileManager 确实提供独立的角色、Saga、Mission、Challenge 状态接口。
- UnlockAll() 目前已确认是设置单个 PlayerPrefs 整数键为 1；其键名和游戏逻辑用途仍待确认。
- dump.cs 不含函数体；单凭方法名不能推断覆盖范围。
- ResetAll() 可能影响进度，不能在未反汇编前调用或修改。

## 商店相关入口
| 功能 | 方法 | RVA |
|---|---|---:|
| 显示解锁状态 | StoreSceneManager.DisplayUnlockAllStatus() | 0xBD1E6C |
| 显示已解锁角色 | DisplayUnlockedCharacters() | 0xBD1FF0 |
| 购买角色 | BuyCharacterPressed(GameObject) | 0xBD2708 |
| 确认购买 | ConfirmBuyCharacterPressed(GameObject) | 0xBD2CDC |
| 全部解锁按钮 | UnlockAllButtonPressed() | 0xBD3194 |
| 刷新商店状态 | RefreshStatus() | 0xBD32D4 |

对 0xBD3194 的初步反汇编显示其调用 AudioManager.PlayAudio；不能仅凭方法名认定它直接完成了解锁。需要继续检查函数边界、后续调用和相关 UI 事件。

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
1. 解析 UnlockAll() 使用的 PlayerPrefs key，并追踪该 key 的读取位置，确定它代表的真实功能。
2. 反汇编 UnlockCharacter、UnlockSagaLevel、UnlockMissionLevel 和 Challenge 写入路径，确认数据结构及参数范围。
3. 检查 StoreSceneManager 的按钮事件绑定和完整函数体，区分音效、广告、付费与本地状态逻辑。
4. 追踪 Tournament / Battle Survival / Challenge 的解锁判定与存档键。
5. 只有确认数据结构与写入路径后，才制作可回滚的本地补丁。

## 回滚与测试要求
- 修改前复制原 APK 和游戏存档。
- 每次只改一个逻辑点，保留原始文件与补丁差异。
- 先在离线/本地环境验证，不上传修改版游戏 APK。
- 验证角色选择、Saga、Mission、Challenge、Tournament 页面均能正常加载；同时确认通关记录与设置未被清空。