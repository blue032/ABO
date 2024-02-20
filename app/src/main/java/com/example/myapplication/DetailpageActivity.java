package com.example.myapplication;

import static com.example.myapplication.DetailMenuItemAdapter.VIEW_TYPE_HEADER;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailpageActivity extends AppCompatActivity {

    private RecyclerView rvMenuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailpage);
        rvMenuItems = (RecyclerView) findViewById(R.id.rv_menuItems);
        DetailMenuItemAdapter menuItemAdapter = new DetailMenuItemAdapter(new DetailMenuItemAdapter.DetailMenuItemDiffUtil());
        rvMenuItems.setAdapter(menuItemAdapter);
        menuItemAdapter.submitList(createMenuItems());
    }

    private ArrayList<DetailMenuItem> createMenuItems() {
        ArrayList<DetailMenuItem> items = new ArrayList<>();

        DetailMenuItem header = new DetailMenuItem();
        header.setViewType(VIEW_TYPE_HEADER);
        items.add(header);

        DetailMenuItem item1 = new DetailMenuItem();
        item1.setImageResId(R.drawable.cafelatte);
        item1.setTitle("아이스 아메리카노");
        item1.setPrice(3000);
        item1.setOptions(Arrays.asList("ICE"));
        item1.setCalorie("309kcal");
        item1.setSugar("10g");
        items.add(item1);

        DetailMenuItem item2 = new DetailMenuItem();
        item2.setImageResId(R.drawable.cafelatte);
        item2.setTitle("핫 아메리카노");
        item2.setPrice(3000);
        item2.setOptions(Arrays.asList("HOT"));
        item2.setCalorie("309kcal");
        item2.setSugar("10g");
        items.add(item2);

        DetailMenuItem item3 = new DetailMenuItem();
        item3.setImageResId(R.drawable.cafelatte);
        item3.setTitle("아이스 카레 라떼");
        item3.setPrice(3500);
        item3.setOptions(Arrays.asList("ICE"));
        item3.setCalorie("309kcal");
        item3.setSugar("10g");
        items.add(item3);

        DetailMenuItem item4 = new DetailMenuItem();
        item4.setImageResId(R.drawable.cafelatte);
        item4.setTitle("핫 카레 라떼");
        item4.setPrice(3500);
        item4.setOptions(Arrays.asList("HOT"));
        item4.setCalorie("309kcal");
        item4.setSugar("10g");
        items.add(item4);

        DetailMenuItem item5 = new DetailMenuItem();
        item5.setImageResId(R.drawable.cafelatte);
        item5.setTitle("카푸치노");
        item5.setPrice(4000);
        item5.setOptions(new ArrayList<>());
        item5.setCalorie("309kcal");
        item5.setSugar("10g");
        items.add(item5);

        DetailMenuItem item6 = new DetailMenuItem();
        item6.setImageResId(R.drawable.cafelatte);
        item6.setTitle("아이스 바닐라 라떼");
        item6.setPrice(4000);
        item6.setOptions(Arrays.asList("ICE"));
        item6.setCalorie("309kcal");
        item6.setSugar("10g");
        items.add(item6);

        DetailMenuItem item7 = new DetailMenuItem();
        item7.setImageResId(R.drawable.cafelatte);
        item7.setTitle("핫 바닐라 라떼");
        item7.setPrice(4000);
        item7.setOptions(Arrays.asList("HOT"));
        item7.setCalorie("309kcal");
        item7.setSugar("10g");
        items.add(item7);

        DetailMenuItem item8 = new DetailMenuItem();
        item8.setImageResId(R.drawable.cafelatte);
        item8.setTitle("카라멜 라떼");
        item8.setPrice(4500);
        item8.setOptions(new ArrayList<>());
        item8.setCalorie("309kcal");
        item8.setSugar("10g");
        items.add(item8);

        DetailMenuItem item9 = new DetailMenuItem();
        item9.setImageResId(R.drawable.cafelatte);
        item9.setTitle("아인슈페너");
        item9.setPrice(4500);
        item9.setOptions(new ArrayList<>());
        item9.setCalorie("309kcal");
        item9.setSugar("10g");
        items.add(item9);

        DetailMenuItem item10 = new DetailMenuItem();
        item10.setImageResId(R.drawable.cafelatte);
        item10.setTitle("로얄 밀크티");
        item10.setPrice(5000);
        item10.setOptions(new ArrayList<>());
        item10.setCalorie("309kcal");
        item10.setSugar("10g");
        items.add(item10);

        DetailMenuItem item11 = new DetailMenuItem();
        item11.setImageResId(R.drawable.cafelatte);
        item11.setTitle("루이보스");
        item11.setPrice(4500);
        item11.setOptions(new ArrayList<>());
        item11.setCalorie("309kcal");
        item11.setSugar("10g");
        items.add(item11);

        DetailMenuItem item12 = new DetailMenuItem();
        item12.setImageResId(R.drawable.cafelatte);
        item12.setTitle("아이스 캐모마일");
        item12.setPrice(3500);
        item12.setOptions(Arrays.asList("ICE"));
        item12.setCalorie("309kcal");
        item12.setSugar("10g");
        items.add(item12);

        DetailMenuItem item13 = new DetailMenuItem();
        item13.setImageResId(R.drawable.cafelatte);
        item13.setTitle("핫 캐모마일");
        item13.setPrice(3500);
        item13.setOptions(Arrays.asList("HOT"));
        item13.setCalorie("309kcal");
        item13.setSugar("10g");
        items.add(item13);

        DetailMenuItem item14 = new DetailMenuItem();
        item14.setImageResId(R.drawable.cafelatte);
        item14.setTitle("아이스 레몬에이드");
        item14.setPrice(3500);
        item14.setOptions(Arrays.asList("ICE"));
        item14.setCalorie("309kcal");
        item14.setSugar("10g");
        items.add(item14);

        DetailMenuItem item15 = new DetailMenuItem();
        item15.setImageResId(R.drawable.cafelatte);
        item15.setTitle("아이스 자몽에이드");
        item15.setPrice(4000);
        item15.setOptions(Arrays.asList("ICE"));
        item15.setCalorie("309kcal");
        item15.setSugar("10g");
        items.add(item15);

        DetailMenuItem item16 = new DetailMenuItem();
        item16.setImageResId(R.drawable.cafelatte);
        item16.setTitle("아포카토");
        item16.setPrice(5000);
        item16.setOptions(new ArrayList<>());
        item16.setCalorie("309kcal");
        item16.setSugar("10g");
        items.add(item16);

        DetailMenuItem item17 = new DetailMenuItem();
        item17.setImageResId(R.drawable.cafelatte);
        item17.setTitle("핫 히비스커스 티");
        item17.setPrice(6000);
        item17.setOptions(Arrays.asList("HOT"));
        item17.setCalorie("309kcal");
        item17.setSugar("10g");
        items.add(item17);

        DetailMenuItem item18 = new DetailMenuItem();
        item18.setImageResId(R.drawable.cafelatte);
        item18.setTitle("아이스 히비스커스 티");
        item18.setPrice(3500);
        item18.setOptions(Arrays.asList("ICE"));
        item18.setCalorie("309kcal");
        item18.setSugar("10g");
        items.add(item18);

        DetailMenuItem item19 = new DetailMenuItem();
        item19.setImageResId(R.drawable.cafelatte);
        item19.setTitle("아이스 그린티 라떼");
        item19.setPrice(4500);
        item19.setOptions(Arrays.asList("ICE"));
        item19.setCalorie("309kcal");
        item19.setSugar("10g");
        items.add(item19);

        DetailMenuItem item20 = new DetailMenuItem();
        item20.setImageResId(R.drawable.cafelatte);
        item20.setTitle("핫 그린티 라떼");
        item20.setPrice(4500);
        item20.setOptions(Arrays.asList("HOT"));
        item20.setCalorie("309kcal");
        item20.setSugar("10g");
        items.add(item20);

        return items;
    }

    public static class DetailMenuItem implements Serializable {
        int viewType = 1;
        int imageResId;
        String title;
        long price;
        List<String> options;
        String calorie;
        String sugar;

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getPrice() {
            return price;
        }

        public void setPrice(long price) {
            this.price = price;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public String getCalorie() {
            return calorie;
        }

        public void setCalorie(String calorie) {
            this.calorie = calorie;
        }

        public String getSugar() {
            return sugar;
        }

        public void setSugar(String sugar) {
            this.sugar = sugar;
        }

        public int getImageResId() {
            return imageResId;
        }

        public void setImageResId(int imageResId) {
            this.imageResId = imageResId;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return super.equals(obj);
        }
    }
}

