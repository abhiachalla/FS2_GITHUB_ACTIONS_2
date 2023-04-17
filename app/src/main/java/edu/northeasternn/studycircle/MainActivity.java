package edu.northeasternn.studycircle;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import edu.northeasternn.studycircle.util.HomePagerAdapter;

public class MainActivity extends AppCompatActivity {


    private ViewPager2 homeScreenViewPager;
    private TabLayout homeScreenTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeScreenTabs = findViewById(R.id.homeScreenToolbar);

        int [] tabIcons = {R.drawable.home_icon_foreground,R.drawable.groups_icon_foreground,R.drawable.profile_icon_foreground};
        int [] tabLabels = {R.string.home,R.string.groups,R.string.profile};
        TabLayout.Tab homeTab = homeScreenTabs.newTab();
        homeTab.setIcon(R.drawable.home_icon_foreground);
        homeTab.setText(R.string.home);

        TabLayout.Tab groupsTab = homeScreenTabs.newTab();
        groupsTab.setIcon(R.drawable.groups_icon_foreground);
        groupsTab.setText(R.string.groups);

        TabLayout.Tab profileTab = homeScreenTabs.newTab();
        profileTab.setIcon(R.drawable.profile_icon_foreground);
        profileTab.setText(R.string.profile);


        homeScreenTabs.addTab(homeTab);
        homeScreenTabs.addTab(groupsTab);
        homeScreenTabs.addTab(profileTab);
        homeScreenTabs.setTabGravity(TabLayout.GRAVITY_FILL);

        homeScreenViewPager = findViewById(R.id.homeScreenPager);
        final HomePagerAdapter homePagerAdapter = new HomePagerAdapter(this);
        homeScreenViewPager.setAdapter(homePagerAdapter);
        new TabLayoutMediator(homeScreenTabs, homeScreenViewPager, (tab, position) -> {
            tab.setIcon(tabIcons[position]);
            tab.setText(tabLabels[position]);
        }).attach();
//        final MainActivityTabAdapter myAdapter = new MainActivityTabAdapter(
//                getSupportFragmentManager(), homeScreenTabs.getTabCount());
//        homeScreenViewPager.setAdapter(myAdapter);

//        int color = Color.parseColor("#000000");
//        homeScreenTabs.getTabAt(homeScreenViewPager.getCurrentItem()).getIcon()
//                .setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        //homeScreenViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(homeScreenTabs));
        homeScreenViewPager.setOffscreenPageLimit(2);
        homeScreenTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                homeScreenViewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
}