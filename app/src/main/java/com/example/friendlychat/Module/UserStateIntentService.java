package com.example.friendlychat.Module;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UserStateIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.friendlychat.Module.action.FOO";
    private static final String ACTION_BAZ = "com.example.friendlychat.Module.action.BAZ";

    private static final String ACTION_SAVE_LAST_TIME_ACTIVATION = "com.example.friendlychat.Module.action.last_time_activation";
    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.friendlychat.Module.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.friendlychat.Module.extra.PARAM2";
    private static final String TAG = UserStateIntentService.class.getSimpleName();

    public UserStateIntentService() {
        super("UserStateIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method

    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UserStateIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }
    public static void startActionSaveLastTimeActivation(Context context) {
        Intent intent = new Intent(context, UserStateIntentService.class);
        intent.setAction(ACTION_SAVE_LAST_TIME_ACTIVATION);
        context.startService(intent);
    }
    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UserStateIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE_LAST_TIME_ACTIVATION.equals(action)) {
                handleActionSaveLastTimeActivation();
            }
        }
    }

    private void handleActionSaveLastTimeActivation() {

        Log.d(TAG, "service work form the background");
       /* final android.os.Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               Log.d(TAG, "from handler after 2 seconds");
                Toast.makeText(UserStateIntentService.this, "after 2s", Toast.LENGTH_SHORT).show();
            }
        }, 2000);*/

    }


    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
