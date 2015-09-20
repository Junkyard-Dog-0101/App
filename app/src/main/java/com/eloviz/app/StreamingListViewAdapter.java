package com.eloviz.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Julien on 18/09/2015.
 */

public class StreamingListViewAdapter extends BaseAdapter {

    private Activity mActivity;
    private JSONArray mData;

    static class ViewHolderItem {
        TextView textViewItem;
    }

    public StreamingListViewAdapter(Activity activity, JSONArray data)
    {
        mActivity = activity;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return mData.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderItem viewHolder;

    /*
     * The convertView argument is essentially a "ScrapView" as described is Lucas post
     * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
     * It will have a non-null value when ListView is asking you recycle the row layout.
     * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
     */
        if (convertView == null) {
            convertView = ((LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.streaming_list_item_adapter, parent, false);
        }

//        ((TextView) convertView.findViewById(R.id.drawer_list_item_adapter_text)).setText(mDrawerFragmentList.get(position).getName());
  //      ((ImageView) convertView.findViewById(R.id.drawer_list_item_adapter_icon)).setImageResource(mDrawerFragmentList.get(position).getIcon());
    //    if (convertView == null) {

            // inflate the layout
      //      LayoutInflater inflater = mActivity.getLayoutInflater();
        //    convertView = inflater.inflate(R.layout.streaming_list_item_adapter, parent, false);

            // well set up the ViewHolder
          //  viewHolder = new ViewHolderItem();
            //viewHolder.textViewItem = (TextView) convertView.findViewById(R.id.textViewStreamingItem);

            // store the holder with the view.
//            convertView.setTag(viewHolder);

  //      } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
    //        viewHolder = (ViewHolderItem) convertView.getTag();
      //  }

        // object item based on the position
        //    ObjectItem objectItem = data[position];
        JSONObject objectItem;
        try
        {
            objectItem = ((JSONObject) mData.get(position));
            Log.e("poutesds", objectItem.toString());
        } catch (JSONException e) {
            objectItem = null;
            e.printStackTrace();
        }


        // assign values if the object is not null
        if (objectItem != null) {
            // get the TextView from the ViewHolder and then set the text (item name) and tag (item ID) values
            try {
                Log.e("dfgsdf", objectItem.getString("title"));
 //               Log.e("dfgsdf", objectItem.getString("title"));
                ((TextView) convertView.findViewById(R.id.textViewStreamingItem)).setText(objectItem.getString("title"));
//                viewHolder.textViewItem.setText(objectItem.getString("title"));
            } catch (JSONException e) {

//            viewHolder.textViewItem.setTag(objectItem.itemId);
            }


        }
        return convertView;
    }
}