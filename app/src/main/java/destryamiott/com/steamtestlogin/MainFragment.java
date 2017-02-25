package destryamiott.com.steamtestlogin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class MainFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private TextView profileName;
    private ImageView profileImage;
    private String tempID;
    private String steamBit;
    private String imageUrl;




    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        new Prefs.Builder()
                .setContext(this.getActivity())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getActivity().getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);


        if(Prefs.contains("steamid")){

            tempID = (Prefs.getString("steamid", ""));
            long tempInt = Long.valueOf(tempID);

            long steamConversion = (tempInt - (76561197960265728L + (tempInt % 2))) / 2;

            steamBit = "STEAM_0:" + (tempInt % 2 ) + ":" + steamConversion;

            new PostDataTask().execute("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=XXXXXXXXXXXXXXXXXXXXXXXX&steamids=" + tempID); //This app will not build without your STEAM API KEY. Grab it from https://steamcommunity.com/dev/apikey

            profileName = (TextView) v.findViewById(R.id.profileName);
            profileImage = (ImageView) v.findViewById(R.id.profileImage);

            return v;


        }
        else{

            profileName = (TextView) v.findViewById(R.id.profileName);
            profileImage = (ImageView) v.findViewById(R.id.profileImage);

            return v;

        }

    }


    class PostDataTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params){

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null){
                    buffer.append(line + '\n');
                    Log.d("Response: ", "< " + line);
                }

                return buffer.toString();
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null){
                        reader.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            return null;
        }



        @Override
        protected void onPostExecute(String result) {


            try {


                Log.e("Result: ", result);

                JSONObject jsonObject = new JSONObject(result);

                JSONObject response = jsonObject.getJSONObject("response");
                JSONArray jsonArray = response.getJSONArray("players");

                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject profile = jsonArray.getJSONObject(i);
                    String steamName = profile.getString("personaname");
                    profileName.setText(steamName);

                    imageUrl = profile.getString("avatarfull");

                    new DownloadImage().execute(imageUrl);
                }

            } catch (JSONException e){
                Log.e("JSON EXCEPTION ", e.toString());
            }

            super.onPostExecute(result);


            if (progressDialog != null){
                progressDialog.dismiss();
            }

        }



        private void setImage(Drawable drawable){
            profileImage.setImageDrawable(drawable);
        }



        public class DownloadImage extends AsyncTask<String, Integer, Drawable> {

            @Override
            protected Drawable doInBackground(String... arg0) {
                return downloadImage(arg0[0]);
            }

            protected void onPostExecute(Drawable image){
                setImage(image);
            }


            private Drawable downloadImage(String _url){

                URL url;
                BufferedOutputStream out;
                InputStream in;
                BufferedInputStream bufferIn;

                try {
                    url = new URL(_url);
                    in = url.openStream();

                    bufferIn = new BufferedInputStream(in);

                    Bitmap bitmap = BitmapFactory.decodeStream(bufferIn);

                    if(in != null){
                        in.close();
                    }
                    if(bufferIn != null){
                        bufferIn.close();
                    }

                    return new BitmapDrawable(bitmap);
                } catch (Exception e){
                    Log.e("Bitmap Error: ", e.toString());
                }

                return null;
            }


        }

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
