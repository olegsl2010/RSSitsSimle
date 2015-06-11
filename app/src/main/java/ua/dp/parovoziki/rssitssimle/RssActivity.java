package ua.dp.parovoziki.rssitssimle;

import android.app.Activity;
import android.os.Bundle;

public class RssActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acrivity_main);

        if (savedInstanceState == null) {
            RssListFragment trendsFragment = new RssListFragment();
            getFragmentManager().beginTransaction().add(R.id.cont, trendsFragment).commit();
        }
    }

}