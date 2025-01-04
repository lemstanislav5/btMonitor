package com.example.btapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.btapp.R;
import java.util.ArrayList;
import java.util.List;

public class BtAdapter extends ArrayAdapter<ListItem> {
    public static final String DEF_ITEM_TYPE = "normal";
    public static final String TITLE_ITEM_TYPE = "title";
    public static final String DISCOVERI_ITEM_TYPE = "discoveri";
    private List<ListItem> mainList;
    private List<ViewHolder> listViewHolders;
    private boolean isDiscoveryTupe = false;

    private SharedPreferences pref;
    public BtAdapter(@NonNull Context context, int resource, List<ListItem> btList) {
        super(context, resource, btList);
        mainList = btList;
        listViewHolders = new ArrayList<>();
        pref = context.getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        switch (mainList.get(position).getItemType()){
            case TITLE_ITEM_TYPE:
                convertView = titleItem(convertView, parent);
                break;
            default: convertView = defaultItem(convertView, position, parent);
            break;

        }
//        convertView = defaultItem(convertView, position, parent);
        return convertView;
    }
    private void savePref(int position){
        //Editor сохраняет данные
        SharedPreferences.Editor editor = pref.edit();
        // Указываем название поля в Editor и вторым параметром передаем данные из mainList
        editor.putString(BtConsts.MAC_KEY, mainList.get(position).getBtDevice().getAddress());
        editor.apply();
    }
    @SuppressLint("MissingPermission")
    private View defaultItem(View convertView, int position, ViewGroup parent){
        ViewHolder viewHolder;
        // Если convertView отсутствует то мы его формируем, если данные есть, то назначаем из памяти
        boolean hasViewHolder = false;
        if(convertView != null) hasViewHolder = (convertView.getTag() instanceof ViewHolder);
        if(convertView == null || !hasViewHolder){
            viewHolder = new ViewHolder();
            listViewHolders.add(viewHolder);
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item, null, false);
            viewHolder.tvBtName = convertView.findViewById(R.id.tvBtName);
            viewHolder.tvMac = convertView.findViewById(R.id.tvMac);
            viewHolder.chBtSelect = convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.chBtSelect.setChecked(false);
        }
        if(mainList.get(position).getItemType().equals(BtAdapter.DISCOVERI_ITEM_TYPE)){
            viewHolder.chBtSelect.setVisibility(View.GONE);
            isDiscoveryTupe = true;
        } else {
            viewHolder.chBtSelect.setVisibility(View.VISIBLE);
            isDiscoveryTupe = false;
        }
        viewHolder.tvBtName.setText(mainList.get(position).getBtDevice().getName());
        viewHolder.tvMac.setText((mainList).get(position).getBtDevice().getAddress());
        viewHolder.chBtSelect.setOnClickListener(view -> {
            for(ViewHolder holder : listViewHolders){
                holder.chBtSelect.setChecked(false);
            }
            viewHolder.chBtSelect.setChecked(true);
            savePref(position);
        });
        // Сравнение сохраненного mac-адресса и того, что был нажат
        if(pref.getString(BtConsts.MAC_KEY, "no bt selected").equals(mainList.get(position).getBtDevice().getAddress())){
            viewHolder.chBtSelect.setChecked(true);
        }
        isDiscoveryTupe =false;
        return convertView;
    }
    private View titleItem(View convertView, ViewGroup parent){
        // Если convertView отсутствует то мы его формируем, если данные есть, то назначаем из памяти
        boolean hasViewHolder = false;
        // Содержит ли convertView Tag: ViewHolder
        if(convertView != null) hasViewHolder = (convertView.getTag() instanceof ViewHolder);
        if(convertView == null || hasViewHolder){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item_title, null, false);
        }
        return convertView;
    }
    static class ViewHolder{
        TextView tvBtName;
        TextView tvMac;
        CheckBox chBtSelect;
    }
}
