package com.mmdev.meetups.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mmdev.meetups.R;
import com.mmdev.meetups.models.FeedItem;
import com.mmdev.meetups.ui.activities.MainActivity;
import com.mmdev.meetups.ui.adapters.FeedAdapter;
import com.mmdev.meetups.utils.EndlessRecyclerViewScrollListener;
import com.mmdev.meetups.utils.FeedManager;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FeedFragment extends Fragment
{
	private MainActivity mMainActivity;
	private RecyclerView rvFeedList;
	private FeedAdapter mFeedAdapter;
	private List<FeedItem> mFeedItems = new ArrayList<>();

	@Override
	public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_main, container, false);
	}

	@Override
	public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
		rvFeedList = view.findViewById(R.id.content_main_recyclerview);
		initFeeds();
	}

	@Override
	public void onActivityCreated (@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getActivity()!=null) mMainActivity = (MainActivity) getActivity();
	}

	private void initFeeds() {
		mFeedItems.addAll(FeedManager.generateDUmmyFeeds());
		mFeedAdapter = new FeedAdapter(mFeedItems);
		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mMainActivity, RecyclerView.VERTICAL, false);
		rvFeedList.setLayoutManager(linearLayoutManager);
		rvFeedList.setAdapter(mFeedAdapter);
		rvFeedList.setItemAnimator(new DefaultItemAnimator());
		rvFeedList.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
			@Override
			public void onLoadMore(int page, int totalItemsCount) { loadMoreFeeds(); }
		});
	}

	private void loadMoreFeeds() {
		rvFeedList.post(() -> {
			mFeedItems.addAll(FeedManager.generateDUmmyFeeds());
			mFeedAdapter.notifyItemInserted(mFeedItems.size() - 1);
		});
	}
}
