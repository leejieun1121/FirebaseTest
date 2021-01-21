## Firebase 연습 !

---

📌 콘솔에 앱 등록하기 -> google-services.json파일 app 폴더 안에 넣어줘야 함 

📌 구현하려는 기능에 따라 파이어베이스 문서를 참고해서 라이브러리 추가하기

---


## 1.Auth

파이어베이스를 이용한 Google Login, Email Login, Facebook Login 기능 구현 !

콘솔 들어가서 로그인하려는 기능을 전부 사용 설정 해줘야함 

![스크린샷 2021-01-21 오후 6.47.29](/Users/jieun/Library/Application Support/typora-user-images/스크린샷 2021-01-21 오후 6.47.29.png)

**<activity_main.xml>**

~~~
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">


    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/layout1"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="80dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/et_email"
            android:hint="Email"
            android:background="@color/white"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:ems="10"
            android:inputType="textEmailAddress"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintHorizontal_bias="0.497"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

    </com.google.android.material.textfield.TextInputLayout>



    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/layout2"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/layout1">


    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/et_password"
        android:hint="Password"
        android:background="@color/white"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="20dp"
        android:ems="10"
        android:inputType="textPassword"
 />
    </com.google.android.material.textfield.TextInputLayout>

    <Button
        android:id="@+id/btn_signup"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="로그인"
        android:layout_marginTop="40dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/layout2" />

    <com.facebook.login.widget.LoginButton
        android:id="@+id/btn_facebook"
        app:layout_constraintStart_toStartOf="parent"
        android:layout_marginTop="20dp"
        app:layout_constraintTop_toBottomOf="@+id/btn_signup"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>

    <com.google.android.gms.common.SignInButton
        android:id="@+id/button_login"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        android:layout_marginTop="20dp"
        app:layout_constraintTop_toBottomOf="@+id/btn_facebook" />

</androidx.constraintlayout.widget.ConstraintLayout>
~~~

* TextInputLayout과 TextInputEditText

  1) EditText에 포커스가 주어질 때 EditText의 힌트가 TextInputLayout 으로 이동하여 라벨메세지로 표시됨

  2) setCounterEnabled() -> 글자수를 세어 하단에 표시도 해줌 

  3) setError() -> 에러메세지도 밑에 띄워줌

  https://prince-mint.tistory.com/7 

  

### - 구글 로그인

**<MainActivity.java>**

~~~
//필요한 전역 변수들
private static final int RC_SIGN_IN = 10;
private GoogleSignInClient mGoogleSignInClient;
private FirebaseAuth mAuth;
~~~



~~~
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton btnLogin = findViewById(R.id.button_login);
        //구글 로그인 버튼 클릭 -> 만들어진 구글 회원가입 객체와 requestCode를 보냄 
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        mAuth = FirebaseAuth.getInstance();
~~~



~~~
 @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
       
       // 구글 로그인 버튼을 누른 뒤 받아온 결과값이 RC_SIGN_IN 이라면 
       if (requestCode == RC_SIGN_IN) {
       			// 구글로 로그인 한 계정을 가져와서 
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // 토큰을 넘기기 
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }
~~~



~~~
//넘겨받은 토큰으로 구글 아이디 생성 
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                            Log.d("로그인","성공");
                            Toast.makeText(MainActivity.this,"아이디 생성 완료",Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //이미 가입된 유저라면 그냥 로그인 화면으로 넘어가돌
                            if(user != null) {

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                        else {
//                            // If sign in fails, display a message to the user.
//                            Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                            Log.d("로그인","실패");
                            Toast.makeText(MainActivity.this,"아이디 생성 실패",Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
~~~



### - Email Login 

~~~
    private EditText etEmail;
    private EditText etPassword;
~~~



~~~
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        Button btnSignup = findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //EditText에 입력한 아이디 비번 넘겨줌
                createUser(etEmail.getText().toString(),etPassword.getText().toString());
            }
        });
~~~

~~~
 //아이디 비번으로 회원가입
    private void createUser(String email,String password){
    //입력받은 아이디와 비번으로 계정 생성 
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                        	//이미 생성이 되어있는 계정이라면 , create하지 않고 로그인으로 넘어감 ! 
                            // If sign in fails, display a message to the user.
                            loginUser(email,password);
//                            Toast.makeText(MainActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            Log.d("tag",task.getResult().toString());
                        }

                        // ...
                    }
                });
    }
~~~



~~~
private void loginUser(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("tag", "signInWithEmail:success");
//                            updateUI(user);
														//로그인이 성공한다면(파베에 있는 유저라면), 홈액티비티로 넘어감  
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null) {

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("tag", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                            // ...

                        }

                        // ...
                    }
                });
    }
~~~



### - Facebook 로그인 

앞에서 했던 구글 , 아이디 비밀번호 생성 로그인과 다르게 페이스북 로그인은 절차가 조금 더 까다롭다. 

먼저 파베랑 페북을 연동하려면  세 가지 과정이 필요하다. 

1) [Facebook for Developers](https://developers.facebook.com/apps/849073492558707/rate-limit-details/app/) 여기로 가서 로그인을 한 뒤 앱을 등록하고 설정으로 이동한 다음, 유효한 QAuth 리디렉션 URI에  파이어베이스 콘솔-> 로그인 방법의 QAuth 리디렉션 URI 를  복사해서 붙여넣기 한다.

2) 앱 ID, 시크릿 코드를 파이어베이스 리디렉션 URI 복사했던 위에 붙여넣기 한다. 

3) [Facebook for Developers 문서](https://developers.facebook.com/docs/facebook-login/android) 여기로 가서 나머지 단계들을 진행해준다. 



~~~
    private CallbackManager mCallbackManager;
~~~



~~~
 mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.btn_facebook);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
            //사용자가 정상적으로 로그인 할 시 로그인한 사용자의 액세스 토큰을 가져옴 
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                // ...
            }
        });
~~~



~~~
private void handleFacebookAccessToken(AccessToken token) {
        Log.d("tag", "handleFacebookAccessToken:" + token);
				//가져온 페이스북 사용자의 토큰을 Firebase 사용자로 교환하고 인증받음 
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("tag", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //이미 가입된 유저라면 홈화면으로 넘어감 
                            if(user != null) {

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.

                        }

                        // ...
                    }
                });
    }
~~~



~~~
 @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
				
				//콜백을 위한 코드 
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

       // ... 구글 로그인 관련 코드들 
    }
~~~

### - 로그아웃

로그인이 완료되면 HomeActivity 로 넘어간다. 여기서 DrawerLayout 의 로그아웃을 누르면 로그아웃이 진행된다.

**<HomeActivity.java>**

~~~
    private FirebaseAuth auth;
~~~

~~~
navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.nav_logout){
                    auth.signOut();
                    LoginManager.getInstance().logOut();//페이스북 연동 로그아웃 
                    finish();
                    Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                    startActivity(intent);
                } 
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
~~~



![화면-기록-2021-01-21-오후-11 56 25](https://user-images.githubusercontent.com/53978090/105384564-b8f56e80-5c55-11eb-99e9-50fc17e246e9.gif)





---

## 2. Storage

### 갤러리에서 이미지 가져와서 Storage에 업로드 하기

갤러리 접근을 위한 설정

**<AndroidManifest.xml>** 

~~~
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
~~~



**<HomeActivity.java>**

~~~
    private FirebaseStorage storage;
~~~



~~~
 //권한 설정
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }
        
  navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) { 
                int id = item.getItemId();
             
                if(id == R.id.nav_gallery){
                    //ACTION_PICK을 사용하면 선택한 이미지를 가져올 수 있다.
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    //setType -> 인텐트의 MIME 유형 설정, 반환하려는 유형 데이터를 나타냄
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent,GALLERY_CODE);
                }
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

~~~



~~~
@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE) {
            //갤러리에서 선택된 사진 하나의 경로
						//이 경로를 알 수 없기 때문에 꼭 getPath 코드로 변환해줘야함
            imagePath = getPath(data.getData());
         //선택된 사진은 storage에 업로드 됨
        StorageReference storageRef = storage.getReferenceFromUrl("gs://fir-authtest-29e5b.appspot.com/");
        Uri file = Uri.fromFile(new File(imagePath));
        StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);
        }
    }
~~~



~~~
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

~~~

![화면-기록-2021-01-22-오전-12 21 22](https://user-images.githubusercontent.com/53978090/105384613-c7dc2100-5c55-11eb-8bea-6edeb2db5682.gif)

---

## 3. Database

### - 사진, 제목, 글 내용 데이터베이스에 업로드하기

**<content_main.xml>**

~~~
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:foregroundTint="#FFFFFF"
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
    tools:showIn="@layout/app_bar_main">


    <fragment
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:defaultNavHost="true"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:navGraph="@navigation/mobile_navigation" />

    <ImageView
        android:id="@+id/img_content"
        android:layout_width="150dp"
        android:layout_height="150dp"
        android:layout_marginTop="10dp"
        android:layout_marginLeft="10dp"
        android:background="@color/com_facebook_button_background_color"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        tools:layout_editor_absoluteX="46dp"
        tools:layout_editor_absoluteY="43dp" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/textInputLayout"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="10dp"
        app:layout_constraintBottom_toBottomOf="@+id/img_content"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@+id/img_content"
        app:layout_constraintTop_toTopOf="parent">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/et_title"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="#fff"
            android:hint="Title" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="250dp"
        android:layout_marginTop="24dp"
        android:layout_marginHorizontal="10dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.0"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/img_content" >

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/et_description"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:hint="hint"
            android:gravity="top"
            android:inputType="textMultiLine"
            android:background="#fff" />
    </com.google.android.material.textfield.TextInputLayout>


    <Button
        android:id="@+id/btn_upload"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="업로드"
        android:textColor="#fff"
        android:textStyle="bold"
        android:layout_marginHorizontal="10dp"
        app:layout_constraintBottom_toBottomOf="@+id/nav_host_fragment"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
~~~



**<ImageDTO>**

~~~
package com.example.firebaseauthtest;

import java.util.HashMap;
import java.util.Map;

public class ImageDTO {

    public String imageUrl;
    public String title;
    public String description;
    public String uid;
    public String userId;
    
}

~~~



**<HomeActivity.java>**

```
private FirebaseDatabase firebaseDatabase;
private ImageView imgContent;
private EditText etTitle;
private EditText etDescription;
```



~~~
   btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사진,타이틀,본문 쓰고 업로드 버튼 누르기
                upload();
            }
        });
~~~



~~~
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

~~~



### - Recyclerview 이용해서 데이터베이스에 저장된 내용 보여주기 & 좋아요 & 삭제 

**<item_board.xml>**

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    >

    <ImageView
        android:id="@+id/img_content"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_marginTop="5dp"
        android:layout_marginHorizontal="5dp"
        />

    <TextView
        android:id="@+id/tv_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="5dp"
        android:layout_marginHorizontal="5dp"/>

    <TextView
        android:id="@+id/tv_description"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="5dp"
        android:layout_marginTop="5dp"
        />
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="5dp"

        >
        <ImageView
            android:id="@+id/img_like"
            android:background="@drawable/baseline_favorite_border_black_18dp"
            android:layout_width="wrap_content"
            android:layout_marginLeft="5dp"
            android:layout_height="wrap_content"/>

        <TextView
            android:id="@+id/tv_like_count"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_marginLeft="5dp"
            android:textSize="22sp"
            android:text="0"
            />
        <View
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_weight="1"
            />

        <ImageView
            android:id="@+id/img_delete"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="right"
            android:background="@drawable/baseline_delete_black_18dp"
            />
    </LinearLayout>


</LinearLayout>
```



**<activity_board.xml>**

~~~
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".BoardActivity">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_board"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
~~~



**<BoardActivity.java>**

~~~
    private RecyclerView recyclerView; //게시글을 보여주기 위한 레이아웃 
    //키
    private List<ImageDTO> list = new ArrayList<>();
    //value
    private List<String> uidLists = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase; 
    private FirebaseAuth auth;
    private FirebaseStorage storage;
~~~

~~~
 recyclerView = findViewById(R.id.rv_board); //리싸이클러뷰에 어댑터 등록 
        BoardRecyclerViewAdapter boardRecyclerViewAdapter = new BoardRecyclerViewAdapter();
        recyclerView.setAdapter(boardRecyclerViewAdapter);
~~~

### 리싸이클러뷰 어댑터

~~~
class BoardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //밑에 만들어둔 뷰홀더에서 연결해준 아이템들에다가 데이터를 set 
            ((CustomViewHolder) holder).tvTitle.setText(list.get(position).title);
            ((CustomViewHolder) holder).tvDescription.setText(list.get(position).description);
            Glide.with(holder.itemView.getContext()).load(list.get(position).imageUrl)
                    .into(((CustomViewHolder) holder).imgContent);
            ((CustomViewHolder) holder).tvLikeCount.setText(list.get(position).likeCount + "");

            ((CustomViewHolder) holder).imgLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //게시물하나당 키를 가지고 있음 타고 들어가서 해당 게시물을 알려줌 -> 좋아요 클릭 함수 발생
                    onLikeClicked(firebaseDatabase.getReference().child("images").child(uidLists.get(position)));
                }
            });

            //해시맵의 유저 아이디가 존재한다면 좋아요 누른거니까 채워진 하트
            if (list.get(position).imgLike.containsKey(auth.getCurrentUser().getUid())) {
                Glide.with(holder.itemView).load(R.drawable.baseline_favorite_black_18dp)
                        .into(((CustomViewHolder) holder).imgLike);
            } else {
                Glide.with(holder.itemView).load(R.drawable.baseline_favorite_border_black_18dp)
                        .into(((CustomViewHolder) holder).imgLike);
            }

            ((CustomViewHolder) holder).imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteContent(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


				//좋아요 기능 
        private void onLikeClicked(DatabaseReference postRef) {
            //트랜잭션 -> 동시 수정 데이터 다루는 경우 (즐겨찾기,좋아요) 변화 겹치지 않도록
            postRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    ImageDTO imgeDTO = mutableData.getValue(ImageDTO.class);
                    if (imgeDTO == null) {
                        return Transaction.success(mutableData);
                    }
                    //imgLike해시맵 -> 해당 게시물에 좋아요 누르면 좋아요 누른 유저의 아이디가 저장됨
                    //해시맵의 해당 유저 아이디가 존재한 상태에서 클릭했을때 -> 좋아요 해지, count-1
                    if (imgeDTO.imgLike.containsKey(auth.getCurrentUser().getUid())) {
                        // Unstar the post and remove self from stars
                        imgeDTO.likeCount = imgeDTO.likeCount - 1;
                        imgeDTO.imgLike.remove(auth.getCurrentUser().getUid());
                    } else {
                        // Star the post and add self to stars
                        imgeDTO.likeCount = imgeDTO.likeCount + 1;
                        imgeDTO.imgLike.put(auth.getCurrentUser().getUid(), true);
                    }

                    // Set value and report transaction success
                    mutableData.setValue(imgeDTO);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed,
                                       DataSnapshot currentData) {
                    // Transaction completed
                    Log.d("tag", "postTransaction:onComplete:" + databaseError);
                }
            });
        }

				//게시물 삭제 
        private void deleteContent(int position) {
        
				//선택된 아이템의 position 을 얻어와서 데이터베이스의 images를 순회하다 해당 포지션의 게시글을 삭제             storage.getReference().child("images").child(list.get(position).imageName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(BoardActivity.this,"이미지 삭제 완료",Toast.LENGTH_SHORT).show();
                    //            firebaseDatabase.getReference().child("images").child("디비 키 값").setValue(null);
                    //콜백방식 -> 이미지 삭제와 게시물 삭제를 따로 두면 이미지 삭제는 되고 게시물 삭제는 안되는 경우 즉, 꼬일 수 있음
                    //이미지 삭제 성공하면 게시물 삭제 되도록
                    firebaseDatabase.getReference().child("images").child(uidLists.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BoardActivity.this, "게시글 삭제 완료", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BoardActivity.this, "게시글 삭제 실패", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener(){

                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(BoardActivity.this,"이미지 삭제 실패",Toast.LENGTH_SHORT).show();

                }
            });


        }

    }

~~~



### 리싸이클러 뷰홀더

~~~
private class CustomViewHolder extends RecyclerView.ViewHolder {
//item_board 에 만들어뒀던 아이템들을 하나씩 불러와서 찾아줌 
        ImageView imgContent;
        TextView tvTitle;
        TextView tvDescription;
        ImageView imgLike;
        TextView tvLikeCount;
        ImageView imgDelete;

        public CustomViewHolder(View view) {
            super(view);
            imgContent = view.findViewById(R.id.img_content);
            tvTitle = view.findViewById(R.id.tv_title);
            tvDescription = view.findViewById(R.id.tv_description);
            imgLike = view.findViewById(R.id.img_like);
            tvLikeCount = view.findViewById(R.id.tv_like_count);
            imgDelete = view.findViewById(R.id.img_delete);
        }
    }
~~~

![화면-기록-2021-01-22-오전-12 34 41](https://user-images.githubusercontent.com/53978090/105384626-cf032f00-5c55-11eb-8432-0420d5323530.gif)

![화면-기록-2021-01-22-오전-12 56 38](https://user-images.githubusercontent.com/53978090/105384642-d3c7e300-5c55-11eb-9e52-fb467b760a11.gif)



---



## 4. Crash Reporting

앱이 터지면 그걸 테스트한 기종과 비정상 종료된 이유등을 알려주는 좋은 기능 ! 

앱을 출시 할 때 코드 보안을 위해 Proguard 를 사용하면 난독화 때문에 해석하기 힘들다. 

https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?hl=ko&platform=android 

---

## 5. Remote Config

Remote Config 기능은 처음 알았다. 

안드로이드 스튜디오에서 직접 코드를 수정하지 않아도, 바꿀 파라미터를 파이어베이스에 설정해 놓고 파이어베이스에서 값을 바꾸면 바로 앱에 반영되는 신기한 기능이다 !

xml 폴더 밑에 파이어베이스에서 바꿀 값들을 적어준다.

**<remote_config_defaults.xml>**

~~~
<?xml version="1.0" encoding="utf-8"?>
<!-- START xml_defaults -->
<defaultsMap>
    <entry>
        <key>toolBarColor</key>
        <value>#000000</value>
    </entry>
    <entry>
        <key>welcome_message_caps</key>
        <value>false</value>
    </entry>
    <entry>
        <key>welcome_message</key>
        <value>Welcome to my awesome app!</value>
    </entry>
</defaultsMap>
    <!-- END xml_defaults -->
~~~



그 다음 remoteConfig 를 호출하면 파이어베이스에서 값을 바꿀 때 바로바로 적용 된다.

얼만큼의 시간마다 요청할지도 정할 수 있고 remote_config_defaults 의 값을 기본값으로 참조하고있다. 

**<HomeActivity.java>**

~~~
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
~~~



~~~
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
~~~

![화면-기록-2021-01-22-오전-1 11 38](https://user-images.githubusercontent.com/53978090/105384646-d591a680-5c55-11eb-82a8-ff514c2b6f07.gif)

---

## 6. DynamicLinks 

앱 설치를 유도할 때 이 기능을 사용해서 바로 설치 화면으로 이동시켜줄 수 있다!!

Firebase 에서 먼저 URL 프리픽스를 생성한다. 

![스크린샷 2021-01-22 오전 1 23 00](https://user-images.githubusercontent.com/53978090/105384793-0245be00-5c56-11eb-8db9-712af853e29d.png)



그리고 만들어둔 URL프리픽스를 가지고, 이동할 화면의 링크를 setLink를 통해 적어준다.

딥링크(다이나믹 링크)는 원래 주소의 길이가 긴데, 짧게도 만들 수 있다.

**<MainActivity.java>**

~~~
void createDynamicLink(){
        //에뮬이 아닌 핸드폰에서 실행하면 플레이스토어로 넘어감
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("http://date.jsontest.com"))
                .setDomainUriPrefix("https://jieun1121.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("date.jsontest.com").build())
                // Open links with com.example.ios on iOS
//                .setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        Log.d("tag_longLink",dynamicLinkUri.toString());

        //생성된 링크가 너무 길어서 짧게 만들어줌
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("http://date.jsontest.com"))
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
                            Log.d("tag_shortLink",shortLink.toString());

                            Uri flowchartLink = task.getResult().getPreviewLink();
                        } else {
                            // Error
                            // ...
                        }
                    }
                });
    }
~~~



~~~
private void receiveDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        //그냥 앱으로 실행시켰을 때
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            getTime(deepLink.toString());
                            Log.d("tag_receiveLink", deepLink.toString());
                        }else{
                            //링크 타고 들어왔을 때
                            Log.d("tag_ReceiveLink", "No have DynamicLink");

                        }

                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("tag", "getDynamicLink:onFailure", e);
                    }
                });
    }
~~~



~~~
 void getTime(String url){
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("tag_time",e.getLocalizedMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            Log.d("tag_time",jsonObject.toString());
                            tvTime.setText(jsonObject.getString("time"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

~~~



앱을 실행시키면 다음과 같이 긴 딥링크, 짧은 딥링크가 하나씩 생성된다.

<img width="1191" alt="스크린샷 2021-01-22 오전 1 25 13" src="https://user-images.githubusercontent.com/53978090/105384829-096ccc00-5c56-11eb-8c3c-dc2a7a2f1404.png">

생성된 동적 링크를 복사해서 앱으로 들어가면 No have DynamicLink라고 떴던 ReceiveLink에 setLink로 적어뒀던 링크의 주소가 나타나면서 해당 링크의 시간을 가져와 홈화면에 띄워준다. 

<img width="1180" alt="스크린샷 2021-01-22 오전 1 27 26" src="https://user-images.githubusercontent.com/53978090/105384847-0d005300-5c56-11eb-8f86-ba920339bdc0.png">

![스크린샷 2021-01-22 오전 1 28 55](https://user-images.githubusercontent.com/53978090/105384874-138eca80-5c56-11eb-9b31-ca5c5dcb5bf8.png)



![스크린샷 2021-01-22 오전 1 29 31](https://user-images.githubusercontent.com/53978090/105384896-1a1d4200-5c56-11eb-894e-ea62b10016df.png)

---

## 7. Google AdMop

앱에 들어 갔을 때, 특히 모바일 게임을 할 때 뜨는 광고를 많이 보았을 것이다.

수익을 창출하기 위해 구글의 다양한 AdMop을 사용하게 된다.

 📌 AdMop 에 가입하고 <AndroidMenifest.xml>에  AdMob 아이디를 메타데이터로 추가해줘야한다.

 📌 앱스토어에 등록해서 진짜 수익을 창출 할것이 아니라면 문서에 적혀있는 테스트용 아이디를 사용하자.

**<activity_ad.xml>**

~~~
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".AdActivity">

    <Button
        android:id="@+id/btn_interstitial"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="100dp"
        android:text="interstitial"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.498"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintBottom_toTopOf="@id/adView" />

    <Button
        android:id="@+id/btn_rewardedAds"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="rewardedAds"
        android:layout_marginBottom="20dp"
        app:layout_constraintBottom_toTopOf="@+id/btn_interstitial"
        app:layout_constraintEnd_toEndOf="@+id/btn_interstitial"
        app:layout_constraintStart_toStartOf="@+id/btn_interstitial" />

    <com.google.android.ads.nativetemplates.TemplateView
        android:id="@+id/adView2"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout="@layout/gnt_small_template_view"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.0"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.gms.ads.AdView
            android:id="@+id/adView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentBottom="true"
            android:layout_centerHorizontal="true"
            app:adSize="BANNER"
            app:adUnitId="ca-app-pub-3940256099942544/6300978111"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent" />
    </androidx.constraintlayout.widget.ConstraintLayout>
~~~

### 

**<AdActivity.java>**

~~~
  private InterstitialAd mInterstitialAd;
    private Button btnInterstitial;
    private AdView mAdView;
    private Button btnRewardedAds;
    private RewardedAd rewardedAd;
~~~



### - Banner

: 앱 하단이나 레이아웃에 일자 형식으로 뜨는 광고 

~~~
 void createBanner(){
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-4638376201794325~4734437848");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
}
~~~

![스크린샷 2021-01-22 오전 1 49 46](https://user-images.githubusercontent.com/53978090/105384927-1f7a8c80-5c56-11eb-9673-091958e7243a.png)

### - Interstitial

: 앱의 화면을 전부 덮는 전체 화면 광고 

~~~
 private void createInterstitial(){
        btnInterstitial = findViewById(R.id.btn_interstitial);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
}
~~~

![스크린샷 2021-01-22 오전 1 50 06](https://user-images.githubusercontent.com/53978090/105384950-24d7d700-5c56-11eb-9eec-b0b89eee704a.png)

### - Native ads

: 여러 사이즈로 나타날 수 있는 광고 

📌 [구글 제공 템플릿](https://github.com/googleads/googleads-mobile-android-native-templates) 여기서 템플릿 다운로드 해야함 !

~~~
 void createNativeAd(){
        AdLoader.Builder builder = new AdLoader.Builder(
                this, "ca-app-pub-3940256099942544/2247696110");

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                TemplateView template = findViewById(R.id.adView2);
                template.setNativeAd(unifiedNativeAd);
            }
        });

        AdLoader adLoader = builder.build();
        adLoader.loadAd(new AdRequest.Builder().build());

    }
~~~

![스크린샷 2021-01-22 오전 1 50 42](https://user-images.githubusercontent.com/53978090/105384966-299c8b00-5c56-11eb-920e-b1cae2419b51.png)

### - Rewarded ads

: 동영상이 뜨는 광고로, 동영상을 정해진 시간동안 보면 보상을 받는 광고다. 

~~~
void createRewardedAds(){
        btnRewardedAds = findViewById(R.id.btn_rewardedAds);
        rewardedAd = new RewardedAd(this, "ca-app-pub-3940256099942544/5224354917");

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
  }
~~~



![스크린샷 2021-01-22 오전 1 52 15](https://user-images.githubusercontent.com/53978090/105384981-2e613f00-5c56-11eb-93b7-69502b54f55c.png)



이렇게 생성해준 광고에 클릭 리스너를 달아서 광고 클릭 할 때, 닫을 때 등 원하는 기능이나 모션을 추가할 수 있다.

![스크린샷 2021-01-22 오전 1 51 35](https://user-images.githubusercontent.com/53978090/105384998-3325f300-5c56-11eb-9c0e-a447c6b746f0.png)



---



**참고**

인프런 강의 : https://www.inflearn.com/course/firebase-android/dashboard
파이어베이스 콘솔:  https://firebase.google.com/docs/auth/android/start
파이어베이스 문서 :  https://firebase.google.com/docs/storage/android/start?authuser=0

구글 애드몹 : https://developers.google.com/admob/android/native/start

딥링크: https://gun0912.tistory.com/78

​			https://zetal.tistory.com/entry/Firebase-Dynamic-Link-%EC%83%9D%EC%84%B1-%EB%B0%A9%EB%B2%95

adMob : https://flowarc.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%95%B1%EC%97%90-%EA%B4%91%EA%B3%A0Admob-%EB%84%A3%EB%8A%94-%EB%B0%A9%EB%B2%95

​			https://superwony.tistory.com/100
