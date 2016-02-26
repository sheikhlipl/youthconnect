package com.luminous.dsys.youthconnect.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.replicator.Replication;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.activity.Application;
import com.luminous.dsys.youthconnect.activity.MainActivity;
import com.luminous.dsys.youthconnect.pojo.Doc;
import com.luminous.dsys.youthconnect.util.Constants;

import java.io.IOException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interfaces
 * to handle interaction events.
 * Use the {@link ShowcaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowcaseFragment extends Fragment implements
        Replication.ChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CAMP_CODE = "campcode";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParamCampCode;
    private String mParam2;
    private Application application;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListView listView;
    private ShowcaseDataAdapterExp adapter;
    private static final String TAG = "ShowcaseFragment";
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowcaseFragment newInstance(String param1, String param2) {
        ShowcaseFragment fragment = new ShowcaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAMP_CODE, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ShowcaseFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_showcase, container, false);

        if(application == null && getActivity() != null
                && getActivity() instanceof MainActivity) {
            application = ((MainActivity) getActivity()).application;
        }
        try {
            showListInListView(getView());
        } catch (CouchbaseLiteException exception){
            Log.e(TAG, "onCreateView()", exception);
        } catch(IOException exception){
            Log.e(TAG, "onCreateView()", exception);
        } catch(Exception exception){
            Log.e(TAG, "onCreateView()", exception);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void setNoRecordsTextView(View view, List<Doc> listWhichIsSetToList){
        TextView tvNoRecordFoundText = (TextView) view.findViewById(R.id.tvNoRecordFoundText);
        if(listWhichIsSetToList != null && listWhichIsSetToList.size() > 0) {
            tvNoRecordFoundText.setVisibility(View.GONE);
        } else {
            tvNoRecordFoundText.setVisibility(View.VISIBLE);
        }
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
        public void onFragmentInteraction(String str);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.e("gif--", "fragment back key is clicked");
                    getActivity().getSupportFragmentManager().popBackStack(Constants.FRAGMENT_HOME_SHOWCASE, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
        if (visible && isResumed()){
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

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(Constants.FRAGMENT_HOME_SHOWCASE_PAGE);
        //setPage(getView());
    }

    @Override
    public void onDestroy() {
        System.gc();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        System.gc();
        super.onDestroyView();
    }

    @Override
    public void changed(Replication.ChangeEvent event) {
        Replication replication = event.getSource();
        com.couchbase.lite.util.Log.i(TAG, "Replication : " + replication + "changed.");
        if(!replication.isRunning()){
            String msg = String.format("Replicator %s not running", replication);
            com.couchbase.lite.util.Log.i(TAG, msg);
        } else{
            int processed = replication.getCompletedChangesCount();
            int total = replication.getChangesCount();
            String msg = String.format("Replicator processed %d / %d", processed, total);
            com.couchbase.lite.util.Log.i(TAG, msg);
        }

        if(event.getError() != null){
            showError("Sync error", event.getError());
        }

        if(getActivity() != null && getView() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        showListInListView(getView());
                    } catch (CouchbaseLiteException exception) {
                        com.couchbase.lite.util.Log.e(TAG, "changed()", exception);
                    } catch (IOException exception) {
                        com.couchbase.lite.util.Log.e(TAG, "changed()", exception);
                    }
                }
            });
        }
    }

    public void showError(final String  errorMessage, final Throwable throwable){
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String msg = String.format("%s: %s", errorMessage, throwable);
                    com.couchbase.lite.util.Log.e(TAG, msg, throwable);
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showListInListView(View view) throws CouchbaseLiteException, IOException {
        listView = (ListView) view.findViewById(R.id.showcaseEventRecycleList);
        if(application == null && getActivity() != null
                && getActivity() instanceof MainActivity) {
            application = ((MainActivity) getActivity()).application;
        }

        if(getActivity() != null
                && application != null
                && application.getPublishedDocQuery(application.getDatabase()) != null) {
            adapter = new ShowcaseDataAdapterExp(getActivity(),
                    application.getPublishedDocQuery(application.getDatabase()).toLiveQuery());
            listView.setAdapter(adapter);
        }
    }
}