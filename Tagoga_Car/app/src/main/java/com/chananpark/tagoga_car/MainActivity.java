package com.chananpark.tagoga_car;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    // 네이버 지도 변수
    private MapView mapView;
    private static NaverMap naverMap;

    String NAVER_CLIENT_ID = "c02o8ej4vv";
    String NAVER_CLIENT_SECRET_ID = "awHlsFYKM3J1QHz2nTtkOCvysI6rBYHgIjnNXGrT";

    // 현 위치
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private Point currentPoint;
    private String msg_x;
    private String msg_y;

    private Button sendbt;
    private TextView x_location;
    private TextView y_location;

    public String msg;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    List<Object> Array = new ArrayList<Object>();

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference dbref_x = firebaseDatabase.getReference().child("location").child("x");
    private DatabaseReference dbref_y = firebaseDatabase.getReference().child("location").child("y");
    private DatabaseReference dbref_flag = firebaseDatabase.getReference().child("flag");

    @RequiresApi(api= Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);

        sendbt = (Button)findViewById(R.id.button);

        mapView = (MapView)findViewById(R.id.map_view);
        mapView.onCreate(savedInstance);
        mapView.getMapAsync(this);

        x_location = (TextView)findViewById(R.id.my_x);
        y_location = (TextView)findViewById(R.id.my_y);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

    }


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

        sendbt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                dbref_flag.setValue(0);
                naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
                    @Override
                    public void onLocationChange(@NonNull Location location) {
                        currentPoint = new Point();
                        currentPoint.x = location.getLatitude();
                        currentPoint.y = location.getLongitude();
                        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

                        msg_x = String.valueOf(currentPoint.x);
                        msg_y = String.valueOf(currentPoint.y);
                        x_location.setText(msg_x);
                        y_location.setText(msg_y);
                        dbref_x.setValue(currentPoint.x);
                        dbref_y.setValue(currentPoint.y);


                    }
                });
                Toast.makeText(getApplication(), "Location Sending..", Toast.LENGTH_SHORT).show();
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
}