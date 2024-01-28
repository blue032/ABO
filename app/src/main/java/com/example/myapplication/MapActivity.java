package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // SupportMapFragment를 가져와서 지도가 준비되면 알림을 받습니다.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 다양한 위치에 마커 추가
        addMarkerWithCustomIcon(googleMap, new LatLng(37.37500867159571, 126.63387174042461), "카페드림(6호관)", R.drawable.location_green);
        addMarkerWithCustomIcon(googleMap, new LatLng(37.37591543320427, 126.63281734018747), "스낵바(13호관)", R.drawable.location_green);
        addMarkerWithCustomIcon(googleMap, new LatLng(37.37452483159567, 126.6332926552895), "O.O카페(7호관)", R.drawable.location_green);
        addMarkerWithCustomIcon(googleMap, new LatLng(37.372401288059535, 126.6313160023207), "미유(29호관)", R.drawable.location_green);
        addMarkerWithCustomIcon(googleMap, new LatLng(37.37340586676641, 126.62985469283342), "혜윰(기숙사)", R.drawable.location_green);
        addMarkerWithCustomIcon(googleMap, new LatLng(37.37439777449398, 126.63154896625312), "카페드림(11호관)", R.drawable.location_green);
    }

    // 마커를 추가하고 사용자 지정 아이콘을 설정하는 메소드
    private void addMarkerWithCustomIcon(GoogleMap map, LatLng position, String title, int iconResId) {
        map.addMarker(new MarkerOptions().position(position).title(title)
                .icon(BitmapDescriptorFactory.fromResource(iconResId)));
    }
}
