package com.twtstudio.bbs.bdpqchen.bbs.main.latestPost;

import com.twtstudio.bbs.bdpqchen.bbs.commons.base.BasePresenter;
import com.twtstudio.bbs.bdpqchen.bbs.commons.base.BaseView;

import java.util.List;

/**
 * Created by bdpqchen on 17-5-11.
 */

interface LatestPostContract {
    interface View extends BaseView{
        void addAnnounce(List<LatestPostModel.AnnounceBean> announceBeen);
        void refreshAnnounce(List<LatestPostModel.AnnounceBean> announceBeen);
        void failedToGetLatestPost(String msg);
    }
    interface Presenter extends BasePresenter<View>{
        void refreshAnnounce();
        void addAnnounce();
    }
}
