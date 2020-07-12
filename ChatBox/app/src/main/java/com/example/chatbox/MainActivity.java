package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Toolbar main_toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdaptor tabsAccessorAdaptor;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    boolean flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        main_toolbar=(Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);
        getSupportActionBar().setTitle("ChatBox");

        viewPager=(ViewPager)findViewById(R.id.tabs_pager);
        tabsAccessorAdaptor = new TabsAccessorAdaptor(getSupportFragmentManager());

        viewPager.setAdapter(tabsAccessorAdaptor);

        tabLayout =(TabLayout)findViewById(R.id.tab_layout);

        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(firebaseUser==null)
        {
            sendUserToLoginActivity();
        }
        else{
            verifyingUserExistence();
        }
    }

    private void verifyingUserExistence() {
        String userId=firebaseAuth.getCurrentUser().getUid();

        databaseReference.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("username").exists()))
                {
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{
                    flag=false;
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent authIntent=new Intent(MainActivity.this,LoginActivity.class);
        authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(authIntent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent authIntent=new Intent(MainActivity.this,SettingActivity.class);
        if(flag==false)
        {
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(authIntent);
            finish();
        }
        else{
            startActivity(authIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menus,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.signout)
         {
            firebaseAuth.signOut();
            sendUserToLoginActivity();
         }
        else if(item.getItemId()==R.id.find_friends)
        {
            sendUserToFindFriendsActivity();
        }
        else if(item.getItemId()==R.id.settings)
        {
            sendUserToSettingsActivity();
        }
         else if(item.getItemId()==R.id.create_group)
         {
             createNewGroup();
         }
        return true;
    }

    private void sendUserToFindFriendsActivity() {
        Intent authIntent=new Intent(MainActivity.this,FindFriensActivity.class);
        startActivity(authIntent);
    }


    private void createNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.AlertDialogCustom);
        builder.setTitle("Enter Group Name");
        final EditText groupName=new EditText(this);
        groupName.setHint("group name");
        builder.setView(groupName);

        builder.setPositiveButton("Create Group", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String group_name=groupName.getText().toString();
                if(TextUtils.isEmpty(group_name))
                {
                    Toast.makeText(MainActivity.this, "Please enter a group name", Toast.LENGTH_SHORT).show();
                }
                else{
                    databaseReference.child("Groups").child(group_name).setValue("")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(MainActivity.this, group_name+" is created successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

       builder.show();
    }
}
