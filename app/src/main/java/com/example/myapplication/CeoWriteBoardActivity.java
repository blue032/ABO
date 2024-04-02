package com.example.myapplication;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class CeoWriteBoardActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;
    private String postId;
    private boolean isEditing;
    private String currentPhotoPath; // 이미지 파일 경로를 저장할 변수
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    private ImageView uploadedPhoto;
    private Uri imageUri; // 이미지 URI 저장
    private boolean isNewImageSelected = false; // 클래스 멤버 변수로 추가
    private ArrayList<Uri> imageUriList = new ArrayList<>();
    private RecyclerView imagesRecyclerView;
    private ImageAdapter imageAdapter;
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 현재 이미지 URI가 있을 경우, 상태에 저장합니다.
        if (imageUri != null) {
            outState.putString("imageUri", imageUri.toString());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceowriteboard);

        // UI 컴포넌트 초기화
        editTextTitle = findViewById(R.id.editTextPostTitle);
        editTextContent = findViewById(R.id.editTextPostContent);
        buttonSubmit = findViewById(R.id.buttonSubmitPost);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);

        // RecyclerView 설정
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageUriList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, new ArrayList<>()); // 초기 상태에서는 비어 있는 어댑터
        imagesRecyclerView.setAdapter(imageAdapter);

        // Firebase 데이터베이스 참조
        databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard");

        Intent intent = getIntent();
        isEditing = intent.getBooleanExtra("isEditing", false);
        postId = intent.getStringExtra("postId");

        if (isEditing) {
            editTextTitle.setText(intent.getStringExtra("title"));
            editTextContent.setText(intent.getStringExtra("content"));


            // String 리스트를 사용하는 대신 Uri 리스트를 사용
            ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra("imageUris");
            if (imageUris != null) {
                imageAdapter.setImageUris(imageUris);
                imageAdapter.notifyDataSetChanged();
            }
        }

        // 이전 상태 복원 (회전 등으로 인한 재생성 처리)
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getString("imageUri") != null ? Uri.parse(savedInstanceState.getString("imageUri")) : null;
        }

        initializeActivityResultLaunchers();

        buttonSubmit.setOnClickListener(v -> submitPost());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_home) {
                startActivity(new Intent(CeoWriteBoardActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                startActivity(new Intent(CeoWriteBoardActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                startActivity(new Intent(CeoWriteBoardActivity.this, NotificationActivity.class));
                return true;
            } else if (itemId == R.id.action_mypage) {
                startActivity(new Intent(CeoWriteBoardActivity.this, MypageActivity.class));
                return true;
            }
            return false;
        });
    }

    private void initializeActivityResultLaunchers() {
        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri contentUri = Uri.fromFile(new File(currentPhotoPath));
                        imageUriList.add(contentUri);
                        imageAdapter.setImageUris(imageUriList);
                        imageAdapter.notifyDataSetChanged();
                        isNewImageSelected = true; // 사용자가 새 이미지를 선택했습니다
                    }
                });
        ArrayList<Uri> selectedImageUris = new ArrayList<>();

        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ClipData clipData = result.getData().getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                Uri imageUri = clipData.getItemAt(i).getUri();
                                imageUriList.add(imageUri);
                            }
                        } else {
                            Uri imageUri = result.getData().getData();
                            imageUriList.add(imageUri);
                        }
                        imageAdapter.setImageUris(imageUriList);
                        imageAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void onIconPhotoClick(View view) {
        final CharSequence[] options = {"촬영하기", "갤러리에서 찾기", "취소"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 업로드");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("촬영하기")) {
                takePhotoFromCamera();
            } else if (options[item].equals("갤러리에서 찾기")) {
                choosePhotosFromGallery();
            } else if (options[item].equals("취소")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error handling
                Toast.makeText(this, "File creation failed", Toast.LENGTH_SHORT).show();
                return;
            }

            // photoURI 변수를 if 블록 바깥으로 이동
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraActivityResultLauncher.launch(takePictureIntent);
            }
        }
    }


    private void choosePhotosFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryActivityResultLauncher.launch(intent);
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (!title.isEmpty() && !content.isEmpty() && !imageUriList.isEmpty()) {
            uploadImages(imageUriList, new OnAllImagesUploadedListener() {
                @Override
                public void onAllImagesUploaded(List<String> imageUrls) {
                    savePostToDatabase(title, content, imageUrls, userName);
                }
            });
        } else {
            Toast.makeText(this, "제목, 내용, 사진을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImages(ArrayList<Uri> imageUris, final OnAllImagesUploadedListener listener) {
        final List<String> uploadedImageUrls = new ArrayList<>();
        AtomicInteger uploadCounter = new AtomicInteger();

        for (Uri imageUri : imageUris) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("postImages/" + System.currentTimeMillis() + "_" + getFileExtension(imageUri));
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedImageUrls.add(uri.toString());
                if (uploadCounter.incrementAndGet() == imageUris.size()) {
                    listener.onAllImagesUploaded(uploadedImageUrls);
                }
            })).addOnFailureListener(e -> Toast.makeText(CeoWriteBoardActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void savePostToDatabase(String title, String content, List<String> imageUrls, String userName) {
        DatabaseReference newPostRef = databaseReference.push();
        CeoBoardPost newPost = new CeoBoardPost(title, content, System.currentTimeMillis(), imageUrls, userName);
        newPostRef.setValue(newPost).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CeoWriteBoardActivity.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CeoWriteBoardActivity.this, "Failed to upload post: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    interface OnAllImagesUploadedListener {
        void onAllImagesUploaded(List<String> imageUrls);
    }

    private void navigateToBoardActivity() {
        Intent intent = new Intent(this, CeoBoardActivity.class);
        startActivity(intent);
        finish();
    }
}