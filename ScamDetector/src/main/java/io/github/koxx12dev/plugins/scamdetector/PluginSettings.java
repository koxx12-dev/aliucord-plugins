package io.github.koxx12dev.plugins.scamdetector;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    }

    public void reloadPlugin() {
        PluginManager.stopPlugin(plugin);
        PluginManager.startPlugin(plugin);
    }
}
