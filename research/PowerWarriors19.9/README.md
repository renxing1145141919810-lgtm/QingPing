# Power Warriors 19.9 本地解锁研究

本目录记录对用户提供的 Power Warriors 19.9 Android APK 的静态分析。目标是研究本地单机存档/解锁状态，不分发原版游戏资源。

## 目标
- 解锁可解锁角色
- 解锁 Story / Saga 关卡
- 解锁 Missions 关卡
- 确认并处理 Arena 相关内容（目前尚未定位到明确的 Arena 解锁入口）

## 当前进度
- [x] 确认目标为 Unity IL2CPP 游戏
- [x] 从 DummyDll / dump.cs 确认 ProfileManager 的角色、Saga、Mission、Challenge 状态接口
- [x] 记录关键 RVA
- [ ] 从原始 libil2cpp.so 核对函数机器码与调用关系
- [ ] 明确 UnlockAll() 的实际覆盖范围
- [ ] 定位竞技场/锦标赛等模式的解锁条件
- [ ] 制作仅供本地测试的补丁
- [ ] 在目标设备验证，并确认原始存档可恢复

## 重要说明
dump.cs 是 IL2CPP 元数据导出的声明清单。方法体显示为 { }，不包含真实实现。因此，方法名叫 UnlockAll 并不能单凭名称证明它会解锁全部角色、所有剧情关卡和竞技场。必须继续检查对应版本的 libil2cpp.so，并在修改前备份原 APK 与存档。

当前没有生成或上传修改版 APK。不要把此目录中的 RVA 当作可直接套用到其他游戏版本的固定偏移。