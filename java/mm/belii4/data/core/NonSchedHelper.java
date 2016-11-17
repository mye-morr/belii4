package mm.belii4.data.core;

import android.content.Context;

import mm.belii4.data.AbstractHelper;
import mm.belii4.data.AbstractModel;

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
}