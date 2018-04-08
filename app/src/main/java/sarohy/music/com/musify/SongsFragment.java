package sarohy.music.com.musify;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import sarohy.music.com.musify.util.BitmapWorkerTask;
import sarohy.music.com.musify.util.MediaItem;

public class SongsFragment extends Fragment  implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    RecyclerView recyclerViewSongs;
    private SongAdapter mAdapter;
    private Uri albumsUri;
    private ArrayList<MediaItem> songList;
    private SongSelection selected;
    private boolean isLoaded=false;
    public SongsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        selected= (SongSelection) context;
    }
    public static SongsFragment newInstance(String param1, String param2) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        getLoaderManager().initLoader(0, null, this);
        songList=new ArrayList<>();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerViewSongs= (RecyclerView) v.findViewById(R.id.recycler_view_songs);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewSongs.setLayoutManager(mLayoutManager);
        recyclerViewSongs.setItemAnimator(new DefaultItemAnimator());
        return v;
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new android.support.v4.content.CursorLoader(getActivity(), albumsUri,
                new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.COMPOSER},
                null, null, null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!isLoaded) {
            if (data.moveToFirst()) {
                do {
                    MediaItem songData = new MediaItem();
                    String title = data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String album = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    long duration = data.getLong(data.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String data1 = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA));
                    long albumId = data.getLong(data.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    String composer = data.getString(data.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
                    songData.setTitle(title);
                    songData.setAlbum(album);
                    songData.setArtist(artist);
                    songData.setDuration(duration);
                    songData.setPath(data1);
                    songData.setAlbumId(albumId);
                    songData.setComposer(composer);
                    songList.add(songData);
                } while (data.moveToNext());
            }
            isLoaded=true;
        }
        mAdapter = new SongAdapter(songList);
        recyclerViewSongs.setAdapter(mAdapter);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    public void updateList(String newText) {
        mAdapter.getFilter().filter(newText);
    }
    public interface SongSelection{
        public void songSelected(ArrayList<MediaItem> audioList, int position);
    }
    public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> implements Filterable {

        private ArrayList<MediaItem> mediaList;
        private ArrayList<MediaItem> filteredMedia;
        private Filter filter;

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new MediaFilter();
            }
            return filter;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTitle, tvArtist, tvDuration;
            public ImageView ivImage;

            public MyViewHolder(View view) {
                super(view);
                ivImage = (ImageView) view.findViewById(R.id.img_albumart);
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
                        MediaItem song = filteredMedia.get(itemPosition);
                        for (int i = 0; i < mediaList.size(); i++) {
                            if (song.getTitle().equals(mediaList.get(i).getTitle())) {
                                itemPosition = i;
                                break;
                            }
                        }
                        Toast.makeText(getContext(), song.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
                        selected.songSelected(songList, itemPosition);
                    }
                });
            }
        }

        public SongAdapter(ArrayList<MediaItem> moviesList) {
            this.mediaList = moviesList;
            this.filteredMedia = mediaList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_song_listview, parent, false);

            return new MyViewHolder(itemView);
        }

        public void loadBitmap(String albumId, ImageView mImage) {
            BitmapWorkerTask mTask = new BitmapWorkerTask(mImage, getActivity());
            mTask.execute(albumId);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            MediaItem audio = filteredMedia.get(position);
            if (audio.getAlbumId() != 0) {
                //set the current album's art on the background thread. by an asynctask
                loadBitmap(String.valueOf(audio.getAlbumId()), holder.ivImage);
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_menu_gallery);
                holder.ivImage.setAlpha(0.5f);

            }
            holder.tvTitle.setText(audio.getTitle());
            holder.tvArtist.setText(audio.getArtist());
            holder.tvDuration.setText(audio.getDuration());
        }

        @Override
        public int getItemCount() {
            return filteredMedia.size();
        }

        private class MediaFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    ArrayList<MediaItem> filteredList = new ArrayList<>();
                    for (int i = 0; i < mediaList.size(); i++) {
                        if (mediaList.get(i).getTitle().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredList.add(mediaList.get(i));
                        }
                    }
                    results.count = filteredList.size();
                    results.values = filteredList;
                } else {
                    results.count = mediaList.size();
                    results.values = mediaList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredMedia = (ArrayList<MediaItem>) results.values;
                notifyDataSetChanged();
            }
        }
    }
}
