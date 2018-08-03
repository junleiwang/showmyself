package com.example.wv.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnSearch,btnHaved;
    private ListView lvHaved,lvSearching;
    
    private BluetoothAdapter mAdapter = null;
    private BluetoothDevice device= null;

    public static final String TAG = "BluetoothActivity";

    private Set<String> nameList = new HashSet<>();
    private Set<String> addressList = new HashSet<>();

    private Set<String> deviceNameList = new HashSet<>();
    private Set<String> deviceAdressList = new HashSet<>();

    private List<String> addressItems = new ArrayList<>();
    private List<String> deviceaddressItems = new ArrayList<>();
    private List<String> devicenameItems = new ArrayList<>();

    private BroadcastReceiver receiver;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        setupView();
    }

    private void setupView() {
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnHaved = (Button) findViewById(R.id.btn_haved);

        lvSearching = (ListView) findViewById(R.id.lv_search);
        lvHaved = (ListView) findViewById(R.id.lv_haved);

        btnSearch.setOnClickListener(this);
        btnHaved.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btn_search:

                Toast.makeText(this,"正在搜索蓝牙设备...",Toast.LENGTH_LONG).show();

                for (int i = 0;i<3;i++){
                    bluetoothSearching();


                MyAdapter myAdapter = new MyAdapter(this,addressList,nameList);
                lvSearching.setAdapter(myAdapter);
                }

                //每个搜索到的蓝牙设备的点击事件，点击进行配对
                lvSearching.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Iterator<String> address = addressList.iterator();
                        while (address.hasNext()){
                            addressItems.add(address.next());
                        }
                        String ads = addressItems.get(position);

                        device = mAdapter.getRemoteDevice(ads);
                        int connectState = device.getBondState();
                        switch (connectState){
                            // 未配对
                            case BluetoothDevice.BOND_NONE:
                                // 配对
                                try {
                                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                    createBondMethod.invoke(device);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }

                    }
                });

                break;
            case R.id.btn_haved:

                bluetoothHaved();
                Toast.makeText(this,"正在搜索已配对蓝牙设备...",Toast.LENGTH_LONG).show();

                final MyAdapter myAdapter1 = new MyAdapter(this,deviceAdressList,deviceNameList);
                lvHaved.setAdapter(myAdapter1);


                //点击进行连接
                lvHaved.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Iterator<String> address = deviceAdressList.iterator();
                        while (address.hasNext()){
                            deviceaddressItems.add(address.next());
                        }
                        String ads = deviceaddressItems.get(position);


                        Iterator<String> name = deviceNameList.iterator();
                        while (name.hasNext()){
                            devicenameItems.add(name.next());
                        }
                        String name1 = devicenameItems.get(position);

                        Intent intent = new Intent(BluetoothActivity.this,SendMessageActivity.class);
                        intent.putExtra("mac",ads);
                        intent.putExtra("bluetoothname",name1);

                        device = mAdapter.getRemoteDevice(ads);
                        try {
                            Toast.makeText(getApplicationContext(),"connecting...",Toast.LENGTH_LONG).show();
                            connect(device);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        startActivity(intent);
                    }
                });

                break;
            default:
                break;

        }
        
    }

    //蓝牙的连接方法
    private void connect(BluetoothDevice device) throws IOException {
        // 固定的UUID
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        socket.connect();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw.write("我是传送数据");
        pw.print(pw);
        Toast.makeText(getApplicationContext(),"连接后发送的数据",Toast.LENGTH_LONG).show();
    }


    /*
    * 蓝牙的搜索方法
    * */
    private void bluetoothSearching() {

          //mAdapter = BluetoothAdapter.getDefaultAdapter();

        //搜索蓝牙设备
        if (mAdapter == null){
            Log.d(TAG,"该设备不支持蓝牙");
        }else if (!mAdapter.isEnabled()){
            mAdapter.enable();
            mAdapter.cancelDiscovery();//关闭蓝牙搜索
        }

        //BluetoothDevice device = mAdapter.getRemoteDevice();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        this.registerReceiver(receiver, intentFilter);
        // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
        mAdapter.startDiscovery();

         receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //devices.add(device);
                    //Log.d(TAG,"device=="+device.getName()+device.getAddress());
                    System.out.println("device名字=="+device.getName()+"deviceMAC=="+device.getAddress());

                    //for (Iterator<BluetoothDevice> it = )
                    nameList.add(device.getName());
                    addressList.add(device.getAddress());
                }
            }
        };
    }

    /*
    * 已配对设备
    * */
    public void bluetoothHaved(){
        //获取可以配对的蓝牙设备
        Set<BluetoothDevice> Bondedevice=mAdapter.getBondedDevices();


        if (Bondedevice.size()>0){

            for(Iterator<BluetoothDevice> it=Bondedevice.iterator();it.hasNext();){
                BluetoothDevice btd=it.next();
                deviceNameList.add(btd.getName());
                deviceAdressList.add(btd.getAddress());

            }
        }

    }

    //适配器用于测试数据的传输
    static class ViewHolder
    {
        public TextView tvName;
        public TextView tvMac;}

    class MyAdapter extends BaseAdapter {

        private Set<String> addresSet1 = new HashSet<>();
        private Set<String> nameSet1 = new HashSet<>();

        private LayoutInflater mInflater = null;

        public MyAdapter(Context context,Set<String> addresSet,Set<String> nameSet){
            this.addresSet1 = addresSet;
            this.nameSet1 = nameSet;

            this.mInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return addresSet1.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView ==null) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.layoutadapter,null);

                viewHolder.tvMac = (TextView) convertView.findViewById(R.id.tv_phonemac);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_phonename);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            List<String> addresString = new ArrayList<>();
            List<String> nameString = new ArrayList<>();

            Iterator<String> strAdress = addresSet1.iterator();
            while (strAdress.hasNext()){
                addresString.add(strAdress.next());
            }
            viewHolder.tvMac.setText(addresString.get(position));

            Iterator<String> strName = nameSet1.iterator();
            while (strName.hasNext()){
                nameString.add(strName.next());
            }
            viewHolder.tvName.setText(nameString.get(position));

            return convertView;
        }
    }


}
