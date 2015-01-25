package com.eloviz.app;

import android.support.v4.app.Fragment;

public abstract class ADrawerFragment extends Fragment {
    private Integer mIcon;
    private String mName;

    public Integer getIcon() {
        return mIcon;
    }

    public void setIcon(Integer mIcon) {
        this.mIcon = mIcon;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }
}
