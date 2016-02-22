package com.nego.flite.Adapter;

        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.app.FragmentStatePagerAdapter;

        import com.nego.flite.Pages.ImgFragment;

        import java.util.ArrayList;
        import java.util.List;

public class ImgViewPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    public ImgViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }
    @Override
    public int getCount() {
        return mFragmentList.size();
    }
    public void addFrag(String img, String number) {
        mFragmentList.add(ImgFragment.newInstance(img, number));
    }
}