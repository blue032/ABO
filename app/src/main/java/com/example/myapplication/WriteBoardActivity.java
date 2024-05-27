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
    private String userName;
    private boolean isEditing;
    private String postId;
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    private Uri imageUri;
    private boolean isNewImageSelected = false;
    private ArrayList<Uri> imageUriList = new ArrayList<>();
    private RecyclerView imagesRecyclerView;
    private ImageAdapter imageAdapter;
    private List<String> initialImageUrls = new ArrayList<>();

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUri != null) {
            outState.putString("imageUri", imageUri.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writeboard);

        editTextTitle = findViewById(R.id.editTextPostTitle);
        editTextContent = findViewById(R.id.editTextPostContent);
        buttonSubmit = findViewById(R.id.buttonSubmitPost);

        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        ImageView backLogo = findViewById(R.id.backlogo);
        backLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WriteBoardActivity.this, BoardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageUriList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, new ArrayList<>());
        imagesRecyclerView.setAdapter(imageAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Board");

        Intent intent = getIntent();
        isEditing = intent.getBooleanExtra("isEditing", false);
        postId = intent.getStringExtra("postId");

        if (isEditing) {
            editTextTitle.setText(intent.getStringExtra("title"));
            editTextContent.setText(intent.getStringExtra("content"));
            ArrayList<String> imageUriStrings = intent.getStringArrayListExtra("photoUrls");
            if (imageUriStrings != null) {
                initialImageUrls = new ArrayList<>(imageUriStrings); // 기존 이미지를 유지하기 위해 초기 이미지 URL을 저장
                ArrayList<Uri> imageUris = new ArrayList<>();
                for (String uriString : imageUriStrings) {
                    imageUris.add(Uri.parse(uriString));
                }
                imageAdapter.setImageUris(imageUris);
                imageAdapter.notifyDataSetChanged();
            }
        }

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
            userName = email != null ? email : "Anonymous";
        }
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
                        isNewImageSelected = true;
                    }
                });

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
                Toast.makeText(this, "File creation failed", Toast.LENGTH_SHORT).show();
                return;
            }

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
        String nickname = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (!title.isEmpty() && !content.isEmpty()) {
            if (!imageUriList.isEmpty()) {
                uploadImages(imageUriList, new OnAllImagesUploadedListener() {
                    @Override
                    public void onAllImagesUploaded(List<String> imageUrls) {
                        // 기존 이미지와 새로 업로드된 이미지를 합침
                        List<String> allImageUrls = new ArrayList<>(initialImageUrls);
                        allImageUrls.addAll(imageUrls);
                        savePostToDatabase(title, content, allImageUrls, nickname, userName);
                    }
                });
            } else {
                savePostToDatabase(title, content, initialImageUrls, nickname, userName); // 기존 이미지를 그대로 사용
            }
        } else {
            Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
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
            })).addOnFailureListener(e -> Toast.makeText(WriteBoardActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void savePostToDatabase(String title, String content, List<String> imageUrls, String nickname, String userName) {
        DatabaseReference postReference;
        if (isEditing) {
            postReference = databaseReference.child(postId);
        } else {
            postReference = databaseReference.push();
            postId = postReference.getKey();
        }

        BoardPost post = new BoardPost(title, content, System.currentTimeMillis(), imageUrls, nickname, userName);
        postReference.setValue(post).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("postId", postId);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(WriteBoardActivity.this, isEditing ? "게시글이 수정되었습니다." : "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(WriteBoardActivity.this, "게시글 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnAllImagesUploadedListener {
        void onAllImagesUploaded(List<String> imageUrls);
    }
}
