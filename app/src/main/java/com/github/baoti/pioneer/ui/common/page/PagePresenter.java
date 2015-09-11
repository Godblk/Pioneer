/*
 * Copyright (c) 2014-2015 Sean Liu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.baoti.pioneer.ui.common.page;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;

import com.github.baoti.android.presenter.Presenter;
import com.github.baoti.pioneer.app.task.PageTask;
import com.github.baoti.pioneer.app.task.Tasks;
import com.github.baoti.pioneer.biz.interactor.PageInteractor;

import java.util.Collection;

/**
 * Created by liuyedong on 2015/1/2.
 */
public class PagePresenter<V extends IPageView<E>, E> extends Presenter<V>
        implements PageTask.LifecycleListener<E>, SwipeRefreshLayout.OnRefreshListener {

    private final PageTask<E> pageTask = new PageTask<>();

    private PageInteractor<E> initialResInteractor;
    private PageInteractor<E> refreshInteractor;

    public void setInitialResInteractor(PageInteractor<E> interactor) {
        initialResInteractor = interactor;
    }

    @Override
    protected void onTakeView(V view) {
        super.onTakeView(view);
        pageTask.setLifecycleListener(this);
    }

    @Override
    protected void onLoad(@Nullable Bundle savedInstanceState, boolean reusing) {
        super.onLoad(savedInstanceState, reusing);
        if (pageTask.hasLoadedResources()) {
            getView().showResources(pageTask.getLoadedResources(),
                    0, 0, pageTask.getLoadedResources().size());
        }
    }

    @Override
    protected void onDropView(V view) {
        super.onDropView(view);
        pageTask.setLifecycleListener(null);
    }

    @Override
    protected void onClose() {
        super.onClose();
        pageTask.cancel(true);
    }

    public void onSwipeRefreshPrepared() {
        if (pageTask.isLoadingFirstPage()) {
            if (hasView()) {
                getView().showRefreshing();
            }
        } else if (!pageTask.hasLoadedResources()) {
            loadInitialResources();
        }
    }

    public void loadInitialResources() {
        if (initialResInteractor != null) {
            refresh(initialResInteractor);

        } else {
            if (hasView()) {
                getView().disableSwipeRefreshing();
            }
        }
    }

    public void clearRefreshInteractor() {
        refreshInteractor = null;
        getView().disableSwipeRefreshing();
    }

    protected void refresh(PageInteractor<E> interactor) {
        pageTask.loadFirstPage(interactor);
        refreshInteractor = interactor;
        if (hasView()) {
            getView().enableSwipeRefreshing();
        }
    }

    @Override
    public void onRefresh() {
        if (refreshInteractor != null) {
            refresh(refreshInteractor);
        } else {
            getView().hideRefreshing();
        }
    }

    public boolean hasNextPage() {
        return pageTask.hasNextPage();
    }

    public PageTask.LoadState loadNextPage() {
        return pageTask.loadNextPage();
    }

    public boolean isLoadingNextPage() {
        return pageTask.isLoadingNextPage();
    }

    public boolean isFailedToLoadNextPage() {
        return pageTask.isFailedToLoadNextPage();
    }

    @Override
    public void onStarted(Tasks.SafeTask task) {
        if (!hasView()) {
            return;
        }
        PageTask pageTask = (PageTask) task;
        if (pageTask.isFirstPage()) {
            getView().showRefreshing();
        } else {
            getView().updateLoadingMore();
        }
    }

    @Override
    public void onStopped(Tasks.SafeTask task) {
        if (!hasView()) {
            return;
        }
        if (((PageTask) task).isFirstPage()) {
            getView().hideRefreshing();
        }
        getView().updateLoadingMore();
    }

    @Override
    public void onPageChanged(PageTask pageTask, int start, int before, int count) {
        if (!hasView()) {
            return;
        }
        //noinspection unchecked
        Collection<E> resources = pageTask.getLoadedResources();
        getView().showResources(resources, start, before, count);
    }
}
