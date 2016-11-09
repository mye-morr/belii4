package mm.belii4.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import mm.belii4.data.core.ContactItemHelper;
import mm.belii4.data.core.GamesHelper;
import mm.belii4.data.core.MessageHelper;
import mm.belii4.data.core.NonSchedHelper;
import mm.belii4.data.core.ScheduleHelper;

/**
 * Created by farflk on 6/18/2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    static final String dbName = "schedule_sms_db";
    static DatabaseHelper databaseHelper;
    private List<AbstractHelper> modelHelpers;

    public static void init(Context context){
        if(databaseHelper == null){
            databaseHelper = new DatabaseHelper(context);

            // this creates new tables through onCreate (AbstractHelper)
            databaseHelper.modelHelpers.add(new MessageHelper(context));
            databaseHelper.modelHelpers.add(new ScheduleHelper(context));
            databaseHelper.modelHelpers.add(new NonSchedHelper(context));
            databaseHelper.modelHelpers.add(new ContactItemHelper(context));
            databaseHelper.modelHelpers.add(new GamesHelper(context));
        }
    }

    public static DatabaseHelper getInstance(){
        return databaseHelper;
    }

    public <T> T getHelper(Class<T> type){
        for (int i = 0; i < modelHelpers.size(); i++) {
            if(modelHelpers.get(i).getClass().equals(type)){
                return (T)modelHelpers.get(i);
            }
        }
        return null;
    }

    private DatabaseHelper(Context context) {
        super(context, dbName, null, 2);
        modelHelpers = new ArrayList<AbstractHelper>();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for (int i = 0; i < modelHelpers.size(); i++) {
            modelHelpers.get(i).onCreate(sqLiteDatabase);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        for (int i = 0; i < modelHelpers.size(); i++) {
            modelHelpers.get(i).onUpgrade(sqLiteDatabase, oldVer, newVer);
        }
    }
}
