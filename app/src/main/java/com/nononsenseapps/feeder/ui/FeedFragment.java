package com.nononsenseapps.feeder.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nononsenseapps.feeder.R;
import com.nononsenseapps.feeder.model.RssLoader;
import com.shirwa.simplistic_rss.RssItem;
import com.squareup.picasso.Picasso;

import java.util.List;


public class FeedFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<RssItem>> {

    private static final int FEED_LOADER = -1;

    private static final String ARG_FEED_TITLE = "feed_title";
    private static final String ARG_FEED_URL = "feed_url";
    private FeedAdapter mAdapter;
    private AbsListView mRecyclerView;
    //private LinearLayoutManager mLayoutManager;
    // TODO change this
    private String title = "Android Police Dummy";
    private String url = "http://feeds.feedburner.com/AndroidPolice";

    public FeedFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FeedFragment newInstance(String title, String url) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FEED_TITLE, title);
        args.putString(ARG_FEED_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((FeedActivity) activity)
                .onFragmentAttached(getArguments().getString(ARG_FEED_TITLE));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_FEED_TITLE);
            url = getArguments().getString(ARG_FEED_URL);
        }

        setHasOptionsMenu(false);

        // Load some RSS
        getLoaderManager().restartLoader(FEED_LOADER, new Bundle(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView =
                inflater.inflate(R.layout.fragment_feed, container, false);
        mRecyclerView = (AbsListView) rootView.findViewById(android.R.id.list);

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager(getActivity());
        //mRecyclerView.setLayoutManager(mLayoutManager);

        // I want some dividers
        //mRecyclerView.addItemDecoration(new DividerItemDecoration
        //       (getActivity(), DividerItemDecoration.VERTICAL_LIST));

        // specify an adapter
        mAdapter = new FeedAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        ((BaseActivity) getActivity()).enableActionBarAutoHide(mRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<List<RssItem>> onCreateLoader(final int ID,
            final Bundle bundle) {
        if (ID == FEED_LOADER) {
            return new RssLoader(getActivity(), url);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<List<RssItem>> rssFeedLoader,
            final List<RssItem> rssFeed) {
        mAdapter.setData(rssFeed);
    }

    @Override
    public void onLoaderReset(final Loader<List<RssItem>> rssFeedLoader) {
        mAdapter.setData(null);
    }


    class FeedAdapter extends ArrayAdapter<RssItem> {

        // 64dp at xhdpi is 128 pixels
        private final int defImgWidth = 2 * 128;
        private final int defImgHeight = 2 * 128;
        //private List<RssItem> items = null;

        public FeedAdapter(final Context context) {
            super(context, R.layout.view_story);
        }

        @Override
        public View getView(int hposition, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (getItemViewType(hposition) == 0) {
                    convertView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.padding_header_item, parent,
                                    false);
                } else {
                    convertView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.view_story, parent, false);
                    convertView.setTag(new ViewHolder(convertView));
                }
            }

            if (getItemViewType(hposition) == 0) {
                // Header
                return convertView;
            }

            // Item
            ViewHolder holder = (ViewHolder) convertView.getTag();
            // position in data set
            final int position = hposition - 1;
            final RssItem item = getItem(position);

            holder.link = item.getLink();

            if (item.getTitle() == null) {
                holder.titleTextView.setVisibility(View.GONE);
            } else {
                holder.titleTextView.setVisibility(View.VISIBLE);
                holder.titleTextView.setText(item.getTitle());
            }
            if (item.getDescription() == null) {
                holder.bodyTextView.setVisibility(View.GONE);
            } else {
                holder.bodyTextView.setVisibility(View.VISIBLE);
                //                holder.bodyTextView.setText(android.text.Html.fromHtml(item
                //                        .getDescription()));
                holder.bodyTextView.setText(item.getSnippet());
            }
            if (item.getImageUrl() == null) {
                holder.imageView.setVisibility(View.GONE);
            } else {
                int w = holder.imageView.getWidth();
                if (w <= 0) {
                    w = defImgWidth;
                }
                int h = holder.parent.getHeight();
                if (h <= 0) {
                    h = defImgHeight;
                }
                Picasso.with(getActivity()).load(item.getImageUrl())
                        .resize(w, h).centerCrop().into(holder.imageView);
                holder.imageView.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        //        @Override
        //        public int getCount() {
        //            if (items == null) {
        //                return 0;
        //            } else {
        //                // 1 header + the rest
        //                return 1 + items.size();
        //            }
        //        }

        public void setData(List<RssItem> feed) {
            //this.items = feed;
            clear();
            if (feed != null)
                addAll(feed);

            notifyDataSetChanged();
        }

        // Provide a reference to the type of views that you are using
        public class ViewHolder {
            public final View parent;
            public final TextView titleTextView;
            public final TextView bodyTextView;
            public final ImageView imageView;
            public String link;

            public ViewHolder(View v) {
                //v.setOnClickListener(this);
                parent = v;
                titleTextView = (TextView) v.findViewById(R.id.story_title);
                bodyTextView = (TextView) v.findViewById(R.id.story_body);
                imageView = (ImageView) v.findViewById(R.id.story_image);
            }

            /**
             * OnItemClickListener replacement
             *
             * @param view
             */
            //            @TargetApi(Build.VERSION_CODES.L)
            //            @Override
            //            public void onClick(final View view) {
            //                // Just open in browser for now
            //                Intent i = new Intent(Intent.ACTION_VIEW);
            //                i.setData(Uri.parse(link));
            //                startActivity(i);

                /*
                Intent story = new Intent(getActivity(), StoryActivity.class);
                story.putExtra("title", titleTextView.getText());
                story.putExtra("body", bodyTextView.getText());

                Bundle activityOptions = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(getActivity(),
                                    new Pair<View, String>(titleTextView,
                                            "title"),
                                    new Pair<View, String>(bodyTextView,
                                            "body"),
                                    new Pair<View, String>(imageView, "image"));

                    getActivity().setExitSharedElementListener(new SharedElementListener() {
                                @Override
                                public void remapSharedElements(List<String> names,
                                        Map<String, View> sharedElements) {
                                    super.remapSharedElements(names,
                                            sharedElements);
                                    sharedElements.put("title", titleTextView);
                                    sharedElements.put("body", bodyTextView);
                                    sharedElements.put("image", imageView);
                                }
                            });
                    activityOptions = options.toBundle();
                }

                startActivity(story, activityOptions);*/
            //            }
        }
    }
}
