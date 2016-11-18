package mm.belii4.data.core;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import mm.belii4.data.AbstractHelper;
import mm.belii4.data.SearchEntry;

/**
 * Created by tedwei on 11/18/16.
 */

public class NonSchedContentHelper extends AbstractHelper<NonSchedContent> {

    public NonSchedContentHelper(Context context) {
        super(context);
        this.tableName = "core_tbl_nonsched_content";
        this.columns.add("nonschedid TEXT");
        this.columns.add("content TEXT");
    }

    @Override
    protected NonSchedContent getModelInstance() {
        return new NonSchedContent();
    }

    public void removeAllByNonSchedId(String nonschedid) {
        List<SearchEntry> keys = new ArrayList<SearchEntry>();
        keys.add(new SearchEntry(SearchEntry.Type.STRING, "nonschedid", SearchEntry.Search.EQUAL, nonschedid));
        delete(keys);
    }

    public List<NonSchedContent> findAllByNonSchedId(String nonschedid) {
        return findBy("nonschedid", nonschedid);
    }

}