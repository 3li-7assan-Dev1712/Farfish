package com.example.farfish.fragments.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.farfish.Adapters.MessagesListAdapter;
import com.example.farfish.Module.preferences.SharedPreferenceUtils;
import com.example.farfish.Module.util.Connection;
import com.example.farfish.Module.workers.CleanUpOldDataPeriodicWork;
import com.example.farfish.R;
import com.example.farfish.data.MainViewModel;
import com.example.farfish.data.repositories.ChatsRepository;
import com.example.farfish.databinding.FragmentUserChatsBinding;
import com.example.farfish.fragments.dialogs.InternetConnectionDialog;
import com.example.farfish.fragments.profile.UserProfileFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * This fragment is responsible for just display the chats data (all chats between the user and other users
 * along with photos and last messages and time for each chat in the list).
 * <p>
 * and following the separation of concerns principles the fragment dose'nt responsible for providing
 * the chats logic (reading from the database and keep track of any changes), it just
 * responsible for displaying the ready and formatted data.
 */
@AndroidEntryPoint
public class UserChatsFragment extends Fragment implements MessagesListAdapter.ChatClick,
        ChatsRepository.DataReadyInterface, UserProfileFragment.CleanViewModel {

    public MainViewModel mainViewModel;
    @Inject
    public MessagesListAdapter mListAdapter;
    private NavController mNavController;
    private FragmentUserChatsBinding mBinding;

    public UserChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        mBinding = FragmentUserChatsBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();

        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            navigateToSignIn();
        }
        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(mBinding.mainToolbarFrag);
        // start the periodic work
        uniquelyScheduleCleanUPWorker();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            NavBackStackEntry backStackEntry = mNavController.getBackStackEntry(R.id.nav_graph);
            mainViewModel = new ViewModelProvider(
                    backStackEntry,
                    HiltViewModelFactory.create(requireContext(), backStackEntry)
            ).get(MainViewModel.class);

            mBinding.userContactsRecyclerView.setAdapter(mListAdapter);
            mainViewModel.getChatsRepository().setDataReadyInterface(this);
            mListAdapter.setIsGeneral(true);
            mListAdapter.setChatClick(this);
            mainViewModel.getUserChats().observe(getViewLifecycleOwner(), userChats -> {
                mListAdapter.submitList(userChats);
                mBinding.userChatsProgressBar.setVisibility(View.GONE);
                if (userChats.size() == 0) {
                    // when there is no chat replace the current white meaningless screen
                    // with indicative image! (new update after stoping (+_*)
                }
            });
        }
        checkUserConnection();
        UserProfileFragment.setCleaner(this);
        return view;
    }


    /**
     * overriding this method to provide the item selection functionality
     *
     * @param item it takes the selected item from the menu bar
     *             to response to the user accordingly.
     * @return return by a boolean type to tell the OS (the android operating system)
     * that we handle this method and should be executed as it.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!Connection.isUserConnected(requireContext())) {
            new InternetConnectionDialog().show(requireActivity().getSupportFragmentManager(), "internet_dialog");
            return true;
        }
        int id = item.getItemId();
        switch (id) {
            case R.id.sign_out:
                SharedPreferenceUtils.saveUserSignOut(requireContext());
                onDestroy();
                mNavController.navigate(R.id.action_userChatsFragment_to_fragmentSignIn);
                break;
            case R.id.go_to_profile:
                mNavController.navigate(R.id.action_userChatsFragment_to_userProfileFragment);
                break;
            case R.id.report_issue:
                sendEmailIssue();
                break;
        }
        return true;
    }

    /**
     * override the method to inflate the custom menu bar.
     *
     * @param menu     the menu that contain the items (like the person icon to open the ProfileFragment)
     * @param inflater a menu inflater to inflate the menu resource.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    /**
     * when a user tab in {Report Issue} from the dropdown menu this method
     * will be called to provide sending the issue functionality.
     */
    private void sendEmailIssue() {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain")
                .putExtra(Intent.EXTRA_EMAIL, "alihassan17122002@gmail.com")
                .putExtra(Intent.EXTRA_REFERRER)
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_issue_email))
                .putExtra(Intent.EXTRA_TEXT, getString(R.string.type_your_issue));
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_app_to_send_emial)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(requireActivity(), getString(R.string.no_app_found), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * when calling this method it will let the user navigate to the SignInFragment.
     */
    private void navigateToSignIn() {
        mNavController.navigate(R.id.fragmentSignIn);
    }

    /**
     * this method checks the internet connection and inform the user if so
     * by a snackbar.
     */
    private void checkUserConnection() {
        if (!Connection.isUserConnected(requireContext())) {
            Snackbar.make(requireActivity().findViewById(R.id.bottom_nav), R.string.user_offline_msg, BaseTransientBottomBar.LENGTH_LONG)
                    .setAnchorView(R.id.bottom_nav).show();
        }
    }

    /**
     * this method is responsible for uniquely start a periodic work using the WorkManager SDK the work job
     * cleans the old messages (older than 3 months) and statuses (older than 2 days).
     *
     *
     */
    private void uniquelyScheduleCleanUPWorker() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
        PeriodicWorkRequest cleanUpRequest =
                new PeriodicWorkRequest.Builder(CleanUpOldDataPeriodicWork.class, 3, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build();
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "cleanUpWork",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanUpRequest);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainViewModel != null) {
            mainViewModel.getChatsRepository().removeValueEventListener();
            mainViewModel.getMessagingRepository().resetLastTimeSeen();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    /**
     * when the user tab on a chat this method will be called to get the selected chat info and then
     * navigate to the ChatsFragment.
     *
     * @param position take the position of the selected chat in the RecyclerView to be used
     *                 in the ChatsRepo to get the info from the list.
     */
    @Override
    public void onChatClick(int position) {
        String targetUserId = mainViewModel.getChatsRepository().getMessageInPosition(position).getTargetId();
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("target_user_id", targetUserId);
        mNavController.navigate(R.id.action_userChatsFragment_to_chatsFragment, primaryDataBundle);
    }


    /**
     * following the separation of concerns principles the Fragment is not responsible for the data logic
     * it just responsible this data.
     * So the ChatsRepository (inside the MainViewModel) do this logic (getting the chats data and keep track
     * of any changes). Then call this method to update the data in the fragment.
     */
    @Override
    public void dataIsReady() {
        mainViewModel.updateChats();
        if (mBinding != null) mBinding.userChatsProgressBar.setVisibility(View.GONE);
    }

    /**
     * this method will be called to make the view model null, which it is really
     * needed in case the user signs out and then signs in with different
     * account in the same time.
     */
    @Override
    public void cleanViewModel() {
        this.mainViewModel = null;
    }
}