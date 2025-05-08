package lk.handyhub.handyhub.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfileViewAdapter extends FragmentStateAdapter {

    public ProfileViewAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        if (position == 1) {
            return new WorkFragment();
        }
        return new MeFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
