package bigleg.com.wifidirect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import bigleg.com.wifidirect.DeviceListFragment.DeviceActionListener;

public class WiFiDirectActivity extends Activity implements WifiP2pManager.ChannelListener, DeviceActionListener{

    public static final String TAG = "基于WIFI直连的群组文件共享";
    WifiP2pManager mManager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter mIntentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver = null;

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
                        // 初始化完成，开始查找，成功时调用Receiver的onReceive函数
                        //Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        // Code for when the discovery initiation fails goes here.Alert the user that something went wrong.
                        Toast.makeText(WiFiDirectActivity.this, "初始化失败 : " + reasonCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mChannel = mManager.initialize(this, getMainLooper(), null);
        //mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
    }

    /**
     *TODO 更新组成员
     * @param deviceList
     */
    public void updateGroupMembers(List<WifiP2pDevice> deviceList){
        GroupDeviceListFragment frg = (GroupDeviceListFragment) getFragmentManager().findFragmentById(R.id.grp_list);
        frg.updatePeers(deviceList);
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
                            //setTitle(filepath); // 把文件路径显示在标题上
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
}
