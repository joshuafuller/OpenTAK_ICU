package io.opentakserver.opentakicu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.appintro.AppIntro;
import com.github.appintro.SlidePolicy;
import com.github.appintro.model.SliderPagerBuilder;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

public class OnBoardingActivity extends AppIntro {
    private static final String TAG = "OnBoardingActivity";
    public static final String ENABLE_BACK_BUTTON = "back_enabled";
    private static final String LOGTAG = "OnBoardingActivity";
    private static final String WELCOME_SLIDE = "welcome_slide";
    private static final String PERMISSIONS_SLIDE = "permissions_slide";
    private static final String BACKGROUND_LOCATION_SLIDE = "background_location_slide";
    private static final String STORAGE_PERMISSION_SLIDE = "storage_permission_slide";
    private static final String OUI_SLIDE = "oui_slide";
    private static final String ROOT_SLIDE = "root_slide";
    private static final String SENTINEL_SERVER_SLIDE = "sentinel_server_slide";
    private static final String FINALIZE_SLIDE = "finalize_slide";
    private static Handler UIHandler = new Handler(Looper.getMainLooper());

    public static class CustomLayout extends Fragment implements SlidePolicy {
        private static LinearLayout username_layout;
        private static LinearLayout password_layout;
        private static TextView username_label;
        private static TextView password_label;
        private static HashMap<CharSequence, EditText> usernameEditText = new HashMap<>();
        private static HashMap<CharSequence, EditText> passwordEditText = new HashMap<>();
        private static Button button;
        private static TextView description;
        private static HashMap<CharSequence, TextView> ouiStatus = new HashMap<>();
        private static HashMap<CharSequence, ProgressBar> progressBar = new HashMap<>();

        @Override
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstance) {
            View view = layoutInflater.inflate(R.layout.appintro_with_button, container, false);
            if(view == null) {
                return null;
            }

            Bundle args = getArguments();
            CharSequence title = args.getCharSequence("title");
            int image = args.getInt("image");

            DisplayMetrics metrics = getResources().getDisplayMetrics();

            TextView titleTextView = view.findViewById(R.id.title);
            titleTextView.setText(title);

            // fixes links from Utils.getText not clickable
            TextView tv = view.findViewById(R.id.description);
            tv.setAutoLinkMask(0);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setText(args.getCharSequence("pd_descr"));
            tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

            // Disable auto-sizing, as it makes the text not readable
            TextViewCompat.setAutoSizeTextTypeWithDefaults(tv, TextView.AUTO_SIZE_TEXT_TYPE_NONE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            // Fix excessive vertical padding, causing scroll
            ViewGroup.LayoutParams lp = tv.getLayoutParams();
            if(lp instanceof ConstraintLayout.LayoutParams) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) lp;
                params.setMargins(0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics), 0, 0);
                tv.setPadding(tv.getPaddingLeft(), 0, tv.getPaddingRight(), 0);
                tv.setLayoutParams(params);
            }

            // fix drawable tint and size
            ImageView imageView = view.findViewById(R.id.image);
            imageView.setImageResource(image);
            int tint = args.getInt("pd_image_tint");
            if(tint > 0)
                imageView.setColorFilter(ContextCompat.getColor(view.getContext(), tint));
            if(args.getBoolean("pd_image_autosz")) {
                imageView.setAdjustViewBounds(true);
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220, metrics);
            }

            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstance) {
            super.onViewCreated(view, savedInstance);

            Bundle args = getArguments();
            CharSequence id = args.getCharSequence("slide_id");
            Log.d(LOGTAG, "onViewCreated " + id);
            username_layout = view.findViewById(R.id.username_linear_layout);
            password_layout = view.findViewById(R.id.password_linear_layout);
            username_label = view.findViewById(R.id.username_label);
            password_label = view.findViewById(R.id.password_label);
            usernameEditText.put(id, view.findViewById(R.id.username));
            passwordEditText.put(id, view.findViewById(R.id.password));
            button = view.findViewById(R.id.button);
            description = view.findViewById(R.id.description);
            ouiStatus.put(id, view.findViewById(R.id.oui_status));
            progressBar.put(id, view.findViewById(R.id.progress_bar));

            if (id.equals(WELCOME_SLIDE) || id.equals(PERMISSIONS_SLIDE) || id.equals(BACKGROUND_LOCATION_SLIDE)) {
                username_layout.setVisibility(View.GONE);
                password_layout.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                ouiStatus.get(id).setVisibility(View.GONE);
                progressBar.get(id).setVisibility(View.GONE);
            }
            else if (id.equals(SENTINEL_SERVER_SLIDE)) {
                progressBar.get(id).setVisibility(View.GONE);
                button.setText(R.string.save);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
            else if (id.equals(FINALIZE_SLIDE)) {
                username_layout.setVisibility(View.GONE);
                password_layout.setVisibility(View.GONE);
                ouiStatus.get(id).setVisibility(View.VISIBLE);
                progressBar.get(id).setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
                button.setText("removeme");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        }

        @Override
        public boolean isPolicyRespected() {
            return true;
        }

        @Override
        public void onUserIllegallyRequestedNextPage() {

        }

        public static CustomLayout createInstance(CharSequence title, CharSequence description, int imageRes, boolean imageAutosize, String id) {
            CustomLayout fragment = new CustomLayout();
            Bundle args = new SliderPagerBuilder()
                    .title(title)
                    //.description(description) see below
                    .imageDrawable(imageRes)
                    .backgroundColorRes(R.color.colorBackground)
                    .titleColorRes(R.color.textColor)
                    .descriptionColorRes(R.color.textColor)
                    .build().toBundle();

            args.putCharSequence("pd_descr", description);
            args.putBoolean("pd_image_autosz", imageAutosize);
            args.putCharSequence("slide_id", id);
            args.putCharSequence("title", title);
            args.putInt("image", imageRes);
            fragment.setArguments(args);

            return fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean backEnabled = false;

        Intent intent = getIntent();
        if(intent != null)
            backEnabled = intent.getBooleanExtra(ENABLE_BACK_BUTTON, false);

        addSlide(CustomLayout.createInstance(getString(R.string.welcome_to_open_tak_icu),
                getText(R.string.app_intro_welcome_msg),
                R.mipmap.ic_launcher, true, WELCOME_SLIDE));

        addSlide(CustomLayout.createInstance(getString(R.string.permissions),
                getText(R.string.permissions_slide_description),
                R.mipmap.ic_launcher, true, PERMISSIONS_SLIDE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            addSlide(CustomLayout.createInstance(getString(R.string.background_location_permission),
                    getText(R.string.background_location_permissions_slide_description),
                    R.mipmap.ic_launcher, true, BACKGROUND_LOCATION_SLIDE));

            String[] background_location_permission = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};
            askForPermissions(background_location_permission, 3, true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            /*addSlide(CustomLayout.createInstance(getString(R.string.storage_permission),
                    getString(R.string.storage_permission_description),
                    R.mipmap.ic_launcher, true, STORAGE_PERMISSION_SLIDE));*/
        }

        addSlide(CustomLayout.createInstance(getString(R.string.tak_server), getText(R.string.server_info),
                R.mipmap.ic_launcher, true, SENTINEL_SERVER_SLIDE));

        addSlide(CustomLayout.createInstance(getString(R.string.finished),
                getText(R.string.finished_onboarding),
                R.mipmap.ic_launcher, true, FINALIZE_SLIDE));

        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(android.Manifest.permission.RECORD_AUDIO);
        permissions.add(android.Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        askForPermissions(permissions.toArray(new String[0]), 2, true);

        showStatusBar(true);
        setSkipButtonEnabled(false);
        setIndicatorEnabled(true);
        setSystemBackButtonLocked(false);

        // Theme
        int colorAccent = ContextCompat.getColor(this, R.color.colorAccent);
        setIndicatorColor(colorAccent, ContextCompat.getColor(this, R.color.colorAccent));
        setBackArrowColor(colorAccent);
        setColorSkipButton(colorAccent);
        setNextArrowColor(colorAccent);
        setBackArrowColor(colorAccent);
        setColorDoneText(colorAccent);
    }

    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        runMainActivity();
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        runMainActivity();
    }

    private void runMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void error(String message) {
        Log.e(LOGTAG, message);
    }
}
