package org.bigleg.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private WiFiDirectActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WiFiDirectActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    //WifiP2pManager.discover成功后调用
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //当WifiP2p状态发生变化时触发（如果WifiP2p可用，那么当BroadcastReceiverregister时，也会收到该广播）
            // 检查是否支持WIFI直连
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mActivity.setIsWifiP2pEnabled(true);
            } else {
                mActivity.setIsWifiP2pEnabled(false);
                mActivity.resetData();
            }
            Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //异步方法，获取可用的peers列表，DeviceListFragment实现了PeerListListener接口，完成时会调用DeviceListFragment的onPeersAvailable方法(点击连接）(连接成功)
            //当WifiP2p扫描时，发现device列表发生变化时，触发该广播。该广播不含extra，开发者应该接收到此广播后，调用WifiP2pManager.requestPeers()函数查询当前设别列表
            if (mManager != null) {
                mManager.requestPeers(mChannel, (PeerListListener) mActivity.getFragmentManager().findFragmentById(R.id.frag_list));
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {//(连接成功）
            /*当WifiP2p的group发生变化时，触发该广播。
            该广播包含两个extra：
            key：WifiP2pManager.EXTRA_NETWORK_INFO,其值为NetworkInfo类型。
            key：WifiP2pManager.EXTRA_P2P_INFO,其值为WifiP2pInfo类型。
            A third extra provides the details of the group.
            PS：这里的WifiP2p group发生变化包含如下情况：
            1. 建立group
            2. member加入到group
            3. member退出group
            4. 关闭group*/
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                //获取群主信息
                GroupDeviceListFragment fragment = (GroupDeviceListFragment) mActivity.getFragmentManager().findFragmentById(R.id.grp_list);
                mManager.requestConnectionInfo(mChannel, fragment);
                //DONE 看看下面直接获取的groupInfo能不能获取到所有的Client列表
                //下面的方法只能获取到当前设备连接的组内的成员
                mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                        if(wifiP2pGroup.isGroupOwner()){
                            Collection<WifiP2pDevice> groupCol = wifiP2pGroup.getClientList();
                            List<WifiP2pDevice> groupList = new ArrayList<>();
                            groupList.addAll(groupCol);
                            //将组长加入组内
                            groupList.add(WiFiDirectActivity.ThisDevice);
                            mActivity.updateGroupFragmentWithDeviceList(groupList);
                        }
                    }
                });
            } else {
                mActivity.resetData();
            }
//            try {
//                WifiP2pInfo p2pInfo = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
//                String groupAddr = p2pInfo.groupOwnerAddress.getAddress().toString();
//                System.out.println(groupAddr);
//            }catch (Exception ex){}
//            WifiP2pGroup groupInfo = (WifiP2pGroup) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
//            WifiP2pDevice owner = groupInfo.getOwner();
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //intent action indicating that this device details have changed.
            DeviceListFragment fragment = (DeviceListFragment) mActivity.getFragmentManager().findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            WiFiDirectActivity.HostName = ((WifiP2pDevice) ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE))).deviceName;
            //WiFiDirectActivity.HostIsGroupOwner = String.valueOf(((WifiP2pDevice) ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE))).isGroupOwner());
        }
    }
}