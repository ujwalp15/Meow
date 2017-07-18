package ujwal.android.com.meow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.danielstone.materialaboutlibrary.model.MaterialAboutList;

public class LicenseActivity extends AboutActivity {

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull final Context c) {
        return Demo.createMaterialAboutLicenseList(c, colorIcon);
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.mal_title_licenses);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }
}
