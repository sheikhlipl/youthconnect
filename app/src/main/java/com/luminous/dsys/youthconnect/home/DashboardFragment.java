package com.luminous.dsys.youthconnect.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.replicator.Replication;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.activity.Application;
import com.luminous.dsys.youthconnect.activity.DocListActivity;
import com.luminous.dsys.youthconnect.activity.MainActivity;
import com.luminous.dsys.youthconnect.activity.QAAnsweredActivity;
import com.luminous.dsys.youthconnect.activity.QAPendingActivity;
import com.luminous.dsys.youthconnect.helper.LiveQueryAdapter;
import com.luminous.dsys.youthconnect.util.Constants;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interfaces
 * to handle interaction events.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment implements
        AbsListView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener,
        Replication.ChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CAMP_CODE = "campcode";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParamCampCode;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    protected String[] mXValuesQA = new String[] {
            "Pending", "Answered", "Published"
    };

    protected String[] mXValuesFeedback = new String[] {
            "Pending", "Submitted"
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAMP_CODE, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamCampCode = getArguments().getString(ARG_CAMP_CODE);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        if(getView() != null){
            init(getView());
        }

        LinearLayout layoutQAnswered = (LinearLayout) view.findViewById(R.id.layoutQAnswered);
        layoutQAnswered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QAAnsweredActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout layoutPendinQA = (LinearLayout) view.findViewById(R.id.layoutPendinQA);
        layoutPendinQA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QAPendingActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout layoutShowcase = (LinearLayout) view.findViewById(R.id.layoutShowcase);
        layoutShowcase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabView);
                    TabLayout.Tab tab = tabLayout.getTabAt(1);
                    tab.select();
                }
            }
        });

        LinearLayout layoutDoc = (LinearLayout) view.findViewById(R.id.layoutDoc);
        layoutDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DocListActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void init(View view){
        Application application = ((MainActivity) getActivity()).application;

        final TextView tvQuestionsAnswered = (TextView) view.findViewById(R.id.tvQuestionsAnswered);
        final TextView tvPendingQuestions = (TextView) view.findViewById(R.id.tvPendingQuestions);
        final TextView tvShowcaseEvents = (TextView) view.findViewById(R.id.tvShowcaseEvents);
        final TextView tvDocuments = (TextView) view.findViewById(R.id.tvDocuments);
        int user_type_id = getActivity().getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getInt(Constants.SP_USER_ID, 0);
        if(user_type_id == 1){
            LiveQuery liveQuery1 = application.getQAAnsweredForAdminQuery(application.getDatabase()).toLiveQuery();
            liveQuery1.addChangeListener(new LiveQuery.ChangeListener() {
                @Override
                public void changed(final LiveQuery.ChangeEvent event) {
                    //TODO: Revise
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            QueryEnumerator enumerator = event.getRows();
                            if (enumerator != null) {
                                int totalAnsweredForCurrentlyLoggedInNodalUser = enumerator.getCount();
                                tvQuestionsAnswered.setText(totalAnsweredForCurrentlyLoggedInNodalUser + "");
                            }
                        }
                    });
                }
            });
            liveQuery1.start();


            LiveQuery liveQuery2 = application.getQAUnAnsweredForAdminQuery().toLiveQuery();
            liveQuery2.addChangeListener(new LiveQuery.ChangeListener() {
                @Override
                public void changed(final LiveQuery.ChangeEvent event) {
                    //TODO: Revise
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            QueryEnumerator enumerator = event.getRows();
                            if (enumerator != null) {
                                int totalAnsweredForCurrentlyLoggedInNodalUser = enumerator.getCount();
                                tvPendingQuestions.setText(totalAnsweredForCurrentlyLoggedInNodalUser + "");
                            }
                        }
                    });
                }
            });
            liveQuery2.start();

            LiveQuery liveQuery4 = application.getDocForAdminQuery(application.getDatabase()).toLiveQuery();
            liveQuery4.addChangeListener(new LiveQuery.ChangeListener() {
                @Override
                public void changed(final LiveQuery.ChangeEvent event) {
                    //TODO: Revise
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            QueryEnumerator enumerator = event.getRows();
                            if (enumerator != null) {
                                int totalAnsweredForCurrentlyLoggedInNodalUser = enumerator.getCount();
                                tvDocuments.setText(totalAnsweredForCurrentlyLoggedInNodalUser + "");
                            }
                        }
                    });
                }
            });
            liveQuery4.start();
        } else{
            LiveQuery liveQuery1 = application.getQAAnsweredForNodalQuery(application.getDatabase()).toLiveQuery();
            liveQuery1.addChangeListener(new LiveQuery.ChangeListener() {
                @Override
                public void changed(final LiveQuery.ChangeEvent event) {
                    //TODO: Revise
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            QueryEnumerator enumerator = event.getRows();
                            if(enumerator != null){
                                int totalAnsweredForCurrentlyLoggedInNodalUser = enumerator.getCount();
                                tvQuestionsAnswered.setText(totalAnsweredForCurrentlyLoggedInNodalUser+"");
                            }
                        }
                    });
                }
            });
            liveQuery1.start();

            LiveQuery liveQuery2 = application.getQAUnAnsweredForNodalQuery().toLiveQuery();
            liveQuery2.addChangeListener(new LiveQuery.ChangeListener() {
                @Override
                public void changed(final LiveQuery.ChangeEvent event) {
                    //TODO: Revise
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            QueryEnumerator enumerator = event.getRows();
                            if (enumerator != null) {
                                int totalAnsweredForCurrentlyLoggedInNodalUser = enumerator.getCount();
                                tvPendingQuestions.setText(totalAnsweredForCurrentlyLoggedInNodalUser + "");
                            }
                        }
                    });
                }
            });
            liveQuery2.start();

            LiveQuery liveQuery4 = application.getDocForNodalQuery(application.getDatabase()).toLiveQuery();
            liveQuery4.addChangeListener(new LiveQuery.ChangeListener() {
                @Override
                public void changed(final LiveQuery.ChangeEvent event) {
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            QueryEnumerator enumerator = event.getRows();
                            if (enumerator != null) {
                                int totalAnsweredForCurrentlyLoggedInNodalUser = enumerator.getCount();
                                tvDocuments.setText(totalAnsweredForCurrentlyLoggedInNodalUser + "");
                            }
                        }
                    });
                }
            });
            liveQuery4.start();
        }

        LiveQuery liveQuery3 = application.getPublishedDocQuery(application.getDatabase()).toLiveQuery();
        liveQuery3.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(final LiveQuery.ChangeEvent event) {
                (getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        QueryEnumerator enumerator = event.getRows();
                        if (enumerator != null) {
                            int totalAnsweredForCurrentlyLoggedInNodalUser = enumerator.getCount();
                            tvShowcaseEvents.setText(totalAnsweredForCurrentlyLoggedInNodalUser + "");
                        }
                    }
                });
            }
        });
        liveQuery3.start();
    }

    @Override
    public void onClick(View view) {

        if(getActivity() == null){
            return;
        }
        int user_type_id = getActivity().getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getInt(Constants.SP_USER_TYPE, 0);
        int id = view.getId();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        switch (id){
            case R.id.layoutQAnswered:
                if(getActivity() != null) {
                    Intent intent = new Intent(getActivity(), QAAnsweredActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.layoutPendinQA:
                if(getActivity() != null) {
                    Intent intent = new Intent(getActivity(), QAPendingActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.layoutShowcase:
                ShowcaseFragment fragmentShowcase = ShowcaseFragment.newInstance("", "");
                ft.addToBackStack(Constants.FRAGMENT_HOME_SHOWCASE_PAGE);
                ft.commitAllowingStateLoss();
                ft.attach(fragmentShowcase);
                ft.commitAllowingStateLoss();
                break;
            case R.id.layoutDoc:
                if(getActivity() != null) {
                    Intent intent = new Intent(getActivity(), DocListActivity.class);
                    startActivity(intent);
                }
                break;
            default:
                //TODO
                break;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        final View _view = view;
        fetchData(_view);
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        if (getView() != null) {
            fetchData(getView());
        }
    }

    @Override
    public void changed(Replication.ChangeEvent event) {
        fetchData(getView());
    }

    public void fetchData(View view) {

        if(view == null){
            return;
        }

        if(getView() != null){
            init(getView());
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String str) {
        if (mListener != null) {
            mListener.onDashboardFragmentInteraction(this);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interfaces (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onDashboardFragmentInteraction(this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interfaces must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onDashboardFragmentInteraction(DashboardFragment dashboardFragment);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getActivity() == null){
            return;
        }

        if(getView() == null){
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.e("gif--", "fragment back key is clicked");
                    getActivity().finish();

                    return true;
                }
                return false;
            }
        });
        setHasOptionsMenu(true);
    }

    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!getUserVisibleHint() || getView() == null)
        {
            return;
        }
        fetchData(getView());
    }

    @Override
    public void onDestroyView() {
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        System.gc();
        super.onDestroy();
    }
}