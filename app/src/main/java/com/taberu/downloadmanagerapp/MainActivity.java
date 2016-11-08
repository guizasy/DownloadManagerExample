package com.taberu.downloadmanagerapp;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    private long myDownloadId;
    private long dl_progress;
    private int mProgressStatus;
    private boolean mDownloading;
    private TextView textView;
    private TextView textView1;
    private Cursor mCursor;
    private Handler mHandler = new Handler();
    private View mainView;

    private IntentFilter dci = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    private BroadcastReceiver dcr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id == myDownloadId) {
                textView.setText("Finalizado ***");
                Log.i(TAG, "Download finalizado: " + myDownloadId);
            } else {
                Log.i(TAG, "Download nao relacionado" + id);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) findViewById(R.id.status);
        textView1 = (TextView) findViewById(R.id.progress_bar);

        textView.setText("Status");
        textView1.setText("Download: 0%");

//        this.registerReceiver(dcr, dci);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.unregisterReceiver(dcr);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.registerReceiver(dcr, dci);
    }

//    @Override
//    public void onPause()
//    {
//        super.onPause();
//        this.unregisterReceiver(dcr);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);

        int id = item.getItemId();

        if (id == R.id.menu_download) {
//            Intent intent = new Intent(MainActivity.this, HttpExampleActivity.class);
//            startActivity(intent);
            mainView = this.findViewById(android.R.id.content);
            try {
                myClickHandler(mainView);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        if (id == R.id.menu_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isNetworkAvailable() {
        boolean retVal;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            retVal = true;
        } else {
            retVal = false;
        }

        return retVal;
    }

//    public void myClickHandler(View view) {
//        DownloadManager dm;
//
//        if (isNetworkAvailable()) {
//            textView.setText("Iniciado +++");
//            Uri uri = Uri.parse("http://robocupssl.cpe.ku.ac.th/_media/rules:ssl-rules-2015.pdf");
//            DownloadManager.Request request = new DownloadManager.Request(uri);
//
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
//
//            request.setTitle("Exemplo download");
//            request.setDescription("Download somente no wi-fi");
//
//            dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//            myDownloadId = dm.enqueue(request);
//
//            Log.i(TAG, "Pedido de download realizado: " + myDownloadId);
//        }
//    }

    public void myClickHandler(View view) throws IOException {
        final DownloadManager dm;
        if (isNetworkAvailable()) {
            textView.setText("Iniciado +++");

            Uri uri = Uri.parse("https://www.acm.org/education/CS2013-final-report.pdf");
            DownloadManager.Request request = new DownloadManager.Request(uri);

            // personalizando notificacao
            request.setTitle("Seu Download");
            request.setDescription("Download em progresso");
            dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            myDownloadId = dm.enqueue(request);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mDownloading = true;
                    while (mDownloading) {
                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(myDownloadId);
                        mCursor = dm.query(q);
                        if (mCursor != null && mCursor.getCount() > 0) {
                            mCursor.moveToFirst();
                            int bytes_downloaded = mCursor.getInt(mCursor
                                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            int bytes_total = mCursor.getInt(mCursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                            if (mCursor.getInt(mCursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                mDownloading = false;
                            }
                            dl_progress = ((bytes_downloaded * 100) / bytes_total);
                            Log.d("PROGRESS",Double.toString(dl_progress));
                        }
                        if (mCursor != null) {
                            mCursor.close();
                        }
                    }
                }
            }).start();

            new Thread(new Runnable() {
                public void run() {
                    while (mProgressStatus < 100) {
                        mProgressStatus = (int) dl_progress;
                        // Update the progress bar
                        mHandler.post(new Runnable() {
                            public void run() {
//                                mProgress.setProgress(mProgressStatus);
                                textView1.setText("Download: " + (Integer.toString(mProgressStatus)+"%"));
                            }
                        });
                    }
                }
            }).start();

            Log.i("Download", "Pedido de download realizado: " + myDownloadId);
        }
    }
}
