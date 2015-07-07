package com.eloviz.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ConfigStreamFragment extends ADrawerFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config_stream, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int stackHeight = getActivity().getSupportFragmentManager().getBackStackEntryCount();
                //   if (stackHeight > 0) { // if we have something on the stack (doesn't include the current shown fragment)
                //     getActivity().getActionBar().setHomeButtonEnabled(true);
                //   getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
                // } else {
                //   getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
                //  getActivity().getActionBar().setHomeButtonEnabled(false);
                //}
            }

        });
        //get list
        Button b = (Button) getActivity().findViewById(R.id.validateConfButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = new WebRTCStreamFragment();
                EditText editText = (EditText) getActivity().findViewById(R.id.textInputRoom);
                Bundle bundle = new Bundle();
                if (editText.getText().toString().equals("")) {
                    bundle.putString("room", "simplechat");

                } else {
                    bundle.putString("room", editText.getText().toString());
                    Log.e("lol", editText.getText().toString());/* à tester demain */
                }
                fragment.setArguments(bundle);
                //fragmentManager.bac
                fragmentManager.beginTransaction().add(R.id.contentFrame, fragment).addToBackStack("fragBack").commit();
            }
        });

        //mDrawerList.setItemChecked(position, true);
        //setTitle(mDrawerFragmentList.get(position).getName());
        // mDrawerLayout.closeDrawer(mDrawerList);
        super.onActivityCreated(savedInstanceState);
    }
}
