package mm.belii4.data.core;

import android.content.Context;

import mm.belii4.data.AbstractHelper;

/**
 * Created by tedwei on 11/18/16.
 */

public class NonSchedContentHelper extends AbstractHelper<NonSchedContent> {

    public NonSchedContentHelper(Context context) {
        super(context);
        this.tableName = "core_tbl_games";
        this.columns.add("nonschedid TEXT");
        this.columns.add("content TEXT");
    }

    @Override
    protected NonSchedContent getModelInstance() {
        return new NonSchedContent();
    }

}