package sarohy.music.com.musify;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import sarohy.music.com.musify.util.ArtistItem;
import sarohy.music.com.musify.util.BitmapWorkerTask;
import sarohy.music.com.musify.util.MediaItem;


public class ArtistFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    private SongSelection mListener;
    private ArrayList<ArtistItem> albumsList;
    private ArtistAdapter mAdapter;
    private Uri albumsUri;
    private RecyclerView recyclerViewAlbums;
    private RecyclerView recyclerViewSongs;
    private boolean isLoaded=false;
    public ArtistFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumsUri= MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        getLoaderManager().initLoader(0, null, this);
        albumsList=new ArrayList<>();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_artist, container, false);
        recyclerViewAlbums=v.findViewById(R.id.recycler_view_album);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewAlbums.setLayoutManager(mLayoutManager);
        recyclerViewAlbums.setItemAnimator(new DefaultItemAnimator());
        recyclerViewSongs=v.findViewById(R.id.recycler_view_songs);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewSongs.setLayoutManager(mLayoutManager);
        recyclerViewSongs.setItemAnimator(new DefaultItemAnimator());
        // Inflate the layout for this fragment
        return v;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (recyclerViewSongs.getVisibility()==View.VISIBLE){
                        recyclerViewSongs.setVisibility(View.GONE);
                        recyclerViewAlbums.setVisibility(View.VISIBLE);
                    }
                    else {
                        getActivity().finish();
                    }
                    return true;
                }
                return false;
            }
        });
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener= (SongSelection) context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new android.support.v4.content.CursorLoader(getActivity(), MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{ MediaStore.Audio.ArtistColumns.ARTIST,MediaStore.Audio.Artists._ID,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS},
                null, null, null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!isLoaded) {
            if (data.moveToFirst()) {
                do {
                    ArtistItem mAlbumItem = new ArtistItem();
                    mAlbumItem.setArtistID(data.getInt(data.getColumnIndex(MediaStore.Audio.Albums._ID)));
                    mAlbumItem.setArtistName(data.getString(data.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)));
                    albumsList.add(mAlbumItem);
                }
                while (data.moveToNext());
            }
            isLoaded=true;
        }
        mAdapter = new ArtistAdapter(albumsList);
        recyclerViewAlbums.setAdapter(mAdapter);
    }
    public void updateList(String newText) {
        mAdapter.getFilter().filter(newText);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.MyViewHolder> implements Filterable {

        private ArrayList<ArtistItem> mediaList;
        private ArrayList<ArtistItem> filteredMedia;
        private Filter filter;
        @Override
        public Filter getFilter() {
            if(filter == null){
                filter = new MediaFilter();
            }
            return filter;
        }
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvAlbumTitle;

            public MyViewHolder(View view) {
                super(view);
                tvAlbumTitle = (TextView) view.findViewById(R.id.tv_title);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int itemPosition = recyclerViewAlbums.getChildLayoutPosition(view);
                        ArtistItem album = filteredMedia.get(itemPosition);
                        Cursor c = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.DURATION,
                                MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media.COMPOSER},MediaStore.Audio.Media.ARTIST + " = ?",new String[]{album.getArtistName()}, null);

                        ArrayList<MediaItem> listOfSongs = new ArrayList<MediaItem>();
                        c.moveToFirst();
                        while(c.moveToNext()){
                            MediaItem songData =  new MediaItem();
                            String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
                            String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                            String album1 = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                            long duration = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
                            String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                            long albumId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                            String composer = c.getString(c.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
                            songData.setTitle(title);
                            songData.setAlbum(album1);
                            songData.setArtist(artist);
                            songData.setDuration(duration);
                            songData.setPath(data);
                            songData.setAlbumId(albumId);
                            songData.setComposer(composer);
                            listOfSongs.add(songData);
                        }
                        c.close();
                        Log.d("SIZE", "SIZE: " + listOfSongs.size());
                        recyclerViewAlbums.setVisibility(View.GONE);
                        recyclerViewSongs.setVisibility(View.VISIBLE);
                        SongAdapter mSongAdapter = new SongAdapter(listOfSongs);
                        recyclerViewSongs.setAdapter(mSongAdapter);
                    }
                });
            }
        }
        public ArtistAdapter(ArrayList<ArtistItem> moviesList) {
            this.mediaList = moviesList;
            filteredMedia=mediaList;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_artist_listview, parent, false);

            return new MyViewHolder(itemView);
        }
        public void loadBitmap (String albumId, ImageView mImage)
        {
            BitmapWorkerTask mTask = new BitmapWorkerTask(mImage,getActivity());
            mTask.execute(albumId);
        }
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            ArtistItem audio = filteredMedia.get(position);
            holder.tvAlbumTitle.setText(audio.getArtistName());
        }
        @Override
        public int getItemCount() {
            return filteredMedia.size();
        }
        private class MediaFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults results = new FilterResults();
                if(constraint != null && constraint.length() > 0){
                    ArrayList<ArtistItem> filteredList = new ArrayList<>();
                    for(int i=0; i < mediaList.size(); i++)
                    {
                        String s=mediaList.get(i).getArtistName();
                        if(mediaList.get(i).getArtistName().toLowerCase().contains(constraint.toString().toLowerCase()))
                        {
                            filteredList.add(mediaList.get(i));
                        }
                    }
                    results.count = filteredList.size();
                    results.values = filteredList;
                }
                else{
                    results.count = mediaList.size();
                    results.values = mediaList;
                }
                return results;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                filteredMedia = (ArrayList<ArtistItem>) results.values;
                notifyDataSetChanged();
            }
        }
    }
    public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

        private ArrayList<MediaItem> audioList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTitle, tvArtist, tvDuration;
            public ImageView ivImage;

            public MyViewHolder(View view) {
                super(view);
                ivImage=view.findViewById(R.id.img_albumart);
                ivImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivImage
                        .setLayoutParams(new LinearLayout.LayoutParams(270, 270));
                tvTitle = (TextView) view.findViewById(R.id.tv_title);
                tvArtist = (TextView) view.findViewById(R.id.tv_artist);
                tvDuration = (TextView) view.findViewById(R.id.tv_duration);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int itemPosition = recyclerViewSongs.getChildLayoutPosition(view);
                        MediaItem movie = audioList.get(itemPosition);
                        Toast.makeText(getContext(), movie.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
                        mListener.songSelected(audioList,itemPosition);
                    }
                });
            }
        }
        public SongAdapter(ArrayList<MediaItem> moviesList) {
            this.audioList = moviesList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_song_listview, parent, false);

            return new MyViewHolder(itemView);
        }

        public void loadBitmap (String albumId, ImageView mImage)
        {
            BitmapWorkerTask mTask = new BitmapWorkerTask(mImage,getActivity());
            mTask.execute(albumId);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            MediaItem audio = audioList.get(position);
            if (audio.getAlbumId()!=0)
            {
                //set the current album's art on the background thread. by an asynctask
                loadBitmap(String.valueOf(audio.getAlbumId()),holder.ivImage);
            }
            else
            {
                holder.ivImage.setImageResource(R.drawable.ic_menu_gallery);
                holder.ivImage.setAlpha(0.5f);

            }
            holder.tvTitle.setText(audio.getTitle());
            holder.tvArtist.setText(audio.getArtist());
            holder.tvDuration.setText(audio.getDuration());
        }

        @Override
        public int getItemCount() {
            return audioList.size();
        }
    }
    public interface SongSelection {
      public void songSelected(ArrayList<MediaItem> audioList, int position);
    }
}
