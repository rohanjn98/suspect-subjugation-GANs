package com.example.cb_f;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import android.transition.ChangeBounds;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class HomeFragment extends Fragment {

    public static final int MY_PERMISSON_INTERNET_REQUEST = 99;

    FirebaseRecyclerAdapter<Criminal, CriminalCardViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference criminalRef;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FragmentNavigator.Extras extras;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true); //must be called before any firebase instance
        }catch (Throwable t){}
        super.onCreate(savedInstanceState);

        if(!isInternetAvailable()){

        }


    }

    private boolean isInternetAvailable() {
        try{
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");
        }catch(Exception e){
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressBar = view.findViewById(R.id.p_bar);
        progressBar.setVisibility(View.VISIBLE);

        Toolbar toolbar = view.findViewById(R.id.tool_bar_home);
        toolbar.setTitleTextColor(getResources().getColor(R.color.OnPrimary));
        toolbar.setTitle(R.string.app_name);

        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager =new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        database = FirebaseDatabase.getInstance();
        criminalRef = database.getReference("Criminals");
        Log.d(TAG, "onCreateView: " + criminalRef);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        DisplayCriminal();
    }

    public void DisplayCriminal(){
        FirebaseRecyclerOptions<Criminal> options =
                new FirebaseRecyclerOptions.Builder<Criminal>()
                        .setQuery(criminalRef, Criminal.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Criminal, CriminalCardViewHolder>(options) {
            @NonNull
            @Override
            public CriminalCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.card_criminal, viewGroup, false);

                return new CriminalCardViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(@NonNull final CriminalCardViewHolder holder, int position, final Criminal model) {

                holder.name.setText(model.getName());
                holder.age.setText(model.getAge() + " years ");
                Log.d(TAG, "onBindViewHolder: "+ model.getName());
                ViewCompat.setTransitionName(holder.photo, "photoTransition");
                try {
                    URL url = new URL(model.getPhoto());
                    Log.d(TAG, "onBindViewHolder: "+url);
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .fitCenter();
                    final Uri uri = Uri.parse(url.toString());
                    Glide.with(getActivity())
                            .load(uri).apply(options).into(holder.photo);
                } catch (IOException e) {
                    //Log.e(TAG, e.getMessage());
                }
                holder.photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("Id", model.getId());
                        extras  = new FragmentNavigator.Extras.Builder()
                                .addSharedElement(holder.photo, holder.photo.getTransitionName())
                        .build();
                        final NavController navController = NavHostFragment.findNavController(HomeFragment.this);
                        //HomeFragmentDirections.actionHomeFragmentToCriminalInfoFragment();
                        //HomeFragmentDirections direction = HomeFragmentDirections.homeAction(holder.photo.getTransitionName());

                        navController.navigate(R.id.action_homeFragment_to_criminalInfoFragment,
                                bundle, null, extras);

                    }
                });
                /*holder.photo.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.homeAction, bundle));*/

                holder.name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }

        };
        if(recyclerView == null){
            Log.d(TAG, "DisplayCriminal: Null RecyclerView");
            return;
        }
        if(adapter == null ){
            Log.d(TAG, "DisplayCriminal: Null adapter");
            return;
        }
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        Log.d("ONStart", "Listening");
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                progressBar.setVisibility(View.GONE);
                adapter.unregisterAdapterDataObserver(this);
            }
        });
    }

    public class CriminalCardViewHolder extends RecyclerView.ViewHolder{

        public TextView name, age;
        public ImageView photo;

        public CriminalCardViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.name);
            age = (TextView) v.findViewById(R.id.age);
            photo = (ImageView) v.findViewById(R.id.photo);

        }

    }

    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }



}
