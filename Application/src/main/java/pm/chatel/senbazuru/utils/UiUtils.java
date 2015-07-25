/**
 * Senbazuru for Android
 * <p/>
 * Copyright (c) 2015 Pierre CHATEL
 * Copyright (c) 2012-2015 Frederic Julian - for parts derived from Flym
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pm.chatel.senbazuru.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LongSparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;

import pm.chatel.senbazuru.MainApplication;
import pm.chatel.senbazuru.R;

public class UiUtils {

    static public int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MainApplication.getContext().getResources().getDisplayMetrics());
    }

    static public Bitmap getScaledBitmap(byte[] iconBytes, int sizeInDp) {
        if (iconBytes != null && iconBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
            if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
                int bitmapSizeInDip = UiUtils.dpToPixel(sizeInDp);
                if (bitmap.getHeight() != bitmapSizeInDip) {
                    Bitmap tmp = bitmap;
                    bitmap = Bitmap.createScaledBitmap(tmp, bitmapSizeInDip, bitmapSizeInDip, false);
                    tmp.recycle();
                }

                return bitmap;
            }
        }

        return null;
    }

    static public void updateFindFABButton(FloatingActionButton findFABButton) {
        if (PrefUtils.getBoolean(PrefUtils.SHOW_SEARCH, true)) {
            findFABButton.setColorNormal(findFABButton.getContext().getResources().getColor(R.color.primary_dark));

            //findFABButton.setColorNormalResId(getAttrResource(findFABButton.getContext(), R.attr.colorPrimaryDark, R.color.light_theme_color_primary_dark));
        } else {
            findFABButton.setColorNormal(findFABButton.getContext().getResources().getColor(R.color.accent));
            //findFABButton.setColorNormalResId(getAttrResource(findFABButton.getContext(), R.attr.colorPrimary, R.color.light_theme_color_primary));
        }
    }


    static public void addEmptyFooterView(ListView listView, int dp) {
        View view = new View(listView.getContext());
        view.setMinimumHeight(dpToPixel(dp));
        view.setClickable(true);
        listView.addFooterView(view);
    }

    static public int getAttrResource(Context context, int attrId, int defValue) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attrId});
        int result = a.getResourceId(0, defValue);
        a.recycle();
        return result;
    }
}
