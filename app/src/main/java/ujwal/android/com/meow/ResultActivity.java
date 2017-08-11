package ujwal.android.com.meow;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class ResultActivity extends AppCompatActivity {

    ImageView image;
    TextView t1,t2;
    CardView c1,c2,c3;
    int serverResponseCode = 0;
    MaterialDialog dialog = null;
    String imagePath;

    String myJSON;

    private static final String TAG_ID = "id";
    private static final String TAG_VALUE = "value";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setHomeButtonEnabled(true);
        }

        imagePath = getIntent().getStringExtra("IMAGE_PATH");
        image = findViewById(R.id.imageView);
        t1 = findViewById(R.id.prediction1);
        t2 = findViewById(R.id.prediction2);
        c1 = findViewById(R.id.cardView);
        c2 = findViewById(R.id.cardView1);
        c3 = findViewById(R.id.cardView2);

        c1.setVisibility(View.INVISIBLE);
        c2.setVisibility(View.INVISIBLE);
        c3.setVisibility(View.INVISIBLE);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this).title(R.string.progress_dialog_title_upload).content(R.string.progress_dialog_desc).progress(true, 0);
        dialog = builder.build();
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("uploadFile", "Uploading");
                    }
                });
                int response= uploadFile(imagePath);
                System.out.println("RES : " + response);
            }
        }).start();
    }

    public int uploadFile(String sourceFileUri) {
        String upLoadServerUri = "http://ujwalp15.pagekite.me/upload_media_test.php";
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            Log.d("uploadFile", "Source File Does not exist");
            return 0;
        }
        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            if(serverResponseCode == 200){
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toasty.success(ResultActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT,true).show();
                    }
                });
                getData();
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            dialog.dismiss();
            ex.printStackTrace();
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toasty.error(ResultActivity.this, "MalformedURLException", Toast.LENGTH_SHORT,true).show();
                }
            });
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (final Exception e) {
            dialog.dismiss();
            e.printStackTrace();
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toasty.error(ResultActivity.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT,true).show();
                }
            });
            Log.e("Upload file failed", "Exception : " + e.getMessage(), e);
        }
        dialog.dismiss();
        final MaterialDialog.Builder builder1 = new MaterialDialog.Builder(this).title(R.string.progress_dialog_title_fetch).content(R.string.progress_dialog_desc).progress(true, 0);
        this.runOnUiThread(new Runnable() {
            public void run() {
                dialog = builder1.build();
                dialog.show();
            }
        });

        return serverResponseCode;
    }

    public void getData(){
        class GetDataJSON extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httppost = new HttpPost("http://ujwalp15.pagekite.me/experiments/run.php");

            // Depends on your web service
            httppost.setHeader("Content-type", "application/json");
            InputStream inputStream = null;
            String result = null;
            try {
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();
            } catch (Exception e) {
                // Oops
            }
            finally {
                try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result){
        myJSON=result;
        showList();
        }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    protected void showList(){
        ArrayList<String> cat_breed = new ArrayList<String>();
        ArrayList<String> cat_value = new ArrayList<String>();
        try {
            JSONArray jsonArr = new JSONArray(myJSON);

            for(int i=0;i<jsonArr.length();i++){
                JSONObject c = jsonArr.getJSONObject(i);
                String id = c.getString(TAG_ID);
                String value = c.getString(TAG_VALUE);

                cat_breed.add(id);
                cat_value.add(value);
            }
            final String could_be = cat_breed.get(0) +" "+ cat_value.get(0);
            final String could_be_2 = cat_breed.get(1) +" "+ cat_value.get(1);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Picasso.with(ResultActivity.this).load(new File(imagePath)).into(image);
                    t1.setText(could_be);
                    t2.setText(could_be_2);
                    dialog.dismiss();
                    showResult();
                }
            }, 3000);

            } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void showResult() {
        c1.postDelayed(new Runnable() {
            @Override
            public void run() {
                revealEffect(c1);
            }
        },300);

        c2.postDelayed(new Runnable() {
            @Override
            public void run() {
                revealEffect(c2);
            }
        },600);

        c3.postDelayed(new Runnable() {
            @Override
            public void run() {
                revealEffect(c3);
            }
        },900);
    }

    void revealEffect(View v) {
        int cx = v.getMeasuredWidth()/2;
        int cy = v.getMeasuredHeight()/2;
        int finalRadius = Math.max(v.getWidth(),v.getHeight());
        Animator a = ViewAnimationUtils.createCircularReveal(v,cx,cy,0,finalRadius);
        a.setDuration(1000);
        v.setVisibility(View.VISIBLE);
        a.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
