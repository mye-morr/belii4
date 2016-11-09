package mm.belii4.form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import mm.belii4.MainActivity;

/**
 * Created by Faraj on 6/12/14.
 */
public abstract class AbstractPopulator {
    protected Context context;
    protected View rootView;
    protected String category;
    protected String state;

    public AbstractPopulator(Context context){
        this.context = context;
        this.state = "";
    }

    public void setup(View rootView, String category) {
        this.rootView = rootView;
        this.category = category;
        this.state = "";
    }

    public void resetup(){
            setup(rootView, category);
    }

    protected Dialog showDialogFromResource(int resourceId){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(resourceId);
        dialog.show();
        return dialog;
    }

    protected AlertDialog.Builder getDialogBuilder(int resource){
        LayoutInflater inflater = ((MainActivity)context).getLayoutInflater();
        View dialogLayout = inflater.inflate(resource, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogLayout);
        return builder;
    }
}
