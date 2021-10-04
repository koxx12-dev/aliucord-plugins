package io.github.koxx12dev.plugins.scamdetector;

// Import several packages such as Aliucord's CommandApi and the Plugin class

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.NotificationsAPI;
import com.aliucord.entities.NotificationData;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePrePatchFn;
import com.aliucord.utils.GsonUtils;
import com.aliucord.utils.IOUtils;
import com.discord.api.message.Message;
import com.discord.app.AppActivity;
import com.discord.stores.StoreGatewayConnection;
import com.discord.stores.StoreStream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lytefast.flexinput.R;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Random;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class ScamDetector extends Plugin {

    private final String notifChannelId = "ScamDetectorAliucordNotifChannel";
    public Logger LOGGER = new Logger("ScamDetector");
    private Drawable pluginIcon;
    private Context context;
    private NotificationManagerCompat notificationManager;
    private Boolean isPaused = true;

    public ScamDetector() {
        settingsTab = new SettingsTab(PluginSettings.class).withArgs(settings);
    }

    @Override
    public void start(Context context) throws NoSuchMethodException {

        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotifChannel();
        }

        patcher.patch(AppActivity.class.getDeclaredMethod("onPause"), new PinePrePatchFn(view -> {
            isPaused = true;
        }));

        patcher.patch(AppActivity.class.getDeclaredMethod("onResume"), new PinePrePatchFn(view -> {
            isPaused = false;
        }));


        patcher.patch(StoreGatewayConnection.class.getDeclaredMethod("handleDispatch", String.class, Object.class), new PinePrePatchFn(callFrame -> {

            String type = (String) Array.get(callFrame.args, 0);
            Object obj = Array.get(callFrame.args, 1);

            if (Objects.equals(type, "MESSAGE_CREATE") && obj != null) {
                Message msg = (Message) obj;
                String content = msg.i().toLowerCase();
                if (!Objects.equals(msg.e().e(),true) && content.contains("free") && content.contains("nitro") && content.contains("http") && content.contains("discord") && !content.matches("[0-9]+\\|`(?s:.)*`(\\|[0-9.]+)?") && msg.e().i() != StoreStream.getUsers().getMe().getId()) {
                    if (settings.getBool("showPopups", true)) {
                        notifScamInApp(msg);
                    }
                    if (settings.getBool("showAndroidNotifWhenPaused", true) && isPaused) {
                        notifScamWithAndroid(msg);
                    }
                    if (settings.getString("webhook", null) != null) {
                        try {
                            sendWebhookMessage(msg);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }));

    }

    @Override
    public void load(Context context) {
        pluginIcon = ContextCompat.getDrawable(context, R.d.ic_search_grey_24dp);
    }

    @Override
    public void stop(Context context) {

        patcher.unpatchAll();

    }

    public void notifScamInApp(Message msg) {

        NotificationData notif = new NotificationData();

        String discordName;

        if (msg.m() != null) {
            discordName = StoreStream.getGuilds().getGuild(msg.m()).getName();
        } else {
            discordName = msg.e().r() + " dms";
        }

        //Message(id=891985148994789398, channelId=891982759457857547, author=User(id=378587857796726785, username=Koxx12, avatar=Value(value=a0ed1524376ec6bb742b1837cbd4c928), banner=null, discriminator=8061, publicFlags=64, flags=null, bot=null, system=null, token=null, email=null, verified=null, locale=null, nsfwAllowed=null, mfaEnabled=null, phone=null, analyticsToken=null, premiumType=null, approximateGuildCount=null, member=null, bio=null, bannerColor=null), content=freenitrohttp, timestamp=UtcDateTime(dateTimeMillis=1632736231000), editedTimestamp=null, tts=false, mentionEveryone=false, mentions=[], mentionRoles=[], attachments=[], embeds=[], reactions=null, nonce=898155929109790720, pinned=false, webhookId=null, type=0, activity=null, application=null, applicationId=null, messageReference=null, flags=0, stickers=null, stickerItems=null, referencedMessage=null, interaction=null, thread=null, components=[], call=null, guildId=null, member=null, hit=null)

        notif.setBody("Detected a scam message in " + discordName + "\nClick this popup to copy the message");
        notif.setIconUrl("https://raw.githubusercontent.com/koxx12-dev/aliucord-plugins/images/temp_img.png");
        notif.setOnClick(view -> {
            Utils.setClipboard("Scam message with message id " + msg.o(), msg.e().i() + "|`" + msg.i() + "`");
            return null;
        });

        NotificationsAPI.display(notif);

    }

    public void notifScamWithAndroid(Message msg) {

        //Intent intent = new Intent(context, NotifClickEvent.class);

        //intent.putExtra("Text",msg.e().i()+"|`"+msg.i()+"`");
        //intent.putExtra("Label","Scam message with message id " + msg.o());

        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Random rand = new Random();
        int notifId = rand.nextInt(999999999) + 1;

        String discordName;
        if (msg.m() != null) {
            discordName = StoreStream.getGuilds().getGuild(msg.m()).getName();
        } else {
            discordName = msg.e().r() + " dms";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notifChannelId)
                .setSmallIcon(R.d.ic_search_grey_24dp)
                .setContentTitle("Scam Found")
                .setContentText("Detected a scam message in " + discordName)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        notificationManager.notify(notifId, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotifChannel() {
        CharSequence name = "ScamDetector Aliucord";
        String description = "Notification Channel used by ScamDetector plugin on Aliucord";
        int importance = NotificationManager.IMPORTANCE_MAX;

        NotificationChannel channel = new NotificationChannel(notifChannelId, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void sendWebhookMessage(Message msg) throws IOException, JSONException {

        String content = msg.i();

        String body = generateEmbed(msg);

        Http.Response req = new Http.Request(settings.getString("webhook", null), "POST").setHeader("Content-Type", "application/json").executeWithBody(body);

        LOGGER.debug(req.statusCode+"|"+req.statusMessage+"|"+content);

        if (req.statusCode == 400) {
            LOGGER.debug(body);
        }

        //"{\"content\": null,\"embeds\": [{\"title\": \"Possible scam message detected!\",\"color\": 9645823,\"fields\": [{\"name\": \"User\",\"value\": \"`authorUserID`\"},{\"name\": \"Message\",\"value\": \"`content`\"}],\"footer\": {\"text\": \"Sent using ScamDetector by koxx12\" }}],\"username\": \"Scam Detected Webhook (User: currUser)\"}"



    }

    public String generateEmbed(Message msg) throws JSONException {

        String currUser = StoreStream.getUsers().getMe().getUsername();
        long authorUserID = msg.e().i();
        String content = msg.i();

        JSONObject json = new JSONObject();

        json.put("content",null);
        json.put("username","Scam Detected Webhook (User: "+currUser+")");

        JSONArray embeds = new JSONArray();

        JSONObject embed1 = new JSONObject();

        embed1.put("title","Possible scam message detected!");
        embed1.put("color",9645823);

        JSONObject footer = new JSONObject();

        footer.put("text","Sent using ScamDetector by koxx12");

        embed1.put("footer",footer);

        JSONArray fields = new JSONArray();
        JSONObject field1 = new JSONObject();
        JSONObject field2 = new JSONObject();

        field1.put("name","User");
        field1.put("value","`"+authorUserID+"`");

        field2.put("name","Message");
        field2.put("value","`"+content+"`");

        fields.put(field1);
        fields.put(field2);

        embed1.put("fields",fields);

        embeds.put(embed1);

        json.put("embeds",embeds);

        return json.toString();
    }

}
