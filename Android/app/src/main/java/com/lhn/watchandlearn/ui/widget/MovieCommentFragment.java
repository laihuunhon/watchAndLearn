package com.lhn.watchandlearn.ui.widget;

import java.util.ArrayList;

import org.joda.time.DateTime;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.pojo.Comment;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.pojo.Video;
import com.lhn.watchandlearn.api.request.comments.AddCommentRequest;
import com.lhn.watchandlearn.api.request.comments.GetCommentListRequest;
import com.lhn.watchandlearn.api.response.base.Response;
import com.lhn.watchandlearn.api.response.comments.CommentsResponse;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.home.LoginActivity;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;

// In this case, the fragment displays simple text based on the page
public class MovieCommentFragment extends Fragment {
	private final String TAG = MovieCommentFragment.class.getName();

	private static Movie currentMovie;
	private static int currentVideoIndex;
	private static ArrayList<Video> mVideos = new ArrayList<Video>();
	private static Video currentVideo;

	public static final String ARG_PAGE = "ARG_PAGE";
	private CommentAdapter mAdapter;
	private ArrayList<Comment> mComments = new ArrayList<Comment>();

	private boolean mIsFetchDone = false;
	private boolean mIsFetching = false;
	private int mCurrentPage = -1;

	public static Fragment newInstance(Movie movie, int videoIndex) {
		currentMovie = movie;
		currentVideoIndex = videoIndex;
		mVideos = movie.getItems();
		currentVideo = mVideos.get(currentVideoIndex - 1);

		MovieCommentFragment fragment = new MovieCommentFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_movie_comment, container, false);
		ListView listView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new CommentAdapter();
		listView.setAdapter(mAdapter);

		final EditText edtComment = (EditText) view.findViewById(R.id.edtComment);
		edtComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(final TextView aTextView, final int i, final KeyEvent aKeyEvent) {
				System.out.println(i);
				if (i == EditorInfo.IME_ACTION_DONE) {
					addComment(edtComment.getText().toString());
					EditTextUtils.hideKeyboard(getBaseActivity());
					edtComment.setText("");
					return true;
				}
				return false;
			}
		});
		edtComment.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                	EditTextUtils.hideKeyboard(getBaseActivity());
                	edtComment.setText("");
                }                    
            }
        });

		fetchData();

		return view;
	}

	public class CommentAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mComments.size();
		}

		@Override
		public Comment getItem(final int i) {
			return mComments.get(i);
		}

		@Override
		public long getItemId(final int i) {
			return i;
		}

		@Override
		public View getView(final int i, View aView, final ViewGroup aViewGroup) {
			ViewHolder holder;
			if (aView == null) {
				aView = getActivity().getLayoutInflater().inflate(R.layout.adapter_comment, null);
				holder = new ViewHolder();
				holder.emailText = (TextView) aView.findViewById(R.id.tvEmail);
				holder.textText = (TextView) aView.findViewById(R.id.tvText);
				holder.dateText = (TextView) aView.findViewById(R.id.tvDate);

				aView.setTag(holder);
			} else {
				holder = (ViewHolder) aView.getTag();
			}

			Comment comment = getItem(i);
			holder.emailText.setText(comment.getEmail());
			holder.textText.setText(comment.getText());
			DateTime dt = new DateTime(comment.getCreated());

			holder.dateText.setText(dt.toString("hh:mm:ss dd/MM/yyyy"));

			if (i == getCount() - 1 && !mIsFetchDone && !mIsFetching) {
				fetchData();
			}

			return aView;
		}
	}

	private static class ViewHolder {
		TextView emailText;
		TextView textText;
		TextView dateText;
	}

	private void fetchData() {
		if (!mIsFetchDone && !mIsFetching) {
			mIsFetching = true;
			mCurrentPage++;

			getBaseActivity().showLoadingDialog(getString(R.string.loading_waiting));
			GetCommentListRequest request = new GetCommentListRequest();
			request.setLimit(20);
			request.setOffset(mCurrentPage * 20);
			request.setVideoId(currentVideo.getId());

			getBaseActivity().send(request, new SimpleResponseListener<CommentsResponse>(getBaseActivity()) {

				@Override
				public void onBeforeResult() {
					super.onBeforeResult();
					mIsFetching = false;
					getBaseActivity().dismissLoadingDialog();
				}

				@Override
				public void onSuccess(final CommentsResponse aResult) {
					Log.i(TAG, aResult.toString());
					mComments.addAll(aResult.getItems());
					if (mComments.size() >= currentVideo.getCommented()) {
						mIsFetchDone = true;
					}

					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	private void addComment(String text) {
		getBaseActivity().showLoadingDialog(getString(R.string.loading_waiting));
		AddCommentRequest request = new AddCommentRequest();
		request.setMovieId(currentMovie.getId());
		request.setVideoId(currentVideo.getId());
		request.setText(text);

		getBaseActivity().send(request, new SimpleResponseListener<Response>(getBaseActivity()) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				getBaseActivity().dismissLoadingDialog();
			}

			@Override
			public void onSuccess(final Response aResult) {
				Log.i(TAG, aResult.toString());

				mIsFetchDone = false;
				mIsFetching = false;
				mCurrentPage = -1;
				mComments.clear();

				fetchData();
			}
		});
	}

	private BaseActivity getBaseActivity() {
		return ((BaseActivity) getActivity());
	}
}