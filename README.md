# DJI SDK 模块分析


## 已成功删除的模块 ✅

### 1. UX SDK 模块（约 30MB）✅
- 地图库（MapLibre）
- 动画库（Lottie）
- Google Play Services
- 各种图标库

### 2. 视频编解码模块（FFmpeg，约 16MB）✅
- `libavcodec.so` - 10.84 MB
- `libavfilter.so` - 2.18 MB
- `libavformat.so` - 1.96 MB
- `libavutil.so` - 0.33 MB
- `libavresample.so` - 0.06 MB
- `libavdevice.so` - 0.05 MB
- `libswscale.so` - 0.43 MB
- `libswresample.so` - 0.08 MB

### 3. 流媒体/直播模块（约 13MB）✅
- `libmrtc_onvif.so` - 8.55 MB
- `libmrtc_rtmp.so` - 1.80 MB
- `libmrtc_rtsp.so` - 0.91 MB
- `libmrtc_core.so` - 0.62 MB
- `libmrtc_data.so` - 0.38 MB
- `libmrtc_core_jni.so` - 0.36 MB
- `libmrtc_28181.so` - 0.33 MB
- `libmrtc_agora.so` - 0.13 MB
- `libmrtc_webrtc.so` - 0.04 MB
- `libmrtc_log.so` - 0.01 MB

### 4. 云服务/网络模块（约 35MB）✅
- `libcloud_access_jni.so` - 19.83 MB
- `libPPAL.so` - 13.93 MB
- `libppal-jni.so` - 1.52 MB

### 5. Agora RTSA SDK（约 7.4MB）✅
- `libagora-rtsa-sdk.so` - 7.43 MB

**已删除总计：约 101MB**

---

## 当前剩余的模块

### 1. 核心 SDK 模块（必需，约 92MB）
- `libdjisdk_jni.so` - 65.04 MB ⭐ **最大**
- `libdjibase.so` - 25.28 MB
- `libDJIRegister.so` - 1.00 MB
- `libDJICSDKCommon.so` - 0.17 MB
- `libconstants.so` - 0.25 MB
- `libc++_shared.so` - 1.01 MB


### 3. 飞控/安全模块（约 27MB）❌ **测试失败，不能删除**
- `libDJIFlySafeCore-CSDK.so` - 10.41 MB
- `libDJIUpgradeCore.so` - 8.27 MB
- `libDJIWaypointV2Core-CSDK.so` - 3.41 MB
- `libFlightRecordEngine.so` - 2.38 MB
- `libDJIUpgradeJNI.so` - 1.93 MB
- `libdjifs_jni-CSDK.so` - 0.96 MB
- `libdjiwpv2-CSDK.so` - 0.21 MB

### 4. 视频处理模块（约 0.7MB）❓ **部分可删除**
- `libwaes.so` - 0.06 MB
- `libDJIOpus.so` - 0.20 MB
- `libopus.so` - 0.40 MB
- ~~`libagora-rtsa-sdk.so` - 7.43 MB~~ ✅ **已删除**

### 5. RTK 定位模块（约 0.2MB）❓ **未测试**
- `librtcm.so` - 0.20 MB

### 6. 数据/存储模块（约 15MB）❓ **未测试**
- `libdataclx.so` - 8.89 MB
- `libsqlcipher.so` - 3.36 MB
- `libDJIProtobuf.so` - 2.32 MB
- `libDJIFileSystem.so` - 0.80 MB
- `libhash.so` - 0.22 MB

### 7. 其他工具模块（约 7MB）❓ **未测试**
- `libwpmz_jni.so` - 6.23 MB
- `libSdkyclx_clx.so` - 0.84 MB
- `libdcl_jni.so` - 0.19 MB
- `libartcm_jni.so` - 0.12 MB
- `libxcrash_dumper.so` - 0.11 MB
- `libxcrash.so` - 0.07 MB
- `libntp_client.so` - 0.01 MB

---

## 测试结果总结

### ✅ 可以删除的模块
1. **UX SDK 模块** - 约 30MB（已删除）
2. **视频编解码模块（FFmpeg）** - 约 16MB（已删除）
3. **流媒体/直播模块** - 约 13MB（已删除）
4. **云服务/网络模块** - 约 35MB（已删除）
5. **Agora RTSA SDK** - 约 7.4MB（已删除）

### ❌ 不能删除的模块（会导致崩溃）
1. **飞控/安全模块** - 约 27MB（被核心模块依赖）
2. **视频处理模块（部分）** - 约 0.7MB（`libwaes.so`、`libDJIOpus.so`、`libopus.so` 不能删除）
3. **其他工具模块** - 约 7MB（被核心模块依赖）
4. **数据/存储模块** - 约 15MB（被核心模块依赖）

### ❓ 未测试的模块（可以尝试删除）
1. **RTK 定位模块** - 约 0.2MB

---



