package mm.belii4.data.core;

import android.content.ContentValues;

import java.util.Map;

import mm.belii4.data.AbstractModel;

/**
 * Created by tedwei on 11/18/16.
 */

public class NonSchedContent extends AbstractModel {

    private String nonschedid = "";
    private String content = "";

    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();

        contentValues.put("nonschedid", nonschedid);
        contentValues.put("content", content);

        return contentValues;
    }

    @Override
    public void populateWith(Map<String, Object> data) {
        super.populateWith(data);

        nonschedid = fetchData(data, "nonschedid");
        content = fetchData(data, "content");
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
}
