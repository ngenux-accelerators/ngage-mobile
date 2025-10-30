package com.amazon.ivs.realtimecollab.core.handlers.stage

import com.amazon.ivs.realtimecollab.appContext
import com.amazon.ivs.realtimecollab.core.handlers.StageType
import com.amazonaws.ivs.broadcast.BroadcastConfiguration
import com.amazonaws.ivs.broadcast.CustomImageSource
import com.amazonaws.ivs.broadcast.Device
import com.amazonaws.ivs.broadcast.Device.Descriptor.DeviceType
import com.amazonaws.ivs.broadcast.Device.Descriptor.Position
import com.amazonaws.ivs.broadcast.DeviceDiscovery

internal object DeviceDiscoveryWrapper {
    private val _deviceDiscovery by lazy { DeviceDiscovery(appContext) }

    fun getImageSource(width: Float, height: Float): CustomImageSource? = _deviceDiscovery.createImageInputSource(
        BroadcastConfiguration.Vec2(width, height)
    )

    fun getDevice(deviceType: DeviceType, stageType: StageType, position: Position? = null): Device? {
        if (stageType == StageType.Viewer) return null
        val devices: List<Device> = _deviceDiscovery.listLocalDevices().sortedBy { it.descriptor.deviceId }
        return devices.find {
            it.descriptor.type == deviceType && (position == null || it.descriptor.position == position)
        }
    }
}
