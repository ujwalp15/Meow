package ujwal.android.com.meow;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.joaquimley.faboptions.FabOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int PROFILE_SETTING = 100000;

    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer result = null;

    private IProfile profile;

    private FabOptions mFabOptions;

    private static final int REQUEST_CODE_CHOOSE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFabOptions = (FabOptions) findViewById(R.id.fab_options);
        mFabOptions.setButtonsMenu(R.menu.menu_fab);
        mFabOptions.setBackgroundColor(R.color.colorPrimary);
        mFabOptions.setOnClickListener(this);

        new DrawerBuilder().withActivity(this).build();

        // Initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }


        });

        // Create a few sample profile
        profile = new ProfileDrawerItem().withName("Meow").withEmail("ujwalp09@gmail.com").withIcon(getResources().getDrawable(R.drawable.cat));

        // Create the AccountHeader
        buildHeader(false, savedInstanceState);

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withIdentifier(1),
                        //here we use a customPrimaryDrawerItem we defined in our sample app
                        //this custom DrawerItem extends the PrimaryDrawerItem so it just overwrites some methods
                        new PrimaryDrawerItem().withName(R.string.drawer_item_breeds).withDescription("Supported cat breeds").withIcon(CommunityMaterial.Icon.cmd_cat).withSelectable(false).withIdentifier(2),
                        new SectionDrawerItem().withName(R.string.drawer_item_section_header),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_about).withIcon(GoogleMaterial.Icon.gmd_info_outline).withSelectable(false).withIdentifier(3),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withSelectedIconColor(Color.GRAY).withIconTintingEnabled(true).withIcon(new IconicsDrawable(this, CommunityMaterial.Icon.cmd_account).actionBar().paddingDp(5).colorRes(R.color.material_drawer_dark_primary_text)).withEnabled(false).withIdentifier(4),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(CommunityMaterial.Icon.cmd_help_circle_outline).withEnabled(false).withIdentifier(5),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_feedback).withIcon(FontAwesome.Icon.faw_paper_plane).withEnabled(false).withIdentifier(6)
                ) // add the items we want to use with our Drawer
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        //this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
                        //if the back arrow is shown. close the activity
                        MainActivity.this.finish();
                        //return true if we have consumed the event
                        return true;
                    }
                })
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_github).withEnabled(false).withIdentifier(7),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_license).withIcon(GoogleMaterial.Icon.gmd_class).withSelectable(false).withIdentifier(8),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_cog).withEnabled(false).withIdentifier(9)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            Intent intent = null;
                            if(drawerItem.getIdentifier()==2) {
                                intent = new Intent(MainActivity.this,BreedsActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(MainActivity.this, AboutActivity.class);
                            } else if (drawerItem.getIdentifier() == 8) {
                                intent = new Intent(MainActivity.this, LicenseActivity.class);
                            }
                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

    }

    private void buildHeader(boolean compact, Bundle savedInstanceState) {
        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_cat)
                .withCompactStyle(compact)
                .addProfiles(
                        profile,
                        //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                        new ProfileSettingDrawerItem()
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        //sample usage of the onProfileChanged listener
                        //if the clicked item has the identifier 1 add a new profile ;)
                        if (profile instanceof IDrawerItem && ((IDrawerItem) profile).getIdentifier() == PROFILE_SETTING) {
                            IProfile newProfile = new ProfileDrawerItem().withNameShown(true).withName("Batman").withEmail("batman@gmail.com").withIcon(getResources().getDrawable(R.drawable.cat));
                            if (headerResult.getProfiles() != null) {
                                //we know that there are 2 setting elements. set the new profile above them ;)
                                headerResult.addProfile(newProfile, headerResult.getProfiles().size() - 2);
                            } else {
                                headerResult.addProfiles(newProfile);
                            }
                        }

                        //false if you have not consumed the event and it should close the drawer
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        mFabOptions.setButtonColor(R.id.fab_options_camera, R.color.md_white_1000);
        mFabOptions.setButtonColor(R.id.fab_options_gallery, R.color.md_white_1000);
        mFabOptions.setButtonColor(R.id.fab_options_favourite, R.color.md_white_1000);
        mFabOptions.setButtonColor(R.id.fab_options_share, R.color.md_white_1000);
        switch (view.getId()) {
            case R.id.fab_options_camera:
                mFabOptions.setButtonColor(R.id.fab_options_camera, R.color.colorAccent);
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                MainActivity.this.startActivity(intent);
                break;

            case R.id.fab_options_gallery:
                mFabOptions.setButtonColor(R.id.fab_options_gallery, R.color.colorAccent);
                Matisse.from(MainActivity.this)
                        .choose(MimeType.allOf())
                        .theme(R.style.Matisse_Dracula)
                        .countable(true)
                        .maxSelectable(1)
                        .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.85f)
                        .imageEngine(new PicassoEngine())
                        .forResult(REQUEST_CODE_CHOOSE);
                break;

            case R.id.fab_options_favourite:
                mFabOptions.setButtonColor(R.id.fab_options_favourite, R.color.colorAccent);
                Toast.makeText(MainActivity.this, "Favourite", Toast.LENGTH_SHORT).show();
                break;


            case R.id.fab_options_share:
                mFabOptions.setButtonColor(R.id.fab_options_share, R.color.colorAccent);
                Toast.makeText(MainActivity.this, "Share", Toast.LENGTH_SHORT).show();
                break;

            default:
                // no-op
        }
    }

    List<Uri> mSelected;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            String path = getURIPath(mSelected.get(0));
            Toast.makeText(MainActivity.this, "mSelected: " + path, Toast.LENGTH_SHORT).show();
        }
    }

    String getURIPath(Uri uriValue)
    {
        String[] mediaStoreProjection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uriValue, mediaStoreProjection, null, null, null);
        if (cursor != null){
            int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String colIndexString=cursor.getString(colIndex);
            cursor.close();
            return colIndexString;
        }
        return null;
    }
}
