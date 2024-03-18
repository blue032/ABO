package com.example.myapplication;
//ff
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CeoIdSearchActivity extends AppCompatActivity {

    private EditText editName, editBirth, editCeoNumber;
    private Button searchCeoButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ceo_id_search);

        databaseReference = FirebaseDatabase.getInstance().getReference("CeoUsers");

        editName = findViewById(R.id.editname);
        editBirth = findViewById(R.id.editbirth);
        editCeoNumber = findViewById(R.id.editceonumber);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editName.addTextChangedListener(textWatcher);
        editBirth.addTextChangedListener(textWatcher);
        editCeoNumber.addTextChangedListener(textWatcher);

        searchCeoButton = findViewById(R.id.search_ID);
        searchCeoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findCeoId();
            }
        });
    }

    private void checkInputs() {
        String name = editName.getText().toString();
        String birth = editBirth.getText().toString();
        String ceoNumber = editCeoNumber.getText().toString();

        searchCeoButton.setEnabled(!name.isEmpty() && !birth.isEmpty() && !ceoNumber.isEmpty());
    }

    private void findCeoId() {
        String name = editName.getText().toString();
        String birth = editBirth.getText().toString();
        String ceoNumber = editCeoNumber.getText().toString();

        String year = birth.substring(0, 4);
        String month = birth.substring(4, 6);
        String day = birth.substring(6, 8);
        String birthDate = year + "-" + month + "-" + day;

        databaseReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean ceoFound = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CEO ceo = snapshot.getValue(CEO.class);
                    if (ceo != null && ceo.getBirth().equals(birthDate) && ceo.getCeoNumber().equals(ceoNumber)) {
                        Intent intent = new Intent(CeoIdSearchActivity.this, IdKnownActivity.class);
                        intent.putExtra("ceoemail", ceo.getEmail());
                        startActivity(intent);
                        ceoFound = true;
                        break;
                    }
                }
                if (!ceoFound) {
                    Toast.makeText(CeoIdSearchActivity.this, "일치하는 CEO가 없습니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CeoIdSearchActivity.this, "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static class CEO {
        private String birth, ceoNumber, email, name;

        public CEO() {}

        public String getBirth() {
            return birth;
        }

        public String getCeoNumber() {
            return ceoNumber;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }
}
