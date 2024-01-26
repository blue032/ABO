package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DetailpageActivity extends AppCompatActivity {

    private ListView list;
    String mTitle[]={"카페라떼","딸기스무디","녹차","샌드위치"};//listview에 title부분 설정
    String mDescription[]={"3,500원 (ICE/HOT)\\n 칼로리: ~~, 당류: ~~","4,000원 ICE\\n 칼로리: ~~, 당류: ~~"," 3,000원 (ICE/HOT)\\n 칼로리: ~~, 당류: ~~","3,400원(햄치즈/햄/치즈)\n 칼로리: ~~, 당류: ~~"};//listview에 설명부분
    int images[]={R.drawable.cafelatte,R.drawable.greentea,R.drawable.sandwich,R.drawable.strawberrysmoothie};
    //listview에 들어가는 사진

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailpage);

        list=(ListView)findViewById(R.id.list);

        MyAdapter adapter=new MyAdapter(this,mTitle,mDescription,images);
        list.setAdapter(adapter);//리스트에 어뎁터 설정

    }
    class MyAdapter extends ArrayAdapter<String>{

        Context context;
        String rTitle[];
        String rDescription[];
        int rImgs[];

        MyAdapter(Context c, String title[],String description[],int imgs[]){
            super(c,R.layout.row,R.id.textView1,title);
            this.context=c;
            this.rTitle=title;
            this.rDescription=description;
            this.rImgs=imgs;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //앞에서 만든 row xml파일을 view 객체로 만들기 위해서는 layoutInflater를 이용
            LayoutInflater layoutInflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row=layoutInflater.inflate(R.layout.row,parent,false);

            ImageView images=row.findViewById(R.id.image);
            TextView myTitle=row.findViewById(R.id.textView1);
            TextView myDescription=row.findViewById(R.id.textView2);

            images.setImageResource(rImgs[position]);
            myTitle.setText(rTitle[position]);
            myDescription.setText(rDescription[position]);


            return row;//앞에서 만든 xml 파일
        }
    }

}