package com.danielkim.soundrecorder.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.text.format.DateUtils;

import com.danielkim.soundrecorder.DBHelper;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.RecordingItem;
import com.danielkim.soundrecorder.fragments.PlaybackFragment;
import com.danielkim.soundrecorder.listeners.OnDatabaseChangedListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    private DBHelper mDatabase;
    private LinkedList<String> filePaths;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    RecordingItem item;
    Context mContext;
    LinearLayoutManager llm;

    public FileViewerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        llm = linearLayoutManager;

        filePaths = mDatabase.getFilePaths();
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {

        item = getItem(position);
        long itemDuration = item.getLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);

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
        holder.vTag.setText(item.getTag());
        holder.vTag.setBackgroundColor(Color.parseColor(item.getColour()));

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

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ArrayList<String> entrys = new ArrayList<String>();
                entrys.add(mContext.getString(R.string.dialog_file_share));
                entrys.add(mContext.getString(R.string.dialog_file_rename));
                entrys.add(mContext.getString(R.string.dialog_file_delete));
                entrys.add("Cloud Share");
                entrys.add("Edit Tag");

                final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);


                // File delete confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            shareFileDialog(holder.getPosition());
                        } if (item == 1) {
                            renameFileDialog(holder.getPosition());
                        } else if (item == 2) {
                            deleteFileDialog(holder.getPosition());
                        } else if( item == 3){
                            cloudShare(holder.getLayoutPosition());
                        } else if( item == 4){
                            addTagDialog(holder.getLayoutPosition());
                        }
                    }
                });
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

        return new RecordingsViewHolder(itemView);
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected TextView vFileSize;
        protected Button vTag;
        protected View cardView;

        public RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.file_name_text);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
            vFileSize = (TextView) v.findViewById((R.id.file_size_text));
            vTag = (Button) v.findViewById((R.id.recordingTag));
            cardView = v.findViewById(R.id.card_view);
        }
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    public RecordingItem getItem(int position) {

        return mDatabase.getItemByFilePath(filePaths.get(position));
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        filePaths = mDatabase.getFilePaths();
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {

    }

    public void cloudShare(int position) {

        //location of audio file in internal storage
        File file = new File(getItem(position).getFilePath());

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
                            String imageUrl = uri.toString();
                            System.out.println("\n\n---------------------------------------DB File URL = " + imageUrl + "\n\n");
                        }
                    });


                }
            });
//                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Uri> task) {
//                            if (task.isSuccessful()) {
//                                Uri downloadUri = task.getResult();
//                                System.out.println("\n\n---------------------------------------DB File URL = " + downloadUri.toString() + "\n\n");
//                            } else{
//                                Toast.makeText(mContext, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
        }

      /*  if(file != null)
        {
            Toast.makeText(mContext, "Uploading...", Toast.LENGTH_LONG).show();
            final ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            ref = storageReference.child("audios/"+ UUID.randomUUID().toString());
            ref.putFile(Uri.fromFile(file))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            String name = taskSnapshot.getMetadata().getName();
                            String url = ref.getDownloadUrl().toString();

                            System.out.println("\n\n Cloud name = " + name + "\n\n");
                            System.out.println("\n\n Cloud url = " + url + "\n\n");

                        }
                    });

        } */

        System.out.println("\nFIle Name = " + file + "\n");

    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage

        //delete file from storage
        //remove item from database, recyclerview and storage

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
        notifyItemRemoved(position);

        updateFilePaths();
    }

    //TODO
    public void removeOutOfApp(String filePath) {
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

        updateFilePaths();
    }
    public void editTag(int position, String tagName)
    {
        String tagColour = "#FFFFFF";
        if(tagName.equalsIgnoreCase("School"))
        {
            tagColour = "#FFFF00";
        }
        else if(tagName.equalsIgnoreCase("Work"))
        {
            tagColour = "#FF0000";
        }
        else if(tagName.equalsIgnoreCase("Sports"))
        {
            tagColour = "#0000FF";
        }
        else if(tagName.equalsIgnoreCase("Groceries"))
        {
            tagColour = "#00FF00";
        }
        mDatabase.changeTag(getItem(position),tagName.toLowerCase(),tagColour);
        notifyItemChanged(position);
    }
    public void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));

        updateFilePaths();
    }
    public void addTagDialog(final int position)
    {
        AlertDialog.Builder addTagBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_add_tag,null);

        final EditText input = (EditText) view.findViewById(R.id.tag_entry);
        addTagBuilder.setTitle("Edit Tag");
        addTagBuilder.setCancelable(true);
        addTagBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    String value = input.getText().toString().trim();
                    editTag(position, value);

                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }
                dialog.cancel();
            }
        });
        addTagBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        addTagBuilder.setView(view);
        AlertDialog alert = addTagBuilder.create();
        alert.show();
    }
    public void renameFileDialog (final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = (EditText) view.findViewById(R.id.tag_entry);

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

        updateFilePaths();
    }

    public void deleteFileDialog (final int position) {

        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            remove(position);

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

        updateFilePaths();
    }

    public void updateFilePaths(){

        filePaths = mDatabase.getFilePaths();
        this.notifyDataSetChanged();
    }

    public void updateFilePaths(String clause){

        filePaths = mDatabase.getFilePaths(clause);
        this.notifyDataSetChanged();
    }

}
