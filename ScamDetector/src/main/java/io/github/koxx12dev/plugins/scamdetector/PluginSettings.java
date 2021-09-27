package io.github.koxx12dev.plugins.scamdetector;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import com.aliucord.Constants;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Divider;
import com.aliucord.views.TextInput;
import com.aliucord.widgets.LinearLayout;
import com.discord.views.CheckedSetting;
import org.jetbrains.annotations.NotNull;
import com.lytefast.flexinput.R;

import java.util.Arrays;

public class PluginSettings extends SettingsPage {

    private static final String plugin = "ScamDetector";
    private final SettingsAPI mSettings;

    public PluginSettings(SettingsAPI settings) {
        mSettings = settings;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        setActionBarTitle(plugin);
        setPadding(0);

        int padding = Utils.getDefaultPadding();

        var context = view.getContext();
        var layout = getLinearLayout();

        var appearanceHeader = new TextView(context, null, 0, R.h.UiKit_Settings_Item_Header);
        appearanceHeader.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
        appearanceHeader.setText("Popups");
        layout.addView(appearanceHeader);

        var showPopups = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show popups", "Shows popups about scam messages.");
        showPopups.setChecked(mSettings.getBool("showPopups", true));
        showPopups.setOnCheckedListener(c -> {
            mSettings.setBool("showPopups", c);
            reloadPlugin();
        });

        layout.addView(new Divider(context));
        layout.addView(showPopups);

        var showAndroidNotifWhenPaused = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show android notifications when in BG", "Shows android notifications when aliucord app is in background\n(Clicking on them does nothing)");
        showAndroidNotifWhenPaused.setChecked(mSettings.getBool("showAndroidNotifWhenPaused", true));
        showAndroidNotifWhenPaused.setOnCheckedListener(c -> {
            mSettings.setBool("showAndroidNotifWhenPaused", c);
            reloadPlugin();
        });

        layout.addView(new Divider(context));
        layout.addView(showAndroidNotifWhenPaused);

        TextInput webhook = new TextInput(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(padding, padding, padding, 0);
        webhook.setLayoutParams(params);
        webhook.setHint("Send scam messages to webhook");

        layout.addView(new Divider(context));
        layout.addView(webhook);

        EditText editText = webhook.getEditText();
        if (editText != null) {
            editText.setText(mSettings.getString("webhook", null));
            editText.setMaxLines(1);
            editText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (s.toString().matches("^\\s*$")) {
                        mSettings.setString("webhook", null);
                    } else {
                        mSettings.setString("webhook", s.toString());
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
            });
        }

    }

    public void reloadPlugin() {
        PluginManager.stopPlugin(plugin);
        PluginManager.startPlugin(plugin);
    }
}
