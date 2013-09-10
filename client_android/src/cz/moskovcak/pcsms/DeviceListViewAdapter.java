package cz.moskovcak.pcsms;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DeviceListViewAdapter extends ArrayAdapter<BluetoothDevice> {
    private final Context context;
    private final List<BluetoothDevice> values;

    public DeviceListViewAdapter (Context context, List<BluetoothDevice> values) {
      super(context, R.layout.device_list_item, values);
      this.context = context;
      this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View rowView = inflater.inflate(R.layout.device_list_item, parent, false);
      
      TextView deviceNameText = (TextView) rowView.findViewById(R.id.deviceName);
      TextView deviceAddressText = (TextView) rowView.findViewById(R.id.deviceAddress);
      deviceNameText.setText(values.get(position).getName());
      deviceAddressText.setText(values.get(position).getAddress());
      
      return rowView;
    }
    
  } 