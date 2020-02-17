package com.sukelven.linetv;

import android.os.AsyncTask;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Collected public parameters/functions for some Activities to get data.
 *
 * @author Kelvin Su
 */
@SuppressWarnings("deprecation")
public class MyLib implements TypeDefine {
	public static int iResponseStatusCode;

	static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		try{
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		}catch(Exception e){e.printStackTrace();}
	}

	public synchronized static String GetHttpsUrlResponse(String uri){
		try {
			iResponseStatusCode = 0;

			HttpsURLConnection connection = (HttpsURLConnection) new URL(uri).openConnection();
			connection.setConnectTimeout(SocketTimeout);
			connection.setReadTimeout(SocketTimeout);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			connection.setRequestProperty("Accept", "*/*");
			connection.setDoInput(true);
			iResponseStatusCode = connection.getResponseCode();
			if(iResponseStatusCode == SC_OK){
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				StringBuilder response = new StringBuilder();
				while ((line = reader.readLine()) != null)
					response.append(line + "\n");
				reader.close();
				return response.toString();
			}else{
				return connection.getResponseCode()+"\n"+connection.getResponseMessage();
			}
		}catch(RuntimeException e){
		}catch(SocketTimeoutException e){iResponseStatusCode=SC_TIMEOUT;
		}catch(ConnectTimeoutException e){iResponseStatusCode=SC_TIMEOUT;
		}catch(Exception e){}
		return null;
	}

    public synchronized static byte[] GetHttpUrlBitmap(String uri){
        try {
            iResponseStatusCode = 0;
            HttpURLConnection connection = (HttpURLConnection) new URL(uri).openConnection();
            connection.setConnectTimeout(SocketTimeout);
            connection.setReadTimeout(SocketTimeout);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setRequestProperty("Accept", "*/*");
            //connection.setRequestProperty("Authorization", "Basic "+auth);
            connection.setDoInput(true);
            iResponseStatusCode = connection.getResponseCode();
            if(iResponseStatusCode == SC_OK){
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return buffer.toByteArray();
            }
        }catch(RuntimeException e){e.printStackTrace();
        }catch(SocketTimeoutException e){iResponseStatusCode=SC_TIMEOUT;e.printStackTrace();
        }catch(ConnectTimeoutException e){iResponseStatusCode=SC_TIMEOUT;e.printStackTrace();
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
}
