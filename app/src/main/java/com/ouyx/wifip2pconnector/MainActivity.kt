package com.ouyx.wifip2pconnector

import android.Manifest
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ouyx.lib_wifip2p_connector.facade.listener.PeerDevicesListener
import com.ouyx.lib_wifip2p_connector.launch.WiFiP2PConnector
import com.ouyx.wifip2pconnector.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val mWifiP2pDeviceList = mutableListOf<WifiP2pDevice>()

    private val mDeviceAdapter = DeviceAdapter(mWifiP2pDeviceList)


    private val requestedPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WiFiP2PConnector.get().init(application)

        viewBinding.butSearch.setOnClickListener {
            requestPermission(requestedPermissions, agree = {
                search()
            }, disAgree = {
                DefaultLogger.error("权限不够 $it")
            })
        }

        viewBinding.recy.apply {
            adapter = mDeviceAdapter
            layoutManager = object : LinearLayoutManager(this@MainActivity) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        }
        mDeviceAdapter.onItemClickListener =object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val wifiP2pDevice = mWifiP2pDeviceList.getOrNull(position)
                if (wifiP2pDevice != null) {
                    connect(wifiP2pDevice = wifiP2pDevice)
                }
            }
        }

        viewBinding.butDisConnect.setOnClickListener {
            WiFiP2PConnector.get().disConnect()
        }
    }

    private fun connect(wifiP2pDevice: WifiP2pDevice) {
        WiFiP2PConnector.get().connect(wifiP2pDevice.deviceAddress)
    }

    private fun search() {
        WiFiP2PConnector.get().searchDevices {
            onSearchActionFail {
                DefaultLogger.warning("搜索操作失败 $it")
            }
            onSearchActionSuccess {
                DefaultLogger.warning("搜索操作成功")
            }
        }

        WiFiP2PConnector.get().setPeerListListener(object : PeerDevicesListener {
            override fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>) {
                mWifiP2pDeviceList.clear()
                mWifiP2pDeviceList.addAll(wifiP2pDeviceList)
                mDeviceAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        WiFiP2PConnector.get().close()
    }
}