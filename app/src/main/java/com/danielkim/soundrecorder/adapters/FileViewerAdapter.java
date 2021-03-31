package com.danielkim.soundrecorder.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Environment;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.text.format.DateUtils;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.activities.MainActivity;
import com.danielkim.soundrecorder.fragments.PlaybackFragment;
import com.danielkim.soundrecorder.fragments.TagViewerFragment;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Daniel on 12/29/2014.
 */
public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
    implements OnDatabaseChangedListener{

    private static final String LOG_TAG = "FileViewerAdapter";


    private MainActivity mMainActivity;

    private DBHelper mDatabase;
    private LinkedList<String> filePaths;
    private String secondLastClause;
    private String lastClause;

    private int positionHelper;
    private RecordingItem itemHelper;
    private FirebaseStorage storage;
    private boolean doQuickFilter;

    Context mContext;
    LinearLayoutManager llm;

    public FileViewerAdapter(Context context, LinearLayoutManager linearLayoutManager, MainActivity mainActivity) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        llm = linearLayoutManager;
        mMainActivity = mainActivity;
        doQuickFilter = false;

        lastClause = DBHelper.DELETED;
        secondLastClause = DBHelper.DELETED;

        FirebaseApp.initializeApp(mContext);
        storage = FirebaseStorage.getInstance();

        updateFilePaths();
        updateFilePaths();
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, final int position) {

        // variables
        final RecordingItem item = getItem(position);
        long itemDuration = item.getLength();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);


        // assign
        holder.vName.setText(item.getName());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vFileSize.setText(item.getSizeFormatted());
        holder.vDateAdded.setText(
            DateUtils.formatDateTime(
                mContext,
                item.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
            )
        );
        holder.recordingFilePath = item.getFilePath();

        // assign only based on cloud download
        if(item.getIsCloud() == 1)
        {
            holder.vClipart.setImageDrawable(mContext.getDrawable(R.drawable.ic_action_cloud_done));
        }else
        {
            holder.vClipart.setImageDrawable(mContext.getDrawable(R.drawable.ic_fileviewer_round));
        }

        // update the tag color
        holder.vTag.setText(item.getTag());
        LayerDrawable layers = (LayerDrawable) holder.vTag.getBackground();
        GradientDrawable shape = (GradientDrawable) (layers.findDrawableByLayerId(R.id.clr));
        shape.setColor(Color.parseColor(item.getColour()));
        final String temp = item.getTag();

        // do quickfilter
        holder.vTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!lastClause.matches(DBHelper.NOT_DELETED)){
                    if(!doQuickFilter) {
                        updateFilePaths(
                                DBHelper.DBHelperItem.SAVED_RECORDING_TAG + " = '" + temp + "' " +
                                        " and " + DBHelper.DELETED);
                        doQuickFilter = !doQuickFilter;
                    }
                    else{

                        // variables
                        String temp;

                        temp = lastClause;
                        lastClause = secondLastClause;
                        secondLastClause = temp;
                        updateFilePathsLastClauseFalse();
                        doQuickFilter = !doQuickFilter;
                    }
                }
            }
        });

        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlaybackFragment playbackFragment =
                            new PlaybackFragment().newInstance(getItem(holder.getPosition()));

                    FragmentTransaction transaction = ((FragmentActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    playbackFragment.show(transaction, "dialog_playback");

                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }
            }
        });

        // assign the menu options for the system
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                // variables
                RecordingItem recording = getItem(position);
                ArrayList<String> entrys = new ArrayList<String>();

                // update selection options
                entrys.add(mContext.getString(R.string.dialog_file_rename));
                entrys.add("Move to Trash");
                entrys.add("Edit Tag");
                if(recording.getUrl().equals(""))
                    entrys.add("Cloud Share");

                final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);


                // File delete confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));

                // our logic tree, check first if the recording is deleted, then check
                if(!recording.getFilePath().matches(".*\\/SoundRecorder\\/Deleted\\/.*")){
                    if (recording.getUrl().equals("")) {
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {

                                    renameFileDialog(holder.getPosition());
                                }
                                if (item == 1) {

                                    moveToTrashDialog(holder.getPosition());
                                } else if (item == 2) {

                                    addTagDialog(holder.recordingFilePath);
                                } else if (item == 3) {

                                    cloudShare(holder.getLayoutPosition());
                                }
                            }
                        });
                    } else {
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {

                                    renameFileDialog(holder.getPosition());
                                }
                                if (item == 1) {

                                    moveToTrashDialog(holder.getPosition());
                                } else if (item == 2) {

                                    addTagDialog(holder.recordingFilePath);
                                }
                            }
                        });
                    }
                }
                else {

                    // variables
                    ArrayList<String> deleteEntrys;
                    final CharSequence[] deleteItems;

                    // assign
                    deleteEntrys = new ArrayList<String>();

                    // update selection options
                    deleteEntrys.add("Restore");
                    deleteEntrys.add("Delete");
                    deleteItems = deleteEntrys.toArray(new CharSequence[deleteEntrys.size()]);

                    builder.setItems(deleteItems, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (item == 0)
                                restoreFileDialog(holder.getPosition());
                            if (item == 1)
                                deleteFileDialog(holder.getPosition());
                        }
                    });

                }

                // finish other options for the menu
                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                return false;
            }
        });
    }


    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);

        mContext = parent.getContext();
        lastClause = DBHelper.DELETED;
        secondLastClause = DBHelper.DELETED;

        return new RecordingsViewHolder(itemView);
    }


    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected TextView vFileSize;
        protected ImageView vClipart;
        protected Button vTag;
        protected View cardView;
        protected String recordingFilePath;


        public RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.upload_file_name);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            vDateAdded = (TextView) v.findViewById(R.id.upload_url);
            vFileSize = (TextView) v.findViewById((R.id.file_size_text));
            vTag = (Button) v.findViewById((R.id.recordingTag));
            vClipart = (ImageView) v.findViewById((R.id.imageView));
            cardView = v.findViewById(R.id.upload_card_view);
        }
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    public RecordingItem getItem(int position) {

        //return mDatabase.getItemAt(position);
        return mDatabase.getItemByFilePath(filePaths.get(position));
    }

    public int getItemID(int position) {

        return mDatabase.getItemByFilePath(filePaths.get(position)).getId();
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        try {
            if(getItemCount() < 0) {
                notifyItemInserted(getItemCount());
                llm.scrollToPosition(getItemCount());
            }
        }
        catch (Exception e){
            Log.e(e.getMessage(),e.getStackTrace().toString());
        }

        updateFilePathsLastClause();
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {
        updateFilePathsLastClause();
    }

    public void moveToTrash(int position) {

        // Make a folder for deleted files named Deleted
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/Deleted/");
        if (!folder.exists()) {
            folder.mkdir();
        }
        String name = getItem(position).getName();
//        File file = new File(getItem(position).getFilePath());
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/Deleted/" + name;
        File f = new File(mFilePath);


        File oldFilePath = new File(getItem(position).getFilePath());
        oldFilePath.renameTo(f);
        mDatabase.renameItem(getItem(position), name, mFilePath);
        notifyItemChanged(position);

        Toast.makeText(
                mContext,
                String.format(
                        mContext.getString(R.string.toast_file_delete),
                        getItem(position).getName()
                ),
                Toast.LENGTH_SHORT
        ).show();

//        mDatabase.moveToDeletedFiles(getItem(position).getId());

        // update recycler view
        updateFilePathsLastClause();
        notifyDataSetChanged();
    }

    public void cloudShare(int position) {

        //location of audio file in internal storage
        RecordingItem item = getItem(position);

        File file = new File(item.getFilePath());
        final String fileName = item.getName();

        System.out.println("---------fileName = " + fileName);

        positionHelper = position;
        if(file != null) {
            String path = "SoundRecorder/" + UUID.randomUUID() + ".mp4";

            final StorageReference storageRef = storage.getReference(path);
            UploadTask uploadTask = storageRef.putFile(Uri.fromFile(file));

            //Showing Progress bar
            final ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            progressDialog.setCancelable(false);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(mContext, "Success", Toast.LENGTH_LONG).show();

                    String downURL = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                    Task<Uri> url = storageRef.getDownloadUrl();
                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //URL of audio file
                            final String audioURL = uri.toString();

                            mDatabase.changeUrl(getItem(positionHelper), audioURL );
                            notifyItemChanged(positionHelper);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                            alertDialogBuilder.setTitle("Audio file URL");
                            alertDialogBuilder.setMessage(audioURL);
                            alertDialogBuilder.setPositiveButton("Copy", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id) {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Cloud URL", audioURL);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(mContext, "Copied", Toast.LENGTH_SHORT).show();
                                }
                            });
                            alertDialogBuilder.create().show();

                            mDatabase.addCloudUpload(getItem(positionHelper).getName(),audioURL);
                        }
                    });
                }
            });
        }

        // update recycler view
        updateFilePathsLastClause();
        notifyItemChanged(position);
    }

    public void restore(int position){

        Toast.makeText(
                mContext,
                String.format(
                        "%1$s successfully restored",
                        getItem(position).getName()
                ),
                Toast.LENGTH_SHORT
        ).show();

        mDatabase.restoreDeletedFile(getItem(position).getFilePath());

        // update recycler view
        updateFilePathsLastClause();
        notifyDataSetChanged();
    }

    public void delete(int position) {

        //delete file from storage
        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(
                mContext,
                String.format(
                        mContext.getString(R.string.toast_file_delete),
                        getItem(position).getName()
                ),
                Toast.LENGTH_SHORT
        ).show();

        mDatabase.removeItemWithId(getItem(position).getId());

        // update recycler view
        updateFilePathsLastClause();
        //notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void rename(int position, String name) {

        //rename a file
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(mContext,
                    String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();

        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }

        // update recycler view
        notifyItemChanged(position);
        updateFilePaths();
    }

    @Deprecated
    public void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
    }

    public void addTagDialog(String recordingFilePath) {

        // code for upadating a tag

        // create the filter fragment
        android.app.FragmentTransaction ft = mMainActivity.getFragmentManager().beginTransaction();
        android.app.Fragment prev = mMainActivity.getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        TagViewerFragment newFragment = TagViewerFragment.newInstance(recordingFilePath);
        newFragment.show(ft, "dialog");
    }

    public void renameFileDialog(final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        renameFileBuilder.setView(view);
        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim();
                            rename(position, value);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    public void moveToTrashDialog(final int position) {

        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle("Confirm...");
        confirmDelete.setMessage("Are you sure you would like to move this file to the trash?");
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //move file to TRASH
                            moveToTrash(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    public void restoreFileDialog(final int position) {

        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle("Confirm Restore...");
        confirmDelete.setMessage("Are you sure you want to restore this file?");
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //move file to TRASH
                            restore(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    public void deleteFileDialog(final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //move file to TRASH
                            delete(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    public void updateFilePaths(){

        //upate the file paths to the default
        doQuickFilter = false;
        secondLastClause = lastClause;
        lastClause = DBHelper.DELETED;
        filePaths = mDatabase.getFilePaths(lastClause);

        this.notifyDataSetChanged();
    }

    public void updateFilePaths(String clause){

        // update to the current clause
        doQuickFilter = false;
        secondLastClause = lastClause;
        lastClause = clause;
        filePaths = mDatabase.getFilePaths(lastClause);
        this.notifyDataSetChanged();
    }

    public void updateFilePathsLastClause(){

        // update file paths to the previous clause
        doQuickFilter = false;
        filePaths = mDatabase.getFilePaths(lastClause);

        this.notifyDataSetChanged();
    }

    public void updateFilePathsLastClauseFalse(){

        // update file paths to the previous clause
        filePaths = mDatabase.getFilePaths(lastClause);

        this.notifyDataSetChanged();
    }

}
