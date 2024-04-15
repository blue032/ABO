package com.example.myapplication;

import android.app.AlertDialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class CafeMenu_WriteBoard extends AppCompatActivity {
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
        setContentView(R.layout.activity_cafemenu_writeboard);

        // 이미지를 표시할 RecyclerView 초기화
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // ImageAdapter 초기화
        imageAdapter = new ImageAdapter(this, new ArrayList<>()); // 빈 리스트로 초기화
        imagesRecyclerView.setAdapter(imageAdapter);

        // UI 컴포넌트 초기화
        editTextTitle = findViewById(R.id.editTextPostTitle);
        editTextContent = findViewById(R.id.editTextPostContent);
        buttonSubmit = findViewById(R.id.buttonSubmitPost);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        // Firebase 데이터베이스 참조
        databaseReference = FirebaseDatabase.getInstance().getReference("OOcafemenu");


        Intent intent = getIntent();
        isEditing = intent.getBooleanExtra("isEditing", false);
        postId = intent.getStringExtra("postId");

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

    }

    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        // 제목과 내용이 비어있지 않은지 확인
        if (!title.isEmpty() && !content.isEmpty()) {
            // 이미지가 있는 경우와 없는 경우를 구분하여 처리
            if (!imageUriList.isEmpty()) {
                // 이미지 업로드 후 게시글 업로드
                uploadImages(imageUriList, new OnAllImagesUploadedListener() {
                    @Override
                    public void onAllImagesUploaded(List<String> imageUrls) {
                        // 이미지 업로드 성공 후 게시글 정보와 함께 저장
                        savePostToDatabase(title, content, imageUrls);
                    }
                });
            } else {
                // 이미지 없이 게시글 정보만 저장
                savePostToDatabase(title, content, new ArrayList<>());
            }
        } else {
            // 제목이나 내용이 비어 있는 경우 사용자에게 알림
            Toast.makeText(CafeMenu_WriteBoard.this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }


    private void savePostToDatabase(String title, String content, List<String> imageUrls) {

        // Create a new post object
        CeoBoardPost newPost = new CeoBoardPost(title, content, System.currentTimeMillis(), imageUrls);

        // Push the new post to the database
        DatabaseReference newPostRef = databaseReference.push();
        newPostRef.setValue(newPost).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "게시물이 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
                // 여기에서 CafeMenu_Detail 액티비티로 이동하는 코드를 추가
                Intent detailIntent = new Intent(CafeMenu_WriteBoard.this, CafeMenu_Detail.class);
                detailIntent.putExtra("postID", newPostRef.getKey()); // 저장된 게시물의 고유 ID를 전달
                detailIntent.putExtra("title", title); // 게시물 제목 전달
                detailIntent.putExtra("content", content); // 게시물 내용 전달
                detailIntent.putStringArrayListExtra("photoUrls", (ArrayList<String>) imageUrls); // 이미지 URL 리스트 전달
                startActivity(detailIntent);
                finish(); // 작성 화면 종료
            } else {
                Toast.makeText(this, "게시물 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
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
            })).addOnFailureListener(e -> Toast.makeText(CafeMenu_WriteBoard.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                Toast.makeText(CafeMenu_WriteBoard.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(CafeMenu_WriteBoard.this, "Failed to upload post: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    interface OnAllImagesUploadedListener {
        void onAllImagesUploaded(List<String> imageUrls);
    }

    private void navigateToBoardActivity() {
        Intent intent = new Intent(this, CafeMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
