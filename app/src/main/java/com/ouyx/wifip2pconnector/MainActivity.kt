package com.ouyx.wifip2pconnector

import android.Manifest
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ouyx.lib_wifip2p_connector.facade.data.PeerDevice
import com.ouyx.lib_wifip2p_connector.facade.listener.PeerChangedLsistener
import com.ouyx.lib_wifip2p_connector.launch.WiFiP2PConnector
import com.ouyx.wifip2pconnector.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val mPeerList = mutableListOf<PeerDevice>()

    private val mDeviceAdapter = DeviceAdapter(mPeerList)


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
                val peerDevice = mPeerList.getOrNull(position)
                if (peerDevice != null) {
                    connect(wifiP2pDevice = peerDevice)
                }
            }
        }

        viewBinding.butDisConnect.setOnClickListener {
            WiFiP2PConnector.get().disConnect()
        }
    }

    private fun connect(wifiP2pDevice: PeerDevice) {
        WiFiP2PConnector.get().connect(wifiP2pDevice.deviceAddress)
    }

    private fun search() {
        mPeerList.clear()
        mDeviceAdapter.notifyDataSetChanged()

        WiFiP2PConnector.get().searchDevices {
            onSearchFail {
                DefaultLogger.warning("搜索操作失败 $it")
            }
            onSearchStart {
                DefaultLogger.warning("开始搜索")
            }
            onSearchSuccess {
                DefaultLogger.warning("搜索成功 $it")

                mPeerList.addAll(it)
                mDeviceAdapter.notifyDataSetChanged()
            }
        }

        WiFiP2PConnector.get().setPeerListListener(object : PeerChangedLsistener {
            override fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>) {

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        WiFiP2PConnector.get().close()
    }
}