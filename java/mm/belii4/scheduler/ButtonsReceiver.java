package mm.belii4.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import mm.belii4.data.DatabaseHelper;
import mm.belii4.data.core.Games;
import mm.belii4.data.core.GamesHelper;

public class ButtonsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Games game = new Games();
        game.setCat(intent.getStringExtra("CATEGORY_PRESSED"));
        game.setSubcat("");
        game.setContent(intent.getStringExtra("STRING_PRESSED"));
        game.setPts(intent.getStringExtra("POINTS_PRESSED"));
        game.setTimestamp(Calendar.getInstance());

        DatabaseHelper.getInstance().getHelper(GamesHelper.class).createOrUpdate(game);
    }

}
