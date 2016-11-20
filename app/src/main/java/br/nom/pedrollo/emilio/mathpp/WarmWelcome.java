package br.nom.pedrollo.emilio.mathpp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import br.nom.pedrollo.emilio.mathpp.utils.NonSwipeableViewPager;

public class WarmWelcome extends FragmentActivity {

    PageAdapter pageAdapter;
    NonSwipeableViewPager viewPager;


    public final static int PAGE_WELCOME = 0;
    public final static int PAGE_SETUP_USER = 1;
//    public final static int PAGE_INSTRUCTIONS = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pageAdapter = new PageAdapter(getSupportFragmentManager());

        viewPager = (NonSwipeableViewPager) findViewById(R.id.welcome_pager);
        viewPager.setAdapter(pageAdapter);
    }

    public static class WarmWelcomePage1Fragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_welcome_page_1, container, false);
            assert container != null;
            Button configureNewUser = (Button) view.findViewById(R.id.welcome_config_user);
            configureNewUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((NonSwipeableViewPager) container).setCurrentItem(PAGE_SETUP_USER);
                }
            });
            return view;
        }
    }

    public static class WarmWelcomePage2Fragment extends Fragment {
        String selectedRole = "";

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_welcome_page_2, container, false);
            assert container != null;

            RadioButton radioButtonStudent = (RadioButton) view.findViewById(R.id.radio_student);
            RadioButton radioButtonMonitor = (RadioButton) view.findViewById(R.id.radio_monitor);
            RadioButton radioButtonTeacher = (RadioButton) view.findViewById(R.id.radio_teacher);

            radioButtonStudent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) selectedRole = "student";
                }
            });
            radioButtonMonitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) selectedRole = "monitor";
                }
            });
            radioButtonTeacher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) selectedRole = "teacher";
                }
            });

            final EditText nameEditor = (EditText) view.findViewById(R.id.welcome_page2_name);

            Button nextButton = (Button) view.findViewById(R.id.welcome_page2_next);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (nameEditor.getText().toString().equals("")){
                        Toast.makeText(getContext(),"Please enter a name",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedRole.equals("")){
                        Toast.makeText(getContext(),"Please enter a role",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                    prefs.edit()
                            .putString(getResources().getString(R.string.prefs_key_display_name),nameEditor.getText().toString())
                            .putString(getResources().getString(R.string.prefs_key_user_category),selectedRole)
                            .putBoolean("FIRST_RUN",false)
                            .apply();




                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();

//                    ((NonSwipeableViewPager) container).setCurrentItem();
                }
            });
            Button prevButton = (Button) view.findViewById(R.id.welcome_page2_back);
            prevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((NonSwipeableViewPager) container).setCurrentItem(WarmWelcome.PAGE_WELCOME);
                }
            });
            return view;
        }
    }

    private class PageAdapter extends FragmentStatePagerAdapter {
        private int NUM_ITEMS = 2;

        PageAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }


        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGE_WELCOME:
                    return new WarmWelcomePage1Fragment();
                case PAGE_SETUP_USER:
                    return new WarmWelcomePage2Fragment();
//                case 2:
//                    return new WarmWelcomePage2Fragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }

}
