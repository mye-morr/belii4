package mm.belii4.data.core;

import android.content.Context;

import mm.belii4.data.AbstractHelper;

public class ContentHelper extends AbstractHelper<Content> {

    public ContentHelper(Context context) {
        super(context);
        this.tableName = "core_tbl_content";
        this.columns.add("nonschedid TEXT");
        this.columns.add("content TEXT");
        this.columns.add("weight REAL");
    }

    @Override
    protected Content getModelInstance() {
        return new Content();
    }

}