package com.example.myapplication;

import android.view.MenuItem;

import java.util.List;

public class Orders {
    private int WaitNumber;
    private int day;
    private List<MenuItem> menu;
    private Time time; // Time 객체에 대한 참조

    private long totalWaitTimeMillis; // 총 대기 시간을 밀리초로 저장하는 필드

    public static class MenuItem{
        private String name;
        private int price;
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
}
