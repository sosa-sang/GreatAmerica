package com.promeets.android.activity;

import com.promeets.android.adapter.RecycleReviewAdapter;
import com.promeets.android.custom.PromeetsDialog;
import android.graphics.Color;
import com.promeets.android.Constant;
import com.promeets.android.listeners.IServiceResponseHandler;
import com.promeets.android.object.ServiceReview;
import com.promeets.android.services.GenericServiceHandler;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.promeets.android.util.PromeetsPreferenceUtil;
import com.promeets.android.util.PromeetsUtils;
import android.view.View;
import android.widget.TextView;
import com.promeets.android.object.UserPOJO;
import com.promeets.android.pojo.AllReviewsResp;
import com.promeets.android.pojo.ServiceResponse;
import com.promeets.android.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.promeets.android.util.ServiceHeaderGeneratorUtil;
import com.promeets.android.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This is for showing event review list
 *
 * Similar to ServiceReviewActivity
 *
 * @source: EventDetailActivity
 *
 */
public class EventReviewListActivity extends BaseActivity implements View.OnClickListener, IServiceResponseHandler {

    @BindView(R.id.most_useful)
    TextView mBtnMostUseFul;

    @BindView(R.id.most_recent)
    TextView mBtnMostRecent;

    @BindView(R.id.review_list)
    RecyclerView mRVReview;

    @BindView(R.id.refresh_layout)
    SmartRefreshLayout mLayRefresh;

    private ArrayList<ServiceReview> serviceReview = new ArrayList<>();

    int page = 1;

    private RecycleReviewAdapter reviewAdapter;

    boolean isMostUsefulSelected = true;

    private String mEventId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ButterKnife.bind(this);

        mEventId = getIntent().getStringExtra("eventId");

        if (isMostUsefulSelected)
            handleMostUseFul();
        else
            handleMostRecent();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRVReview.setLayoutManager(linearLayoutManager);
        reviewAdapter = new RecycleReviewAdapter(this, serviceReview, false);
        mRVReview.setAdapter(reviewAdapter);
    }

    @Override
    public void initElement() {

    }

    @Override
    public void registerListeners() {
        mBtnMostRecent.setOnClickListener(this);
        mBtnMostUseFul.setOnClickListener(this);

        mLayRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(500);
            }
        });
        mLayRefresh.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                if (isMostUsefulSelected)
                    callService(Constant.SORTKEY_MOST_USEFUL);
                else
                    callService(Constant.SORTKEY_MOST_RECENT);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.most_recent:
                page = 1;
                isMostUsefulSelected = false;
                serviceReview.clear();
                reviewAdapter.notifyDataSetChanged();
                handleMostRecent();
                break;
            case R.id.most_useful:
                page = 1;
                isMostUsefulSelected = true;
                serviceReview.clear();
                reviewAdapter.notifyDataSetChanged();
                handleMostUseFul();
                break;
        }
    }

    private void handleMostRecent() {
        mBtnMostRecent.setTextColor(Color.WHITE);
        mBtnMostRecent.setBackground(getResources().getDrawable(R.drawable.right_select));
        mBtnMostUseFul.setTextColor(ContextCompat.getColor(this, R.color.primary));
        mBtnMostUseFul.setBackground(getResources().getDrawable(R.drawable.left_unselect));
        callService(Constant.SORTKEY_MOST_RECENT);
    }

    private void handleMostUseFul() {
        mBtnMostUseFul.setTextColor(Color.WHITE);
        mBtnMostUseFul.setBackground(getResources().getDrawable(R.drawable.left_select));
        mBtnMostRecent.setTextColor(ContextCompat.getColor(this, R.color.primary));
        mBtnMostRecent.setBackground(getResources().getDrawable(R.drawable.right_unselect));
        callService(Constant.SORTKEY_MOST_USEFUL);
    }

    private void callService(String softKey) {
        HashMap<String, String> header = new HashMap<>();
        header.put("ptimestamp", ServiceHeaderGeneratorUtil.getInstance().getPTimeStamp());
        header.put("promeetsT", ServiceHeaderGeneratorUtil.getInstance().getPromeetsTHeader(Constant.EVENT_REVIEW_DETAIL));
        header.put("accessToken", ServiceHeaderGeneratorUtil.getInstance().getAccessToken());
        header.put("API_VERSION", Utility.getVersionCode());

            //Check for internet Connection
            if (!hasInternetConnection()) {
                PromeetsDialog.show(this, getString(R.string.no_internet));
                return;
            }
            PromeetsDialog.showProgress(this);
                UserPOJO userPOJO = (UserPOJO) PromeetsUtils.getUserData(PromeetsPreferenceUtil.USER_OBJECT_KEY, UserPOJO.class);
                int userId = 0;
                if (userPOJO != null)
                    userId = userPOJO.id;
                String[] key = {Constant.USERID, Constant.EVENTID, Constant.PAGENUMBER, Constant.SORTKEY};
                String[] value = {userId + "", mEventId, page++ + "", softKey};


                new GenericServiceHandler(Constant.ServiceType.REVIEW_LIST, this, PromeetsUtils.buildURL(Constant.EVENT_REVIEW_DETAIL, key, value), null, header, IServiceResponseHandler.GET, false, "Please wait!", "Processing..").execute();
    }

    @Override
    public void onServiceResponse(ServiceResponse serviceResponse, Constant.ServiceType serviceType) {
        PromeetsDialog.hideProgress();
        mLayRefresh.finishLoadmore();
        AllReviewsResp result = (AllReviewsResp) serviceResponse.getServiceResponse(AllReviewsResp.class);
        if (EventReviewListActivity.this.isSuccess(result.info.code)) {
            if (page == 1)
                mLayRefresh.setLoadmoreFinished(false);

            if (result.reviewList.size() > 0) {
                serviceReview.addAll(result.reviewList);
                reviewAdapter.notifyItemInserted(reviewAdapter.getItemCount());
            } else
                mLayRefresh.setLoadmoreFinished(true);
        } else {
            onErrorResponse(result.info.code + " : " + result.info.description);
        }
    }

    @Override
    public void onErrorResponse(String errorMessage) {
        PromeetsDialog.hideProgress();
        mLayRefresh.finishLoadmore();
        PromeetsDialog.show(this, errorMessage);
    }

    @Override
    public void onErrorResponse(Throwable serviceException) {
        onErrorResponse(serviceException.getLocalizedMessage());
    }
}
