package mm.belii4.form.schedule;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mm.belii4.MainActivity;
import mm.belii4.R;
import mm.belii4.data.DatabaseHelper;
import mm.belii4.data.core.NonSched;
import mm.belii4.data.core.NonSchedHelper;
import mm.belii4.form.NewWizardDialog;
import mm.belii4.util.BaseItemTouchHelperCallback;
import mm.belii4.util.ItemTouchHelperAdapter;

/**
 * Created by tedwei on 11/24/16.
 */

public class NonSchedRecyclerViewAdapter extends RecyclerView.Adapter<NonSchedRecyclerViewAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private Context context;
    private ItemTouchHelper itemTouchHelper;
    private List<NonSched> nonSchedList;
    private NonSchedHelper nonSchedHelper = DatabaseHelper.getInstance().getHelper(NonSchedHelper.class);

    public NonSchedRecyclerViewAdapter(Context context) {
        this.context = context;
        ItemTouchHelper.Callback callback = new BaseItemTouchHelperCallback(this);
        this.itemTouchHelper = new ItemTouchHelper(callback);
    }

    public void setList(List<NonSched> nonSchedList) {
        this.nonSchedList = nonSchedList;
        this.notifyDataSetChanged();
    }

    public ItemTouchHelper getItemTouchHelper() {
        return this.itemTouchHelper;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(nonSchedList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        new SaveOrderTask().execute();
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_non_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final NonSched nonSched = nonSchedList.get(position);
        holder.setData(nonSched);

        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return nonSchedList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private NonSched item;
        private TextView itemSummaryView;
        private ImageView itemIconView;
        private final ImageView handleView;

        private ViewHolder(View itemView) {
            super(itemView);
            itemView.setBackgroundColor(0x00000000);
            handleView = (ImageView) itemView.findViewById(R.id.handle);
            itemSummaryView = (TextView) itemView.findViewById(R.id.schedule_item_summary);
            itemIconView = (ImageView) itemView.findViewById(R.id.schedule_item_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
                    List<String> optsList = new ArrayList<String>();

                    optsList.add("Add to Player");

                    optsList.add("Edit");
                    if (item.get_state().equalsIgnoreCase("active")) {
                        optsList.add("Deactivate");
                    } else if (item.get_state().equalsIgnoreCase("inactive")) {
                        optsList.add("Activate");
                    }

                    optsList.add("Delete");

                    final String[] options = optsList.toArray(new String[]{});
                    alertOptions.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, options), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (options[i].equalsIgnoreCase("EDIT")) {

                                new NewWizardDialog(context, item).show();

                            }
                            else if (options[i].equalsIgnoreCase("ADD TO PLAYER")) {
                                DatabaseHelper dh = DatabaseHelper.getInstance();
                                SQLiteDatabase database = dh.getWritableDatabase();

                                String sNewId = java.util.UUID.randomUUID().toString();

                                ContentValues contentValues = item.getContentValues();
                                contentValues.remove("_id");
                                contentValues.put("_id", sNewId);
                                contentValues.put("wt", "0");
                                contentValues.put("extpct", "0");
                                contentValues.put("extthr", "0");

                                Log.i("DB", "Insert into " + "core_tbl_player" + ":" + contentValues.getAsString("_id"));
                                if (database.insert("core_tbl_player", null, contentValues) > 0) {
                                    Toast.makeText(context, "Added to Player.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(context, "Adding to Player failed.", Toast.LENGTH_SHORT).show();
                                }

                                String[] sxLines = item.getContent().split("\\n");
                                for(int j=0; j<sxLines.length; j++) {
                                    ContentValues cv = new ContentValues();
                                    cv.put("_state", "active");
                                    cv.put("playerid", sNewId);
                                    cv.put("content", sxLines[j]);
                                    cv.put("weight", "0.1");

                                    database.insert("core_tbl_content", null, cv);
                                }
                            }
                            else if (options[i].equalsIgnoreCase("DELETE")) {
                                Toast.makeText(context, "Schedule deleted.", Toast.LENGTH_SHORT).show();

                                nonSchedHelper.delete(item.get_id());

                                ((MainActivity) context).getSchedulePopulator().resetup();
                                dialogInterface.dismiss();

                            }
                        }
                    });

                    alertOptions.setCancelable(true);
                    alertOptions.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertOptions.show();
                }
            });
        }

        private void setData(NonSched item) {
            this.item = item;

            String sLabel = "";
            String nsCat = item.getCat();

            // see NewWizardDialog: DialogStep_1Step_Events
            if(nsCat.equalsIgnoreCase("COMTAS")) {
                sLabel = item.getName() + " -= " + item.getContent();
            }
            else if(nsCat.equalsIgnoreCase("PLAYER")) {
                sLabel = item.getName();
            }
            else {
                sLabel = item.getContent();
            }

            itemSummaryView.setText(sLabel);

            if(item.get_state().equalsIgnoreCase("active")) {
                itemIconView.setImageResource(R.drawable.schedule_single);
            }else{
                itemIconView.setImageResource(R.drawable.schedule_single_inactive);
            }
        }
    }

    class SaveOrderTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            NonSched[] array = nonSchedList.toArray(new NonSched[0]);
            for (int i = 0 ; i < array.length ; i++) {
                NonSched nonSched = array[i];
                String iprio = "" + i;
                if (!iprio.equals(nonSched.getIprio())) {
                    nonSched.setIprio(iprio);
                    nonSchedHelper.update(nonSched);
                }
            }
            return null;
        }
    }
}
