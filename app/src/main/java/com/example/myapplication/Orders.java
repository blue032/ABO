package com.example.myapplication;

import android.view.MenuItem;

import com.google.firebase.database.PropertyName;

import java.util.List;
import java.util.Objects;

public class Orders {
    private int WaitNumber;
    private int day;

    private String in_or_takeout; // 매장 내 주문인지 포장 주문인지
    @PropertyName("price")
    private int totalPrice; // 데이터베이스의 'price' 필드와 매핑
    private List<MenuItem> menu;
    private Time time; // Time 객체에 대한 참조

    private String id; // 주문 ID
    private long totalWaitTimeMillis; // 총 대기 시간을 밀리초로 저장하는 필드

    public static class MenuItem{
        private String name;
        private int price; //개별 메뉴의 가격
        private int quantity;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
    // Time 내부 클래스
    public static class Time {
        private int hour;
        private int minute;
        private int second;

        // Time 클래스의 getter와 setter 메서드들
        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public int getSecond() {
            return second;
        }

        public void setSecond(int second) {
            this.second = second;
        }
    }


    // Orders 클래스의 기본 생성자
    public Orders() { }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    // Orders 클래스의 getter와 setter 메서드들
    public int getWaitNumber() {
        return WaitNumber;
    }

    public void setWaitNumber(int WaitNumber) {
        this.WaitNumber = WaitNumber;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
    // in_or_takeout의 게터 및 세터
    public String getIn_or_takeout() {
        return in_or_takeout;
    }
    public void setIn_or_takeout(String in_or_takeout) {
        this.in_or_takeout = in_or_takeout;
    }
    // 게터 메서드
    @PropertyName("price")
    public int getTotalPrice() {
        return totalPrice;
    }

    // 세터 메서드
    @PropertyName("price")
    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public void setMenu(List<MenuItem> menu) {
        this.menu = menu;
    }

    // Time 객체에 대한 getter와 setter (Orders 클래스 내부에 정의되어야 함)
    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public long getTotalWaitTimeMillis() {
        return totalWaitTimeMillis;
    }

    // totalWaitTimeMillis 필드에 대한 setter
    public void setTotalWaitTimeMillis(long totalWaitTimeMillis) {
        this.totalWaitTimeMillis = totalWaitTimeMillis;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Orders orders = (Orders) obj;
        return WaitNumber == orders.WaitNumber;  // 주문을 식별할 유일한 필드를 사용
    }
    @Override
    public int hashCode() {
        return Objects.hash(WaitNumber);  // 식별자로 WaitNumber 사용
    }
}