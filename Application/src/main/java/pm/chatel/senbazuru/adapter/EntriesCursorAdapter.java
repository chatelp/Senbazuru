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
 * <p/>
 * <p/>
 * Some parts of this software are based on "Sparse rss" under the MIT license (see
 * below). Please refers to the original project to identify which parts are under the
 * MIT license.
 * <p/>
 * Copyright (c) 2010-2012 Stefan Handschuh - for parts derived from Sparse rss
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package pm.chatel.senbazuru.adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pm.chatel.senbazuru.Constants;
import pm.chatel.senbazuru.MainApplication;
import pm.chatel.senbazuru.R;
import pm.chatel.senbazuru.provider.FeedData;
import pm.chatel.senbazuru.provider.FeedData.CategoryColumns;
import pm.chatel.senbazuru.provider.FeedData.EntryColumns;
import pm.chatel.senbazuru.provider.FeedData.FeedColumns;
import pm.chatel.senbazuru.utils.CircleTransform;
import pm.chatel.senbazuru.utils.NetworkUtils;
import pm.chatel.senbazuru.utils.StringUtils;

public class EntriesCursorAdapter extends ResourceCursorAdapter {

    private final Uri mUri;
    private final boolean mShowFeedInfo;
    private final CircleTransform mCircleTransform = new CircleTransform();
    private int mIdPos, mTitlePos, mMainImgPos, mDatePos, mIsReadPos, mFavoritePos, mFeedIdPos, mCategoryPos;

    public EntriesCursorAdapter(Context context, Uri uri, Cursor cursor, boolean showFeedInfo) {
        super(context, R.layout.item_entry_list, cursor, 0);
        mUri = uri;
        mShowFeedInfo = showFeedInfo;

        reinit(cursor);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        if (view.getTag(R.id.holder) == null) {
            ViewHolder holder = new ViewHolder();
            holder.titleTextView = (TextView) view.findViewById(android.R.id.text1);
            holder.dateTextView = (TextView) view.findViewById(android.R.id.text2);
            holder.mainImgView = (ImageView) view.findViewById(R.id.main_icon);
            holder.starImgView = (ImageView) view.findViewById(R.id.favorite_icon);
            holder.difficultyImgView = (ImageView) view.findViewById(R.id.difficulty_icon);
            view.setTag(R.id.holder, holder);
        }

        final ViewHolder holder = (ViewHolder) view.getTag(R.id.holder);

        String titleText = cursor.getString(mTitlePos);
        holder.titleTextView.setText(titleText);

        String mainImgUrl = cursor.getString(mMainImgPos);
        mainImgUrl = TextUtils.isEmpty(mainImgUrl) ? null : NetworkUtils.getDownloadedOrDistantImageUrl(cursor.getLong(mIdPos), mainImgUrl);

        TextDrawable letterDrawable = TextDrawable.builder().buildRound((titleText != null ? titleText.substring(0, 1).toUpperCase() : ""), context.getResources().getColor(R.color.primary_light));
        if (mainImgUrl != null) {
            Picasso.with(context).load(mainImgUrl).transform(mCircleTransform).placeholder(letterDrawable).error(letterDrawable).into(holder.mainImgView);
        } else {
            Picasso.with(context).cancelRequest(holder.mainImgView);
            holder.mainImgView.setImageDrawable(letterDrawable);
        }

        holder.isFavorite = cursor.getInt(mFavoritePos) == 1;

        holder.starImgView.setVisibility(holder.isFavorite ? View.VISIBLE : View.INVISIBLE);

        holder.dateTextView.setText(StringUtils.getDateTimeString(cursor.getLong(mDatePos)));

        if (cursor.isNull(mIsReadPos)) {
            holder.titleTextView.setEnabled(true);
            holder.dateTextView.setEnabled(true);
            holder.isRead = false;
        } else {
            holder.titleTextView.setEnabled(false);
            holder.dateTextView.setEnabled(false);
            holder.isRead = true;
        }

        holder.difficultyImgView.setImageResource(this.getDifficultyImage(context, cursor.getString(mIdPos)));
    }

    private int getDifficultyImage(Context context, String entryID) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(CategoryColumns.CATEGORIES_FOR_ENTRY_CONTENT_URI(entryID), new String[]{CategoryColumns.CATEGORY}, "category LIKE 'Difficulté%'", null, null);
            while (cursor.moveToNext()) {
                String category = cursor.getString(0);
                Pattern pattern = Pattern.compile("★{1,3}$");
                Matcher matcher = pattern.matcher(category);
                while(matcher.find()) {
                    switch (matcher.group().length()) {
                        case 1:
                            return R.drawable.cool_lightgrey_64pt;
                        case 2:
                            return R.drawable.happy_lightgrey_64pt;
                        case 3:
                            return R.drawable.evil_lightgrey_64pt;
                    }
                    break;
                }
                break;
            }
        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            } catch(Exception ex) {}
        }

        return android.R.color.transparent;
    }

    public void toggleReadState(final long id, View view) {
        final ViewHolder holder = (ViewHolder) view.getTag(R.id.holder);

        if (holder != null) { // should not happen, but I had a crash with this on PlayStore...
            holder.isRead = !holder.isRead;

            if (holder.isRead) {
                holder.titleTextView.setEnabled(false);
                holder.dateTextView.setEnabled(false);
            } else {
                holder.titleTextView.setEnabled(true);
                holder.dateTextView.setEnabled(true);
            }

            new Thread() {
                @Override
                public void run() {
                    ContentResolver cr = MainApplication.getContext().getContentResolver();
                    Uri entryUri = ContentUris.withAppendedId(mUri, id);
                    cr.update(entryUri, holder.isRead ? FeedData.getReadContentValues() : FeedData.getUnreadContentValues(), null, null);
                }
            }.start();
        }
    }

    public void toggleFavoriteState(final long id, View view) {
        final ViewHolder holder = (ViewHolder) view.getTag(R.id.holder);

        if (holder != null) { // should not happen, but I had a crash with this on PlayStore...
            holder.isFavorite = !holder.isFavorite;

            if (holder.isFavorite) {
                holder.starImgView.setVisibility(View.VISIBLE);
            } else {
                holder.starImgView.setVisibility(View.INVISIBLE);
            }

            new Thread() {
                @Override
                public void run() {
                    ContentValues values = new ContentValues();
                    values.put(EntryColumns.IS_FAVORITE, holder.isFavorite ? 1 : 0);

                    ContentResolver cr = MainApplication.getContext().getContentResolver();
                    Uri entryUri = ContentUris.withAppendedId(mUri, id);
                    cr.update(entryUri, values, null, null);
                }
            }.start();
        }
    }

    public void markAllAsRead(final long untilDate) {
        new Thread() {
            @Override
            public void run() {
                ContentResolver cr = MainApplication.getContext().getContentResolver();
                String where = EntryColumns.WHERE_UNREAD + Constants.DB_AND + '(' + EntryColumns.FETCH_DATE + Constants.DB_IS_NULL + Constants.DB_OR + EntryColumns.FETCH_DATE + "<=" + untilDate + ')';
                cr.update(mUri, FeedData.getReadContentValues(), where, null);
            }
        }.start();
    }

    @Override
    public void changeCursor(Cursor cursor) {
        reinit(cursor);
        super.changeCursor(cursor);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        reinit(newCursor);
        return super.swapCursor(newCursor);
    }

    @Override
    public void notifyDataSetChanged() {
        reinit(null);
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        reinit(null);
        super.notifyDataSetInvalidated();
    }

    private void reinit(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            mIdPos = cursor.getColumnIndex(EntryColumns._ID);
            mTitlePos = cursor.getColumnIndex(EntryColumns.TITLE);
            mMainImgPos = cursor.getColumnIndex(EntryColumns.IMAGE_URL);
            mDatePos = cursor.getColumnIndex(EntryColumns.DATE);
            mIsReadPos = cursor.getColumnIndex(EntryColumns.IS_READ);
            mFavoritePos = cursor.getColumnIndex(EntryColumns.IS_FAVORITE);
            mFeedIdPos = cursor.getColumnIndex(EntryColumns.FEED_ID);
        }
    }

    private static class ViewHolder {
        public TextView titleTextView;
        public TextView dateTextView;
        public ImageView mainImgView;
        public ImageView starImgView;
        public ImageView difficultyImgView;
        public boolean isRead, isFavorite;
    }
}
