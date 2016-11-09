package mm.belii4.data.core;

import android.content.ContentValues;

import java.util.Map;

import mm.belii4.data.AbstractModel;

// to store flashcards, games, comTas

public class NonSched extends AbstractModel{
    private String type = "";
    private String cat = "";
    private String subcat = "";
    private String subsub = "";
    private String name = "";
    private String abbrev = "";
    private String content = "";
    private String wt = "";
    private String extpct = "";
    private String extthr = "";
    private String pts = "";
    private String notes = "";

    public ContentValues getContentValues() {

        ContentValues contentValues = super.getContentValues();

        contentValues.put("type", type);
        contentValues.put("cat", cat);
        contentValues.put("subcat", subcat);
        contentValues.put("subsub", subsub);
        contentValues.put("name", name);
        contentValues.put("abbrev", abbrev);
        contentValues.put("content", content);
        contentValues.put("wt", wt);
        contentValues.put("extpct", extpct);
        contentValues.put("extthr", extthr);
        contentValues.put("pts", pts);
        contentValues.put("notes", notes);

        return contentValues;
    }

    @Override
    public void populateWith(Map<String, Object> data) {
        super.populateWith(data);

        type = fetchData(data, "type");
        cat = fetchData(data, "cat");
        subcat = fetchData(data, "subcat");
        subsub = fetchData(data, "subsub");
        name = fetchData(data, "name");
        abbrev = fetchData(data, "abbrev");
        content = fetchData(data, "content");
        wt = fetchData(data, "wt");
        extpct = fetchData(data, "extpct");
        extthr = fetchData(data, "extthr");
        pts = fetchData(data, "pts");
        notes = fetchData(data, "notes");
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getCat() { return cat; }

    public void setCat(String cat) { this.cat = cat; }

    public String getSubcat() { return subcat; }

    public void setSubcat(String subcat) { this.subcat = subcat; }

    public String getSubsub() { return subsub; }

    public void setSubsub(String subsub) { this.subsub = subsub; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getAbbrev() { return abbrev; }

    public void setAbbrev(String abbrev) { this.abbrev = abbrev; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getWt() { return wt; }

    public void setWt(String wt) { this.wt = wt; }

    public String getExtpct() { return extpct; }

    public void setExtpct(String extpct) { this.extpct = extpct; }

    public String getExtthr() { return extthr; }

    public void setExtthr(String extthr) { this.extthr = extthr; }

    public String getPts() { return pts; }

    public void setPts(String pts) { this.pts = pts; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

}
