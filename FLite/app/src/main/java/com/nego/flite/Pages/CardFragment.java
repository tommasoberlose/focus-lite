package com.nego.flite.Pages;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nego.flite.R;
public class CardFragment extends Fragment {

    public CardFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.card_stack, container, false);
        ((TextView) view.findViewById(R.id.title)).setText(getArguments().getString("TITLE"));
        ((TextView) view.findViewById(R.id.subtitle)).setText(getArguments().getString("SUBTITLE"));
        ((ImageView) view.findViewById(R.id.img)).setImageResource(getArguments().getInt("IMG"));
        return view;
    }

    public static CardFragment newInstance(String title, String subtitle, int img) {

        Bundle args = new Bundle();
        args.putString("TITLE", title);
        args.putString("SUBTITLE", subtitle);
        args.putInt("IMG", img);
        CardFragment fragment = new CardFragment();
        fragment.setArguments(args);
        return fragment;
    }


}