package Pokemon;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;



public class HTTPRequest {

    public JSONObject requestGetMethod(String stringUrl) {

        URL url;

        try {
            url = new URL(stringUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            String response = IOUtils.toString(
                    connection.getInputStream(),
                    "utf-8");

            JSONObject jsonObject = new JSONObject(response);


            return jsonObject;
            

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        } catch (JSONException e) {
        	e.printStackTrace();
        }
      return null;
    }
}

