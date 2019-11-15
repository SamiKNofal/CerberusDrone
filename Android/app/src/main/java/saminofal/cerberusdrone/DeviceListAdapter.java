package saminofal.cerberusdrone;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;
import java.util.ArrayList;


public class  DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    public ArrayList<BluetoothDevice> mDevices;
    public int mRSSI;
    public int  mViewResourceId;
    public TextView deviceName, deviceAddress, deviceRSSI;


    public DeviceListAdapter(Context context, int tvResourceId,
                             ArrayList<BluetoothDevice> devices, int rssi){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        this.mRSSI = rssi;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }


    @NonNull
    @SuppressLint({"ViewHolder", "SetTextI18n"})
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);
        BluetoothDevice device = mDevices.get(position);

        deviceName = convertView.findViewById(R.id.tvDeviceName);
        deviceAddress = convertView.findViewById(R.id.tvDeviceAddress);
        deviceRSSI = convertView.findViewById(R.id.tvDeviceRSSI);

        if (device != null) {

            if(!device.getName().equals("")) {
                if (deviceName != null) {
                    deviceName.setText(device.getName());
                }
            }
            else {
                deviceName.setText("Unknown");
            }

            if (deviceAddress != null) {
                deviceAddress.setText(device.getAddress());
            }

            if(!(mRSSI == 0)) {
                if(deviceRSSI != null) {
                    deviceRSSI.setText(mRSSI + " dBm");
                }
            }
            else {
                deviceRSSI.setText("Unknown");
            }

        }

        return convertView;
    }


    public void clearList() {
        try {
            mDevices.clear();
            notifyDataSetChanged();
        }
        catch (Exception e) {
            // Do nothing
        }
    }

}