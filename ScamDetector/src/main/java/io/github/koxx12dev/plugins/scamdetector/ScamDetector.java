package io.github.koxx12dev.plugins.scamdetector;

// Import several packages such as Aliucord's CommandApi and the Plugin class
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.aliucord.Logger;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.NotificationsAPI;
import com.aliucord.entities.NotificationData;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.discord.api.message.Message;
import com.discord.stores.StoreGatewayConnection;
import com.discord.stores.StoreStream;

import java.lang.reflect.Array;
import java.util.Objects;
import com.lytefast.flexinput.R;
import org.jetbrains.annotations.NotNull;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class ScamDetector extends Plugin {

    private Drawable pluginIcon;
    Logger LOGGER = new Logger("ScamDetector");

    public ScamDetector() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @NonNull
    @NotNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("koxx12", 378587857796726785L) };
        manifest.description = "Scam Detector for Aliucord. Show popups about possible scam messages.";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/koxx12-dev/aliucord-plugins/builds/updater.json";
        return manifest;
    }

    // Called when your plugin is started. This is the place to register command, add patches, etc
    @Override
    public void start(Context context) throws NoSuchMethodException {

        patcher.patch(StoreGatewayConnection.class.getDeclaredMethod("handleDispatch", String.class, Object.class), new PinePrePatchFn(callFrame -> {

            String type = (String) Array.get(callFrame.args,0);
            Object obj = Array.get(callFrame.args,1);

            if (Objects.equals(type, "MESSAGE_CREATE") && obj != null && settings.getBool("showPopups", true)) {
                Message msg = (Message) obj;
                String content = msg.i();
                if (content.contains("free") && content.contains("nitro") && content.contains("http")) {
                    notifScam(msg, context);
                }
            }
        }));

        // Registers a command with the name hello, the description "Say hello to the world" and no options

    }

    @Override
    public void load(Context ctx) {
        pluginIcon = ContextCompat.getDrawable(ctx, R.d.ic_search_grey_24dp);
    }

    // Called when your plugin is stopped
    @Override
    public void stop(Context context) {
        // Unregisters all commands
        //commands.unregisterAll();
        patcher.unpatchAll();
    }

    public void notifScam(Message msg,Context context) {

        StoreStream.Companion companion = StoreStream.Companion;
        NotificationData notif = new NotificationData();

        String discordName = companion.getGuilds().getGuild(msg.m()).getName();

        notif.setBody("Detected a scam message in "+discordName+"\nClick this popup to copy the message");
        notif.setIconUrl("https://raw.githubusercontent.com/koxx12-dev/aliucord-plugins/images/temp_img.png");
        notif.setOnClick(view -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Scam message with message id " + msg.o(), msg.e().i()+"|`"+msg.i()+"`");
            clipboard.setPrimaryClip(clip);
            return null;
        });

        NotificationsAPI.display(notif);
    }
}
