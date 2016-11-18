package mm.belii4.data.core;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import mm.belii4.data.AbstractHelper;
import mm.belii4.data.AbstractModel;
import mm.belii4.data.DatabaseHelper;
import mm.belii4.data.SearchEntry;

public class NonSchedHelper extends AbstractHelper<NonSched>{

    public NonSchedHelper(Context context) {
        super(context);
        this.tableName = "core_tbl_nonsched";
        this.columns.add("type TEXT");
        this.columns.add("cat TEXT");
        this.columns.add("subcat TEXT");
        this.columns.add("subsub TEXT");
        this.columns.add("iprio TEXT");
        this.columns.add("name TEXT");
        this.columns.add("abbrev TEXT");
        this.columns.add("content TEXT");
        this.columns.add("wt TEXT");
        this.columns.add("extPct TEXT");
        this.columns.add("extThr TEXT");
        this.columns.add("pts TEXT");
        this.columns.add("notes TEXT");
    }

    @Override
    protected NonSched getModelInstance() {
        return new NonSched();
    }

    @Override
    public boolean create(NonSched model) {
        boolean success = super.create(model);
        if (!success) {
            return success;
        }

        NonSchedContentHelper nonSchedContentHelper = DatabaseHelper.getInstance().getHelper(NonSchedContentHelper.class);
        for (NonSchedContent content : model.getContents()) {
            content.setNonschedid(model.get_id());
            success = nonSchedContentHelper.create(content);
        }
        return success;
    }

    @Override
    public boolean update(List<SearchEntry> keys, NonSched model) {
        boolean success =  super.update(keys, model);
        if (!success) {
            return success;
        }

        NonSchedContentHelper nonSchedContentHelper = DatabaseHelper.getInstance().getHelper(NonSchedContentHelper.class);
        nonSchedContentHelper.removeAllByNonSchedId(model.get_id());
        for (NonSchedContent content : model.getContents()) {
            content.setNonschedid(model.get_id());
            success = nonSchedContentHelper.create(content);
        }
        return success;
    }

    @Override
    public List<NonSched> find(List<SearchEntry> keys) {
        List<NonSched> list = super.find(keys);
        for (NonSched model : list) {
            getContents(model);
        }
        return list;
    }

    @Override
    public NonSched get(List<SearchEntry> keys) {
        NonSched model = super.get(keys);
        getContents(model);
        return model;
    }

    private void getContents(NonSched model) {
        NonSchedContentHelper nonSchedContentHelper = DatabaseHelper.getInstance().getHelper(NonSchedContentHelper.class);
        List<NonSchedContent> list = nonSchedContentHelper.findAllByNonSchedId(model.get_id());
        model.setContents(list);
    }
}