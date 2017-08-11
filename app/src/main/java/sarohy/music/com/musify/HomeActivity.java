package sarohy.music.com.musify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sarohy.music.com.musify.controls.Controls;
import sarohy.music.com.musify.service.SongService;
import sarohy.music.com.musify.util.MediaItem;
import sarohy.music.com.musify.util.PlayerConstants;
import sarohy.music.com.musify.util.UtilFunctions;

public class HomeActivity extends AppCompatActivity implements SongsFragment.SongSelection,AlbumFragment.SongSelection,ArtistFragment.SongSelection {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SongsFragment songsList;
    private AlbumFragment albumList;
    private ArtistFragment artistFragment;
    private PaymentFragment paymentFragment;
    static TextView playingSong;
    Button btnPlayer;
    static Button btnPause, btnPlay, btnNext, btnPrevious;
    Button btnStop;
    LinearLayout mediaLayout;
    static LinearLayout linearLayoutPlayingSong;
    public static ProgressBar progressBar;
    static TextView textBufferDuration, textDuration;
    static ImageView imageViewAlbumArt;
    static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        context=this;
        getViews();
        setListeners();
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        playingSong.setSelected(true);

    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        songsList=new SongsFragment();
        adapter.addFragment(songsList,"Songs");
        albumList=new AlbumFragment();
        adapter.addFragment(albumList,"Album");
        artistFragment=new ArtistFragment();
        adapter.addFragment(artistFragment,"Artist");
        paymentFragment=new PaymentFragment();
        adapter.addFragment(paymentFragment,"Payment");
        viewPager.setAdapter(adapter);
    }
    private void setListeners() {
        btnPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(),AudioPlayerActivity.class);
                startActivity(i);
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.playControl(getApplicationContext());
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.pauseControl(getApplicationContext());
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Controls.nextControl(getApplicationContext());
            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Controls.previousControl(getApplicationContext());
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SongService.class);
                stopService(i);
                linearLayoutPlayingSong.setVisibility(View.GONE);
            }
        });
        imageViewAlbumArt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(),AudioPlayerActivity.class);
                startActivity(i);
            }
        });
    }
    public static void updateUI() {
        try{
            MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
            playingSong.setText(data.getTitle() + " " + data.getArtist() + "-" + data.getAlbum());
            Bitmap albumArt = UtilFunctions.getAlbumart(context, data.getAlbumId());
            if(albumArt != null){
                imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(albumArt));
            }else{
                imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
            }
            linearLayoutPlayingSong.setVisibility(View.VISIBLE);
        }catch(Exception e){}
    }
    public static void changeButton() {
        if(PlayerConstants.SONG_PAUSED){
            btnPause.setVisibility(View.GONE);
            btnPlay.setVisibility(View.VISIBLE);
        }else{
            btnPause.setVisibility(View.VISIBLE);
            btnPlay.setVisibility(View.GONE);
        }
    }
    public static void changeUI(){
        updateUI();
        changeButton();
    }
    private void getViews() {
        playingSong = (TextView) findViewById(R.id.textNowPlaying);
        btnPlayer = (Button) findViewById(R.id.btnMusicPlayer);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        linearLayoutPlayingSong = (LinearLayout) findViewById(R.id.linearLayoutPlayingSong);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnStop = (Button) findViewById(R.id.btnStop);
        textBufferDuration = (TextView) findViewById(R.id.textBufferDuration);
        textDuration = (TextView) findViewById(R.id.textDuration);
        imageViewAlbumArt = (ImageView) findViewById(R.id.imageViewAlbumArt);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);
    }
    public static void updateBar(Integer []i){
        progressBar.setProgress(i[2]);
        int sec=i[0] / 1000;
        int min=sec/60;
        sec=sec%60;
        String str=String.format("%02d:%02d",min,sec);
        textBufferDuration.setText(str);
        sec=i[1] / 1000;
        min=sec/60;
        sec=sec%60;
        str=String.format("%02d:%02d",min,sec);
        textDuration.setText(str);
    }
    @Override
    public void songSelected(ArrayList<MediaItem> audioList, int position) {
        Log.d("TAG", "TAG Tapped INOUT(IN)");
        PlayerConstants.SONG_PAUSED = false;
        PlayerConstants.SONGS_LIST=audioList;
        PlayerConstants.SONG_NUMBER = position;
        boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
        if (!isServiceRunning) {
            Intent i = new Intent(getApplicationContext(),SongService.class);
            startService(i);
        } else {
            PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
        }
        updateUI();
        changeButton();
        Log.d("TAG", "TAG Tapped INOUT(OUT)");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
                // based on the current position you can then cast the page to the correct
                // class and call the method:
                if (viewPager.getCurrentItem() == 0 && page != null) {
                    ((SongsFragment) page).updateList(newText);
                }
                else if (viewPager.getCurrentItem() == 1 && page != null) {
                    ((AlbumFragment) page).updateList(newText);
                }
                else if (viewPager.getCurrentItem() == 2 && page != null) {
                    ((ArtistFragment) page).updateList(newText);
                }
                Log.i("well", " this worked");
                return false;
            }
        });
        return true;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
