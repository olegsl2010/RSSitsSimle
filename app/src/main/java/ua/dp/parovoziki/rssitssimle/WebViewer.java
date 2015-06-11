package ua.dp.parovoziki.rssitssimle;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by apple on 09.06.15.
 */
public class WebViewer extends Fragment {

    private static final String TAG = "AAWebViewer";
    private String currentURL;
    private String currentTitle;
    ProgressDialog progress;
    TextView textTitle;
    ImageView imgBut;
    public void init(String url) {
        currentURL = url;
    }
    public void title(String title) {
        currentTitle = title;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.webview, container, false);
        if (currentURL != null) {

            progress = ProgressDialog.show(getActivity(), getString(R.string.dialog_name),getString(R.string.wait_result), true);

            getContent(v);

        }
        imgBut=(ImageView)v.findViewById(R.id.imageButton);
        imgBut.setImageResource(R.drawable.close);
        imgBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().onBackPressed();

            }
        });

        textTitle= (TextView)v.findViewById(R.id.textViewTitle);
        textTitle.setText(currentTitle);

        return v;
    }


    private void getContent(View v) {

        try {
            WebView wv = (WebView) v.findViewById(R.id.webPage);
            wv.getSettings().setJavaScriptEnabled(true);
            wv.setWebViewClient(new SwAWebClient());
            wv.loadUrl(currentURL);
            wv.setWebViewClient(new WebViewClient() {

                public void onPageFinished(WebView view, String url) {
                    progress.dismiss();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }


    private class SwAWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return false;
        }

    }

}
