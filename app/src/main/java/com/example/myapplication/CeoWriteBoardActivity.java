package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

public class CeoWriteBoardActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;
    private String postId;
    private boolean isEditing;
    private String currentPhotoPath; //이미지 파일 경로를 저장할 변수
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    private ImageView uploadedPhoto;
    private Uri imageUri; //이미지 url 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceowriteboard); // 이 layout은 적절히 수정해야 할 수도 있습니다.

        editTextTitle = findViewById(R.id.editTextPostTitle); // layout의 id와 일치해야 합니다.
        editTextContent = findViewById(R.id.editTextPostContent); // layout의 id와 일치해야 합니다.
        uploadedPhoto = findViewById(R.id.iconPhoto);
        buttonSubmit = findViewById(R.id.buttonSubmitPost); // layout의 id와 일치해야 합니다.

        // Firebase 데이터베이스 참조 "ceoBoard" 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard");

        // 인텐트에서 데이터 추출
        Intent intent = getIntent();
        isEditing = intent.getBooleanExtra("isEditing", false);
        // 수정 모드일 때만 제목과 내용, postId를 인텐트에서 가져와서 설정
        if (isEditing) {
            editTextTitle.setText(intent.getStringExtra("title"));
            editTextContent.setText(intent.getStringExtra("content"));
            postId = intent.getStringExtra("postId"); // postId를 멤버 변수에 저장
        if (intent.hasExtra("photoUri")) {
            String imageUriString = intent.getStringExtra("photoUri");
            Uri imageUri = Uri.parse(imageUriString);
            uploadedPhoto.setImageURI(imageUri);
            uploadedPhoto.setVisibility(View.VISIBLE);
        }

        }
        initializeActivityResultLaunchers(); // ActivityResultLauncher 초기화를 별도 메소드로


        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(CeoWriteBoardActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    // 게시판 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(CeoWriteBoardActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    // 알림 아이템이 선택되었을 때의 동작
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    // 메뉴 페이지 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(CeoWriteBoardActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false; // 아무 항목도 선택되지 않았을 경우
            }
        });
    }

    private void initializeActivityResultLaunchers() {
        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri contentUri = Uri.fromFile(new File(currentPhotoPath));
                        uploadedPhoto.setImageURI(contentUri); // 'uploadedphoto' ImageView에 이미지 설정
                        imageUri = contentUri; // 전역 변수에 저장
                    }
                });

        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        uploadedPhoto.setImageURI(selectedImageUri); // 'uploadedphoto' ImageView에 이미지 설정
                        imageUri = selectedImageUri; // 전역 변수에 저장
                    }
                });
    }

    public void onIconPhotoClick(View view) {
        // 옵션을 담은 배열
        final CharSequence[] options = { "촬영하기", "갤러리에서 찾기", "취소" };

        AlertDialog.Builder builder = new AlertDialog.Builder(CeoWriteBoardActivity.this);
        builder.setTitle("사진 업로드");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("촬영하기")) {
                    takePhotoFromCamera();
                } else if (options[item].equals("갤러리에서 찾기")) {
                    choosePhotoFromGallery();
                } else if (options[item].equals("취소")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //카메라앱 호출하여 사진 촬영, 촬영한 사진은 createImageFile 메소드를 통해 생성된 파일에 저장 ,
    //FileProvider를 사용하여 안전하게 파일 URL를 공유
    private void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                // Log statement
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.myapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraActivityResultLauncher.launch(takePictureIntent);
            }
        }
    }
    //갤러리에서 사진 선택
    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(intent);
    }
    //촬영한 사진을 저장할 파일을 생성하고 해당 파일의 경로를 currentPhotoPath에 저장, 파일 이름은 현재시간 기반
    private File createImageFile() throws IOException {
        // 이미지 파일 이름 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // 파일: 경로를 변수에 저장
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String photoUrl = (imageUri != null) ? imageUri.toString() : "";

        if (!title.isEmpty() && !content.isEmpty()) {
            if (isEditing && postId != null) {
                // postId를 사용하여 해당 게시물을 데이터베이스에서 찾고 업데이트
                CeoBoardPost post = new CeoBoardPost(title, content, System.currentTimeMillis(), photoUrl); // CeoBoardPost 객체 생성
                databaseReference.child(postId).setValue(post)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // 수정완료 후 CeoBoardActivity로 이동
                                Toast.makeText(CeoWriteBoardActivity.this, "게시글이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CeoWriteBoardActivity.this, CeoBoardActivity.class);
                                startActivity(intent);
                                finish(); // 현재 액티비티 종료
                            } else {
                                // 실패하면 에러 메시지 표시
                                Toast.makeText(CeoWriteBoardActivity.this, "게시글 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }else {
                // 새 게시물 추가 로직
                String key = databaseReference.push().getKey();
                CeoBoardPost post = new CeoBoardPost(title, content, System.currentTimeMillis(), photoUrl);
                if (key != null) {
                    post.setPostId(key);
                    databaseReference.child(key).setValue(post)
                            .addOnSuccessListener(aVoid -> {
                                // 성공적으로 추가 되었을 때의 로직
                                Toast.makeText(CeoWriteBoardActivity.this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // 추가 실패시
                                Toast.makeText(CeoWriteBoardActivity.this, "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        } else {
            // 제목이나 내용이 비어 있으면 사용자에게 알림
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}