package io.github.koxx12dev.plugins.scamdetector;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aliucord.Logger;
import com.aliucord.Utils;

public class NotifClickEvent extends AppCompatActivity {

   // Logger LOGGER = new Logger("ScamDetectorNotif");

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("start");
        Utils.setClipboard("start","start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.setClipboard("stop","stop");
        System.out.println("stop");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        Utils.setClipboard(extras.get("Label")+"",extras.get("Text")+"");
        System.out.println("create");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.setClipboard("destory","destory");
        System.out.println("destory");
    }
}
