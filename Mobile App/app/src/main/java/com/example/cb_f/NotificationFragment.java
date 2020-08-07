package com.example.cb_f;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.URL;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class NotificationFragment extends Fragment {



    FirebaseRecyclerAdapter<Notification, NotificationCardViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference userRef;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = view.findViewById(R.id.notification_recycler_view);
        RecyclerView.LayoutManager layoutManager =new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Toolbar toolbar = view.findViewById(R.id.tool_bar_noti);
        toolbar.setTitle("Notification");
        toolbar.setTitleTextColor(getResources().getColor(R.color.OnPrimary));
        progressBar = view.findViewById(R.id.pbar_n);

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.child("Notifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }
                else{
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    public void DisplayNotification(){
        FirebaseRecyclerOptions<Notification> options =
                new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(userRef.child("Notifications"), Notification.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Notification,NotificationCardViewHolder    >(options) {
            @NonNull
            @Override
            public NotificationCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.notification_card, viewGroup, false);

                return new NotificationCardViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(@NonNull NotificationCardViewHolder holder, int position, final Notification model) {

                holder.noti_title.setText(model.getNotification_title());
                holder.noti_body.setText(model.getNotification_body());
                Log.d(TAG, "onBindViewHolder: "+ model.getNotification_title());

                if(model.getIcon_uri()!=null){
                    try {
                        Uri uri=Uri.parse(model.getIcon_uri());
                        Log.d(TAG, "onBindViewHolder: "+uri);
                        RequestOptions options = new RequestOptions()
                                .centerCrop()
                                .fitCenter();
                        Glide.with(getActivity())
                                .load(uri)
                                .apply(options)
                                .into(holder.noti_icon);
                    }catch (Exception e){

                    }
                }
            }

        };
        if(recyclerView == null){
            Log.d(TAG, "DisplayNotification: Null RecyclerView");
            return;
        }
        if(adapter == null ){
            Log.d(TAG, "DisplayNotification: Null adapter");
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
                if(itemCount == 0){

                }
            }


        });

    }

    public class NotificationCardViewHolder extends RecyclerView.ViewHolder{

        public TextView noti_title, noti_body;
        public ImageView noti_icon;

        public NotificationCardViewHolder(View v) {
            super(v);
            noti_title = (TextView) v.findViewById(R.id.notification_title);
            noti_body = (TextView) v.findViewById(R.id.notification_body);
            noti_icon = (ImageView) v.findViewById(R.id.notification_icon);

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        DisplayNotification();
    }
}
