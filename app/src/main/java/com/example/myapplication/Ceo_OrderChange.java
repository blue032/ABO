package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Ceo_OrderChange extends AppCompatActivity {

    private EditText editTextWaitingNumber; //대기번호 입력 변수
    private Button buttonSearch; //검색 버튼 변수
    private DatabaseReference mDatabase;
    private TextView textViewOrderDetails; // 주문 세부사항을 표시할 TextView
    private Orders currentOrder;  // 현재 주문 정보를 저장할 변수
    private String currentOrderId;
    private String currentFormattedDate;
    Button buttonStartDate, buttonEndDate, buttonEdit, buttonAdd, buttonDelete;
    // 클래스 레벨에서 메뉴 이름과 가격 정의
    private String[] order_menu;  // 메뉴 이름 배열
    private final int[] menu_prices = {3000, 3000, 3500, 3500, 4000, 4000, 4000, 4500, 4500, 5000, 4500, 3500, 3500, 3500, 4000, 5000, 6000, 3500, 4500, 4500};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceo_orderchange);
        order_menu = getResources().getStringArray(R.array.order_menu);
        // 시작 날짜 선택 버튼 초기화
        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);
        buttonEdit = findViewById(R.id.buttonEdit); //수정버튼
        buttonAdd = findViewById(R.id.buttonAdd); //추가버튼
        buttonDelete = findViewById(R.id.buttonDelete);

        editTextWaitingNumber = findViewById(R.id.editTextWaitingNumber); //대기번호 입력
        buttonSearch = findViewById(R.id.buttonSearch); //검색 버튼
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //대기번호 목록 띄우기
        textViewOrderDetails = findViewById(R.id.textViewOrderDetails);


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

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedDate = buttonStartDate.getText().toString(); // 날짜 선택 필드에서 날짜 가져오기
                try {
                    int waitNumber = Integer.parseInt(editTextWaitingNumber.getText().toString()); // 대기번호 입력 필드에서 대기번호를 정수로 변환
                    searchOrder(selectedDate, waitNumber); // 검색 함수 호출
                } catch (NumberFormatException e) {
                    textViewOrderDetails.setText("유효한 대기번호를 입력해주세요."); // 대기번호 필드에 숫자가 아닌 값이 입력된 경우
                    Log.e("NumberFormatException", "Invalid wait number format", e);
                }
            }
        });

        // 메뉴 아이템 선택을 가정한 예시 코드
        int selectedMenuItemIndex = 0; // 실제 앱에서는 사용자 입력을 통해 이 값을 설정합니다.

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        /*buttonAdd.setOnClickListener(new View.OnClickListener() {
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
        });*/
    }
    //달력이 보이는 코드
    private void showDatePickerDialog(final Button dateButton) {
        // 특정 날짜를 고정
        int year = 2024;
        int month = Calendar.MARCH; // 3월은 Calendar.MARCH (2)로 설정
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Ceo_OrderChange.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 사용자가 날짜를 선택하면 해당 일자를 Button에 표시
                        // 선택된 날짜를 yyyy/MM/dd 형식으로 포맷
                        String date = String.format(Locale.getDefault(), "%d/%02d/%02d", year, monthOfYear + 1, dayOfMonth);
                        dateButton.setText(date); // 버튼 텍스트로 설정
                    }
                },
                year, month, 1); // DatePickerDialog 초기 설정을 2024년 3월 1일로 설정

        DatePicker picker = datePickerDialog.getDatePicker();
        // 최소 날짜 설정 (3월 1일)
        Calendar minDate = Calendar.getInstance();
        minDate.set(year, month, 1);
        picker.setMinDate(minDate.getTimeInMillis());

        // 최대 날짜 설정 (3월 31일)
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(year, month, minDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        picker.setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show(); // DatePickerDialog 표시
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

    // 주문을 검색하는 메소드
    private void searchOrder(String date, int waitNumber) {
        String[] parts = date.split("/");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        String formattedDate = String.format(Locale.getDefault(), "%d/%d/%d", year, month, day);

        mDatabase.child("Order").child(formattedDate)
                .orderByChild("WaitNumber").equalTo(waitNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Log.d("FirebaseData", "No data found for the given date and wait number.");
                            textViewOrderDetails.setText("해당 날짜와 대기번호에 주문 데이터가 없습니다.");
                            return;
                        }
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            currentOrder = snapshot.getValue(Orders.class);
                            if (currentOrder != null) {
                                // 클래스 멤버 변수에 값을 저장
                                currentOrderId = snapshot.getKey();
                                currentFormattedDate = formattedDate;
                                Log.d("Debug", "Order loaded: " + currentOrder);
                                // 화면 업데이트
                                updateTextViewWithOrderDetails(currentOrder);
                                // 수정된 주문 정보를 데이터베이스에 업데이트하기 위해 메서드 호출
                                // 업데이트 메서드 호출
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("FirebaseData", "Error while reading data: " + databaseError.toException());
                        textViewOrderDetails.setText("데이터 로드 실패: " + databaseError.getMessage());
                    }
                });
    }

    private void updateTextViewWithOrderDetails(Orders order) {
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("대기번호: ").append(order.getWaitNumber()).append("\n")
                .append("시간: ").append(order.getTime().getHour()).append(":")
                .append(order.getTime().getMinute()).append(":").append(order.getTime().getSecond()).append("\n")
                .append("메뉴:\n");
        for (Orders.MenuItem item : order.getMenu()) {
            detailsBuilder.append(item.getName())
                    .append(" - 수량: ").append(item.getQuantity())
                    .append(", 가격: ").append(item.getPrice()).append("\n");
        }
        detailsBuilder.append("총 가격: ").append(order.getTotalPrice()).append("\n")
                .append("매장/포장: ").append(order.getIn_or_takeout()).append("\n\n");
        textViewOrderDetails.setText(detailsBuilder.toString());

    }

    //수정버튼 팝업창
    private void showEditDialog() {
        Log.d("Debug", "showEditDialog 시작");
        if (currentOrder == null) {
            Toast.makeText(this, "선택된 주문이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        final Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.ceo_orderchange_modification_dialog);
        dialog.setTitle("Order Modification");

        LinearLayout spinnerContainer = dialog.findViewById(R.id.spinnerContainer);
        spinnerContainer.removeAllViews(); // 이전 뷰 제거

        for (Orders.MenuItem menuItem : currentOrder.getMenu()) {
            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.order_menu));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(adapter.getPosition(menuItem.getName()));
            spinnerContainer.addView(spinner);

            EditText quantityText = new EditText(this);
            quantityText.setInputType(InputType.TYPE_CLASS_NUMBER);
            quantityText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            quantityText.setText(String.valueOf(menuItem.getQuantity()));
            spinnerContainer.addView(quantityText);
        }

        Spinner spinnerInOrTakeout = dialog.findViewById(R.id.spinnerInOrTakeout);
        ArrayAdapter<CharSequence> inOrTakeoutAdapter = ArrayAdapter.createFromResource(this, R.array.in_or_takeout_options, android.R.layout.simple_spinner_item);
        inOrTakeoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInOrTakeout.setAdapter(inOrTakeoutAdapter);
        spinnerInOrTakeout.setSelection(inOrTakeoutAdapter.getPosition(currentOrder.getIn_or_takeout()));

        TextView textViewWaitNumber = dialog.findViewById(R.id.textViewWaitNumber);
        textViewWaitNumber.setText(String.valueOf(currentOrder.getWaitNumber()));
        TextView textViewTime = dialog.findViewById(R.id.textViewTime);
        textViewTime.setText(String.format(Locale.getDefault(), "%02d:%02d", currentOrder.getTime().getHour(), currentOrder.getTime().getMinute()));
        TextView textViewTotalPrice = dialog.findViewById(R.id.textViewTotalPrice);
        textViewTotalPrice.setText(String.format(Locale.getDefault(), "%,d", currentOrder.getTotalPrice()));

        Button buttonConfirm = dialog.findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(v -> {
            int totalPrice = 0;
            ArrayList<Orders.MenuItem> updatedItems = new ArrayList<>();
            for (int i = 0; i < spinnerContainer.getChildCount(); i += 2) {
                Spinner menuSpinner = (Spinner) spinnerContainer.getChildAt(i);
                EditText quantityEditText = (EditText) spinnerContainer.getChildAt(i + 1);

                String selectedMenu = menuSpinner.getSelectedItem().toString();
                int quantity = Integer.parseInt(quantityEditText.getText().toString());
                int price = menu_prices[Arrays.asList(order_menu).indexOf(selectedMenu)]; // Arrays.asList를 사용하여 배열을 리스트로 변환하고 indexOf 사용
                int itemTotalPrice = price * quantity; // 아이템의 총 가격 계산

                Orders.MenuItem updatedItem = new Orders.MenuItem();
                updatedItem.setName(selectedMenu);
                updatedItem.setQuantity(quantity);
                updatedItem.setPrice(itemTotalPrice); // 각 메뉴 아이템에 가격 설정
                updatedItems.add(updatedItem);

                totalPrice += itemTotalPrice; // 전체 주문의 총 가격 업데이트
            }
            currentOrder.setMenu(updatedItems);
            currentOrder.setTotalPrice(totalPrice); // 주문 객체의 총 가격 업데이트

            updateOrderInDatabase(currentOrder, currentOrderId, currentFormattedDate); // DB 업데이트 호출
            dialog.dismiss();
        });

        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // 주문 정보를 데이터베이스에 업데이트하는 메서드
    private void updateOrderInDatabase(Orders updatedOrder, String orderId, String formattedDate) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Order")
                .child(formattedDate) // 여기서 formattedDate는 주문이 저장된 날짜입니다.
                .child(orderId); // 여기서 orderId는 수정할 주문의 고유 ID입니다.

        Map<String, Object> orderUpdates = new HashMap<>();
        orderUpdates.put("price", updatedOrder.getTotalPrice());
        orderUpdates.put("menu", updatedOrder.getMenu());  // 이렇게 메뉴 리스트를 업데이트하는지 확인

        orderRef.updateChildren(orderUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // 업데이트 성공시 사용자에게 알림
                // 로그 추가: 업데이트 성공 로그
                Log.i("UpdateDB", "Order updated successfully.");
                Toast.makeText(Ceo_OrderChange.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
                // 업데이트된 주문 정보를 UI에 반영합니다.
                updateTextViewWithOrderDetails(updatedOrder);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 업데이트 실패시 로그와 사용자에게 알림
                Log.e("UpdateOrder", "Failed to update order", e);
                Toast.makeText(Ceo_OrderChange.this, "Failed to update order", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //추가버튼 팝업창
   /* private void showAddDialog() {
        final Dialog addDialog = new Dialog(this);
        addDialog.setContentView(R.layout.ceo_orderchange__add_dialog); // 새로운 대화상자 레이아웃

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
        addDialog.setContentView(R.layout.ceo_orderchange__delete_dialog); // 새로운 대화상자 레이아웃

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
    }*/
}