package ujwal.android.com.meow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.heinrichreimersoftware.androidissuereporter.IssueReporterLauncher;
import com.joaquimley.faboptions.FabOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
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

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int PROFILE_SETTING = 100000;

    //save our header or result
    private Drawer result = null;

    private IProfile profile;

    private FabOptions mFabOptions;

    private static final int REQUEST_CODE_CHOOSE = 23;

    private static final int CAMERA_RQ = 6969;
    private static final int PERMISSION_RQ = 84;

    List<Uri> mSelected;

    private boolean camera = true;
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission to save videos in external storage
            ActivityCompat.requestPermissions(
                    this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_RQ);
        }

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
        profile = new ProfileDrawerItem().withName("Meow").withEmail("Recognize Cat Breed").withIcon(getResources().getDrawable(R.drawable.cat));

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.header) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withIdentifier(1),
                        //here we use a customPrimaryDrawerItem we defined in our sample app
                        //this custom DrawerItem extends the PrimaryDrawerItem so it just overwrites some methods
                        new PrimaryDrawerItem().withName(R.string.drawer_item_breeds).withDescription("Supported cat breeds").withIcon(CommunityMaterial.Icon.cmd_cat).withSelectable(false).withIdentifier(2),
                        new SectionDrawerItem().withName(R.string.drawer_item_section_header),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_about).withIcon(GoogleMaterial.Icon.gmd_info_outline).withSelectable(false).withIdentifier(3),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(CommunityMaterial.Icon.cmd_book_open_page_variant).withSelectable(false).withIdentifier(4),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(CommunityMaterial.Icon.cmd_help_circle_outline).withSelectable(false).withIdentifier(5),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_feedback).withIcon(FontAwesome.Icon.faw_paper_plane).withSelectable(false).withIdentifier(6)
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
                            if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(MainActivity.this, BreedsActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(MainActivity.this, AboutActivity.class);
                            } else if (drawerItem.getIdentifier() == 4) {
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ujwalp15/Meow-ReadMe"));
                                startActivity(intent);
                            } else if (drawerItem.getIdentifier() == 5) {
                                IntroActivity.prefManager.setFirstTimeLaunch(true);
                                intent = new Intent(MainActivity.this, IntroActivity.class);
                                startActivity(intent);
                            } else if (drawerItem.getIdentifier() == 6) {
                                IssueReporterLauncher.forTarget("ujwalp15", "Meow-ReadMe")
                                        // [Recommended] Theme to use for the reporter.
                                        // (See #theming for further information.)
                                        .theme(R.style.Theme_App_Dark)
                                        // [Optional] Auth token to open issues if users don't have a GitHub account
                                        // You can register a bot account on GitHub and copy ist OAuth2 token here.
                                        // (See #how-to-create-a-bot-key for further information.)
                                        .guestToken("28f479f73db97d912611b27579aad7a76ad2baf5")
                                        // [Optional] Force users to enter an email address when the report is sent using
                                        // the guest token.
                                        .guestEmailRequired(true)
                                        // [Optional] Set a minimum character limit for the description to filter out
                                        // empty reports.
                                        .minDescriptionLength(20)
                                        // [Optional] Include other relevant info in the bug report (like custom variables)
                                        .putExtraInfo("Test 1", "Example string")
                                        .putExtraInfo("Test 2", true)
                                        // [Optional] Disable back arrow in toolbar
                                        .homeAsUpEnabled(true)
                                        .launch(MainActivity.this);
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

    /*@Override
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
    }*/

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
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
                camera=true;
                mFabOptions.setButtonColor(R.id.fab_options_camera, R.color.colorAccent);
                captureImage();
                break;

            case R.id.fab_options_gallery:
                camera=false;
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
                final MaterialStyledDialog.Builder dialogHeader = new MaterialStyledDialog.Builder(this)
                        .setIcon(new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_google_play).color(Color.WHITE))
                        .withDialogAnimation(true)
                        .setTitle("Awesome!")
                        .setDescription("Glad to see you like Meow! If you're up for it, I would really appreciate you reviewing it.")
                        .setHeaderColor(R.color.primary)
                        .setPositiveText("Google Play")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Toasty.info(MainActivity.this,"App will be available soon on play store",Toast.LENGTH_SHORT,true).show();
                            }
                        })
                        .setNegativeText("Later");
                dialogHeader.show();

                break;


            case R.id.fab_options_share:
                mFabOptions.setButtonColor(R.id.fab_options_share, R.color.colorAccent);
                Toasty.info(MainActivity.this, "Share", Toast.LENGTH_SHORT,true).show();
                break;

            default:
                // no-op
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

    private void captureImage() {
        File saveDir = null;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Only use external storage directory if permission is granted, otherwise cache directory is used by default
            saveDir = new File(Environment.getExternalStorageDirectory(), "Meow");
            saveDir.mkdirs();
        }

        MaterialCamera materialCamera =
                new MaterialCamera(this)
                        .saveDir(saveDir)
                        .showPortraitWarning(true)
                        .allowRetry(true)
                        .defaultToFrontFacing(false)
                        .allowRetry(true)
                        .autoSubmit(false);

        materialCamera
                .stillShot() // launches the Camera in still shot mode
                .labelConfirm(R.string.camera_option_use_picture);
        materialCamera.start(CAMERA_RQ);
    }

    private String readableFileSize(long size) {
        if (size <= 0) return size + " B";
        final String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups))
                + " "
                + units[digitGroups];
    }

    private String fileSize(File file) {
        return readableFileSize(file.length());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(camera) {
            // Received recording or error from MaterialCamera
            if (requestCode == CAMERA_RQ) {
                if (resultCode == RESULT_OK) {
                    final File file = new File(data.getData().getPath());
                    Toast.makeText(
                            this,
                            String.format("Saved to: %s, size: %s", file.getAbsolutePath(), fileSize(file)),
                            Toast.LENGTH_LONG)
                            .show();
                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    intent.putExtra("IMAGE_PATH", file.getAbsolutePath());
                    MainActivity.this.startActivity(intent);
                } else if (data != null) {
                    Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                    if (e != null) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        MainActivity.this.startActivity(intent);
                    }
                }
            }
        } else {
            if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
                mSelected = Matisse.obtainResult(data);
                String path = getURIPath(mSelected.get(0));
                Toasty.Config.getInstance()
                        .setInfoColor(getResources().getColor(R.color.fourth_slide_background)).apply();
                Toasty.info(MainActivity.this, "mSelected: " + path, Toast.LENGTH_SHORT,true).show();
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("IMAGE_PATH", path);
                MainActivity.this.startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // Sample was denied WRITE_EXTERNAL_STORAGE permission
            Toasty.Config.getInstance()
                    .setInfoColor(getResources().getColor(R.color.fourth_slide_background)).apply();
            Toasty.info(
                    this,
                    "Photos will be saved in a cache directory instead of an external storage directory since permission was denied.",
                    Toast.LENGTH_LONG,
                    true)
                    .show();
        }
    }
}
