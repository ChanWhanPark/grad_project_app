package com.chananpark.grad_project;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chananpark.grad_project.Model.Point;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static String TAG = "MainActivity";
    // 네이버 지도 변수
    private MapView mapView;
    private static NaverMap naverMap;

    String NAVER_CLIENT_ID = "c02o8ej4vv";
    String NAVER_CLIENT_SECRET_ID = "awHlsFYKM3J1QHz2nTtkOCvysI6rBYHgIjnNXGrT";

    // 현 위치
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    // 마커 변수 선언 및 초기화
    private Marker marker_Gate = new Marker();
    private Marker marker_TIP = new Marker();

    // 네이버 경로 저장 변수
    private List<LatLng> mPathList = new ArrayList<>();

    // 목적지 & 현위치 좌표 변수
    private double goal_x;
    private double goal_y;
    private Point currentPoint;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 버튼
        Button btnMark_G = (Button) findViewById(R.id.btnmark_G);
        Button btnMark_T = (Button) findViewById(R.id.btnmark_T);


        btnMark_G.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setMarker(marker_Gate, 37.3393, 126.7327, R.drawable.ic_twotone_room_24, 0);

                goal_x = 37.3393;
                goal_y = 126.7327;

                marker_Gate.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay)
                    {
                        new Thread(){
                            public void run(){
                                try {
                                    HttpConnection(goal_x, goal_y);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        Toast.makeText(getApplication(), "현위치 입니다", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });

        btnMark_T.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setMarker(marker_TIP, 37.3414, 126.7319, R.drawable.ic_baseline_pin_drop_1, 0);

                marker_TIP.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay)
                    {
                        Toast.makeText(getApplication(), "TIP으로 안내합니다", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });



        // 네이버 지도
        mapView = (MapView)findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 현 위치 받아오기
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

    }
    // 마커 설정
    private void setMarker(Marker marker,  double lat, double lng, int resourceID, int zIndex)
    {
        //원근감 표시
        marker.setIconPerspectiveEnabled(true);
        //아이콘 지정
        marker.setIcon(OverlayImage.fromResource(resourceID));
        //마커의 투명도
        marker.setAlpha(0.8f);
        //마커 위치
        marker.setPosition(new LatLng(lat, lng));
        //마커 우선순위
        marker.setZIndex(zIndex);
        //마커 표시
        marker.setMap(naverMap);
    }

    // 위치정보 권한 설정
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        // 배경 지도 선택
        naverMap.setMapType(NaverMap.MapType.Navi);
        // 건물 표시
        naverMap.setLayerGroupEnabled(naverMap.LAYER_GROUP_BUILDING, true);
        //위치 및 각도 조정
        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(37.3392, 126.7335),   // 위치 지정
                16,                                     // 줌 레벨
                60,                                       // 기울임 각도
                0                                     // 방향
        );
        naverMap.setCameraPosition(cameraPosition);
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                currentPoint = new Point();
                currentPoint.x = location.getLatitude();
                currentPoint.y = location.getLongitude();
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        });
    }

    
    
    // APP 동작을 위한 부분
    @Override
    public void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    // ================ Direction 5 API ==================
    protected void HttpConnection(double x, double y) throws IOException, CloneNotSupportedException, JSONException
    {
        String result = null;
        String mURL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving" +
                "?start=" + this.currentPoint.y + "," + this.currentPoint.x +
                "&goal=" + goal_y + "," + goal_x;
        URL url = new URL(mURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID);
        conn.setRequestProperty("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET_ID);
        InputStream is = conn.getInputStream();

        System.out.println("RESPONSE CODE : " + conn.getResponseMessage());

        // Get the stream
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        // Set the result
        conn.disconnect();
        result = builder.toString();
        Log.d(TAG, result.toString());

        // save in json
        JSONObject root = new JSONObject(result);
        JSONObject route = root.getJSONObject("route");
        JSONObject traoptimal = (JSONObject) route.getJSONArray("traoptimal").get(0);
        JSONObject summary = traoptimal.getJSONObject("summary");
        JSONArray path = traoptimal.getJSONArray("path");

        int naverDistance = summary.getInt("distance");
        String naverDeparutreTime = summary.getString("deparetureTIme");
        int naverTollFare = summary.getInt("tollFare");
        int naverTaxiFare = summary.getInt("taxiFare");
        int naverFuelPrice  = summary.getInt("fuelPrice");

        for (int i=0;i<path.length();i++){
            JSONArray pathIndex = (JSONArray) path.get(i);
            mPathList.add(new LatLng(pathIndex.getDouble(1), pathIndex.getDouble(0)));
        }
        Log.d(TAG, mPathList.toString());
    }
}