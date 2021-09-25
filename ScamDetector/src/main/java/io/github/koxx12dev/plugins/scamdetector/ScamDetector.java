package io.github.koxx12dev.plugins.scamdetector;

// Import several packages such as Aliucord's CommandApi and the Plugin class
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.aliucord.annotations.AliucordPlugin;
import com.discord.R;
import com.aliucord.entities.Plugin;
import com.discord.utilities.fcm.NotificationRenderer;

import java.util.Collections;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class ScamDetector extends Plugin {
    NotificationManagerCompat notificationManager;

    // Called when your plugin is started. This is the place to register command, add patches, etc
    @Override
    public void start(Context context) {
        // Registers a command with the name hello, the description "Say hello to the world" and no options

        notificationManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"notificationChannel")
                .setSmallIcon(2131231850)
                .setContentTitle("title")
                .setContentText("text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(123345678,builder.build());

    }

    // Called when your plugin is stopped
    @Override
    public void stop(Context context) {
        // Unregisters all commands
        commands.unregisterAll();
    }
}
