package ujwal.android.com.meow;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.view.View;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

public class IntroActivity extends MaterialIntroActivity{
    private static final int PERMISSION_RQ = 84;
    public static PrefManager prefManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.primary_dark)
                        .buttonsColor(R.color.primary)
                        .image(R.drawable.cat)
                        .title("Meow")
                        .description("Recognize breed of cats by just clicking its picture!")
                        .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.primary_dark)
                .buttonsColor(R.color.primary)
                .image(R.drawable.cat2)
                .title("Take Picture")
                .description("Click on the option fab and get started!")
                .build());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.primary_dark)
                        .buttonsColor(R.color.primary)
                        .possiblePermissions(new String[]{Manifest.permission.INTERNET})
                        .neededPermissions(new String[]{Manifest.permission.CAMERA})
                        .image(R.drawable.camera)
                        .title("Camera")
                        .description("To take a picture, I need permission to connect to the camera!")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("Permission Granted");
                    }
                }, "Grant Permission"));

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.primary_dark)
                .buttonsColor(R.color.primary)
                .image(R.drawable.process)
                .neededPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .title("Processing")
                .description("Once picture is taken, let us process the image and predict the breed")
                .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("Permission Granted");
                    }
                }, "Grant Permission"));

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.primary_dark)
                .buttonsColor(R.color.primary)
                .image(R.drawable.cat3)
                .title("Thank you")
                .description("Thank you for installing Meow!")
                .build());
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onFinish() {
        super.onFinish();
        launchHomeScreen();
    }
}
