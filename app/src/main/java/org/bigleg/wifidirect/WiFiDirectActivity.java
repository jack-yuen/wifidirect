package org.bigleg.wifidirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.bigleg.wifidirect.DeviceListFragment.DeviceActionListener;

public class WiFiDirectActivity extends Activity implements WifiP2pManager.ChannelListener, DeviceActionListener{

    public static final String TAG = "基于WIFI直连的群组文件共享";
    WifiP2pManager mManager;
    private boolean isWifiP2pEnabled = false;

    private final IntentFilter mIntentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver = null;

    private IPReceiver mIpReceiver = null;

    static private int openfileDialogId = 0;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //单击按钮时打开文件对话框
        findViewById(R.id.btn_openfile).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showDialog(openfileDialogId);
            }
        });

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        //开始使用wifi direct查找附近的peer设备
        findViewById(R.id.btn_findpeers).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this, "初始化失败 : " + reasonCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mIpReceiver = new IPReceiver();
//        Intent startListenGrpIntent = new Intent(this, GroupClientListenService.class);
//        startService(startListenGrpIntent);
        //启动每个端的serverSocket
        Intent intent = new Intent(this, ListenService.class);
        startService(intent);
        registerIpListenReceiver();
    }

    private void registerIpListenReceiver(){
        IntentFilter intentFilter = new IntentFilter(clientSocketService.RECEIVEIP_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mIpReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id==openfileDialogId){
            Map<String, Integer> images = new HashMap<String, Integer>();
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// 根目录图标
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//返回上一层的图标
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//文件夹图标
            images.put("wav", R.drawable.filedialog_wavfile);	//wav文件图标
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
            Dialog dialog = OpenFileDialog.createDialog(id, this, "打开文件", new CallbackBundle() {
                        @Override
                        public void callback(Bundle bundle) {
                            String filepath = bundle.getString("path");
                            startShareFile(filepath);
                        }
                    },
                    "",
                    images);
            return dialog;
        }
        return null;
    }

    protected void startShareFile(String filepath){
        return;
    }

    @Override
    public void showDetails(WifiP2pDevice device) {

    }

    @Override
    public void cancelDisconnect() {

    }

    @Override
    public void connect(WifiP2pConfig config) {
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void onChannelDisconnected() {

    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
    }

    private List<WifiP2pDevice> groupMemList = new ArrayList<>();
    public List<WifiP2pDevice> getGroupList(){
        return this.groupMemList;
    }
    public void setGroupList(List<WifiP2pDevice> groupList) {
        this.groupMemList.clear();
        this.groupMemList.addAll(groupList);
    }

    /**
     * 更新界面中的组成员
     * @param deviceList
     */
    public void updateGroupFragmentWithDeviceList(List<WifiP2pDevice> deviceList){
        this.setGroupList(deviceList);
        GroupDeviceListFragment frg = (GroupDeviceListFragment) getFragmentManager().findFragmentById(R.id.grp_list);
        frg.updatePeersWithDeviceList(deviceList);
    }

    public void updateGroupFragment(List<HashMap<String, String>> mapList){
        GroupDeviceListFragment frg = (GroupDeviceListFragment) getFragmentManager().findFragmentById(R.id.grp_list);
        frg.updatePeers(mapList);
    }

    public String m_clientAddr;
    private class IPReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ArrayList<HashMap<String, String>> mapList = new ArrayList<>();
                //这里记录的是IP信息和群组内成员列表，需要进一步处理
                String clientAddr = intent.getStringExtra(clientSocketService.IP_DATA);
                ArrayList<String> devStrList = intent.getStringArrayListExtra(clientSocketService.GROUP_MEM_LIST);
                m_clientAddr = clientAddr;
                for(int i = 0; i < devStrList.size(); i++){
                    String deviceStr = devStrList.get(i);
                    String[] attrList = deviceStr.split(clientSocketService.DEVICE_SPLIT);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("deviceName", attrList[0]);
                    map.put("deviceAddress", attrList[1]);
                    map.put("status", attrList[2]);
                    map.put("primaryDeviceType", attrList[3]);
                    mapList.add(map);
                }
                updateGroupFragment(mapList);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
