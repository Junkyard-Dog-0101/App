package com.eloviz.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DrawerListAdapter extends BaseAdapter {

    private ArrayList<ADrawerFragment> mDrawerFragmentList;
    private Activity mActivity;

    public DrawerListAdapter(Activity activity, ArrayList<ADrawerFragment> drawerFragmentList) {
        mActivity = activity;
        mDrawerFragmentList = drawerFragmentList;
    }

    @Override
    public int getCount() {
        return (mDrawerFragmentList.size());
    }

    @Override
    public Object getItem(int position) {
        return mDrawerFragmentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.drawer_list_item_adapter, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.drawer_list_item_adapter_text)).setText(mDrawerFragmentList.get(position).getName());
        ((ImageView) convertView.findViewById(R.id.drawer_list_item_adapter_icon)).setImageResource(mDrawerFragmentList.get(position).getIcon());

        return convertView;
    }
}