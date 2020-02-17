package com.sukelven.linetv;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements SearchView.OnQueryTextListener {
    private static final String DATA_JSON = "https://static.linetv.tw/interview/dramas-sample.json";

    private static String searchKeyWord = "";
    private LinearLayout llDetailFrame, llListContent, llSearchBar;
    private TextView tvAppTitle, tvLoading;
    private ImageView ivSearch, ivBack;
    private SearchView svSearchView;

    private List<DramaObj> listDramasObj, listFilterDramasObj;
    private View[] myItemView, myFilterItemView;

    String mOriginalJson;
    private boolean SearchFlag = false;

    private static final String TAG = "KTV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        llDetailFrame = findViewById(R.id.llDetailFrame);
        llListContent = findViewById(R.id.llListContent);
        llSearchBar = findViewById(R.id.llSearchBar);
        tvAppTitle = findViewById(R.id.tvAppTitle);
        ivSearch = findViewById(R.id.ivSearch);
        ivSearch.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {clickSearchBtn();}});
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {clickBackBtn();}});
        tvLoading = findViewById(R.id.tvLoading);

        svSearchView = findViewById(R.id.svSearchView);
        svSearchView.setOnQueryTextListener(this);

        MyLib.executeAsyncTask(new GetDramasSampleTask(), "");
    }

    private void clickSearchBtn(){
        SearchFlag = !SearchFlag;
        showSearchLayout();
        updateFilterDramas();
    }

    private void clickBackBtn(){
        llDetailFrame.setVisibility(View.GONE);
    }

    private void showSearchLayout(){
        tvAppTitle.setVisibility(SearchFlag?View.GONE:View.VISIBLE);
        llSearchBar.setVisibility(SearchFlag?View.VISIBLE:View.GONE);
        ivSearch.setImageResource(SearchFlag?R.mipmap.ic_search_off:R.mipmap.ic_search);
        if(SearchFlag)
            svSearchView.setQuery(searchKeyWord, false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchKeyWord = newText.toLowerCase();
        updateFilterDramas();
        return false;
    }

    private void updateFilterDramas(){
        llListContent.removeAllViews();
        myFilterItemView = new View[listDramasObj.size()];
        int ListIndex = 0;
        listFilterDramasObj = new ArrayList<>();
        DramaObj mDramaObj;
        for(int i=0; i<listDramasObj.size(); i++){
            mDramaObj = listDramasObj.get(i);
            if(!SearchFlag || searchKeyWord==null || searchKeyWord.equals("") || mDramaObj.name.toLowerCase().contains(searchKeyWord)){
                addContentListView(true, ListIndex++, mDramaObj);
            }
        }

        if(listFilterDramasObj.size()<1)
            showNoData();
    }
    private void addContentListView(boolean isFilter, final int index, final DramaObj mDramaObj){
        View itemView = View.inflate(this, R.layout.drama_list_item, null);
        if(isFilter){
            myFilterItemView[index] = itemView;
            listFilterDramasObj.add(mDramaObj);
        }else{
            myItemView[index] = itemView;
            listDramasObj.add(mDramaObj);
        }

        RelativeLayout rlListItem = itemView.findViewById(R.id.rlListItem);
        TextView tvDramaName = itemView.findViewById(R.id.tvDramaName);
        TextView tvDramaRating = itemView.findViewById(R.id.tvDramaRating);
        TextView tvDramaCreatedAt = itemView.findViewById(R.id.tvDramaCreatedAt);
        rlListItem.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {clickListItem(mDramaObj);}});
        tvDramaName.setText(mDramaObj.name);
        tvDramaRating.setText(getRating(mDramaObj.rating));
        tvDramaCreatedAt.setText(getCreatedAt(mDramaObj.created_at));
        if(mDramaObj.bmp!=null){
            ImageView ivPreview = itemView.findViewById(R.id.ivPreview);
            ivPreview.setImageBitmap(mDramaObj.bmp);
        }
        llListContent.addView(itemView);
    }

    private void showNoData(){
        View itemView = View.inflate(this, R.layout.drama_list_item_empty, null);
        llListContent.addView(itemView);
    }

    private void displayResult() throws JSONException {
        JSONObject o = new JSONObject(mOriginalJson);
        JSONArray data = o.getJSONArray("data");

        if(data.length() < 1){
            showOffLine();
            return;
        }

        llListContent.removeAllViews();
        myItemView = new View[data.length()];

        DramaDB db = DramaDB.getInstance(this);
        db.clearDB();

        int ListIndex = 0;
        listDramasObj = new ArrayList<>();
        DramaObj mDramaObj;
        for (int i = 0; i < data.length(); i++) {
            mDramaObj = new DramaObj();
            JSONObject c = data.getJSONObject(i);
            mDramaObj.drama_id = c.getString("drama_id");
            mDramaObj.name = c.getString("name");
            mDramaObj.total_views = c.getString("total_views");
            mDramaObj.created_at = c.getString("created_at");
            mDramaObj.thumb = c.getString("thumb");
            mDramaObj.rating = c.getString("rating");

            db.AddMain(mDramaObj);

            addContentListView(false, ListIndex++, mDramaObj);
        }
        db.close();

        for(int i=0; i<listDramasObj.size(); i++){
            MyLib.executeAsyncTask(new GetPreviewTask(), ""+i, listDramasObj.get(i).thumb);
        }
    }

    private void clickListItem(DramaObj mDramaObj){
        //Toast.makeText(this, "Go to :"+mDramaObj.name, Toast.LENGTH_LONG).show();
        TextView tvDramaDetailName = findViewById(R.id.tvDramaDetailName);
        tvDramaDetailName.setText(mDramaObj.name);
        TextView tvDramaDetailRating = findViewById(R.id.tvDramaDetailRating);
        tvDramaDetailRating.setText(getRating(mDramaObj.rating));
        TextView tvDramaDetailCreatedAt = findViewById(R.id.tvDramaDetailCreatedAt);
        tvDramaDetailCreatedAt.setText(getCreatedAt(mDramaObj.created_at));
        TextView tvDramaDetailTotalViews = findViewById(R.id.tvDramaDetailTotalViews);
        tvDramaDetailTotalViews.setText(getTotalViews(mDramaObj.total_views));
        ImageView ivDetailPreview = findViewById(R.id.ivDetailPreview);
        ivDetailPreview.setImageBitmap(mDramaObj.bmp);
        llDetailFrame.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.panel_wave);
        llDetailFrame.startAnimation(animation);
    }

    private String getRating(String s){
        try{
            return "☆"+String.format("%.1f", Float.parseFloat(s));
        }catch(Exception e){
            return "☆"+s;
        }
    }

    private String getCreatedAt(String s){
        return s.replace("T", " ").replace(".000Z", "");
    }

    private String getTotalViews(String s){
        try{
            int n = Integer.parseInt(s);
            if(n>1000){
                s = String.format("%,d", n/1000)+" k";
            }else{
                s = String.format("%,d", n);
            }
        }catch(Exception e){}
        return s+" "+getString(R.string.views);
    }

    private class GetPreviewTask extends AsyncTask<String, Integer, byte[]> {
        int itemIndex = 0;
        @Override
        protected byte[] doInBackground(String... params) {
            try {
                itemIndex = Integer.parseInt(params[0],10);
                return MyLib.GetHttpUrlBitmap(params[1]);
            }catch(Exception e){}
            return null;
        }
        @Override
        protected void onPostExecute(byte[] byteResult){
            try{
                //Log.i(TAG, "GetPreviewTask["+itemIndex+"] onPostExecute byteResult="+byteResult);
                Bitmap bm = null;
                if(byteResult != null){
                    bm = BitmapFactory.decodeByteArray(byteResult, 0, byteResult.length);
                }
                showContentItemPreview(itemIndex, bm);
            }catch(Exception e){Log.i(TAG, "e="+e.toString());e.printStackTrace();}
        }
    }

    private void savePreviewToDB(String drama_id, Bitmap bm){
        DramaDB db = DramaDB.getInstance(this);
        db.SavePreview(drama_id, bm);
        db.close();
    }

    private void showContentItemPreview(int itemIndex, Bitmap bm){
        try{
            if(bm!=null){
                ImageView ivPreview = myItemView[itemIndex].findViewById(R.id.ivPreview);
                ivPreview.setImageBitmap(bm);
                DramaObj mDramaObj = listDramasObj.get(itemIndex);
                mDramaObj.bmp = bm;
                listDramasObj.set(itemIndex, mDramaObj);
                savePreviewToDB(mDramaObj.drama_id, bm);
            }
        }catch(Exception e){Log.e(TAG, "showContentItemPreview("+itemIndex+") error!");e.printStackTrace();}
    }


    private void showOffLine(){
        Toast.makeText(this, "Loaded from database!!!", Toast.LENGTH_LONG).show();
        DramaDB db = DramaDB.getInstance(this);
        DramaObj[] moo;
        Cursor c = db.query(new String[]{db.M_DRAMA_ID, db.M_NAME, db.M_TOTAL_VIEWS, db.M_CREATED_AT
                , db.M_THUMB, db.M_RATING, db.M_BLOB}, null, null, null,
                null,null, null);
        if (c != null && c.moveToFirst()) {
            moo = new DramaObj[c.getCount()];
            int k = 0;
            while (!c.isAfterLast()) {
                moo[k] = new DramaObj();
                moo[k].drama_id = c.getString(0);
                moo[k].name = c.getString(1);
                moo[k].total_views = c.getString(2);
                moo[k].created_at = c.getString(3);
                moo[k].thumb = c.getString(4);
                moo[k].rating = c.getString(5);
                moo[k].bmp = getBitmap(c.getBlob(6));
                //Log.v(TAG, "k="+k+", id="+moo[k].drama_id+", name="+moo[k].name+", bmp="+moo[k].bmp);
                c.moveToNext();
                k++;
            }
        }else{
            showNoData(); return;
        }
        db.close();

        llListContent.removeAllViews();
        myItemView = new View[moo.length];
        listDramasObj = new ArrayList<>();
        for(int i=0; i<moo.length; i++){
            addContentListView(false, i, moo[i]);
        }
    }

    private Bitmap getBitmap(byte[] vByteArray){
        return BitmapFactory.decodeByteArray(vByteArray,0, vByteArray.length);
    }

    public class GetDramasSampleTask extends AsyncTask<String, Void , String> {
        protected String doInBackground(String... params) {
            return MyLib.GetHttpsUrlResponse(DATA_JSON);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.v(TAG, "onPostExecute result="+result);
            tvLoading.setVisibility(View.GONE);
            if(result == null) {
                showOffLine();
                return;
            }
            try{
                mOriginalJson = result;
                displayResult();
            }
            catch (JSONException e) {
                e.printStackTrace();
                showOffLine();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
