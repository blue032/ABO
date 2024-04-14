package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class Ceo_OrderChange extends AppCompatActivity {

    Button buttonStartDate, buttonEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceo_orderchange);

        // 시작 날짜 선택 버튼 초기화
        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);

        // 버튼 클릭 시 DatePickerDialog를 표시
        buttonStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(buttonStartDate);
            }
        });

        buttonEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(buttonEndDate);
            }
        });
    }

    private void showDatePickerDialog(final Button dateButton) {
        // 현재 날짜를 기준으로 DatePickerDialog 생성
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Ceo_OrderChange.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 월은 0부터 시작하므로 1을 더해야 정확한 날짜 표현
                        String date = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        dateButton.setText(date);  // 선택된 날짜를 버튼 텍스트로 설정
                    }
                }, year, month, day);

        datePickerDialog.show();  // DatePickerDialog 표시
    }
}
