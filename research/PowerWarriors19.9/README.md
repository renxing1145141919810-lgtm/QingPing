# Power Warriors 19.9 本地解锁研究

本目录记录对用户提供的 Power Warriors 19.9 Android IL2CPP 文件的静态分析。目标是研究本地单机存档/解锁状态，不分发原版游戏资源。

## 目标
- 解锁可解锁角色
- 解锁 Story / Saga 关卡
- 解锁 Missions 关卡
- 确认并处理 Arena / Tournament 相关内容（目前尚未定位到明确的竞技场解锁字段）

## 当前进度
- [x] 确认 Unity IL2CPP、ARM64 / AArch64
- [x] 从 dump.cs 确认 ProfileManager 的角色、Saga、Mission、Challenge 状态接口
- [x] 记录关键 RVA
- [x] 反汇编核对 ProfileManager.UnlockAll：它设置 SPrefs 的 UnlockAll 键为 1，并非逐项遍历全部角色和关卡
- [x] 确认 HomeSceneManager.UnlockAllPressed 会调用该标志写入入口
- [ ] 追踪 UnlockAll 标志的读取点及实际影响范围
- [ ] 反汇编角色、Saga、Mission、Challenge 的状态写入逻辑
- [ ] 定位竞技场 / Tournament 的真实解锁条件
- [ ] 获取原始 APK，制作可回滚的本地补丁
- [ ] 在目标设备验证，并确认原始存档可恢复

## 重要说明
dump.cs 只提供元数据声明，不包含方法体；当前结论以同一分析样本的 ARM64 libil2cpp.so 反汇编和 script.json 地址映射为补充。RVA 不能直接套用到其他游戏版本。

当前工作目录有 libil2cpp.so、global-metadata.dat 和 script.json，但没有原始 APK。因此尚不能重打包或签名生成可安装 APK。