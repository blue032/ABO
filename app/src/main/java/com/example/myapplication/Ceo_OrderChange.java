package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Ceo_OrderChange extends AppCompatActivity {

    private EditText editTextWaitingNumber;
    private TextView buttonSearch;
    private DatabaseReference mDatabase;
    private TextView textViewOrderDetails;
    private Orders currentOrder;
    private String currentOrderId;
    private String currentFormattedDate;
    private ImageView buttonStartDate;  // ImageView로 변경
    private TextView buttonEdit;
    private TextView textView1; // 추가된 부분: 날짜를 표시할 TextView
    private String[] order_menu;
    private final int[] menu_prices = {3000, 3000, 3500, 3500, 4000, 4000, 4000, 4500, 4500, 5000, 4500, 3500, 3500, 3500, 4000, 5000, 6000, 3500, 4500, 4500};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceo_orderchange);
        order_menu = getResources().getStringArray(R.array.order_menu);

        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEdit = findViewById(R.id.buttonEdit);
        editTextWaitingNumber = findViewById(R.id.editTextWaitingNumber);
        buttonSearch = findViewById(R.id.buttonSearch);
        textView1 = findViewById(R.id.textView1); // 추가된 부분: 날짜를 표시할 TextView
        mDatabase = FirebaseDatabase.getInstance().getReference();
        textViewOrderDetails = findViewById(R.id.textViewOrderDetails);

        // 기본 날짜 설정
        String defaultDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        buttonStartDate.setTag(defaultDate);

        buttonStartDate.setOnClickListener(view -> showDatePickerDialog());

        buttonSearch.setOnClickListener(view -> {
            Object tag = buttonStartDate.getTag();
            if (tag == null) {
                textViewOrderDetails.setText("날짜를 선택해주세요.");
                return;
            }

            String selectedDate = tag.toString();
            try {
                int waitNumber = Integer.parseInt(editTextWaitingNumber.getText().toString());
                searchOrder(selectedDate, waitNumber);
            } catch (NumberFormatException e) {
                textViewOrderDetails.setText("유효한 대기번호를 입력해주세요.");
                Log.e("NumberFormatException", "Invalid wait number format", e);
            }
        });

        buttonEdit.setOnClickListener(v -> showEditDialog());
    }

    private void showDatePickerDialog() {
        int year = 2024;
        int month = Calendar.MARCH;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Ceo_OrderChange.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d/%02d/%02d", year1, monthOfYear + 1, dayOfMonth);
                    buttonStartDate.setTag(date); // 수정된 부분: setText() 대신 setTag() 사용
                    textView1.setText(date); // 추가된 부분: 선택된 날짜를 textView1에 표시
                },
                year, month, 1);

        DatePicker picker = datePickerDialog.getDatePicker();
        Calendar minDate = Calendar.getInstance();
        minDate.set(year, month, 1);
        picker.setMinDate(minDate.getTimeInMillis());

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(year, month, minDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        picker.setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

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
                                currentOrderId = snapshot.getKey();
                                currentFormattedDate = formattedDate;
                                Log.d("Debug", "Order loaded: " + currentOrder);
                                updateTextViewWithOrderDetails(currentOrder);
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
        spinnerContainer.removeAllViews();

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

        TextView textViewTotalPrice = dialog.findViewById(R.id.textViewTotalPrice);
        textViewTotalPrice.setText(String.format(Locale.getDefault(), "%,d", currentOrder.getTotalPrice()));

        // TextView를 버튼처럼 동작하도록 설정
        TextView buttonConfirm = dialog.findViewById(R.id.buttonconfirm);
        TextView buttonCancel = dialog.findViewById(R.id.buttoncancel);

        // 주문 내역의 시간 설정
        TextView textViewTime = dialog.findViewById(R.id.textViewTime);
        textViewTime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", currentOrder.getTime().getHour(), currentOrder.getTime().getMinute(), currentOrder.getTime().getSecond()));

        buttonConfirm.setOnClickListener(v -> {
            int totalPrice = 0;
            ArrayList<Orders.MenuItem> updatedItems = new ArrayList<>();
            for (int i = 0; i < spinnerContainer.getChildCount(); i += 2) {
                Spinner menuSpinner = (Spinner) spinnerContainer.getChildAt(i);
                EditText quantityEditText = (EditText) spinnerContainer.getChildAt(i + 1);

                String selectedMenu = menuSpinner.getSelectedItem().toString();
                int quantity = Integer.parseInt(quantityEditText.getText().toString());
                int price = menu_prices[Arrays.asList(order_menu).indexOf(selectedMenu)];
                int itemTotalPrice = price * quantity;

                Orders.MenuItem updatedItem = new Orders.MenuItem();
                updatedItem.setName(selectedMenu);
                updatedItem.setQuantity(quantity);
                updatedItem.setPrice(itemTotalPrice);
                updatedItems.add(updatedItem);

                totalPrice += itemTotalPrice;
            }
            currentOrder.setMenu(updatedItems);
            currentOrder.setTotalPrice(totalPrice);

            updateOrderInDatabase(currentOrder, currentOrderId, currentFormattedDate);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateOrderInDatabase(Orders updatedOrder, String orderId, String formattedDate) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Order")
                .child(formattedDate)
                .child(orderId);

        Map<String, Object> orderUpdates = new HashMap<>();
        orderUpdates.put("totalPrice", updatedOrder.getTotalPrice());
        orderUpdates.put("menu", updatedOrder.getMenu());

        orderUpdates.put("time/hour", updatedOrder.getTime().getHour());
        orderUpdates.put("time/minute", updatedOrder.getTime().getMinute());
        orderUpdates.put("time/second", updatedOrder.getTime().getSecond());

        orderRef.updateChildren(orderUpdates).addOnSuccessListener(aVoid -> {
            Log.i("UpdateDB", "Order updated successfully.");
            Toast.makeText(Ceo_OrderChange.this, "Order updated successfully", Toast.LENGTH_SHORT).show();
            updateTextViewWithOrderDetails(updatedOrder);
        }).addOnFailureListener(e -> {
            Log.e("UpdateOrder", "Failed to update order", e);
            Toast.makeText(Ceo_OrderChange.this, "Failed to update order", Toast.LENGTH_SHORT).show();
        });
    }
}
