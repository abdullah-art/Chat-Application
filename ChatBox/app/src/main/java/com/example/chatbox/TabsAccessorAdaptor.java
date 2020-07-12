package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdaptor extends FragmentPagerAdapter {
    public TabsAccessorAdaptor(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
            {
                chats chatsFragment=new chats();
                return chatsFragment;
            }
            case 1:
            {
                groups groupsFragment=new groups();
                return groupsFragment;
            }
            case 2:
            {
                contacts contactsFragment=new contacts();
                return contactsFragment;
            }
            case 3:
            {
                RequestFragment requestFragment=new RequestFragment();
                return requestFragment;
            }
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
            {
                return "Chats";
            }
            case 1:
            {
                return "Groups";
            }
            case 2:
            {
                return "Friends";
            }
            case 3:
            {
                return "Requests";
            }
            default:
                return null;
        }
    }
}
