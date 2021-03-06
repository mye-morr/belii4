package mm.belii4.form.history;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mm.belii4.R;
import mm.belii4.data.DatabaseHelper;
import mm.belii4.data.SearchEntry;
import mm.belii4.data.core.Message;
import mm.belii4.data.core.MessageHelper;
import mm.belii4.form.AbstractPopulator;

public class HistoryPopulator extends AbstractPopulator {

    public HistoryPopulator(Context context) {
        super(context);
    }

    @Override
    public void setup(View rootView, String category) {
        super.setup(rootView, category);
        List<Message> messages = (List<Message>)(List<?>) DatabaseHelper.getInstance().getHelper(MessageHelper.class).findAll();
        Collections.reverse(messages);
        ((ListView) rootView.findViewById(R.id.message_list)).setAdapter(new MessageListAdapter(context, messages));
    }

    public void setupClearHistory(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Clear sent message history");
        builder.setMessage("Are you sure that you want to delete the sent message history?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MessageHelper messageHelper = DatabaseHelper.getInstance().getHelper(MessageHelper.class);
                List<SearchEntry> keys = new ArrayList<SearchEntry>();
                List<String> states = new ArrayList<String>();
                states.add("delivered");
                states.add("failed");
                keys.add(new SearchEntry(SearchEntry.Type.STRING, "_state", SearchEntry.Search.IN, states));
                messageHelper.delete(keys);
                resetup();
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
}
