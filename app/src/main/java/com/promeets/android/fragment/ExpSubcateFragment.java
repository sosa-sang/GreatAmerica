package com.promeets.android.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.promeets.android.activity.BaseActivity;
import com.promeets.android.adapter.ExpSubcateAdapter;
import com.promeets.android.object.SubCate;
import com.promeets.android.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sosasang on 2/1/18.
 */

public class ExpSubcateFragment extends DialogFragment {
    @BindView(R.id.list_view)
    public ListView mListView;
    @BindView(R.id.root_layout)
    LinearLayout mLayRoot;

    private BaseActivity mBaseActivity;
    private ArrayList<SubCate> list;
    private ExpSubcateAdapter adapter;

    public static ExpSubcateFragment newInstance(ArrayList<SubCate> list) {
        ExpSubcateFragment f = new ExpSubcateFragment();
        f.list = list;
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mBaseActivity = (BaseActivity) getActivity();
        final RelativeLayout root = new RelativeLayout(mBaseActivity);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(mBaseActivity);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exp_cate, container);
        ButterKnife.bind(this, view);

        mLayRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        adapter = new ExpSubcateAdapter(mBaseActivity, list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCallback.OnSubcateSelect(i);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    private Callback mCallback;
    public interface Callback {
        void OnSubcateSelect(int i);
    }
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
}
