package mm.belii4.data.core;

import android.content.ContentValues;

import java.util.Map;

import mm.belii4.data.AbstractModel;

public class Content extends AbstractModel {

    private String nonschedid = "";
    private String content = "";
    private double weight = 0.0;

    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();

        contentValues.put("nonschedid", nonschedid);
        contentValues.put("content", content);
        contentValues.put("weight", weight);

        return contentValues;
    }

    @Override
    public void populateWith(Map<String, Object> data) {
        super.populateWith(data);

        nonschedid = fetchData(data, "nonschedid");
        content = fetchData(data, "content");
        weight = fetchDataDouble(data, "weight");
    }

    public String getNonschedid() {
        return nonschedid;
    }

    public void setNonschedid(String nonschedid) {
        this.nonschedid = nonschedid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getWeight() { return weight; }

    public void setWeight(double weight) { this.weight = weight; }

}
