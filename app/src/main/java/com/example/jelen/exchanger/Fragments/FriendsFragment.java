package com.example.jelen.exchanger.Fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jelen.exchanger.Activities.HomeActivity;
import com.example.jelen.exchanger.Activities.user;
import com.example.jelen.exchanger.Bluetooth.ChatService;
import com.example.jelen.exchanger.DeviceListActivity;
import com.example.jelen.exchanger.FriendListAdapter;
import com.example.jelen.exchanger.FriendModel;
import com.example.jelen.exchanger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the  factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("ALL")
public class FriendsFragment extends Fragment {
    private static final String FRIEND_REQUEST_CODE = "FRIEND_REQUEST_";
    private static final int BT_DISCOVERABLE_TIME = 120;
    private static final int ADD_POINTS_NEW_FRIEND = 10;
    private FriendListAdapter mAdapter;
    private ArrayList<FriendModel> mFriends;
    private ArrayList<String> friendsList;

    public static boolean pauseWaitingForFriendsList = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        ListView lvHighscore = (ListView) view.findViewById(R.id.highscore_list);

        mFriends = new ArrayList<>();
        friendsList = new ArrayList<>();

        mAdapter = new FriendListAdapter(getActivity().getApplicationContext(), mFriends);
       // lvHighscore.setAdapter(mAdapter);

        Button friends = (Button) view.findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friendsActivity = new Intent(getActivity(), com.example.jelen.exchanger.Activities.FriendsActivity.class);
                startActivity(friendsActivity);
            }
        });

        Button btnAddFriend = (Button) view.findViewById(R.id.btn_add_friend);

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null) {
                    Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
                    return;
                }
                ensureDiscoverable(bluetoothAdapter);   //onActivityResult checks if discoverability in enabled and then sends friend request
            }
        });
        getFriendsFromServer();
        pauseWaitingForFriendsList = true;

        return view;
    }

    public void getFriendsFromServer() {
        friendsList.clear();
        mFriends.clear();
        mAdapter.clear();

        String loggedUserId = com.example.jelen.exchanger.Activities.HomeActivity.loggedUser.getUid();;
        getFriendData(loggedUserId);

        DatabaseReference userFriends = FirebaseDatabase.getInstance().getReference("User").child(loggedUserId).child("friends");
        userFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String json = singleSnapshot.toString();

                    String friendUid = json.substring(json.indexOf("value = ") + 8, json.length() - 2);

                    if (!friendUid.equals("")) {
                        if (!friendsList.contains(friendUid)) {
                            getFriendData(friendUid);
                            friendsList.add(friendUid);
                        }
                    }
                }
                pauseWaitingForFriendsList = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getFriendData(final String friendUid) {
        FirebaseDatabase.getInstance().getReference("User").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              //  final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading friends...", true);
                final user friend = dataSnapshot.getValue(user.class);

                if (friend != null) {

                } else {
                   // Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.avatar);
                   // mFriends.add(new FriendModel("fake user\n" + friendUid, friendUid));
                   // bitmap = null;


                    //mAdapter.notifyDataSetChanged();
                }
               // progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private int findModelById(String uId) {
        int i = 0;
        while (i < mFriends.size()) {
            FriendModel model = mFriends.get(i);
            String fId = (String) model.getuId();
            if (fId.equals(uId))
                break;
            i++;
        }
        return i;
    }

    private void addNewFriend() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        };
        Thread btThread = new Thread(r);
        btThread.start();
    }

    //----------------------------------------------------------------------------------------------------------------------------------

    private final static String TAG = "Bluetooth";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private String connectedDeviceName = null;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private ChatService chatService = null;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            sendFriendRequest();
                            break;
                        case ChatService.STATE_CONNECTING:
                            sendFriendRequest();
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String message = new String(readBuf, 0, msg.arg1);

                    int _char = message.lastIndexOf("_");
                    String messageCheck = message.substring(0, _char + 1);
                    final String friendsUid = message.substring(_char + 1);

                    if (messageCheck.equals(FRIEND_REQUEST_CODE)) {
                        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(500);

                        final String myUid = HomeActivity.loggedUser.getUid();
                        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("User").child(myUid);
                        final DatabaseReference dbRef = database.child("friends");

                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Confirm friend request")
                                        .setMessage("Are you sure you want to become friends with a device\n\n" + connectedDeviceName)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        List<String> friendsList = new ArrayList<>();

                                                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                                            String json = singleSnapshot.toString();

                                                            //TODO: deserialize via class, not like this
                                                            String friendUid = json.substring(json.indexOf("value = ") + 8, json.length() - 2);
                                                            friendsList.add(friendUid);
                                                        }

                                                        if (friendsList.contains(friendsUid)) {
                                                            Toast.makeText(getActivity(), "You already have this friend!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                          friendsList.add(friendsUid);
                                                           dbRef.setValue(friendsList);

                                                          getFriendData(friendsUid);

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(getActivity(), "You declined friend request", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setIcon(R.mipmap.logo2)
                                        .show();
                            }
                        });
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getActivity(), "Connected to " + connectedDeviceName + "\nClose upper window and confirm friend request.", Toast.LENGTH_LONG).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getActivity(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == BT_DISCOVERABLE_TIME) {
                    setupChat();
                    addNewFriend();
                } else {
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(DeviceListActivity.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try {
            chatService.connect(device, secure);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error! Other user must click on ADD FRIEND button.", Toast.LENGTH_LONG).show();
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

    private void ensureDiscoverable(BluetoothAdapter bluetoothAdapter) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERABLE_TIME);
        startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT);
    }

    private void sendMessage(String message) {
        if (chatService.getState() != ChatService.STATE_CONNECTED) {
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);
            outStringBuffer.setLength(0);
        }
    }

    private boolean setupChat() {
        chatService = new ChatService(getActivity(), handler);
        outStringBuffer = new StringBuffer("");

        if (chatService.getState() == ChatService.STATE_NONE) {
            chatService.start();
        }

        return true;
    }

    private void sendFriendRequest() {
        String message = FRIEND_REQUEST_CODE + HomeActivity.loggedUser.getUid();
       // String message = FRIEND_REQUEST_CODE;
        sendMessage(message);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }

    public ArrayList<String> getFriendsList() {
        return friendsList;
    }
}
