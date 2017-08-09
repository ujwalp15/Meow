package ujwal.android.com.meow;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ResultActivity extends AppCompatActivity {

    ImageView image;
    TextView t1,t2;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
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

        dialog = ProgressDialog.show(ResultActivity.this, "", "Uploading file...", true);
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
        String upLoadServerUri = "http://192.168.43.169/upload_media_test.php";
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
                        Toast.makeText(ResultActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ResultActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (final Exception e) {
            dialog.dismiss();
            e.printStackTrace();
            this.runOnUiThread(new Runnable() {
                public void run() {
                Toast.makeText(ResultActivity.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Upload file failed", "Exception : " + e.getMessage(), e);
        }
        dialog.dismiss();
        return serverResponseCode;
    }

    public void getData(){
        class GetDataJSON extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httppost = new HttpPost("http://192.168.43.169/experiments/run.php");

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
            Picasso.with(this).load(new File(imagePath)).into(image);
            String could_be = cat_breed.get(0) +" "+ cat_value.get(0);
            String could_be_2 = cat_breed.get(1) +" "+ cat_value.get(1);

            t1.setText(could_be);
            t2.setText(could_be_2);
            } catch (JSONException e) {
            e.printStackTrace();
        }

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
