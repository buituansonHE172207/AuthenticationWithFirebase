package com.kas.authenticationwithfirebase.ui.friend;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomViewModel;
import com.kas.authenticationwithfirebase.ui.message.MessageActivity;
import com.kas.authenticationwithfirebase.ui.userProfile.UserProfileActivity;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FriendFragment extends Fragment {
    private RecyclerView recyclerViewFriends, recyclerViewSearchResults;
    private FriendAdapter friendAdapter, searchAdapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ViewSwitcher viewSwitcher;
    private FriendViewModel friendViewModel;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDarkMode();
        initializeViewModels();
        setupViews(view);
        observeFriend();
    }

    private void setupDarkMode() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void initializeViewModels() {
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
    }

    private void setupViews(View view) {
        viewSwitcher = view.findViewById(R.id.view_switcher);
        recyclerViewFriends = view.findViewById(R.id.recycler_view_friends);
        recyclerViewSearchResults = view.findViewById(R.id.recycler_view_search_results);
        progressBar = view.findViewById(R.id.progress_bar);
        searchView = view.findViewById(R.id.search_view);
        searchView.setQueryHint("Search users");

        // Set up adapters
        friendAdapter = new FriendAdapter();
        searchAdapter = new FriendAdapter();

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFriends.setAdapter(friendAdapter);

        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearchResults.setAdapter(searchAdapter);
        // Observe the list of friends
        observeFriend();

        // Set up listeners for the adapters
        setAdapterListeners();

        // Set up search view listener
        setupSearchViewListener();
    }

    private void observeFriend() {
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
    }

    private void setAdapterListeners() {
        friendAdapter.setOnFriendClickListener(friend -> {
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            intent.putExtra("userId", friend.getUserId());
            startActivity(intent);
        });

        friendAdapter.setOnFriendButtonClickListener(friend -> {
            friendViewModel.createChatRoom(friend.getUserId()).observe(getViewLifecycleOwner(), resource -> {
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    Intent intent = new Intent(getActivity(), MessageActivity.class);
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
//            friendViewModel.addFriend(friend.getUserId()).observe(getViewLifecycleOwner(), resource -> {
//                if (resource.getStatus() == Resource.Status.SUCCESS) {
//                    // Show success message
//                    friendViewModel.getFriendsList();
//                } else if (resource.getStatus() == Resource.Status.ERROR) {
//                    // Show error message
//                } else if (resource.getStatus() == Resource.Status.LOADING) {
//                    // Show loading
//                }
//            });
            Intent intent = new Intent(getActivity(), UserProfileActivity.class);
            intent.putExtra("userId", friend.getUserId());
            startActivity(intent);
        });
        searchAdapter.setOnFriendButtonClickListener(friend -> {
            friendViewModel.createChatRoom(friend.getUserId()).observe(getViewLifecycleOwner(), resource -> {
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    Intent intent = new Intent(getActivity(), MessageActivity.class);
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
    }

    private void setupSearchViewListener() {
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
    }

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