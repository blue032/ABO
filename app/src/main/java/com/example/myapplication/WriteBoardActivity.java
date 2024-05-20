package com.example.myapplication;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class WriteBoardActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;
    private String userName; // 클래스의 멤버 변수로 선언
    private boolean isEditing;
    private String postId;
    private String currentPhotoPath; // 이미지 파일 경로를 저장할 변수
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    private ImageView uploadedPhoto;
    private Uri imageUri; // 이미지 URI 저장
    private boolean isNewImageSelected = false; // 클래스 멤버 변수로 추가
    private ArrayList<Uri> imageUriList = new ArrayList<>();
    private RecyclerView imagesRecyclerView;
    private ImageAdapter imageAdapter;

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
        setContentView(R.layout.activity_writeboard); // 이 layout은 적절히 수정해야 할 수도 있습니다.

        editTextTitle = findViewById(R.id.editTextPostTitle);
        editTextContent = findViewById(R.id.editTextPostContent);
        buttonSubmit = findViewById(R.id.buttonSubmitPost);

        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        ImageView backLogo = (ImageView) findViewById(R.id.backlogo);
        backLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WriteBoardActivity.this, BoardActivity.class);
                startActivity(intent);
                finish();  // 현재 액티비티 종료
            }
        });

        // RecyclerView 설정
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageUriList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, new ArrayList<>()); // 초기 상태에서는 비어 있는 어댑터
        imagesRecyclerView.setAdapter(imageAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Board");

        // 인텐트에서 데이터 추출
        Intent intent = getIntent();
        isEditing = intent.getBooleanExtra("isEditing", false);
        postId = intent.getStringExtra("postId"); // postId를 멤버 변수에 저장

        if (isEditing) {
            editTextTitle.setText(intent.getStringExtra("title"));
            editTextContent.setText(intent.getStringExtra("content"));
            // 인텐트로부터 이미지 URI String 리스트를 받아 Uri 객체 리스트로 변환
            ArrayList<String> imageUriStrings = intent.getStringArrayListExtra("photoUrls");
            if (imageUriStrings != null) {
                ArrayList<Uri> imageUris = new ArrayList<>();
                for (String uriString : imageUriStrings) {
                    imageUris.add(Uri.parse(uriString)); // String을 Uri 객체로 변환하여 추가
                }

                // 변환된 Uri 객체 리스트를 어댑터에 설정
                imageAdapter.setImageUris(imageUris);
                imageAdapter.notifyDataSetChanged();
            }
        }
        // 이전 상태 복원 (회전 등으로 인한 재생성 처리)
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getString("imageUri") != null ? Uri.parse(savedInstanceState.getString("imageUri")) : null;
        }

        initializeActivityResultLaunchers();


        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            userName = email != null ? email : "Anonymous"; // 이메일이 없는 경우 "Anonymous" 사용
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Board");

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
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

    // 게시물을 제출하거나 수정하는 메서드
    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 제목과 내용이 비어있지 않은지 확인
        if (!title.isEmpty() && !content.isEmpty()) {
            // 이미지가 있는 경우와 없는 경우를 구분하여 처리
            if (!imageUriList.isEmpty()) {
                // 이미지 업로드 후 게시글 업로드
                uploadImages(imageUriList, new CeoWriteBoardActivity.OnAllImagesUploadedListener() {
                    @Override
                    public void onAllImagesUploaded(List<String> imageUrls) {
                        // 이미지 업로드 성공 후 게시글 정보와 함께 저장
                        savePostToDatabase(title, content, imageUrls, userName);
                    }
                });
            } else {
                // 이미지 없이 게시글 정보만 저장
                savePostToDatabase(title, content, new ArrayList<>(), userName);
            }
        } else {
            // 제목이나 내용이 비어 있는 경우 사용자에게 알림
            Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImages(ArrayList<Uri> imageUris, final CeoWriteBoardActivity.OnAllImagesUploadedListener listener) {
        final List<String> uploadedImageUrls = new ArrayList<>();
        AtomicInteger uploadCounter = new AtomicInteger();

        for (Uri imageUri : imageUris) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("postImages/" + System.currentTimeMillis() + "_" + getFileExtension(imageUri));
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedImageUrls.add(uri.toString());
                if (uploadCounter.incrementAndGet() == imageUris.size()) {
                    listener.onAllImagesUploaded(uploadedImageUrls);
                }
            })).addOnFailureListener(e -> Toast.makeText(WriteBoardActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                Toast.makeText(WriteBoardActivity.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(WriteBoardActivity.this, "Failed to upload post: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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