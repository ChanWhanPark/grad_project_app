package com.chananpark.Tagoga_Siheung;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chananpark.Tagoga_Siheung.Model.Point;
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
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private Marker marker_QWL = new Marker();
    private Marker marker_Car = new Marker();

    // 네이버 경로 저장 변수
    public List<LatLng> mPathList = new ArrayList<>();
    public List mPointList = new ArrayList<>();
    public List mGuideList = new ArrayList<>();

    // 목적지 & 현위치 좌표 변수
    private double goal_x;
    private double goal_y;
    private Point currentPoint;

    // 경로선 표시
    PathOverlay path_draw = new PathOverlay();

    // FireBase Information
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference dbref_x = firebaseDatabase.getReference().child("location").child("x");
    private DatabaseReference dbref_y = firebaseDatabase.getReference().child("location").child("y");
    private DatabaseReference dbref_path = firebaseDatabase.getReference().child("path");
    private DatabaseReference dbref_point = firebaseDatabase.getReference().child("pointIndex");
    private DatabaseReference dbref_guide = firebaseDatabase.getReference().child("guide");
    private DatabaseReference dbref_my_x = firebaseDatabase.getReference().child("my_location").child("x");
    private DatabaseReference dbref_my_y = firebaseDatabase.getReference().child("my_location").child("y");
    private DatabaseReference dbref_flag = firebaseDatabase.getReference().child("flag");
    private DatabaseReference dbref_arrive = firebaseDatabase.getReference().child("arrive");
    private DatabaseReference dbref_index = firebaseDatabase.getReference().child("currentIndex");
    private DatabaseReference dbref_guideindex = firebaseDatabase.getReference().child("currentguideIndex");

    // 차 위치 표시
    private String car_location_x;
    private String car_location_y;
    private double car_x;
    private double car_y;
    private TextView x_location;
    private TextView y_location;
    private TextView current_index_view;
    private TextView index_location;


    public int flag = 0;
    public int index = 0;
    public int guideindex = 0;
    public int arrive = 0;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 버튼
        Button btnMark_G = (Button) findViewById(R.id.btnmark_G);
        Button btnMark_T = (Button) findViewById(R.id.btnmark_T);
        Button btnMark_Q = (Button) findViewById(R.id.btnmark_Q);
        Button btnMark_F = (Button) findViewById(R.id.btnmark_F);
        Button btn_receive = (Button) findViewById(R.id.btn_receive);
        Button btn_call = (Button) findViewById(R.id.btn_call);
        Button btn_path = (Button) findViewById(R.id.btn_path);

        // 텍스트뷰
        x_location = (TextView) findViewById(R.id.my_x);
        y_location = (TextView) findViewById(R.id.my_y);
        current_index_view = (TextView) findViewById(R.id.my_index);
        index_location = (TextView) findViewById(R.id.my_direction);


        
        // 정문으로 안내
        btnMark_G.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (flag == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("오류");
                    builder.setMessage("차량이 도착하지 않았습니다.");
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("안내");
                    builder.setMessage("정문으로 가시겠습니까?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "정문으로 안내합니다.", Toast.LENGTH_SHORT).show();
                            setMarker(marker_Gate, 37.3393, 126.7327, R.drawable.ic_twotone_room_24, 0);
                            goal_x = 37.3393;
                            goal_y = 126.7327;
                            new Thread() {
                                public void run() {
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
                        }
                    });
                    // NO 버튼
                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "취소", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();

                }
            }
        });

        // TIP으로 안내
        btnMark_T.setOnClickListener(new Button.OnClickListener()
        {

            @Override
            public void onClick(View v) {
                if (flag == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("오류");
                    builder.setMessage("차량이 도착하지 않았습니다.");
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("안내");
                    builder.setMessage("TIP으로 가시겠습니까?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "TIP으로 안내합니다.", Toast.LENGTH_SHORT).show();
                            setMarker(marker_TIP, 37.3414, 126.7319, R.drawable.ic_baseline_pin_drop_1, 0);
                            goal_x = 37.3414;
                            goal_y = 126.7319;
                            new Thread() {
                                public void run() {
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
                        }
                    });
                    // NO 버튼
                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "취소", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();

                }
            }
        });

        // 산학융합관으로 안내
        btnMark_Q.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (flag == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("오류");
                    builder.setMessage("차량이 도착하지 않았습니다.");
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("안내");
                    builder.setMessage("산융으로 가시겠습니까?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "TIP으로 안내합니다.", Toast.LENGTH_SHORT).show();
                            setMarker(marker_QWL, 37.3389, 126.734328, R.drawable.ic_baseline_pin_drop_2, 0);
                            goal_x = 37.3389;
                            goal_y = 126.734328;
                            new Thread() {
                                public void run() {
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
                        }
                    });
                    // NO 버튼
                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "취소", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();

                }
            }
        });

        // 원하는 위치로 이동
        btnMark_F.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });

        // 차량 위치를 받아옴
        btn_receive.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                dbref_x.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        car_location_x = dataSnapshot.getValue(String.class);
                        if (car_location_x == null){
                            Log.d("TAG", " x is null");
                        }
                        else {
                            car_x = Double.parseDouble(car_location_x);
                            Log.d("TAG", car_location_x);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                dbref_y.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        car_location_y = dataSnapshot.getValue(String.class);
                        if (car_location_y == null){
                            Log.d("TAG", " y is null");
                        }
                        else {
                            car_y = Double.parseDouble(car_location_y);
                            Log.d("TAG", car_location_y);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(getApplication(), "Location receive..", Toast.LENGTH_SHORT).show();
                setcarMarker();
            }

        });

        // 차량 호출
        btn_call.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("차량 호출");
                builder.setMessage("차량을 호출하겠습니까?");
                // OK 버튼
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "차량을 호출합니다.", Toast.LENGTH_SHORT).show();

                        /* 이 중간에 알고리즘을 추가함으로써 호출 완료 시에 다음 기능을 사용할 수 있도록 해야함 */
                        dbref_arrive.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                dbref_my_x.setValue(currentPoint.x);
                                dbref_my_y.setValue(currentPoint.y);
                                try {
                                    arrive = dataSnapshot.getValue(Integer.class);
                                }
                                catch (NullPointerException e) {
                                    arrive = 0;
                                }
                                if (arrive == 1){
                                    Toast.makeText(MainActivity.this, "차량이 도착했습니다.", Toast.LENGTH_SHORT).show();
                                    flag = 1;
                                    dbref_flag.setValue(flag);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }

                });
                // NO 버튼
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "취소", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();



            }
        });

        // 안내하기 위한 경로 정보 표시
        btn_path.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mPathList.size() != 0)
                    PathDraw(mPathList);

                else{
                    Toast.makeText(MainActivity.this, "저장된 경로정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                mPathList.clear();
                mGuideList.clear();
                mPointList.clear();
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
    private void setcarMarker(){
        setMarker(marker_Car, car_x, car_y, R.drawable.car_32, 0);
        Toast.makeText(getApplication(), "Car is here~", Toast.LENGTH_SHORT).show();
        Log.d("TAG", "Car is here~");
    }

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
                x_location.setText(String.valueOf(currentPoint.x));
                y_location.setText(String.valueOf(currentPoint.y));
                dbref_index.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                             index = dataSnapshot.getValue(Integer.class);
                        }
                        catch (NullPointerException e) {
                            index = 0;
                        }
                        current_index_view.setText(String.valueOf(index));

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                dbref_guideindex.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            guideindex = dataSnapshot.getValue(Integer.class);
                        }
                        catch (NullPointerException e) {
                            guideindex = 0;
                        }
                        if (guideindex == 1){
                            index_location.setText("↑");
                        }else if (guideindex == 2){
                            index_location.setText("←");
                        }else if (guideindex == 3){
                            index_location.setText("→");
                        }else if (guideindex == 88){
                            index_location.setText("도착!");
                        }else{
                            index_location.setText("○");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
    public void HttpConnection(double x, double y) throws IOException, CloneNotSupportedException, JSONException
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
        Log.d(TAG, result);

        // save in json
        JSONObject root = new JSONObject(result);
        JSONObject route = root.getJSONObject("route");
        JSONObject traoptimal = (JSONObject) route.getJSONArray("traoptimal").get(0);
        JSONArray path = traoptimal.getJSONArray("path");
        JSONArray guide = traoptimal.getJSONArray("guide");

        for (int i=0;i<path.length();i++) {
            JSONArray pathIndex = (JSONArray) path.get(i);
            mPathList.add(new LatLng(pathIndex.getDouble(1), pathIndex.getDouble(0)));
        }
        for (int i=0;i<guide.length();i++){
            JSONObject guideIndex = (JSONObject) guide.get(i);
            mPointList.add(guideIndex.get("pointIndex"));
            mGuideList.add(guideIndex.get("type"));
        }

        dbref_path.setValue(mPathList);
        dbref_point.setValue(mPointList);
        dbref_guide.setValue(mGuideList);
        System.out.println("HTTP 함수에서 길이 출력");
        Log.d(TAG, mPathList.toString());
        Log.d(TAG, guide.toString());
        Log.d(TAG, mPointList.toString());
        Log.d(TAG, mGuideList.toString());
    }

    public void PathDraw(List<LatLng> pathList){
        path_draw.setCoords(pathList);
        path_draw.setColor(Color.RED);
        path_draw.setMap(naverMap);
    }
}