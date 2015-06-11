package ua.dp.parovoziki.rssitssimle;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class TestFragment extends Fragment {

    private static final String TAG = "AATestFragment";
    private static final String TEST_URL = "http://habrahabr.ru/rss/hubs/";
    private static final String ACTION_FOR_INTENT_CALLBACK = "THIS_IS_A_UNIQUE_KEY_WE_USE_TO_COMMUNICATE";
    ImageView imageLogo;
    ProgressDialog progress;
    ListView ourListView;
    Button updateButton;
    private String url;
    private String title;
    private ArrayList<PostData> listData;
    DBHelper dbHelper;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_test, container, false);

        ourListView = (ListView) v.findViewById(R.id.listView);

        imageLogo=(ImageView)v.findViewById(R.id.imageLogo);
        imageLogo.setImageResource(R.drawable.habrlogo);
        dbHelper = new DBHelper(v.getContext());

        getListContent();

        toWebView();

        updateButton=(Button)v.findViewById(R.id.btnUpdateRss);
        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getListContent();
            }
        });

        return v;
    }

    private void getListContent() {
        if (isNetworkConnected()){
            getContent();
        }else
            toastNoInternetConn();
        fromDataBase();
    }

    private void getContent() {
        // the request
        try {
            HttpGet httpGet = new HttpGet(new URI(TEST_URL));
            RestTask task = new RestTask(getActivity(), ACTION_FOR_INTENT_CALLBACK);
            task.execute(httpGet);
            progress = ProgressDialog.show(getActivity(), getString(R.string.dialog_name),getString(R.string.wait_result), true);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter(ACTION_FOR_INTENT_CALLBACK));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }


    /**
     * Our Broadcast Receiver. We get notified that the data is ready this way.
     */
    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String date;
            String putDate;
            PostData data;

            // clear the progress indicator
            if (progress != null) {
                progress.dismiss();
            }
            String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                DocumentBuilder db = dbf.newDocumentBuilder();

                Document document = db.parse(new InputSource(new StringReader(response)));
                Element element = document.getDocumentElement();
                final NodeList nodeList = element.getElementsByTagName("item");
                if (nodeList.getLength() > 0) {
                    listData = new ArrayList<>();
                    SimpleDateFormat dateFormatCheck = new SimpleDateFormat("dd");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    SimpleDateFormat dateAfternoon = new SimpleDateFormat("MMMM dd, HH:mm");
                    Calendar c = Calendar.getInstance();
                    String currentDate = dateFormatCheck.format(c.getTime());


                    for (int i = 0; i < nodeList.getLength(); i++) {


                        Element entry = (Element) nodeList.item(i);
                        Element titleE = (Element) entry.getElementsByTagName(
                                "title").item(0);
                        Element pubDateE = (Element) entry
                                .getElementsByTagName("pubDate").item(0);
                        Element linkE = (Element) entry.getElementsByTagName(
                                "link").item(0);

                        String _title = titleE.getFirstChild().getNodeValue();
                        Date _pubDate = new Date(pubDateE.getFirstChild().getNodeValue());
                        date = dateFormatCheck.format(_pubDate);

                        if (Integer.valueOf(date) == Integer.valueOf(currentDate)) {

                            putDate = "Сегодня в " + String.valueOf(dateFormat.format(_pubDate));
                        } else if (Integer.valueOf(currentDate) - Integer.valueOf(date) > 1) {

                            putDate = String.valueOf(dateAfternoon.format(_pubDate));
                        } else {
                            putDate = "Вчера в " + String.valueOf(dateFormat.format(_pubDate));

                        }
                        String _link = linkE.getFirstChild().getNodeValue();

                        data = new PostData();
                        data.setPostDate(putDate);
                        data.setPostTitle(_title);
                        data.setPostThumbUrl(_link);
                        listData.add(i, data);
                    }

                    toDateBase();

                    toRssList();

                }

            }catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }

    };


    private void toDateBase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("rssTable", null, null);
        ContentValues cv = new ContentValues();
        for (int i = 0; i < listData.size(); i++) {
            cv.put("date", listData.get(i).getPostDate());
            cv.put("title", listData.get(i).getPostTitle());
            cv.put("link", listData.get(i).getPostThumbUrl());
            db.insert("rssTable", null, cv);
        }
    }
    private void fromDataBase(){

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        listData = new ArrayList<>();
        Cursor c = db.query("rssTable", null, null, null, null, null, null);
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int dateColIndex = c.getColumnIndex("date");
            int titleColIndex = c.getColumnIndex("title");
            int linkColIndex = c.getColumnIndex("link");

            do {
                PostData data = new PostData();
                data.setPostDate(c.getString(dateColIndex));
                data.setPostTitle(c.getString(titleColIndex));
                data.setPostThumbUrl(c.getString(linkColIndex));

                listData.add(data);

                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        } else
            Log.d(TAG, "0 rows");
        c.close();

        toRssList();
    }

    private void toWebView() {
        ourListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                                    long id) {
                if (isNetworkConnected()) {
                    url = listData.get(position).getPostThumbUrl();
                    title = listData.get(position).getPostTitle();
                    WebViewer webviews = new WebViewer();
                    webviews.init(url);
                    webviews.title(title);
                    getFragmentManager().beginTransaction().add(android.R.id.content, webviews).addToBackStack("web").commit();

                } else {
                    toastNoInternetConn();
                }

            }
        });
    }

    private void toastNoInternetConn() {
        Toast.makeText(getActivity(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
    }

    private void toRssList() {
        PostItemAdapter itemAdapter = new PostItemAdapter(getActivity(), R.layout.postitem, listData);

        ourListView.setAdapter(itemAdapter);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {

            return false;
        } else
            return true;
    }


}

