package mm.belii4.form.schedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import mm.belii4.R;
import mm.belii4.data.core.Content;

public class ContentListAdapter extends ArrayAdapter<Content> {

    private int resourceId;
    private List<Content> contents;
    private Context context;

    public ContentListAdapter(Context context, List<Content> contents) {
        super(context, R.layout.list_item_schedule, contents);
        this.context = context;
        this.contents = contents;
        this.resourceId = R.layout.list_item_schedule;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = initView(convertView);

        Content content = contents.get(position);

        ((TextView) convertView.findViewById(R.id.schedule_item_summary)).setText(content.getContent());

        if(content.get_state().equalsIgnoreCase("active")) {
            ((ImageView) convertView.findViewById(R.id.schedule_item_icon)).setImageResource(R.drawable.schedule_single);
        }
        else{
            ((ImageView) convertView.findViewById(R.id.schedule_item_icon)).setImageResource(R.drawable.schedule_single_inactive);
        }

        convertView.setBackgroundColor(0x00000000);
        return convertView;
    }

    private View initView(View convertView){
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return vi.inflate(resourceId, null);
        }else{
            return convertView;
        }
    }
}