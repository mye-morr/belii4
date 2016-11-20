package mm.belii4.data.core;

import android.content.Context;

import mm.belii4.data.AbstractHelper;

/**
 * Created by tedwei on 11/18/16.
 */

public class NonSchedContentHelper extends AbstractHelper<NonSchedContent> {

    public NonSchedContentHelper(Context context) {
        super(context);
        this.tableName = "core_tbl_nonsched_content";
        this.columns.add("nonschedid TEXT");
        this.columns.add("content TEXT");
        this.columns.add("weight REAL");
    }

    @Override
    protected NonSchedContent getModelInstance() {
        return new NonSchedContent();
    }

}