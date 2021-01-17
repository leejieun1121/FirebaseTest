package com.example.firebaseauthtest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.loader.content.CursorLoader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;

public class HomeActivity extends AppCompatActivity {

    private static final int GALLERY_CODE = 10;
    private AppBarConfiguration mAppBarConfiguration;
    private TextView tvName;
    private TextView tvEmail;
    private FirebaseAuth auth;
    private Button logout;
    private FirebaseStorage storage;

    private ImageView imgContent;
    private EditText etTitle;
    private EditText etDescription;
    private Button btnUpload;
    private String imagePath;
    private FirebaseDatabase firebaseDatabase;
    private EditText error;

    private Toolbar toolbar;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgContent = findViewById(R.id.img_content);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        btnUpload = findViewById(R.id.btn_upload);
        firebaseDatabase = FirebaseDatabase.getInstance();
        //권한
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //try-catch 사용하면 앱 안터지고 파베로 에러 넘어가게끔 할 수 있음
//                error.setText("error");
            }
        });

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        tvName = headerView.findViewById(R.id.tv_name);
        tvEmail = headerView.findViewById(R.id.tv_email);

        tvName.setText(auth.getCurrentUser().getDisplayName());
        tvEmail.setText(auth.getCurrentUser().getEmail());

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사진,타이틀,본문 쓰고 업로드 버튼 누르기
                upload();
            }
        });

        remoteConfig();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.nav_logout){
                    auth.signOut();
                    LoginManager.getInstance().logOut();//페이스북 연동
                    finish();
                    Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                    startActivity(intent);
                } else if(id == R.id.nav_gallery){
                    //ACTION_PICK을 사용하면 선택한 이미지를 가져올 수 있다.
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    //setType -> 인텐트의 MIME 유형 설정, 반환하려는 유형 데이터를 나타냄
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent,GALLERY_CODE);
                }else if(id == R.id.nav_board){
                    startActivity(new Intent(HomeActivity.this,BoardActivity.class));
                }
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //에뮬이 아닌 핸드폰에서 실행하면 플레이스토어로 넘어감
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.kakao.com/"))
                .setDomainUriPrefix("https://jieun1121.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.kakao.talk").build())
                // Open links with com.example.ios on iOS
//                .setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        Log.d("tag_dynamicLink_long",dynamicLinkUri.toString());

        //생성된 링크가 너무 길어서 짧게 만들어줌
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.kakao.com/"))
                .setDomainUriPrefix("https://jieun1121.page.link")
                // Set parameters
                // ...
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Log.d("tag_dynamicLink_short",shortLink.toString());

                            Uri flowchartLink = task.getResult().getPreviewLink();
                        } else {
                            // Error
                            // ...
                        }
                    }
                });

    }

    private void remoteConfig(){
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        //디버깅 테스트 할 때 사용 -> 과부하 방지 위해 계속 요청 방지
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                //앱 실행마다 요청
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        //서버에 매칭되는 값이 없을 때 참조
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d("tag", "Config params updated: " + updated);
                            Toast.makeText(HomeActivity.this, "Fetch and activate succeeded",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(HomeActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        displayWelcomeMessage();
                    }
                });
    }

    private void displayWelcomeMessage(){
        String toolbarColor = mFirebaseRemoteConfig.getString("toolBarColor");
        Boolean welcomeMessageCaps = mFirebaseRemoteConfig.getBoolean("welcome_message_caps");
        String welcomeMessage = mFirebaseRemoteConfig.getString("welcome_message");

        toolbar.setBackgroundColor(Color.parseColor(toolbarColor));
        if(welcomeMessageCaps){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(welcomeMessage).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //서버 점검중이면 앱이 꺼져버리게
                    HomeActivity.this.finish();
                }
            });
            builder.create().show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE) {
            //이 경로를 알 수 없기 때문에 꼭 getPath 코드로 변환해줘야함
//            System.out.println(data.getData());
            Log.d("tag",getPath(data.getData()));
            //갤러리에서 선택된 사진 하나의 경로
            //TODO 갤러리 갔다가 사진 클릭 안했을때 오류남
            imagePath = getPath(data.getData());
            //파일로 만들어서
            File f = new File(imagePath);
            //이미지뷰에 set 해줌
            imgContent.setImageURI(Uri.fromFile(f));

        }
    }

    //경로 변환 코드(api만들어주지 않아서 꼭 써줘야한다고 함)
    public String getPath(Uri uri){
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this,uri,proj,null,null,null);
        //작업자 스레드에서 호출되어 수행
        Cursor cursor = cursorLoader.loadInBackground();
        //특정 필드 인덱스 값 반환, 존재하지 않을 경우 예외 발생
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(index);
    }


    //작성된 사진의 경로,타이틀, 본문을 파베디비에 저장하는 함수
    private void upload(){
        //선택된 사진은 storage에 업로드 됨
        StorageReference storageRef = storage.getReferenceFromUrl("gs://fir-authtest-29e5b.appspot.com/");
        Uri file = Uri.fromFile(new File(imagePath));
        StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                //사진 업로드가 성공하면, 타이틀,본문과 함께 데이터 베이스에 저장
                //com.google.android.gms~~~ 로 시작하는 주소로 저장된다 이건 글라이드로 안뜸 ㅠ
//                String downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                String downloadUrl = imagePath;
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.imageUrl = downloadUrl;
                imageDTO.title = etTitle.getText().toString();
                imageDTO.description = etDescription.getText().toString();
                imageDTO.uid = auth.getCurrentUser().getUid();
                imageDTO.userId = auth.getCurrentUser().getEmail();
                imageDTO.imageName = file.getLastPathSegment();
                Log.d("tagImageName",imageDTO.imageName);

                //push!! 안해주면 객체로 안묶이고 string 으로 낱개가 되니까 주의
                firebaseDatabase.getReference().child("images").push().setValue(imageDTO);
                Toast.makeText(HomeActivity.this, "작성완료", Toast.LENGTH_SHORT).show();;
            }
        });
    }

}