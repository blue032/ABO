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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CeoWriteBoardActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;
    private String postId;
    private boolean isEditing;
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    private ImageView uploadedPhoto;
    private Uri imageUri;
    private PreviewView previewView;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceowriteboard);

        editTextTitle = findViewById(R.id.editTextPostTitle);
        editTextContent = findViewById(R.id.editTextPostContent);
        uploadedPhoto = findViewById(R.id.iconPhoto);
        buttonSubmit = findViewById(R.id.buttonSubmitPost);
        previewView = findViewById(R.id.previewView);

        databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard");

        Intent intent = getIntent();
        isEditing = intent.getBooleanExtra("isEditing", false);
        if (isEditing) {
            editTextTitle.setText(intent.getStringExtra("title"));
            editTextContent.setText(intent.getStringExtra("content"));
            postId = intent.getStringExtra("postId");
            if (intent.hasExtra("photoUri")) {
                String imageUriString = intent.getStringExtra("photoUri");
                Uri imageUri = Uri.parse(imageUriString);
                uploadedPhoto.setImageURI(imageUri);
                uploadedPhoto.setVisibility(View.VISIBLE);
            }
        }

        initializeActivityResultLaunchers();

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        buttonSubmit.setOnClickListener(v -> submitPost());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_home) {
                startActivity(new Intent(CeoWriteBoardActivity.this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_board) {
                startActivity(new Intent(CeoWriteBoardActivity.this, BoardActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_notification) {
                return true;
            } else if (item.getItemId() == R.id.action_mypage) {
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
                        uploadedPhoto.setImageURI(contentUri);
                        imageUri = contentUri;
                    }
                });

        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        uploadedPhoto.setImageURI(selectedImageUri);
                        imageUri = selectedImageUri;
                    }
                });
    }

    public void onIconPhotoClick(View view) {
        final CharSequence[] options = {"촬영하기", "갤러리에서 찾기", "취소"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CeoWriteBoardActivity.this);
        builder.setTitle("사진 업로드");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("촬영하기")) {
                    takePhotoWithCameraX();
                } else if (options[item].equals("갤러리에서 찾기")) {
                    choosePhotoFromGallery();
                } else if (options[item].equals("취소")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void takePhotoWithCameraX() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                ImageCapture imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

                // 사진 촬영 버튼 클릭 리스너 구현
                findViewById(R.id.button_take_photo).setOnClickListener(view -> {
                    File photoFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

                    imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Uri savedUri = Uri.fromFile(photoFile);
                            runOnUiThread(() -> {
                                // ImageView에 사진 표시
                                uploadedPhoto.setImageURI(savedUri);
                                // 사진의 URI를 사용하여 추가 처리(예: Firebase에 업로드)
                                imageUri = savedUri; // 클래스 변수에 사진 URI 저장
                                Toast.makeText(CeoWriteBoardActivity.this, "Photo Capture Succeeded", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            // 사진 촬영 실패 처리
                            runOnUiThread(() -> Toast.makeText(CeoWriteBoardActivity.this, "Photo Capture Failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                });

            } catch (ExecutionException | InterruptedException e) {
                // CameraProvider를 가져오는 데 실패한 경우
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
        String photoUrl = (imageUri != null) ? imageUri.toString() : "";

        if (!title.isEmpty() && !content.isEmpty()) {
            if (isEditing && postId != null) {
                CeoBoardPost post = new CeoBoardPost(title, content, System.currentTimeMillis(), photoUrl);
                databaseReference.child(postId).setValue(post)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(CeoWriteBoardActivity.this, "게시글이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(CeoWriteBoardActivity.this, "게시글 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                String key = databaseReference.push().getKey();
                CeoBoardPost post = new CeoBoardPost(title, content, System.currentTimeMillis(), photoUrl);
                if (key != null) {
                    databaseReference.child(key).setValue(post)
                            .addOnSuccessListener(aVoid -> Toast.makeText(CeoWriteBoardActivity.this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(CeoWriteBoardActivity.this, "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show());
                    finish();
                }
            }
        } else {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
