package mm.belii4.form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import mm.belii4.MainActivity;

public abstract class AbstractPopulator {
    protected Context context;
    protected View rootView;
    protected String category;

    public AbstractPopulator(Context context){
        this.context = context;
    }

    public void setup(View rootView, String category) {
        this.rootView = rootView;
        this.category = category;
    }

    public void resetup(){
            setup(rootView, category);
    }
}
