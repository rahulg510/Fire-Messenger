package com.fire.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FindUserActivity extends AppCompatActivity {
    
    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private  RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<UserObject> userList, contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);
        userList = new ArrayList<>();
        contactList = new ArrayList<>();
        initializeRecyclerView();
        getContactList();
        
    }

    /**
     * get a cursor that will allow to iterate over the contacts in the phone and add those into our list to of all the contacts to display
     * normalize the phone number value and put ISO prefix
     */
    private void getContactList(){

        String isoPref = getCountryISO();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()){
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ","");
            phone = phone.replace("-","");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(!phone.substring(0,1).equals("+")){
               phone = isoPref + phone;
            }
            UserObject obj = new UserObject("",name,phone);
            contactList.add(obj);
            getUserDetails(obj);

        }
    }

    /**
     * Search the database to check if user exists in the data
     * this is so that all the contacts on the phone will be check to see if any those contacts
     * are already using Fire Messenger
     * @param obj user object to check if exists in the database
     */
    private void getUserDetails(UserObject obj) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(obj.getPhone());
        //add found contact into user list
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //if query found a match than get the name and phone values and add it userList
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "",
                            name = "";
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if (snap.child("phone").getValue() != null)
                            phone = snap.child("phone").getValue().toString();
                        if (snap.child("name").getValue() != null) {
                            name = snap.child("name").getValue().toString();
                        }
                       UserObject user = new UserObject(snap.getKey(), name,phone);
                        if(name.equals(phone)){
                            for(UserObject obj: contactList){
                                if(obj.getPhone().equals(phone)){
                                    user.setName(obj.getName());
                                }
                            }
                        }
                        userList.add(user);
                        mUserListAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * get the country iso for contacts in the phone that do not have an ISO present already
     * use the ISO of the phone as default
     * @return the default ISO of the country based on the location of the device
     */
    private String getCountryISO(){
        String iso = null;
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso() != null){
            if(!telephonyManager.getNetworkCountryIso().equals("")){
                iso = telephonyManager.getNetworkCountryIso().toString();
            }
        }

        return CountryToPhonePrefix.getPhone(iso);
    }

    /**
     * setting up the view to get the contact list and for the contacts to be displayed vertically
     */
    private void initializeRecyclerView() {
        mUserList = findViewById(R.id.userList);
        //scrolls smoothly
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        //layout for the list
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(userList);
        mUserList.setAdapter(mUserListAdapter);
    }
}
