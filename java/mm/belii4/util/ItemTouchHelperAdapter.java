package mm.belii4.util;

import android.support.v7.widget.RecyclerView;

/**
 * Created by tedwei on 11/24/16.
 */

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

}

