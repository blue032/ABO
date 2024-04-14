package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class Ceo_OrderChange extends AppCompatActivity {

    Button buttonStartDate, buttonEndDate, buttonEdit, buttonAdd, buttonDelete;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceo_orderchange);

        // 시작 날짜 선택 버튼 초기화
        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);
        buttonEdit = findViewById(R.id.buttonEdit); //수정버튼
        buttonAdd =  findViewById(R.id.buttonAdd); //추가버튼
        buttonDelete = findViewById(R.id.buttonDelete);

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

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });
    }
    //달력 보이는 코드
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

    //시간을 선택할 수 있도록 하는 코드
    private void showTimePickerDialog(Button timeButton) {
        // 현재 시간을 기준으로 TimePickerDialog 생성
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        // TimePickerDialog 스피너 스타일로 생성
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                Ceo_OrderChange.this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // 스피너 스타일
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // 선택한 시간을 버튼 텍스트로 설정
                        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                        timeButton.setText(selectedTime); // 'timeButton'은 시간을 보여주는 버튼의 변수 이름입니다
                    }
                }, hour, minute, false); // 'false'는 24시간 형식을 사용하지 않겠다는 의미입니다

        // Dialog의 배경을 투명하게 하여 스피너만 보이게 합니다
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        timePickerDialog.show();
    }

    //수정버튼 팝업창
    //수정버튼 -> 시간 선택할 수 있는 팝업창이 나옴 -> 시간을 선택하고 확인 누르기 -> 확인을 누르면 시간선택 버튼에 선택한 시간이 나옴.
    private void showEditDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.ceo_orderchange_modification_dialong); // Make sure the layout name is correct

        Button timeButton = dialog.findViewById(R.id.buttonTime); // Time selection button
        Button confirmButton = dialog.findViewById(R.id.buttonConfirm);
        Button cancelButton = dialog.findViewById(R.id.buttonCancel);

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(timeButton);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to handle edited data
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //추가버튼 팝업창
    private void showAddDialog() {
        final Dialog addDialog = new Dialog(this);
        addDialog.setContentView(R.layout.ceo_orderchange__add_dialong); // 새로운 대화상자 레이아웃

        // 이 ID가 실제 XML 파일에 정의된 버튼 ID와 일치해야 합니다.
        Button timeButton = addDialog.findViewById(R.id.buttonTime);
        Button addButton = addDialog.findViewById(R.id.buttonAdd);
        Button cancelButton = addDialog.findViewById(R.id.buttonCancel);

        // 시간 선택 버튼을 위한 리스너 설정
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(timeButton);
            }
        });

        // 추가 버튼을 위한 리스너 설정
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 추가할 데이터를 처리하는 로직 구현
                addDialog.dismiss();
            }
        });

        // 취소 버튼을 위한 리스너 설정
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog.dismiss();
            }
        });

        addDialog.show();
    }


    //삭제버튼 팝업창
    private void showDeleteDialog() {
        final Dialog addDialog = new Dialog(this);
        addDialog.setContentView(R.layout.ceo_orderchange__delete_dialong); // 새로운 대화상자 레이아웃

        // 이 ID가 실제 XML 파일에 정의된 버튼 ID와 일치해야 합니다.
        Button addButton = addDialog.findViewById(R.id.buttonConfirm);
        Button cancelButton = addDialog.findViewById(R.id.buttonCancel);


        // 추가 버튼을 위한 리스너 설정
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 추가할 데이터를 처리하는 로직 구현
                addDialog.dismiss();
            }
        });

        // 취소 버튼을 위한 리스너 설정
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog.dismiss();
            }
        });

        addDialog.show();
    }
}
