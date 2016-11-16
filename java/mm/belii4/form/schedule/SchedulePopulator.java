package mm.belii4.form.schedule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import mm.belii4.MainActivity;
import mm.belii4.R;
import mm.belii4.data.DatabaseHelper;
import mm.belii4.data.SearchEntry;
import mm.belii4.data.core.Games;
import mm.belii4.data.core.GamesHelper;
import mm.belii4.data.core.NonSched;
import mm.belii4.data.core.NonSchedHelper;
import mm.belii4.data.core.Schedule;
import mm.belii4.data.core.ScheduleHelper;
import mm.belii4.form.AbstractPopulator;
import mm.belii4.form.NewWizardDialog;

public class SchedulePopulator extends AbstractPopulator {
    protected ScheduleHelper scheduleHelper;
    protected NonSchedHelper nonSchedHelper;
    protected GamesHelper gamesHelper;
    protected DatabaseHelper databaseHelper;
    protected Calendar calSimulate;

    protected volatile PlayerTask objCurPlayerTask;

    private static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public SchedulePopulator(Context context) {
        super(context);
        this.scheduleHelper = DatabaseHelper.getInstance().getHelper(ScheduleHelper.class);
        this.nonSchedHelper = DatabaseHelper.getInstance().getHelper(NonSchedHelper.class);
        this.gamesHelper = DatabaseHelper.getInstance().getHelper(GamesHelper.class);
        this.databaseHelper = DatabaseHelper.getInstance();
        this.calSimulate = Calendar.getInstance();
    }

    @Override
    public void setup(View rootView, String category) {
        super.setup(rootView, category);

        if(category.equals("events")) {
            setup_events(rootView, category);
        }
        else if(category.equals("contacts")) {
            setup_contacts(rootView, category);
        }
        else if(category.equals("games")) {
            setup_games(rootView, category);
        }
        else if(category.equals("library")) {
            setup_library(rootView, category);
        }
        else if(category.equals("player")) {
            setup_player(rootView);
        }
        else {
            List<Schedule> schedules = (List<Schedule>) (List<?>) scheduleHelper.findBy("category", category);

            //Collections.reverse(schedules); // the idea here was most recent entries go on top, but not necessary
            final ListView listView = ((ListView) rootView.findViewById(R.id.schedule_list));
            listView.setAdapter(new ScheduleListAdapter(context, schedules));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final Schedule schedule = (Schedule) listView.getItemAtPosition(i);
                    AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
                    List<String> optsList = new ArrayList<String>();

                    optsList.add("Edit");

                    // can't reactivate if completed?!!
                    if (!schedule.get_state().equalsIgnoreCase("completed")) {
                        optsList.add("Postpone");

                        if (schedule.get_state().equalsIgnoreCase("active")) {
                            optsList.add("Deactivate");
                        } else if (schedule.get_state().equalsIgnoreCase("inactive")) {
                            optsList.add("Activate");
                        }
                    }

                    optsList.add("Delete");
                    optsList.add("Show Details");

                    final String[] options = optsList.toArray(new String[]{});
                    alertOptions.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, options), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (options[i].equalsIgnoreCase("POSTPONE")) {
                                AlertDialog.Builder postponeMinutes = new AlertDialog.Builder(context);
                                postponeMinutes.setTitle("Postpone");
                                postponeMinutes.setMessage("Minutes; varia");
                                final EditText input = new EditText(context);
                                postponeMinutes.setView(input);

                                postponeMinutes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Calendar nextExecute = schedule.getNextExecute();
                                        nextExecute.add(Calendar.MINUTE, Integer.parseInt(input.getText().toString()));
                                        schedule.setNextExecute(nextExecute);
                                        scheduleHelper.update(schedule);
                                    }
                                });
                                postponeMinutes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                postponeMinutes.show();

                            } else if (options[i].equalsIgnoreCase("EDIT")) {
                                new NewWizardDialog(context, schedule).show();
                            } else if (options[i].equalsIgnoreCase("DELETE")) {
                                Toast.makeText(context, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                                scheduleHelper.delete(schedule.get_id());
                                ((MainActivity) context).getSchedulePopulator().resetup();
                                dialogInterface.dismiss();
                            } else if (options[i].equalsIgnoreCase("ACTIVATE")) {
                                schedule.set_state("active");
                                //fix - Multiple duplication of schedules Start
                                scheduleHelper.update(schedule);
                                //fix - Multiple duplication of schedules End
                                ((MainActivity) context).getSchedulePopulator().resetup();
                            } else if (options[i].equalsIgnoreCase("DEACTIVATE")) {
                                schedule.set_state("inactive");
                                //fix - Multiple duplication of schedules Start
                                scheduleHelper.update(schedule);
                                //fix - Multiple duplication of schedules End
                                ((MainActivity) context).getSchedulePopulator().resetup();
                            } else if (options[i].equalsIgnoreCase("SHOW DETAILS")) {
                                AlertDialog.Builder showDetails = new AlertDialog.Builder(context);
                                showDetails.setTitle("Show Details");

                                int iMinutesNextDue = schedule.getNextDue().get(Calendar.MINUTE);
                                String sMinutesNextDue = iMinutesNextDue < 10 ? "0" + String.valueOf(iMinutesNextDue) : String.valueOf(iMinutesNextDue);

                                int iMinutesNextExecute = schedule.getNextExecute().get(Calendar.MINUTE);
                                String sMinutesNextExecute = iMinutesNextExecute < 10 ? "0" + String.valueOf(iMinutesNextExecute) : String.valueOf(iMinutesNextExecute);

                                showDetails.setMessage("frame: " + schedule.get_frame()
                                        + "\n" + "state: " + schedule.get_state()
                                        + "\n" + "repeatEnabled: " + schedule.getRepeatEnable()
                                        + "\n" + "repeatEvery: " + schedule.getRepeatValue() + " " + schedule.getRepeatType()
                                        + "\n" + "prepWindow: " + schedule.getPrepWindow()
                                        + "\n" + "prepWindowType: " + schedule.getPrepWindowType()
                                        + "\n" + "prepCount: " + schedule.getPrepCount()
                                        + "\n" + "nD: " + String.valueOf(schedule.getNextDue().get(Calendar.MONTH) + 1) + "/" + String.valueOf(schedule.getNextDue().get(Calendar.DAY_OF_MONTH)) + "/" + String.valueOf(schedule.getNextDue().get(Calendar.YEAR)) + " " + String.valueOf(schedule.getNextDue().get(Calendar.HOUR_OF_DAY)) + ":" + sMinutesNextDue
                                        + "\n" + "nE: " + String.valueOf(schedule.getNextExecute().get(Calendar.MONTH) + 1) + "/" + String.valueOf(schedule.getNextExecute().get(Calendar.DAY_OF_MONTH)) + "/" + String.valueOf(schedule.getNextExecute().get(Calendar.YEAR)) + " " + String.valueOf(schedule.getNextExecute().get(Calendar.HOUR_OF_DAY)) + ":" + sMinutesNextExecute
                                );

                                showDetails.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                showDetails.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                showDetails.show();
                            }
                        }
                    });
                    alertOptions.setCancelable(true);
                    alertOptions.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alertOptions.show();
                }
            });
        }
    }

    public void setup_games(final View rootView, String category) {
        super.setup(rootView, category);

        List<Games> games;
        games = (List<Games>) (List<?>) gamesHelper.findAll();
        final ListView listViewSt = ((ListView) rootView.findViewById(R.id.schedule_list));
        listViewSt.setAdapter(new GamesListAdapter(context, games));

        listViewSt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Games st = (Games) listViewSt.getItemAtPosition(i);
                AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
                List<String> optsList = new ArrayList<String>();

                optsList.add("Delete");

                final String[] options = optsList.toArray(new String[]{});
                alertOptions.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (options[i].equalsIgnoreCase("DELETE")) {
                            Toast.makeText(context, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                            nonSchedHelper.delete(st.get_id());
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

    public void setup_events(final View rootView, String category) {
        super.setup(rootView, category);

        SQLiteDatabase database = this.databaseHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT subcategory FROM core_tbl_schedule WHERE category='"
                        + category + "' ORDER BY subcategory";
        Cursor cursor = database.rawQuery(sql, new String[0]);

        List<String> lsCategories = new ArrayList<String>();
        if(cursor.moveToFirst()){
            do {
                lsCategories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        //fix - android.database.CursorWindowAllocationException Start
        cursor.close();
        //fix - android.database.CursorWindowAllocationException End

        final Spinner spinCategory = ((Spinner)rootView.findViewById(R.id.events_category));
        final ListView listViewSt = ((ListView) rootView.findViewById(R.id.schedule_list));

        ArrayAdapter<String> adapterCategory = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, lsCategories);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategory.setAdapter(adapterCategory);

        final View dialog = rootView;
        final String sCat = category;
        spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String sCat2 = ((Spinner) dialog.findViewById(R.id.events_category)).getSelectedItem().toString();

                List<Schedule> schedules = new List<Schedule>() {
                    @Override
                    public void add(int i, Schedule selfTalk) {

                    }

                    @Override
                    public boolean add(Schedule selfTalk) {
                        return false;
                    }

                    @Override
                    public boolean addAll(int i, Collection<? extends Schedule> collection) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends Schedule> collection) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public Schedule get(int i) {
                        return null;
                    }

                    @Override
                    public int indexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @NonNull
                    @Override
                    public Iterator<Schedule> iterator() {
                        return null;
                    }

                    @Override
                    public int lastIndexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public ListIterator<Schedule> listIterator() {
                        return null;
                    }

                    @NonNull
                    @Override
                    public ListIterator<Schedule> listIterator(int i) {
                        return null;
                    }

                    @Override
                    public Schedule remove(int i) {
                        return null;
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public Schedule set(int i, Schedule selfTalk) {
                        return null;
                    }

                    @Override
                    public int size() {
                        return 0;
                    }

                    @NonNull
                    @Override
                    public List<Schedule> subList(int i, int i1) {
                        return null;
                    }

                    @NonNull
                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @NonNull
                    @Override
                    public <T> T[] toArray(T[] ts) {
                        return null;
                    }
                };

                schedules = (List<Schedule>) (List<?>) scheduleHelper.findBy("subcategory", sCat2);
                listViewSt.setAdapter(new ScheduleListAdapter(context, schedules));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        listViewSt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Schedule sched = (Schedule) listViewSt.getItemAtPosition(i);
                AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
                List<String> optsList = new ArrayList<String>();

                optsList.add("Edit");

                if (sched.get_state().equalsIgnoreCase("active")) {
                    optsList.add("Deactivate");
                } else if (sched.get_state().equalsIgnoreCase("inactive")) {
                    optsList.add("Activate");
                }

                optsList.add("Delete");

                final String[] options = optsList.toArray(new String[]{});
                alertOptions.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (options[i].equalsIgnoreCase("EDIT")) {

                            new NewWizardDialog(context, sched).show();

                        } else if (options[i].equalsIgnoreCase("DELETE")) {
                            Toast.makeText(context, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                            scheduleHelper.delete(sched.get_id());
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

        List<Schedule> schedules;
        schedules = (List<Schedule>) (List<?>) scheduleHelper.findBy("category", sCat);
        final ListView listView = ((ListView) rootView.findViewById(R.id.schedule_list));
        listView.setAdapter(new ScheduleListAdapter(context, schedules));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Schedule schedule = (Schedule) listView.getItemAtPosition(i);
                AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
                List<String> optsList = new ArrayList<String>();

                optsList.add("Edit");
                optsList.add("Postpone");

                if (schedule.get_state().equalsIgnoreCase("active")) {
                    optsList.add("Deactivate");
                } else if (schedule.get_state().equalsIgnoreCase("inactive")) {
                    optsList.add("Activate");
                }

                optsList.add("Delete");

                optsList.add("Show Details");

                final String[] options = optsList.toArray(new String[]{});
                alertOptions.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (options[i].equalsIgnoreCase("POSTPONE")) {
                            AlertDialog.Builder postponeMinutes = new AlertDialog.Builder(context);
                            postponeMinutes.setTitle("Postpone");
                            postponeMinutes.setMessage("Minutes; varia");
                            final EditText input = new EditText(context);
                            postponeMinutes.setView(input);

                            postponeMinutes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Calendar nextExecute = schedule.getNextExecute();
                                    nextExecute.add(Calendar.MINUTE, Integer.parseInt(input.getText().toString()));
                                    schedule.setNextExecute(nextExecute);
                                    scheduleHelper.update(schedule);
                                }
                            });
                            postponeMinutes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            postponeMinutes.show();

                        } else if (options[i].equalsIgnoreCase("EDIT")) {
                            new NewWizardDialog(context, schedule).show();
                        } else if (options[i].equalsIgnoreCase("DELETE")) {
                            Toast.makeText(context, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                            scheduleHelper.delete(schedule.get_id());
                            ((MainActivity) context).getSchedulePopulator().resetup();
                            dialogInterface.dismiss();
                        } else if (options[i].equalsIgnoreCase("ACTIVATE")) {
                            schedule.set_state("active");
                            //fix - Multiple duplication of schedules Start
                            scheduleHelper.update(schedule);
                            //fix - Multiple duplication of schedules End
                            ((MainActivity) context).getSchedulePopulator().resetup();
                        } else if (options[i].equalsIgnoreCase("DEACTIVATE")) {
                            schedule.set_state("inactive");
                            //fix - Multiple duplication of schedules Start
                            scheduleHelper.update(schedule);
                            //fix - Multiple duplication of schedules End
                            ((MainActivity) context).getSchedulePopulator().resetup();
                        } else if (options[i].equalsIgnoreCase("SHOW DETAILS")) {
                            AlertDialog.Builder showDetails = new AlertDialog.Builder(context);
                            showDetails.setTitle("Show Details");

                            int iMinutesNextDue = schedule.getNextDue().get(Calendar.MINUTE);
                            String sMinutesNextDue = iMinutesNextDue < 10 ? "0" + String.valueOf(iMinutesNextDue) : String.valueOf(iMinutesNextDue);

                            int iMinutesNextExecute = schedule.getNextExecute().get(Calendar.MINUTE);
                            String sMinutesNextExecute = iMinutesNextExecute < 10 ? "0" + String.valueOf(iMinutesNextExecute) : String.valueOf(iMinutesNextExecute);

                            showDetails.setMessage("frame: " + schedule.get_frame()
                                    + "\n" + "state: " + schedule.get_state()
                                    + "\n" + "repeatEnabled: " + schedule.getRepeatEnable()
                                    + "\n" + "repeatEvery: " + schedule.getRepeatValue() + " " + schedule.getRepeatType()
                                    + "\n" + "prepWindow: " + schedule.getPrepWindow()
                                    + "\n" + "prepWindowType: " + schedule.getPrepWindowType()
                                    + "\n" + "prepCount: " + schedule.getPrepCount()
                                    + "\n" + "nD: " + String.valueOf(schedule.getNextDue().get(Calendar.MONTH) + 1) + "/" + String.valueOf(schedule.getNextDue().get(Calendar.DAY_OF_MONTH)) + "/" + String.valueOf(schedule.getNextDue().get(Calendar.YEAR)) + " " + String.valueOf(schedule.getNextDue().get(Calendar.HOUR_OF_DAY)) + ":" + sMinutesNextDue
                                    + "\n" + "nE: " + String.valueOf(schedule.getNextExecute().get(Calendar.MONTH) + 1) + "/" + String.valueOf(schedule.getNextExecute().get(Calendar.DAY_OF_MONTH)) + "/" + String.valueOf(schedule.getNextExecute().get(Calendar.YEAR)) + " " + String.valueOf(schedule.getNextExecute().get(Calendar.HOUR_OF_DAY)) + ":" + sMinutesNextExecute
                            );

                            showDetails.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            showDetails.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            showDetails.show();
                        }
                    }
                });
                alertOptions.setCancelable(true);
                alertOptions.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertOptions.show();
            }
        });
    }

    public void setup_contacts(final View rootView, String category) {
        super.setup(rootView, category);

        SQLiteDatabase database = this.databaseHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT subcategory FROM core_tbl_schedule WHERE category='"
                + category + "' ORDER BY subcategory";
        Cursor cursor = database.rawQuery(sql, new String[0]);

        List<String> lsCategories = new ArrayList<String>();
        if(cursor.moveToFirst()){
            do {
                lsCategories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        //fix - android.database.CursorWindowAllocationException Start
        cursor.close();
        //fix - android.database.CursorWindowAllocationException End

        final Spinner spinCategory = ((Spinner)rootView.findViewById(R.id.contacts_category));
        final ListView listViewSt = ((ListView) rootView.findViewById(R.id.schedule_list));

        ArrayAdapter<String> adapterCategory = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, lsCategories);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategory.setAdapter(adapterCategory);

        final View dialog = rootView;
        final String sCat = category;
        spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String sCat2 = ((Spinner) dialog.findViewById(R.id.contacts_category)).getSelectedItem().toString();

                List<Schedule> schedules = new List<Schedule>() {
                    @Override
                    public void add(int i, Schedule selfTalk) {

                    }

                    @Override
                    public boolean add(Schedule selfTalk) {
                        return false;
                    }

                    @Override
                    public boolean addAll(int i, Collection<? extends Schedule> collection) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends Schedule> collection) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public Schedule get(int i) {
                        return null;
                    }

                    @Override
                    public int indexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @NonNull
                    @Override
                    public Iterator<Schedule> iterator() {
                        return null;
                    }

                    @Override
                    public int lastIndexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public ListIterator<Schedule> listIterator() {
                        return null;
                    }

                    @NonNull
                    @Override
                    public ListIterator<Schedule> listIterator(int i) {
                        return null;
                    }

                    @Override
                    public Schedule remove(int i) {
                        return null;
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public Schedule set(int i, Schedule selfTalk) {
                        return null;
                    }

                    @Override
                    public int size() {
                        return 0;
                    }

                    @NonNull
                    @Override
                    public List<Schedule> subList(int i, int i1) {
                        return null;
                    }

                    @NonNull
                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @NonNull
                    @Override
                    public <T> T[] toArray(T[] ts) {
                        return null;
                    }
                };

                schedules = (List<Schedule>) (List<?>) scheduleHelper.findBy("subcategory", sCat2);
                listViewSt.setAdapter(new ScheduleListAdapter(context, schedules));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        listViewSt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Schedule sched = (Schedule) listViewSt.getItemAtPosition(i);
                AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
                List<String> optsList = new ArrayList<String>();

                optsList.add("Edit");

                if (sched.get_state().equalsIgnoreCase("active")) {
                    optsList.add("Deactivate");
                } else if (sched.get_state().equalsIgnoreCase("inactive")) {
                    optsList.add("Activate");
                }

                optsList.add("Delete");

                final String[] options = optsList.toArray(new String[]{});
                alertOptions.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (options[i].equalsIgnoreCase("EDIT")) {

                            new NewWizardDialog(context, sched).show();

                        } else if (options[i].equalsIgnoreCase("DELETE")) {
                            Toast.makeText(context, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                            scheduleHelper.delete(sched.get_id());
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

        List<Schedule> schedules;
        schedules = (List<Schedule>) (List<?>) scheduleHelper.findBy("category", sCat);
        final ListView listView = ((ListView) rootView.findViewById(R.id.schedule_list));
        listView.setAdapter(new ScheduleListAdapter(context, schedules));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Schedule schedule = (Schedule) listView.getItemAtPosition(i);
                AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
                List<String> optsList = new ArrayList<String>();

                optsList.add("Edit");
                optsList.add("Postpone");

                if (schedule.get_state().equalsIgnoreCase("active")) {
                    optsList.add("Deactivate");
                } else if (schedule.get_state().equalsIgnoreCase("inactive")) {
                    optsList.add("Activate");
                }

                optsList.add("Delete");

                optsList.add("Show Details");

                final String[] options = optsList.toArray(new String[]{});
                alertOptions.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (options[i].equalsIgnoreCase("POSTPONE")) {
                            AlertDialog.Builder postponeMinutes = new AlertDialog.Builder(context);
                            postponeMinutes.setTitle("Postpone");
                            postponeMinutes.setMessage("Minutes; varia");
                            final EditText input = new EditText(context);
                            postponeMinutes.setView(input);

                            postponeMinutes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Calendar nextExecute = schedule.getNextExecute();
                                    nextExecute.add(Calendar.MINUTE, Integer.parseInt(input.getText().toString()));
                                    schedule.setNextExecute(nextExecute);
                                    scheduleHelper.update(schedule);
                                }
                            });
                            postponeMinutes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            postponeMinutes.show();

                        } else if (options[i].equalsIgnoreCase("EDIT")) {
                            new NewWizardDialog(context, schedule).show();
                        } else if (options[i].equalsIgnoreCase("DELETE")) {
                            Toast.makeText(context, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                            scheduleHelper.delete(schedule.get_id());
                            ((MainActivity) context).getSchedulePopulator().resetup();
                            dialogInterface.dismiss();
                        } else if (options[i].equalsIgnoreCase("ACTIVATE")) {
                            schedule.set_state("active");
                            //fix - Multiple duplication of schedules Start
                            scheduleHelper.update(schedule);
                            //fix - Multiple duplication of schedules End
                            ((MainActivity) context).getSchedulePopulator().resetup();
                        } else if (options[i].equalsIgnoreCase("DEACTIVATE")) {
                            schedule.set_state("inactive");
                            //fix - Multiple duplication of schedules Start
                            scheduleHelper.update(schedule);
                            //fix - Multiple duplication of schedules End
                            ((MainActivity) context).getSchedulePopulator().resetup();
                        } else if (options[i].equalsIgnoreCase("SHOW DETAILS")) {
                            AlertDialog.Builder showDetails = new AlertDialog.Builder(context);
                            showDetails.setTitle("Show Details");

                            int iMinutesNextDue = schedule.getNextDue().get(Calendar.MINUTE);
                            String sMinutesNextDue = iMinutesNextDue < 10 ? "0" + String.valueOf(iMinutesNextDue) : String.valueOf(iMinutesNextDue);

                            int iMinutesNextExecute = schedule.getNextExecute().get(Calendar.MINUTE);
                            String sMinutesNextExecute = iMinutesNextExecute < 10 ? "0" + String.valueOf(iMinutesNextExecute) : String.valueOf(iMinutesNextExecute);

                            showDetails.setMessage("frame: " + schedule.get_frame()
                                    + "\n" + "state: " + schedule.get_state()
                                    + "\n" + "repeatEnabled: " + schedule.getRepeatEnable()
                                    + "\n" + "repeatEvery: " + schedule.getRepeatValue() + " " + schedule.getRepeatType()
                                    + "\n" + "prepWindow: " + schedule.getPrepWindow()
                                    + "\n" + "prepWindowType: " + schedule.getPrepWindowType()
                                    + "\n" + "prepCount: " + schedule.getPrepCount()
                                    + "\n" + "nD: " + String.valueOf(schedule.getNextDue().get(Calendar.MONTH) + 1) + "/" + String.valueOf(schedule.getNextDue().get(Calendar.DAY_OF_MONTH)) + "/" + String.valueOf(schedule.getNextDue().get(Calendar.YEAR)) + " " + String.valueOf(schedule.getNextDue().get(Calendar.HOUR_OF_DAY)) + ":" + sMinutesNextDue
                                    + "\n" + "nE: " + String.valueOf(schedule.getNextExecute().get(Calendar.MONTH) + 1) + "/" + String.valueOf(schedule.getNextExecute().get(Calendar.DAY_OF_MONTH)) + "/" + String.valueOf(schedule.getNextExecute().get(Calendar.YEAR)) + " " + String.valueOf(schedule.getNextExecute().get(Calendar.HOUR_OF_DAY)) + ":" + sMinutesNextExecute
                            );

                            showDetails.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            showDetails.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            showDetails.show();
                        }
                    }
                });
                alertOptions.setCancelable(true);
                alertOptions.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertOptions.show();
            }
        });
    }

    public void setup_library(final View rootView, String category) {
        super.setup(rootView, category);

        final ListView listViewCategory = ((ListView) rootView.findViewById(R.id.schedule_category_list));
        final ListView listViewSubcategory = ((ListView) rootView.findViewById(R.id.schedule_subcategory_list));
        final ListView listViewLibrary = ((ListView) rootView.findViewById(R.id.schedule_library_list));

        SQLiteDatabase database = this.databaseHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT cat FROM core_tbl_nonsched WHERE type='library' ORDER BY cat";
        Cursor cursor = database.rawQuery(sql, new String[0]);

        List<String> listCat = new ArrayList<String>();
        if(cursor.moveToFirst()){
            do {
                listCat.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        //fix - android.database.CursorWindowAllocationException Start
        cursor.close();
        //fix - android.database.CursorWindowAllocationException End

        ArrayAdapter<String> adapterCat = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, listCat);
        listViewCategory.setAdapter(adapterCat);
        listViewCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sCat = listViewCategory.getItemAtPosition(i).toString();
                ((MainActivity)context).sSelectedCategory = sCat;

                String sql2 = "SELECT DISTINCT subcat FROM core_tbl_nonsched WHERE cat='" + sCat + "' ORDER BY subcat";

                SQLiteDatabase database2 = databaseHelper.getReadableDatabase();
                Cursor cursor2 = database2.rawQuery(sql2, new String[0]);

                List<String> listSubcat = new ArrayList<String>();
                if(cursor2.moveToFirst()){
                    do {
                        listSubcat.add(cursor2.getString(0));
                    } while (cursor2.moveToNext());
                }

                //fix - android.database.CursorWindowAllocationException Start
                cursor2.close();
                //fix - android.database.CursorWindowAllocationException End

                ArrayAdapter<String> adapterSubcat = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, listSubcat);
                listViewSubcategory.setAdapter(adapterSubcat);

                List<NonSched> nonSched = (List<NonSched>) (List<?>) nonSchedHelper.findBy("cat", sCat);
                listViewLibrary.setAdapter(new NonSchedListAdapter(context, nonSched));

            }
        });

        listViewSubcategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sSubCat = listViewSubcategory.getItemAtPosition(i).toString();

                List<SearchEntry> keys = new ArrayList<SearchEntry>();
                keys.add(new SearchEntry(SearchEntry.Type.STRING, "cat", SearchEntry.Search.EQUAL, ((MainActivity)context).sSelectedCategory));
                keys.add(new SearchEntry(SearchEntry.Type.STRING, "subcat", SearchEntry.Search.EQUAL, sSubCat));

                List<NonSched> listNonSched = (List<NonSched>) (List<?>) nonSchedHelper.find(keys);
                listViewLibrary.setAdapter(new NonSchedListAdapter(context, listNonSched));
            }
        });

        listViewLibrary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
              final NonSched st = (NonSched) listViewLibrary.getItemAtPosition(i);
              AlertDialog.Builder alertOptions = new AlertDialog.Builder(context);
              List<String> optsList = new ArrayList<String>();

              optsList.add("Edit");
              if (st.get_state().equalsIgnoreCase("active")) {
                  optsList.add("Deactivate");
              } else if (st.get_state().equalsIgnoreCase("inactive")) {
                  optsList.add("Activate");
              }

              optsList.add("Delete");

              final String[] options = optsList.toArray(new String[]{});
              alertOptions.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, options), new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {

                      if (options[i].equalsIgnoreCase("EDIT")) {

                          new NewWizardDialog(context, st).show();

                      } else if (options[i].equalsIgnoreCase("DELETE")) {
                          Toast.makeText(context, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                          nonSchedHelper.delete(st.get_id());
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

    public void setup_new_player(final View rootView) {
        super.setup(rootView, category);

        final ListView listViewSubcategory = ((ListView) rootView.findViewById(R.id.schedule_subcategory_list));
        final ListView listViewItems = ((ListView) rootView.findViewById(R.id.schedule_item_list));
        final ListView listViewStrings = ((ListView) rootView.findViewById(R.id.schedule_strings_list));

        SQLiteDatabase database = this.databaseHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT subcat FROM core_tbl_nonsched WHERE cat='player' ORDER BY subcat";
        Cursor cursor = database.rawQuery(sql, new String[0]);

        List<String> listSubcat = new ArrayList<String>();
        if(cursor.moveToFirst()){
            do {
                listSubcat.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        //fix - android.database.CursorWindowAllocationException Start
        cursor.close();
        //fix - android.database.CursorWindowAllocationException End

        ArrayAdapter<String> adapterSubcat = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, listSubcat);
        listViewSubcategory.setAdapter(adapterSubcat);
        listViewSubcategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sSubcat = listViewSubcategory.getItemAtPosition(i).toString();

                List<SearchEntry> keys = new ArrayList<SearchEntry>();
                keys.add(new SearchEntry(SearchEntry.Type.STRING, "cat", SearchEntry.Search.EQUAL, "player"));
                keys.add(new SearchEntry(SearchEntry.Type.STRING, "subcat", SearchEntry.Search.EQUAL, sSubcat));

                List<NonSched> listNsSubcat = (List<NonSched>) (List<?>) nonSchedHelper.find(keys);
                listViewItems.setAdapter(new NonSchedListAdapter(context, listNsSubcat));
            }
        });

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NonSched nsItem = (NonSched)listViewItems.getItemAtPosition(i);

                String sContent = nsItem.getContent();
                String[] sxStrings = sContent.split("\\n");

                List<NonSched> listNsStrings = new ArrayList<NonSched>();

                NonSched nsString;
                for(int j=0; j<sxStrings.length; j++) {
                    nsString = new NonSched();
                    nsString.set_state("active");
                    nsString.setCat("player");
                    nsString.setName(sxStrings[j]);
                    listNsStrings.add(nsString);
                }
                listViewStrings.setAdapter(new NonSchedListAdapter(context, listNsStrings));
            }
        });
    }

    public void setup_player(final View rootView) {
        super.setup(rootView, "player");
        final List<NonSched> listSt = (List<NonSched>) (List<?>) nonSchedHelper.findBy("type","player");

        final EditText etPlayerContent = ((EditText) rootView.findViewById(R.id.etPlayerContent));
        final EditText etAddName = ((EditText) rootView.findViewById(R.id.player_add_name));

        final ListView listView = ((ListView) rootView.findViewById(R.id.player_list));
        listView.setAdapter(new NonSchedListAdapter(context, listSt));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final NonSched nsPlayer = (NonSched) listView.getItemAtPosition(i);
                etAddName.setText(nsPlayer.getName());
                etPlayerContent.setText(nsPlayer.getContent());
            }
        });

        final ImageButton addButton = ((ImageButton) rootView.findViewById(R.id.player_add_btn));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NonSched nsPlayer = new NonSched();
                nsPlayer.setType("library");
                nsPlayer.setCat("player");

                //!!! need to eventually add a subcategory etc.
                nsPlayer.setName(etAddName.getText().toString());
                nsPlayer.setContent(etPlayerContent.getText().toString());

                if(DatabaseHelper.getInstance().getHelper(NonSchedHelper.class).createOrUpdate(nsPlayer)) {
                    Toast.makeText(context, "Saved.", Toast.LENGTH_SHORT).show();
                } else {
                Toast.makeText(context, "Saving failed.", Toast.LENGTH_SHORT).show();
                }

                setup_player(rootView);
            }
        });

        final ImageButton delButton = ((ImageButton) rootView.findViewById(R.id.player_del_btn));
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NonSched nsCurrent = (NonSched) nonSchedHelper.getBy("name",etAddName.getText().toString());
                Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show();
                nonSchedHelper.delete(nsCurrent.get_id());

                setup_player(rootView);
            }
        });

        final Button btnSuper = ((Button) rootView.findViewById(R.id.btnSuper));
        btnSuper.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                objCurPlayerTask = new PlayerTask(etPlayerContent.getText().toString().split("\\n"), "SUPER");
                objCurPlayerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        final Button btnHigh = ((Button) rootView.findViewById(R.id.btnHigh));
        btnHigh.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                objCurPlayerTask = new PlayerTask(etPlayerContent.getText().toString().split("\\n"), "HIGH");
                objCurPlayerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        final Button btnMedium = ((Button) rootView.findViewById(R.id.btnMedium));
        btnMedium.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                objCurPlayerTask = new PlayerTask(etPlayerContent.getText().toString().split("\\n"), "MEDIUM");
                objCurPlayerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        final Button btnLow = ((Button) rootView.findViewById(R.id.btnLow));
        btnLow.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                objCurPlayerTask = new PlayerTask(etPlayerContent.getText().toString().split("\\n"), "LOW");
                objCurPlayerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        final Button btnStopPlayer = ((Button) rootView.findViewById(R.id.btnStopFlashcards));
        btnStopPlayer.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (objCurPlayerTask != null) {
                    objCurPlayerTask.cancel(true);
                }

                Toast.makeText(context, "thanks for playing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Calendar stringToDate(String sInput) {

        //schedule.getScheduleDate().set(iYear, iMonth, iDay, calWorking.get(Calendar.HOUR_OF_DAY), calWorking.get(Calendar.MINUTE));

        return Calendar.getInstance();
    }

    private String dateToString(Calendar calInput) {
        String sBuf = "";

        Integer iMonth = calInput.get(Calendar.MONTH);
        Integer iDay = calInput.get(Calendar.DAY_OF_MONTH);
        Integer iHour = calInput.get(Calendar.HOUR_OF_DAY);
        Integer iMinute = calInput.get(Calendar.MINUTE);

        if(iMonth < 10)
            sBuf += "0";

        sBuf += String.valueOf(iMonth) + "/";

        if(iDay < 10)
            sBuf += "0";

        sBuf += String.valueOf(iDay)
                + "/" + calInput.get(Calendar.YEAR) + ", ";

        if (iHour < 10)
            sBuf += "0";
        sBuf += String.valueOf(iHour) + ":";

        if(iMinute < 10)
            sBuf += "0";
        sBuf += String.valueOf(iMinute);

        return sBuf;
    }

    public void setupClearGames(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Clear Games");
        builder.setMessage("Are you sure that you want to delete the games history?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<SearchEntry> keys = new ArrayList<SearchEntry>();
                List<String> listCat = new ArrayList<String>();
                listCat.add("%");
                keys.add(new SearchEntry(SearchEntry.Type.STRING, "cat", SearchEntry.Search.LIKE, listCat));
                gamesHelper.delete(keys);
                ((MainActivity) context).getSchedulePopulator().resetup();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    public void setupNew(String sCategory){
        new NewWizardDialog(context, sCategory).show();
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    private class PlayerTask extends AsyncTask<Void, Void, Integer> {
        private String[] sxItems;
        private Integer[] ixRandIdx;
        private int len;
        private int nxt;
        private int iMinBreak;

        public PlayerTask(String[] sxItems, String sRate) {

            switch(sRate) {
                case "SUPER":
                    this.iMinBreak = 1;
                    break;
                case "HIGH":
                    this.iMinBreak = 2;
                    break;
                case "MEDIUM":
                    this.iMinBreak = 3;
                    break;
                case "LOW":
                    this.iMinBreak = 4;
                    break;
                default:
                    this.iMinBreak = 2;
                    break;
            }

            this.sxItems = sxItems;
            this.len = sxItems.length;
            this.ixRandIdx = genRandIdx(this.len);
            this.nxt = -1;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Void... params) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();

            String sAction;
            String[] fc_output;

            while(!isCancelled()) {

                String sKeyVal = sxItems[ixRandIdx[++nxt]];

                if (nxt == (len-1)) {
                    nxt = -1;
                }

                int iBuf = sKeyVal.indexOf("-=");
                int iBufColon = sKeyVal.indexOf(":");

                if(iBuf > 0) {
                    if (iBufColon > 0) {
                        sAction = "com.example.SendBroadcast.fc_cat_4";
                        fc_output = new String[9];

                        for(int i=0; i<fc_output.length; i++)
                            fc_output[i] = "";

                        String sKey = sKeyVal.substring(0, iBuf).trim();
                        String sParamVal = sKeyVal.substring(iBuf + 2).trim();

                        fc_output[0] = sKey;

                        String[] sxParamVal = sParamVal.split(";");
                        switch (sxParamVal.length) {
                            case 4:
                                sxParamVal[3] = sxParamVal[3].trim();
                                iBuf = sxParamVal[3].indexOf(":");
                                fc_output[7] = sxParamVal[3].substring(0, Math.min(iBuf, 2));
                                fc_output[8] = sxParamVal[3].substring(iBuf + 1).trim();
                            case 3:
                                sxParamVal[2] = sxParamVal[2].trim();
                                iBuf = sxParamVal[2].indexOf(":");
                                fc_output[5] = sxParamVal[2].substring(0, Math.min(iBuf, 2));
                                fc_output[6] = sxParamVal[2].substring(iBuf + 1).trim();
                            case 2:
                                sxParamVal[1] = sxParamVal[1].trim();
                                iBuf = sxParamVal[1].indexOf(":");
                                fc_output[3] = sxParamVal[1].substring(0, Math.min(iBuf, 2));
                                fc_output[4] = sxParamVal[1].substring(iBuf + 1).trim();
                            case 1:
                                sxParamVal[0] = sxParamVal[0].trim();
                                iBuf = sxParamVal[0].indexOf(":");
                                fc_output[1] = sxParamVal[0].substring(0, Math.min(iBuf, 2));
                                fc_output[2] = sxParamVal[0].substring(iBuf + 1).trim();
                            default:
                                break;
                        }
                    }
                    else {
                        sAction = "com.example.SendBroadcast.fc2";
                        fc_output = new String[2];

                        String sKey = sKeyVal.substring(0, iBuf).trim();
                        String sVal = sKeyVal.substring(iBuf + 2).trim();

                        fc_output[0] = sKey;
                        fc_output[1] = sVal;

                    }

                    String sOutput = "";
                    for(int i=0; i < fc_output.length; i++)
                        sOutput += fc_output[i] + " |";

                    Intent i1 = new Intent();
                    i1.setAction(sAction);
                    i1.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    i1.putExtra("STRING_FC", sOutput);
                    i1.putExtra("BOOL_SHOW_ANSWERS", false);

                    context.sendBroadcast(i1);

                    try {
                        Thread.sleep(30 * 1000);

                        Intent i2 = new Intent();
                        i2.setAction(sAction);
                        i2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        i2.putExtra("STRING_FC", sOutput);
                        i2.putExtra("BOOL_SHOW_ANSWERS", true);
                        context.sendBroadcast(i2);

                        Thread.sleep(iMinBreak * 60 * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                }

                else { // no -= delimiter

                    Intent i3 = new Intent();
                    i3.setAction("com.example.SendBroadcast.fc1");
                    i3.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    i3.putExtra("STRING_FC", sKeyVal);
                    i3.putExtra("BOOL_SHOW_ANSWERS", false);

                    context.sendBroadcast(i3);

                    try {
                        Thread.sleep(iMinBreak * 60 * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            wl.release();
            return 1;
        }

        @Override
        protected void onCancelled(Integer id) {
        }

        @Override
        protected void onPostExecute(Integer id) {
        }
    }

    protected Integer[] genRandIdx(int iSize) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < iSize; i++) {
            list.add(i);
        }

        Collections.shuffle(list);
        return list.toArray(new Integer[list.size()]);
    }
}