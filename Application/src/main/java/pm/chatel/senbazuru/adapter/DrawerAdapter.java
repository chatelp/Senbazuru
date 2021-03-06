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

package pm.chatel.senbazuru.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pm.chatel.senbazuru.MainApplication;
import pm.chatel.senbazuru.R;
import pm.chatel.senbazuru.provider.FeedData;
import pm.chatel.senbazuru.provider.FeedData.EntryColumns;
import pm.chatel.senbazuru.utils.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class DrawerAdapter extends BaseAdapter {

    private static final int POS_ID = 0;
    private static final int POS_URL = 1;
    private static final int POS_NAME = 2;
    private static final int POS_IS_GROUP = 3;
    private static final int POS_ICON = 4;
    private static final int POS_LAST_UPDATE = 5;
    private static final int POS_ERROR = 6;
    private static final int POS_UNREAD = 7;

    public static final int ABOUT_DRAWER_POSITION = -1;
    public static final int ALL_DRAWER_POSITION = 0;
    public static final int STARED_DRAWER_POSITION = 1;
    public static final int EASY_DRAWER_POSITION = 2;
    public static final int INTERMEDIATE_DRAWER_POSITION = 3;
    public static final int ADVANCED_DRAWER_POSITION = 4;
    public static final int SEARCH_DRAWER_POSITION = 5;


    private static final int NORMAL_TEXT_COLOR = Color.parseColor("#EEEEEE");
    private static final int GROUP_TEXT_COLOR = Color.parseColor("#BBBBBB");

    private static final String COLON = MainApplication.getContext().getString(R.string.colon);

    private static final int CACHE_MAX_ENTRIES = 100;
    private final Map<Long, String> mFormattedDateCache = new LinkedHashMap<Long, String>(CACHE_MAX_ENTRIES + 1, .75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
            return size() > CACHE_MAX_ENTRIES;
        }
    };

    private final Context mContext;
    private Cursor mFeedsCursor;
    private int mAllNumber, mFavoritesNumber, mEasyNumber, mIntermediateNumber, mAdvancedNumber;

    public DrawerAdapter(Context context, Cursor feedCursor) {
        mContext = context;
        mFeedsCursor = feedCursor;

        updateNumbers();
    }

    public void setCursor(Cursor feedCursor) {
        mFeedsCursor = feedCursor;

        updateNumbers();
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_drawer_list, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.iconView = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.titleTxt = (TextView) convertView.findViewById(android.R.id.text1);
            holder.stateTxt = (TextView) convertView.findViewById(android.R.id.text2);
            holder.countTxt = (TextView) convertView.findViewById(R.id.count);
            holder.separator = convertView.findViewById(R.id.separator);
            convertView.setTag(R.id.holder, holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag(R.id.holder);

        // default init
        holder.iconView.setImageDrawable(null);
        holder.titleTxt.setText("");
        holder.titleTxt.setTextColor(NORMAL_TEXT_COLOR);
        holder.titleTxt.setAllCaps(false);
        holder.stateTxt.setVisibility(View.GONE);
        holder.countTxt.setText("");
        convertView.setPadding(0, 0, 0, 0);
        holder.separator.setVisibility(View.GONE);

        if (position == ALL_DRAWER_POSITION) {

            holder.titleTxt.setText(R.string.all);
            holder.iconView.setImageResource(R.drawable.senbazuru_ui_icon_64pt);
            int unread = mAllNumber;
            if (unread != 0) {
                holder.countTxt.setText(String.valueOf(unread));
            }

            //Attention: position du curseur (0 = premier feed) different de position dans la liste
            if (mFeedsCursor != null && mFeedsCursor.moveToPosition(0)) {
                if (mFeedsCursor.getInt(POS_IS_GROUP) != 1) {
                    holder.stateTxt.setVisibility(View.VISIBLE);

                    if (mFeedsCursor.isNull(POS_ERROR)) {
                        long timestamp = mFeedsCursor.getLong(POS_LAST_UPDATE);

                        // Date formatting is expensive, look at the cache
                        String formattedDate = mFormattedDateCache.get(timestamp);
                        if (formattedDate == null) {

                            formattedDate = mContext.getString(R.string.update) + COLON;

                            if (timestamp == 0) {
                                formattedDate += mContext.getString(R.string.never);
                            } else {
                                formattedDate += StringUtils.getDateTimeString(timestamp);
                            }

                            mFormattedDateCache.put(timestamp, formattedDate);
                        }

                        holder.stateTxt.setText(formattedDate);
                    } else {
                        holder.stateTxt.setText(new StringBuilder(mContext.getString(R.string.error)).append(COLON).append(mFeedsCursor.getString(POS_ERROR)));
                    }
                }
            }
        } else if (position == STARED_DRAWER_POSITION) {
            holder.titleTxt.setText(R.string.favorites);
            holder.iconView.setImageResource(R.drawable.rating_important);
            int unread =  mFavoritesNumber;
            if (unread != 0) {
                holder.countTxt.setText(String.valueOf(unread));
            }
        }
        else if (position == EASY_DRAWER_POSITION) {
            holder.titleTxt.setText(R.string.easy);
            holder.iconView.setImageResource(R.drawable.cool_64pt);
            int unread = mEasyNumber;
            if (unread != 0) {
                holder.countTxt.setText(String.valueOf(unread));
            }
        } else if (position == INTERMEDIATE_DRAWER_POSITION) {
            holder.titleTxt.setText(R.string.intermediate);
            holder.iconView.setImageResource(R.drawable.happy_64pt);
            int unread = mIntermediateNumber;
            if (unread != 0) {
                holder.countTxt.setText(String.valueOf(unread));
            }
        }else if (position == ADVANCED_DRAWER_POSITION) {
            holder.titleTxt.setText(R.string.advanced);
            holder.iconView.setImageResource(R.drawable.evil_64pt);
            int unread = mAdvancedNumber;
            if (unread != 0) {
                holder.countTxt.setText(String.valueOf(unread));
            }
        }
        else if (position == SEARCH_DRAWER_POSITION) {
            holder.titleTxt.setText(R.string.find);
            holder.iconView.setImageResource(R.drawable.action_search);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        //Number of entries in the drawer
        return 6;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2)) {
            return mFeedsCursor.getLong(POS_ID);
        }

        return -1;
    }

    public byte[] getItemIcon(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2)) {
            return mFeedsCursor.getBlob(POS_ICON);
        }

        return null;
    }

    public String getItemName(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2)) {
            return mFeedsCursor.isNull(POS_NAME) ? mFeedsCursor.getString(POS_URL) : mFeedsCursor.getString(POS_NAME);
        }

        return null;
    }

    public boolean isItemAGroup(int position) {
        return mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2) && mFeedsCursor.getInt(POS_IS_GROUP) == 1;

    }

    private void updateNumbers() {
        mAllNumber = mFavoritesNumber = mEasyNumber = mIntermediateNumber = mAdvancedNumber = 0;

        // Gets the numbers of entries (should be in a thread, but it's way easier like this and it shouldn't be so slow)
        Cursor numbers = mContext.getContentResolver().query(EntryColumns.CONTENT_URI, new String[]{FeedData.ALL_NUMBER, FeedData.FAVORITES_NUMBER, FeedData.EASY_NUMBER, FeedData.INTERMEDIATE_NUMBER, FeedData.ADVANCED_NUMBER}, null, null, null);
        if (numbers != null) {
            if (numbers.moveToFirst()) {
                mAllNumber = numbers.getInt(0);
                mFavoritesNumber = numbers.getInt(1);
                mEasyNumber = numbers.getInt(2);
                mIntermediateNumber = numbers.getInt(3);
                mAdvancedNumber = numbers.getInt(4);
            }
            numbers.close();
        }
    }

    private static class ViewHolder {
        public ImageView iconView;
        public TextView titleTxt;
        public TextView stateTxt;
        public TextView countTxt;
        public View separator;
    }
}
