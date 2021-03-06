package com.connection.stopbus.stopbus_user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by Danbk on 2018-04-05.
 */

public class ActivitySearchFav extends AppCompatActivity {


    private SwipeRefreshLayout swipeContainer0;
    private SwipeRefreshLayout swipeContainer1;
    private RecyclerView recyclerView;
    private RecyclerView recyclerView2;

    private EditText SearchText;

    Handler mHandler = new Handler();

    private RecycleAdapter SearchBusListAdapter = new RecycleAdapter(this);
    private RecycleAdapter2 SearchStationListAdapter = new RecycleAdapter2(this);

    private List<ApiData.Station> StationList = new ArrayList<ApiData.Station>();
    private List<ApiData.Route> RouteList = new ArrayList<ApiData.Route>();

    ArrayList<String> favouriteList;


        @Override
        protected void onCreate(final Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_search_fav);

            findViewById(R.id.back).setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            Log.d("sb", "back button pressed");
                            finish();

                        }
                    }
            );

            SearchText = (EditText) findViewById(R.id.search);

            SearchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    SearchStationListAdapter.clear();
                    SearchBusListAdapter.clear();
                    CallData("search?type=route");
                    CallData("search?type=station");

                }
            });

            SearchText.setOnKeyListener(new EditText.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //Enter key Action
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(SearchText.getWindowToken(), 0);
                        SearchText.clearFocus();
                        return true;
                    }
                    return false;
                }
            });


            initUI();
        }

    private void initUI() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                View view = null;

                if (position == 0) {  //작업 이력 레이아웃
                    view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.list_search_bus, null, false);
                    recyclerView = (RecyclerView) view.findViewById(R.id.rv_search_bus_list);
                    swipeContainer0 = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout0);
                    // Setup refresh listener which triggers new data loading
                    swipeContainer0.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            // Your code to refresh the list here.
                            // Make sure you call swipeContainer.setRefreshing(false)
                            // once the network request has completed successfully.
                            CallData("search?type=route");

                            swipeContainer0.setRefreshing(false);

                        }
                    });
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(
                                    getBaseContext(), LinearLayoutManager.VERTICAL, false
                            )
                    );
                    recyclerView.setAdapter(SearchBusListAdapter);
                    CallData("search?type=route");

                } else if (position == 1) {


                    view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.list_search_station, null, false);
                    recyclerView2 = (RecyclerView) view.findViewById(R.id.rv_search_station_list);
                    swipeContainer1 = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout0);
                    // Setup refresh listener which triggers new data loading
                    swipeContainer1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            // Your code to refresh the list here.
                            // Make sure you call swipeContainer.setRefreshing(false)
                            // once the network request has completed successfully.
                            CallData("search?type=station");
                            swipeContainer1.setRefreshing(false);

                        }
                    });
                    recyclerView2.setHasFixedSize(true);
                    recyclerView2.setLayoutManager(new LinearLayoutManager(
                                    getBaseContext(), LinearLayoutManager.VERTICAL, false
                            )
                    );
                    recyclerView2.setAdapter(SearchStationListAdapter);
                    CallData("search?type=station");
                }

                container.addView(view);
                return view;

            }
        });

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_directions_bus_white_48pt),
                        Color.parseColor(colors[4]))
                        .title("버스")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.bus_stop),
                        Color.parseColor(colors[4]))
                        .title("정류장")
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setTitleMode(NavigationTabBar.TitleMode.ACTIVE);

        //IMPORTANT: ENABLE SCROLL BEHAVIOUR IN COORDINATOR LAYOUT
        navigationTabBar.setBehaviorEnabled(false);

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(final NavigationTabBar.Model model, final int index) {
            }

            @Override
            public void onEndTabSelected(final NavigationTabBar.Model model, final int index) {
                model.hideBadge();
            }
        });

        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {


                if(position==0){
                    SearchText.setInputType(InputType.TYPE_CLASS_DATETIME);
                }else{
                    SearchText.setInputType(InputType.TYPE_CLASS_TEXT);
                    SearchText.setPrivateImeOptions("defaultInputmode=korean");
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });


    }

    //검색 불러오는 API
    public synchronized void CallData(final String api) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> args = new HashMap<String, String>();
                args.put("keyword",  SearchText.getText().toString()); //POST

                try {

                    final String response = NetworkService.INSTANCE.postQuery(api, args);
                    Log.d("sb","333333"+response);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (api.equals("search?type=route")) {
                                try {
                                    JSONArray jarray = new JSONObject(response).getJSONArray("body");   // JSONArray 생성

                                        ApiData.Route[] arr = new Gson().fromJson(jarray.toString(), ApiData.Route[].class);
                                        RouteList = new ArrayList<ApiData.Route>(Arrays.asList(arr));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                SearchBusListAdapter.notifyDataSetChanged();

                            } else if (api.equals("search?type=station")) {
                                try {
                                    JSONArray jarray = new JSONObject(response).getJSONArray("body");   // JSONArray 생성


                                        Log.d("sb", "jarray: "+ jarray);
                                        ApiData.Station[] arr2 = new Gson().fromJson(jarray.toString(), ApiData.Station[].class);
                                        StationList = new ArrayList<ApiData.Station>(Arrays.asList(arr2));


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                SearchStationListAdapter.notifyDataSetChanged();
                            }

                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해주세요 ", Toast.LENGTH_LONG).show();
                }
            }
        }).start();

    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

        Context mContext;

        public RecycleAdapter(Context context) {
            this.mContext = context;

        }

        public void clear() {
            final int size = RouteList.size();
            RouteList.clear();
            notifyItemRangeRemoved(0, size);
        }

        @Override
        public RecycleAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_search_bus, parent, false);
            return new RecycleAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecycleAdapter.ViewHolder holder, final int position) {


            final TinyDB tinydb = new TinyDB(ActivitySearchFav.this);
            favouriteList= tinydb.getListString("Favourite");
            Log.d("sb", "favouriteList: " + favouriteList);
            Iterator<String> itr = favouriteList.iterator();

            while(itr.hasNext()){
                String id = itr.next();
                // Log.d("sb", "items : "+ id);
                if(id.equals(RouteList.get(position).routeID)){
                    holder.favourite_btn.setImageResource(R.drawable.ic_star_yellow_36dp);
                    break;
                }else{
                    holder.favourite_btn.setImageResource(R.drawable.ic_star_black_36dp);
                }
            }

            holder.bus_num.setText(RouteList.get(position).routeNumber);
            holder.bus_type.setText(RouteList.get(position).routeTypeName);

            holder.favourite_btn.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            favouriteList= tinydb.getListString("Favourite");
                            Iterator<String> itr = favouriteList.iterator();
                            int flag =0;
                            while(itr.hasNext()){
                                String id = itr.next();
                                Log.d("sb", "Integer.toString(RouteList.get(position).routeID :"+ RouteList.get(position).routeID);
                                if(id.equals(RouteList.get(position).routeID)){
                                    Log.d("sb", "delete");
                                    flag =1;
                                    break;
                                }else{
                                    Log.d("sb", "add");
                                    flag=0;
                                }
                            }
                            if(flag==0){
                                holder.favourite_btn.setImageResource(R.drawable.ic_star_yellow_36dp);
                                favouriteList.add(RouteList.get(position).routeID);
                                tinydb.putListString("Favourite",favouriteList );
                            }else if(flag==1){
                                holder.favourite_btn.setImageResource(R.drawable.ic_star_black_36dp);
                                favouriteList.remove(RouteList.get(position).routeID);
                                tinydb.putListString("Favourite",favouriteList );
                            }

                        }
                    }
            );

            holder.item_fav_bus.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {

                            Shared_Pref.routeID = RouteList.get(position).routeID;
                            Shared_Pref.bt_bus_flag =0;

                            Log.d("sb", "bus route list gogo");
                            Intent i = new Intent(ActivitySearchFav.this, ActivityBus.class);

                            startActivity(i);

                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return RouteList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView bus_num;
            public TextView bus_type;
            public RelativeLayout item_fav_bus;
            public ImageView favourite_btn;

            public ViewHolder(final View itemView) {
                super(itemView);

                bus_num = (TextView) itemView.findViewById(R.id.bus_num);
                bus_type = (TextView) itemView.findViewById(R.id.bus_type);
                item_fav_bus = (RelativeLayout)itemView.findViewById(R.id.item_fav_bus);
                favourite_btn = (ImageView)itemView.findViewById(R.id.favourite_btn);

            }
        }

    }



    public class RecycleAdapter2 extends RecyclerView.Adapter<RecycleAdapter2.ViewHolder> {

        Context mContext;

        public RecycleAdapter2(Context context) {
            this.mContext = context;

        }

        public void clear() {
            final int size = StationList.size();
            StationList.clear();
            notifyItemRangeRemoved(0, size);
        }

        @Override
        public RecycleAdapter2.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_search_station, parent, false);
            return new RecycleAdapter2.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecycleAdapter2.ViewHolder holder, final int position) {


            holder.station_num.setText(StationList.get(position).stationNumber.trim());
            holder.station_name.setText(StationList.get(position).stationName);
            if(StationList.get(position).stationDirect.equals("")){
                holder.station_way.setVisibility(View.GONE);

            }else{
                holder.station_way.setVisibility(View.VISIBLE);
                holder.station_way.setText("| "+ StationList.get(position).stationDirect + " 방면");
            }

            holder.map.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {


                            GoogleMapDialog dialog = GoogleMapDialog.newInstance(StationList.get(position).stationName, new LatLng(StationList.get(position).longitude,StationList.get(position).latitude));
                            dialog.show(getSupportFragmentManager(), "dialog");


                        }
                    }
            );

            holder.item_search_station.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {

                            Shared_Pref.stationNumber = StationList.get(position).stationNumber.trim();
                            Shared_Pref.stationName = StationList.get(position).stationName;
                            Shared_Pref.stationDirect = StationList.get(position).stationDirect;
                            Shared_Pref.stationID = StationList.get(position).stationID;
                            Shared_Pref.bt_station_flag =0;

                            Log.d("sb", "station's bus list gogo");
                            Intent i = new Intent(ActivitySearchFav.this, ActivityStation.class);

                            startActivity(i);

                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return StationList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView station_name;
            public TextView station_num;
            public TextView station_way;
            public RelativeLayout item_search_station;
            public ImageView map;


            public ViewHolder(final View itemView) {
                super(itemView);

                station_name = (TextView) itemView.findViewById(R.id.station_name);
                station_num = (TextView) itemView.findViewById(R.id.station_num);
                station_way = (TextView) itemView.findViewById(R.id.station_way);
                item_search_station = (RelativeLayout) itemView.findViewById(R.id.item_search_station);
                map = (ImageView)itemView.findViewById(R.id.map);
            }
        }

    }
}
