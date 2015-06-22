/**
 * Flym
 * <p/>
 * Copyright (c) 2012-2015 Frederic Julian
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

package pm.chatel.senbazuru.activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import pm.chatel.senbazuru.Constants;
import pm.chatel.senbazuru.R;
import pm.chatel.senbazuru.adapter.DrawerAdapter;
import pm.chatel.senbazuru.fragment.EntriesListFragment;
import pm.chatel.senbazuru.provider.FeedData;
import pm.chatel.senbazuru.provider.FeedData.EntryColumns;
import pm.chatel.senbazuru.provider.FeedData.FeedColumns;
import pm.chatel.senbazuru.provider.FeedDataContentProvider;
import pm.chatel.senbazuru.service.FetcherService;
import pm.chatel.senbazuru.service.RefreshService;
import pm.chatel.senbazuru.utils.PrefUtils;
import pm.chatel.senbazuru.utils.UiUtils;

public class HomeActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_CURRENT_DRAWER_POS = "STATE_CURRENT_DRAWER_POS";

    private static final String FEED_UNREAD_NUMBER = "(SELECT " + Constants.DB_COUNT + " FROM " + EntryColumns.TABLE_NAME + " WHERE " +
            EntryColumns.IS_READ + " IS NULL AND " + EntryColumns.FEED_ID + '=' + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID + ')';

    private static final String WHERE_UNREAD_ONLY = "(SELECT " + Constants.DB_COUNT + " FROM " + EntryColumns.TABLE_NAME + " WHERE " +
            EntryColumns.IS_READ + " IS NULL AND " + EntryColumns.FEED_ID + "=" + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID + ") > 0" +
            " OR (" + FeedColumns.IS_GROUP + "=1 AND (SELECT " + Constants.DB_COUNT + " FROM " + FeedData.ENTRIES_TABLE_WITH_FEED_INFO +
            " WHERE " + EntryColumns.IS_READ + " IS NULL AND " + FeedColumns.GROUP_ID + '=' + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID +
            ") > 0)";

    private static final int LOADER_ID = 0;
    private static final int SEARCH_DRAWER_POSITION = -1;

    private EntriesListFragment mEntriesFragment;
    private DrawerLayout mDrawerLayout;
    private View mLeftDrawer;
    private ListView mDrawerList;
    private DrawerAdapter mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private BitmapDrawable mIcon;
    private int mCurrentDrawerPos;

    private boolean mCanQuit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mEntriesFragment = (EntriesListFragment) getFragmentManager().findFragmentById(R.id.entries_list_fragment);

        mTitle = getTitle();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLeftDrawer = findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectDrawerItem(position);
                if (mDrawerLayout != null) {
                    mDrawerLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawer(mLeftDrawer);
                        }
                    }, 50);
                }
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        if (savedInstanceState != null) {
            mCurrentDrawerPos = savedInstanceState.getInt(STATE_CURRENT_DRAWER_POS);
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);

        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ENABLED, true)) {
            // starts the service independent to this activity
            startService(new Intent(this, RefreshService.class));
        } else {
            stopService(new Intent(this, RefreshService.class));
        }
        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ON_OPEN_ENABLED, true)) {
            if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
                startService(new Intent(HomeActivity.this, FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_DRAWER_POS, mCurrentDrawerPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void finish() {
        if (mCanQuit) {
            super.finish();
            return;
        }

        Toast.makeText(this, R.string.back_again_to_quit, Toast.LENGTH_SHORT).show();
        mCanQuit = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCanQuit = false;
            }
        }, 3000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // We reset the current drawer position
        selectDrawerItem(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickToggleSearch(View view) {
        if (PrefUtils.getBoolean(PrefUtils.SHOW_SEARCH, true)) {
            //Recherche deja affichee -> on repasse a tous les origamis
            selectDrawerItem(0);
            if (mDrawerLayout != null) {
                mDrawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawer(mLeftDrawer);
                    }
                }, 50);
            }
        }
        else {
            this.onClickSearch(view);
        }
    }

    public void onClickSearch(View view) {
        selectDrawerItem(SEARCH_DRAWER_POSITION);
        if (mDrawerLayout != null) {
            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(mLeftDrawer);
                }
            }, 50);
        }
    }

    public void onClickSettings(View view) {
        startActivity(new Intent(this, GeneralPrefsActivity.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = new CursorLoader(this, FeedColumns.GROUPED_FEEDS_CONTENT_URI, new String[]{FeedColumns._ID, FeedColumns.URL, FeedColumns.NAME,
                FeedColumns.IS_GROUP, FeedColumns.ICON, FeedColumns.LAST_UPDATE, FeedColumns.ERROR, FEED_UNREAD_NUMBER}, "" , null, null
        );
        cursorLoader.setUpdateThrottle(Constants.UPDATE_THROTTLE_DELAY);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (mDrawerAdapter != null) {
            mDrawerAdapter.setCursor(cursor);
        } else {
            mDrawerAdapter = new DrawerAdapter(this, cursor);
            mDrawerList.setAdapter(mDrawerAdapter);

            // We don't have any menu yet, we need to display it
            mDrawerList.post(new Runnable() {
                @Override
                public void run() {
                    selectDrawerItem(mCurrentDrawerPos);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mDrawerAdapter.setCursor(null);
    }

    private void selectDrawerItem(int position) {
        mCurrentDrawerPos = position;
        mIcon = null;

        Uri newUri;
        boolean showFeedInfo = true;

        switch (position) {
            case SEARCH_DRAWER_POSITION:
                newUri = EntryColumns.SEARCH_URI(mEntriesFragment.getCurrentSearch());
                PrefUtils.putBoolean(PrefUtils.SHOW_SEARCH, true);
                break;
            case 0:
                newUri = EntryColumns.ALL_ENTRIES_CONTENT_URI;
                PrefUtils.putBoolean(PrefUtils.SHOW_SEARCH, false);
                break;
            case 1:
                newUri = EntryColumns.FAVORITES_CONTENT_URI;
                PrefUtils.putBoolean(PrefUtils.SHOW_SEARCH, false);
                break;
            default:
                long feedOrGroupId = mDrawerAdapter.getItemId(position);
                if (mDrawerAdapter.isItemAGroup(position)) {
                    newUri = EntryColumns.ENTRIES_FOR_GROUP_CONTENT_URI(feedOrGroupId);
                } else {
                    byte[] iconBytes = mDrawerAdapter.getItemIcon(position);
                    Bitmap bitmap = UiUtils.getScaledBitmap(iconBytes, 24);
                    if (bitmap != null) {
                        mIcon = new BitmapDrawable(getResources(), bitmap);
                    }

                    newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(feedOrGroupId);
                    showFeedInfo = false;
                }
                mTitle = mDrawerAdapter.getItemName(position);
                PrefUtils.putBoolean(PrefUtils.SHOW_SEARCH, false);
                break;
        }

        if (!newUri.equals(mEntriesFragment.getUri())) {
            mEntriesFragment.setData(newUri, showFeedInfo);
        }

        mDrawerList.setItemChecked(position, true);

        // First launch
        if (PrefUtils.getBoolean(PrefUtils.FIRST_OPEN, true)) {
            PrefUtils.putBoolean(PrefUtils.FIRST_OPEN, false);

            //Premier lancement: on ajoute le flux RSS à la main
            FeedDataContentProvider.addFeed(HomeActivity.this, "senbazuru.fr/files/feed.xml", "Senbazuru", false);
            //Premier lancement: et on lance le refresh a la main
            if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
                startService(new Intent(HomeActivity.this, FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
            }
        }

        // Set title & icon
        switch (mCurrentDrawerPos) {
            case SEARCH_DRAWER_POSITION:
                getSupportActionBar().setTitle(android.R.string.search_go);
                getSupportActionBar().setIcon(R.drawable.action_search);
                break;
            case 0:
                getSupportActionBar().setTitle(R.string.all);
                getSupportActionBar().setIcon(R.drawable.senbazuru_ui_icon);
                break;
            case 1:
                getSupportActionBar().setTitle(R.string.favorites);
                getSupportActionBar().setIcon(R.drawable.rating_important);
                break;
            default:
                getSupportActionBar().setTitle(mTitle);
                if (mIcon != null) {
                    getSupportActionBar().setIcon(mIcon);
                } else {
                    getSupportActionBar().setIcon(null);
                }
                break;
        }

        // Put the good menu
        invalidateOptionsMenu();
    }
}
