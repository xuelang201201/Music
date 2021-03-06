package com.charles.music.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.charles.music.R;
import com.charles.music.application.AppCache;
import com.charles.music.bean.Music;
import com.charles.music.constant.Extra;
import com.charles.music.constant.Keys;
import com.charles.music.fragment.MyFragment;
import com.charles.music.fragment.OnlineFragment;
import com.charles.music.fragment.PlayFragment;
import com.charles.music.fragment.TimerFragment;
import com.charles.music.service.OnPlayerEventListener;
import com.charles.music.service.PlayService;
import com.charles.music.utils.CoverLoader;
import com.charles.music.utils.SystemUtil;
import com.charles.music.utils.ToastUtil;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MusicActivity extends BaseActivity implements OnPlayerEventListener, NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.activity_music_drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.activity_music_navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_menu)
    ImageView mToolbarMenu;
    @BindView(R.id.toolbar_search)
    ImageView mToolbarSearch;
    @BindView(R.id.activity_music_viewpager)
    ViewPager mViewPager;
    @BindView(R.id.play_bar_cover)
    ImageView mPlayBarCover;
    @BindView(R.id.play_bar_progress_bar)
    ProgressBar mPlayBarProgressBar;
    @BindView(R.id.play_bar_play_or_pause)
    ImageView mPlayBarPlayOrPause;
    @BindView(R.id.play_bar_next)
    ImageView mPlayBarNext;
    @BindView(R.id.play_bar_artist)
    TextView mPlayBarArtist;
    @BindView(R.id.play_bar_title)
    TextView mPlayBarTitle;
    @BindView(R.id.activity_music_play_bar)
    View mPlayBar;
    @BindView(R.id.toolbar_tabs)
    SlidingTabLayout mToolbarTabs;
    @BindView(R.id.activity_music_container)
    View mContainer;

    private PlayFragment mPlayFragment;
    private TimerFragment mTimerFragment;
    private MyFragment mMyFragment;
    private OnlineFragment mOnlineFragment;

    private List<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = { "我的", "在线" };
    /**
     * 是否显示播放界面
     */
    private boolean isPlayFragmentShow = false;
    /**
     * 是否显示定时界面
     */
    private boolean isTimerFragmentShow = false;
    /**
     * 上一次按下返回键的时间
     */
    private long lastClickBackTimeMillis;

    private ActionBarDrawerToggle mToggle;
    private MenuItem mTimerItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        if (!checkServiceAlive()) {
            return;
        }

        getPlayService().setOnPlayEventListener(this);

        initView();
        onChangeImpl(getPlayService().getPlayingMusic());
        parseIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extra.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    private void initView() {
        mMyFragment = new MyFragment();
        mFragments.add(mMyFragment);

        mOnlineFragment = new OnlineFragment();
        mFragments.add(mOnlineFragment);

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mToolbarTabs.setViewPager(mViewPager, mTitles);

        mViewPager.setCurrentItem(1);
        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mNavigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(mToolbar);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        mToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    onBackPressed();
                }
            }
        });

        disableNavigationViewScrolls(mNavigationView);
    }

    /**
     * 去除掉NavigationView的滚动条
     * @param navigationView 侧边导航栏
     */
    private void disableNavigationViewScrolls(NavigationView navigationView) {
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

    @Override
    public void onChange(Music music) {
        onChangeImpl(music);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onChange(music);
        }
    }

    @Override
    public void onPlayerStart() {
        mPlayBarPlayOrPause.setSelected(true);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPlayerStart();
        }
    }

    @Override
    public void onPlayerPause() {
        mPlayBarPlayOrPause.setSelected(false);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPlayerPause();
        }
    }

    /**
     * 更新播放进度
     * @param progress 进度
     */
    @Override
    public void onPublish(int progress) {
        mPlayBarProgressBar.setProgress(progress);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPublish(progress);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onTimer(long remain) {
        if (mTimerItem == null) {
            mTimerItem = mNavigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        mTimerItem.setTitle(remain == 0 ? title : SystemUtil.formatTime(title + "                  mm:ss", remain));
    }

    @Override
    public void onMusicListUpdate() {
        if (mMyFragment != null && mMyFragment.isAdded()) {
            mMyFragment.onMusicListUpdate();
        }
    }

    /**
     * 让返回键和home键功能一样
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mPlayFragment != null && isPlayFragmentShow) {
                hidePlayingFragment();
                return false;
            }

            if (mTimerFragment != null && isTimerFragmentShow) {
                hideTimerFragment();
                return false;
            }

            // 判断是不是连续按下
            long currentClickBackTimeMillis = System.currentTimeMillis();
            if (currentClickBackTimeMillis - lastClickBackTimeMillis > 2000) {
                ToastUtil.show(getString(R.string.click_again_to_return_to_desktop));
                lastClickBackTimeMillis = currentClickBackTimeMillis;
                return true;
            }

//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            this.startActivity(intent);
            moveTaskToBack(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 让 PlayFragment 页面上的返回键 起作用
     */
    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }

        if (mTimerFragment != null && isTimerFragmentShow) {
            hideTimerFragment();
            return;
        }
//        moveTaskToBack(false);
        super.onBackPressed();
    }

    /**
     * 显示正在播放界面
     */
    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();

        isPlayFragmentShow = true;
    }

    private void showTimerFragment() {
        if (isTimerFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_right_in, 0);
        if (mTimerFragment == null) {
            mTimerFragment = new TimerFragment();
            ft.replace(R.id.activity_music_container, mTimerFragment);
        } else {
            ft.show(mTimerFragment);
        }
        ft.commitAllowingStateLoss();

        isTimerFragmentShow = true;
    }

    /**
     * 隐藏正在播放界面
     */
    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    private void hideTimerFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_left_out);
        ft.hide(mTimerFragment);
        ft.commitAllowingStateLoss();
        isTimerFragmentShow = false;
    }

    @OnClick({R.id.toolbar_menu, R.id.toolbar_search, R.id.activity_music_play_bar, R.id.play_bar_next, R.id.play_bar_play_or_pause})
    public void doClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.activity_music_play_bar:
                showPlayingFragment();
                break;

            case R.id.play_bar_play_or_pause:
                playPause();
                break;

            case R.id.play_bar_next:
                next();
                break;
        }
    }

    /**
     * 播放或者暂停
     */
    private void playPause() {
        getPlayService().playPause();
    }

    /**
     * 播放下一首
     */
    private void next() {
        getPlayService().next();
    }

    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        Bitmap cover = CoverLoader.getInstance().loadRound(music);
        mPlayBarCover.setImageBitmap(cover);

        // 向控件内填充数据
        String artist;

        if ("<unknown>".equals(music.getArtist())) {
            artist = getString(R.string.unknown_artist);
        } else {
            artist = music.getArtist();
        }

        changeFont(mPlayBarTitle, false);
        changeFont(mPlayBarArtist, false);

        mPlayBarTitle.setText(music.getTitle());
        mPlayBarArtist.setText(artist);
        mPlayBarPlayOrPause.setSelected(getPlayService().isPlaying() || getPlayService().isPreparing());
        mPlayBarProgressBar.setMax((int) music.getDuration());
        mPlayBarProgressBar.setProgress((int) getPlayService().getCurrentPosition());

        if (mMyFragment != null && mMyFragment.isAdded()) {
            mMyFragment.onItemPlay();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Keys.VIEW_PAGER_INDEX, mViewPager.getCurrentItem());
        mMyFragment.onSaveInstanceState(outState);
        mOnlineFragment.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(savedInstanceState.getInt(Keys.VIEW_PAGER_INDEX), false);
                mMyFragment.onRestoreInstanceState(savedInstanceState);
//                mOnlineFragment.onRestoreInstanceState(savedInstanceState);
            }
        });
    }

    @Override
    protected void onDestroy() {
        PlayService service = AppCache.getPlayService();
        if (service != null) {
            service.setOnPlayEventListener(null);
        }
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mMyFragment != null) {
            mMyFragment.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        mDrawerLayout.closeDrawers();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);
//        return NaviMenuExecutor.onNavigationItemSelected(item, this);

        switch (item.getItemId()) {
            case R.id.action_timer:
                showTimerFragment();
                return true;

            case R.id.action_setting:
                startActivity(MusicActivity.this, SettingActivity.class);
                return true;

            case R.id.action_exit:
                exit();
                return true;
        }
        return false;
    }

    private void exit() {
        finish();
        PlayService service = AppCache.getPlayService();
        if (service != null) {
            service.quit();
        }
    }

    /**
     * Fragment适配器
     */
    private class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}