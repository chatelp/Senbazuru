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

package pm.chatel.senbazuru.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import pm.chatel.senbazuru.Constants;
import pm.chatel.senbazuru.R;
import pm.chatel.senbazuru.fragment.EntryFragment;
import pm.chatel.senbazuru.utils.PrefUtils;
import pm.chatel.senbazuru.utils.UiUtils;


public class EntryActivity extends BaseActivity {

    private EntryFragment mEntryFragment;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry);

        mEntryFragment = (EntryFragment) getFragmentManager().findFragmentById(R.id.entry_fragment);
        if (savedInstanceState == null) { // Put the data only the first time (the fragment will save its state)
            mEntryFragment.setData(getIntent().getData());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // Simulator.
                    .addTestDevice("BE07C00BC1E98AD5F33E968B783B6A6A") // Nexus 5
                    .addTestDevice("BCFD0EFF1162167A639FBD3BB621DB8A") // Nexus 7
                    .build();
        mAdView.loadAd(request);

        if (PrefUtils.getBoolean(PrefUtils.DISPLAY_ENTRIES_FULLSCREEN, false)) {
            setImmersiveFullScreen(true);
        }
    }

    public void showAdView(boolean showView) {
        if(mAdView != null) {
            mAdView.setVisibility(showView?AdView.VISIBLE:AdView.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Bundle b = getIntent().getExtras();
            if (b != null && b.getBoolean(Constants.INTENT_FROM_WIDGET, false)) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            }
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mEntryFragment.setData(intent.getData());
    }
}