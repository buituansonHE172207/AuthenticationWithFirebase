package com.kas.authenticationwithfirebase.ui.friend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import android.widget.ViewSwitcher;


import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.main.MainActivity;
import com.kas.authenticationwithfirebase.ui.message.MessageActivity;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FriendActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFriends, recyclerViewSearchResults;
    private FriendAdapter friendAdapter, searchAdapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ViewSwitcher viewSwitcher;
    private FriendViewModel friendViewModel;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        viewSwitcher = findViewById(R.id.view_switcher);
        recyclerViewFriends = findViewById(R.id.recycler_view_friends);
        recyclerViewSearchResults = findViewById(R.id.recycler_view_search_results);
        progressBar = findViewById(R.id.progress_bar);
        searchView = findViewById(R.id.search_view);

        // Set up adapters
        friendAdapter = new FriendAdapter();
        searchAdapter = new FriendAdapter();

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendAdapter);

        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSearchResults.setAdapter(searchAdapter);

        // ViewModel setup
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);

        friendAdapter.setOnFriendClickListener(friend -> {
            // Create a new chat room
            friendViewModel.createChatRoom(friend.getUserId()).observe(this, resource -> {
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    Intent intent = new Intent(FriendActivity.this, MessageActivity.class);
                    intent.putExtra("chatRoomId", resource.getData().getChatRoomId());
                    intent.putExtra("chatRoomName", resource.getData().getChatRoomName());
                    startActivity(intent);
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    // Handle error
                } else if (resource.getStatus() == Resource.Status.LOADING) {
                    // Show loading
                }
            });
        });

        searchAdapter.setOnFriendClickListener(friend -> {
            // Add friend
            friendViewModel.addFriend(friend.getUserId()).observe(this, resource -> {
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    // Show success message
                    friendViewModel.getFriendsList();
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    // Show error message
                } else if (resource.getStatus() == Resource.Status.LOADING) {
                    // Show loading
                }
            });
        });

        // Observe the list of friends
        friendViewModel.getFriendsList().observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                friendAdapter.updateFriendList(resource.getData());
                progressBar.setVisibility(View.GONE);
                showFriendList();
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        // Set up search function
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    showFriendList();
                } else {
                    performSearch(newText);
                }
                return false;
            }
        });
        // Initialize Bottom Navigation View
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Set default highlighted item in Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.contact);

        // Setup bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.new_chat) {
                return true;
            } else if (item.getItemId() == R.id.message ) {
                Intent intent = new Intent(FriendActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.contact) {
                // Open friends activity
                //Intent intent = new Intent(FriendActivity.this, FriendActivity.class);
                //startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.settings) {
                // Handle settings action, e.g., open settings activity
                return true;
            }
            return false;
        });
    }

    // Show the friend list in ViewSwitcher
    private void showFriendList() {
        if (viewSwitcher.getCurrentView() != recyclerViewFriends) {
            viewSwitcher.showNext();
        }
    }

    // Show the search results in ViewSwitcher
    private void showSearchResults() {
        if (viewSwitcher.getCurrentView() != recyclerViewSearchResults) {
            viewSwitcher.showNext();
        }
    }

    // Perform search and update the UI
    private void performSearch(String query) {
        showSearchResults();
        friendViewModel.searchFriends(query).observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                searchAdapter.updateFriendList(resource.getData());
                progressBar.setVisibility(View.GONE);
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}