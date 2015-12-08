package com.nego.flite.Pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nego.flite.MyDialog;
import com.nego.flite.R;

public class ImgFragment extends Fragment {

    public ImgFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.img_item, container, false);


        ImageView selected_img = (ImageView) view.findViewById(R.id.selected_img);
        selected_img.setImageURI(Uri.parse(getArguments().getString("IMG")));
        if (selected_img.getDrawable() == null) {
            selected_img.setImageResource(R.drawable.ic_action_ic_image_broken_variant_white_48dp);
        }
        selected_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(getArguments().getString("IMG")), "image/*");
                startActivity(intent);
            }
        });

        ImageView cancel_img = (ImageView) view.findViewById(R.id.action_cancel_image);
        cancel_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.attention))
                        .setMessage(getResources().getString(R.string.ask_delete_img) + "?")
                        .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((MyDialog) getActivity()).removeImg(getArguments().getString("IMG"));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        view.findViewById(R.id.action_share_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share_intent = new Intent(Intent.ACTION_SEND);
                share_intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(getArguments().getString("IMG")));
                share_intent.setType("image/*");
                startActivity(Intent.createChooser(share_intent, getString(R.string.action_share)));
            }
        });
        ((TextView) view.findViewById(R.id.nImg)).setText(getArguments().getString("NUMBER"));
        return view;
    }

    public static ImgFragment newInstance(String img, String number) {

        Bundle args = new Bundle();
        args.putString("IMG", img);
        args.putString("NUMBER", number);
        ImgFragment fragment = new ImgFragment();
        fragment.setArguments(args);
        return fragment;
    }


}