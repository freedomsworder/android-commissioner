package edu.columbia.cs.androidiotcomissioner;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.os.ParcelableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class ZeroConfFragment extends Fragment {

    public static final String TAG = "ZeroConfFragment";

    private MainActivity callingActivity;

    private SwitchCompat mServerSwitch;
    private TextView mTextView;
    private RecyclerView mRecyclerView;
    private ZeroConfServiceAdaptor mServiceAdaptor;

    public static ZeroConfFragment newInstance(){
        return new ZeroConfFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callingActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_zeroconf, container, false);

        mServerSwitch = (SwitchCompat) rootView.findViewById(R.id.fragment_zeroconf_switch);
        mTextView = (TextView) rootView.findViewById(R.id.fragment_zeroconf_text);

        mServerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServerSwitch.isChecked()){
                    callingActivity.setServerOn();
                }
                else
                    callingActivity.setServerOff();
            }

        });
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_zeroconf_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        });
        mServiceAdaptor = new ZeroConfServiceAdaptor(callingActivity.getZeroConfServices());
        mRecyclerView.setAdapter(mServiceAdaptor);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));


        // repeatable task

        final Handler handler = new Handler();
        // Define the code block to be executed
        final Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                // Repeat this the same runnable code block again another 2 seconds
                mServiceAdaptor.notifyDataSetChanged();
                handler.postDelayed(this, 5000);
            }
        };

        // Start the initial runnable task by posting through the handler
        handler.postDelayed(runnableCode,5000);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class ZeroConfServiceAdaptor extends RecyclerView.Adapter<ZeroConfServiceHolder> {

        private List<NsdServiceInfo>  mZeroConfServices;

        public ZeroConfServiceAdaptor (List<NsdServiceInfo> services)
        {
            mZeroConfServices = services;
        }

        @Override
        public ZeroConfServiceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.list_item_service, parent, false);
            return new ZeroConfServiceHolder(v);
        }

        @Override
        public void onBindViewHolder(ZeroConfServiceHolder holder, int position) {
            NsdServiceInfo thisService = mZeroConfServices.get(position);
            holder.bindService(thisService);
        }

        @Override
        public int getItemCount() {
            return mZeroConfServices.size();
        }

    }

    public class ZeroConfServiceHolder extends RecyclerView.ViewHolder{
        TextView mServiceNameTextView;
        TextView mServiceAddressTextView;
        ImageView mServiceImageView;
        NsdServiceInfo mNsdServiceInfo;

        public ZeroConfServiceHolder(View itemView) {
            super(itemView);
            mServiceNameTextView = (TextView) itemView.findViewById(R.id.list_item_service_name);
            mServiceAddressTextView = (TextView) itemView.findViewById(R.id.list_item_service_address);
            mServiceImageView = (ImageView) itemView.findViewById(R.id.list_item_service_image);

            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callingActivity.mNsdManager.resolveService(mNsdServiceInfo, new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Toast.makeText(getActivity(),"Failed to resolve",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            String result = serviceInfo.getHost().toString().substring(1)+":"+serviceInfo.getPort();
                            Log.d(TAG,"Resolved:"+result);
                            Toast.makeText(getActivity(),result,Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mNsdServiceInfo.getHost() == null){
                        Toast.makeText(getActivity(),"Resolving...",Toast.LENGTH_SHORT).show();
                        callingActivity.mNsdManager.resolveService(mNsdServiceInfo, new NsdManager.ResolveListener() {
                            @Override
                            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                Toast.makeText(getActivity(),"Failed to resolve",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                String result = serviceInfo.getHost().toString().substring(1)+":"+serviceInfo.getPort();
                                Log.d(TAG,"Resolved:"+result);
                                DialogFragment connectDialog = new ConnectDialogFragment();
                                Bundle args = new Bundle();
                                args.putString("name", serviceInfo.getServiceName());
                                args.putSerializable("address", serviceInfo.getHost());
                                args.putInt("port", serviceInfo.getPort());
                                connectDialog.setArguments(args);
                                connectDialog.show(getFragmentManager(),"service");
                            }
                        });
                    }
                    else {
                        DialogFragment connectDialog = new ConnectDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("name", mNsdServiceInfo.getServiceName());
                        args.putSerializable("address", mNsdServiceInfo.getHost());
                        args.putInt("port", mNsdServiceInfo.getPort());
                        connectDialog.setArguments(args);
                        connectDialog.show(getFragmentManager(), "service");
                    }
                    /*

                    */

                }
            });
        }

        public void bindService(NsdServiceInfo service){
            mNsdServiceInfo = service;
            mServiceNameTextView.setText(service.getServiceName());
            if(service.getHost() != null)
                mServiceAddressTextView.setText(service.getHost().toString().substring(1)+":"+service.getPort());

            if(callingActivity.mPendingService.contains(service.getServiceName())){
                mServiceImageView.setImageResource(R.drawable.ic_service_pending);
                Log.d(TAG,"Set as pending");
            }

            else if(callingActivity.mEnrolledService.contains(service.getServiceName())){
                mServiceImageView.setImageResource(R.drawable.ic_service_done);
                Log.d(TAG,"Set as done");
            }
            else{
                mServiceImageView.setImageResource(R.drawable.ic_service_standby);
                Log.d(TAG,"Set as standby");
            }
        }

    }

    // additional functions

    public void setTextView(String content){
        mTextView.setText(content);
    }

    public ZeroConfServiceAdaptor getServiceAdaptor(){
        return mServiceAdaptor;
    }

}
