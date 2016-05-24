package edu.columbia.cs.androidiotcomissioner;

import android.app.Dialog;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class WiFiP2PFragment extends Fragment {

    public static final String TAG = "WiFiP2PFragment";

    MainActivity hostingAcitivity;

    // for the Lists
    private RecyclerView mRecyclerView;
    private WiFiDeviceAdaptor mDeviceAdaptor;
    private SwitchCompat mSwitchCompat;
    private TextView mTextView;

    public static WiFiP2PFragment newInstance(){
        return new WiFiP2PFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hostingAcitivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifip2p, container, false);

        mTextView = (TextView) rootView.findViewById(R.id.fragment_wifip2p_text);
        // handle the toggle button
        mSwitchCompat = (SwitchCompat) rootView.findViewById(R.id.fragment_wifip2p_switch);
        mSwitchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSwitchCompat.isChecked())
                {
                    Log.d(TAG, "Toggle On");
                    hostingAcitivity.setDiscoveryOn();
                    mTextView.setText("On");
                }
                else{
                    Log.d(TAG, "Toggle Off");
                    hostingAcitivity.setDiscoveryOff();
                    mTextView.setText("Off");
                }
            }
        });

        // handle the recycle view
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.wifip2p_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRecyclerView.setLayoutManager(mLayoutManager);
        mDeviceAdaptor= new WiFiDeviceAdaptor(hostingAcitivity.getWifiP2pDevices());
        mRecyclerView.setAdapter(mDeviceAdaptor);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class WiFiDeviceAdaptor extends RecyclerView.Adapter<WiFiDeviceHolder> {

        private List<WifiP2pDevice> mAdaptorDevices;

        public WiFiDeviceAdaptor (List<WifiP2pDevice> devices)
        {
            mAdaptorDevices = devices;
        }

        @Override
        public WiFiDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.list_item_device, parent, false);
            return new WiFiDeviceHolder(v);
        }

        @Override
        public void onBindViewHolder(WiFiDeviceHolder holder, int position) {
            WifiP2pDevice thisDevice = mAdaptorDevices.get(position);
            holder.bindDevice(thisDevice);
        }

        @Override
        public int getItemCount() {
            return mAdaptorDevices.size();
        }

    }

    public class WiFiDeviceHolder extends RecyclerView.ViewHolder{
        TextView mDeviceNameTextView;
        TextView mDeviceMacTextView;
        ImageView mImageView;
        WifiP2pDevice mDevice;

        public WiFiDeviceHolder(View itemView) {
            super(itemView);
            mDeviceNameTextView = (TextView) itemView.findViewById(R.id.list_item_device_name);
            mDeviceMacTextView = (TextView) itemView.findViewById(R.id.list_item_device_address);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_device_image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment connectDialog = new ConnectDialogFragment();
                    connectDialog.show(getFragmentManager(),mDevice.deviceAddress);
                }
            });
        }

        public void bindDevice(WifiP2pDevice device){
            mDevice = device;
            mDeviceNameTextView.setText(device.deviceName);
            mDeviceMacTextView.setText(device.deviceAddress);
            if(hostingAcitivity.getConnectedP2pDevices().contains(device.deviceAddress)){
                mImageView.setImageResource(R.drawable.ic_iot_connected);
            }
        }

    }

    // additional methods

    public void toggleSwitch(boolean flag){
        mSwitchCompat.setChecked(flag);
    }

    public WiFiDeviceAdaptor getDeviceAdaptor(){
        return mDeviceAdaptor;
    }


    // Async Worker


}
