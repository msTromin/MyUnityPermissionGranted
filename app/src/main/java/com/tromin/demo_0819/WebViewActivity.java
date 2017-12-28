package com.tromin.demo_0819;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by User
 */

public class WebViewActivity extends Activity
{
    public static final int NONE = 0;
    protected Context mContext = null;
    //debug
    protected ImageView imageView = null;//debug display
    protected final boolean isDebugView = false ;//debug imageView
    protected final boolean isDebugMode = true ;//debug tag
    protected String sTAG = "Tromin.TAG" ;//Debug tag name
    //onActivityResult code
    public static final int REQUEST_CODE_TAKE_PHOTO = 1 ;// 拍照
    public static final int REQUEST_CODE_PICK_IMAGE = 2;//相冊
    public static final int REQUEST_CODE_CROP_IMAGE = 3;//裁剪
    private static final int REQUEST_CODE_PERMISSION = 272 ;//相機申請權限
    private static final int REQUEST_CODE_STORAGE_PERMISSIONS = 273 ;//本地存儲權限
    //define filename
    public final static String DEFAULT_IMAGE_TEMP_FILE = "temp.jpg" ;//原始照相圖檔
    public final static String DEFAULT_IMAGE_FILE_NAME = "image.png";//經裁剪後圖檔(拍照或相冊)
    protected String szImageCropFilename = "" ;
    //
    protected Uri imageUri ;//拍照Uri
    protected Uri outputUri;//定義為全局的Uri變量，因為運行時發現裁剪過後顯示的還是未裁剪的圖片，查看相冊時發現有新建的文件夾存儲裁剪後的圖片，所以應該把裁剪後的Uri存下來。下面onActivityResult函數里需要將獲取到的uri轉化為絕對路徑傳給untiy

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        mContext = this;

        String type = this.getIntent().getStringExtra("type");

        //在這裡判斷是打開本地相冊還是直接照相
        if(type.equals("takePhoto"))
        {
            //在android 6.0以後，則需要申請權限，先來調用相機拍照。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//檢查權限
            {
                final String permission = Manifest.permission.CAMERA;  //相機權限
                final String permission1 = Manifest.permission.WRITE_EXTERNAL_STORAGE; //寫入數據權限
                //先判斷是否被賦予權限，沒有則申請權限
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, permission1) != PackageManager.PERMISSION_GRANTED)
                {
                    //給出權限申請說明
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                    {
                        ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
                    }
                    else//直接申請權限
                    {
                        //申請權限，可同時申請多個權限，並根據用戶是否賦予權限進行判斷
                        ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
                    }
                }
                else
                {
                    //賦予過權限，則直接調用相機拍照
                    startTakePhotoActivity();
                }
            }
            else
            {
                startTakePhotoActivity();
            }
        }
        else if(type.equals("openAlbum"))//相冊
        {
            //在android 6.0以後，則需要申請權限，先來調用相機拍照。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//檢查權限
            {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSIONS);
                }
                else
                {
                    startPickImageActivity();
                }
            }
            else
            {
                startPickImageActivity();
            }
        }
        else
        {
            if (isDebugMode) Log.i(sTAG,"onActivityStart=error");
        }
    }
    //拍照
    public void startTakePhotoActivity()
    {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//設置Action為拍照
        String mFilePath = Environment.getExternalStorageDirectory().getPath() + "/" + DEFAULT_IMAGE_TEMP_FILE;// 指定路径
        File file = new File(mFilePath);
        if (isDebugMode) Log.i(sTAG,"onActivityStart=1=ACTION_IMAGE_CAPTURE");

        boolean isVersion2 = true ;//test
        if (isVersion2)
        {
            //針對Android7.0，需要通過FileProvider封裝過的路徑，提供給外部調用
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //表示對目標應用臨時授權該Uri所代表的文件
                //通過FileProvider創建一個content類型的Uri，進行封裝
                imageUri = FileProvider.getUriForFile(WebViewActivity.this, "com.tromin.demo_0819.fileprovider", file);
            } else
            {
                //7.0以下，如果直接拿到相機返回的intent值，拿到的則是拍照的原圖大小，很容易發生OOM，所以我們同樣將返回的地址，保存到指定路徑，返回到Activity時，去指定路徑獲取，壓縮圖片
                imageUri = Uri.fromFile(file);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//將拍取的照片保存到指定URI，不保留在相冊中
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);//啟動拍照
        }
/*      //4.4以下
          Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
          intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), DEFAULT_IMAGE_FILE_NAME)));
          startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
*/
/*
        // 指定拍攝照片的存儲路徑
        File file = new File(Environment.getExternalStorageDirectory(), DEFAULT_IMAGE_FILE_NAME);
        Uri imageUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 1）將拍攝的照片保存到 imageUri中，如果此處設置了extra_ouput路徑，回調的 data就是null，文件路徑對應為Uri；
        // 2）如果沒有設置，data不為null，但是有時從data.getData()取得的uri就為null，此時就需要從 bundle中取出bitmap
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
*/
        //第2種版本 似乎可行?
        if (isVersion2 == false)
        {
            Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (file != null)
            {
                if (Build.VERSION.SDK_INT >= 24)
                {
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, mFilePath);
                    Uri uri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent2, REQUEST_CODE_TAKE_PHOTO);
                }
                else
                {
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(intent2, REQUEST_CODE_TAKE_PHOTO);
                }
            }
            else
                Toast.makeText(this, "拍照儲存失敗，没有文件", Toast.LENGTH_SHORT).show();
        }
    }
    //相冊
    public void startPickImageActivity()
    {
        if (isDebugMode) Log.i(sTAG,"onActivityStart=2=ACTION_PICK");
        //打開相冊
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
/*
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
*/
/*
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
*/
    }
    //裁剪
    public void startCropImageActivity(Uri uri)
    {
        if (isDebugMode) Log.i(sTAG,"onActivityStart=3=com.android.camera.action.CROP");
        //
        Intent intent = new Intent("com.android.camera.action.CROP");
        File file = new File(Environment.getExternalStorageDirectory().getPath()+ "/" + DEFAULT_IMAGE_FILE_NAME);
        Uri photoURI = FileProvider.getUriForFile(WebViewActivity.this, "com.tromin.demo_0819.fileprovider", file);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)//android 7.0
        {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");//剪裁原圖的Uri
        intent.putExtra("crop", "true");
        // aspectX aspectY 是寬高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪圖片寬高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);

        outputUri = photoURI.fromFile(file);//縮略圖保存地址
        intent.putExtra("return-data", false);//將剪切的圖片保存到目標Uri中
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);//剪裁後的圖片的Uri
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);

        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }
    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (isDebugMode) Log.i(sTAG,"onActivityResult=4=resultCode="+resultCode);
        if (resultCode == NONE) return;

        if (isDebugMode) Log.i(sTAG,"onActivityResult=5=requestCode="+requestCode);
        switch (requestCode)
        {
            case REQUEST_CODE_TAKE_PHOTO:// 拍照
                if (isDebugMode) Log.i(sTAG,"onActivityResult=6=imageUri="+imageUri) ;
                //設置文件保存路徑這裡放在跟目錄下
                startCropImageActivity(imageUri);
                break;

            case REQUEST_CODE_PICK_IMAGE://相冊
                if (data == null) break;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    File file = new File(Environment.getExternalStorageDirectory().getPath()+ "/" + DEFAULT_IMAGE_FILE_NAME);
                    Uri newUri = FileProvider.getUriForFile(this, "com.tromin.demo_0819.fileprovider", file);
                    if (isDebugMode) Log.i(sTAG,"onActivityResult=7.1="+newUri);
                    startCropImageActivity(newUri) ;
                }
                else
                {
                    Uri dataUri = data.getData();
                    if (isDebugMode) Log.i(sTAG,"onActivityResult=7="+dataUri);
                    startCropImageActivity(dataUri);
                }
                break;
            case REQUEST_CODE_CROP_IMAGE:// 處理裁剪結果
                String imagePath = outputUri.getPath();
                if (isDebugMode) Log.i(sTAG,"onActivityResult=8=CropDoneImage="+imagePath);
/*
                                 Bundle extras = data.getExtras();
                                 if (extras != null)
                                {
                                 Bitmap photo = extras.getParcelable("data");
                                 //將裁剪獲取到的outputuri賦值給imageUri，並將Uri轉化為絕對路徑

                                try
                                {
                                //將imagePath傳給unity
                                // UnityPlayer.UnitySendMessage("AndroidAlbumCamera", "GetImagePath", imagePath);

                                imageView.setImageBitmap(photo);
                                SaveBitmap(photo);
                                }
                                catch (IOException e)
                                {
                                 e.printStackTrace();
                                }
//                      }
*/
                Bitmap bitmap = getBitmapFromUri(outputUri, this);
                if (bitmap != null)
                {
                    if (isDebugView) imageView.setImageBitmap(bitmap);
                    try
                    {
                        SaveBitmap(bitmap) ;
                        if (isDebugMode) Log.i(sTAG,"onActivityResult=9="+szImageCropFilename);
                        //將imagePath傳給unity
                        UnityPlayer.UnitySendMessage("Canvas","getMessage",szImageCropFilename);
//                                              if (isDebugMode == false) finish() ;
                        finish() ;
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                break ;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE_PERMISSION == requestCode) //申請照相權限的返回值
        {
            switch (grantResults[0])
            {
                case PackageManager.PERMISSION_DENIED://未賦予權限，則做出對應提示
                    boolean isSecondRequest = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
                    if (isSecondRequest)
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION);
                    else
                    {
                        Toast.makeText(this, "拍照權限被禁用，請在權限管理修改", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case PackageManager.PERMISSION_GRANTED: //如果用戶賦予權限，則調用相機
                    startTakePhotoActivity();
                    break;
            }
        }
        //
        if (REQUEST_CODE_STORAGE_PERMISSIONS == requestCode)//調用系統相冊申請Sdcard權限回調
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startPickImageActivity();
            }
            else
            {
                Toast.makeText(this, "權限被禁用，請在權限管理修改", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    //
   /*

      // 4.4及以上系統使用這個方法處理圖片 相冊圖片返回的不再是真實的Uri,而是分裝過的Uri
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document類型的Uri，則通過document id處理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出數字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content類型的Uri，則使用普通方式處理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file類型的Uri，直接獲取圖片路徑即可
            imagePath = uri.getPath();
        }
        cropPhoto(uri);
    }
    */
    //
    public Bitmap getBitmapFromUri(Uri uri, Context mContext)
    {
        try
        {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            return bitmap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    //
    public void SaveBitmap(Bitmap bitmap) throws IOException
    {
        FileOutputStream fOut = null;
        //Unity:Application.persistentDataPath => android: /data/data/Name/files
        //請大家一定要注意這個路徑的寫法， 前面一定要加 「File://」 不然無法讀取。
        String sPath = mContext.getFilesDir().getPath() ;//String sPath = "/mnt/sdcard/Android/data/" + sPackageName + "/files";
        szImageCropFilename = java.util.UUID.randomUUID().toString()+".png";
        try
        {
            //查看這個路徑是否存在，如果並沒有這個路徑，創建這個路徑
            File destDir = new File(sPath);
            if (!destDir.exists())
            {
                destDir.mkdirs();
                if (isDebugMode) Log.i(sTAG,"onActivityResult=A=SaveBitmap="+sPath);
            }
            fOut = new FileOutputStream(sPath + "/" + szImageCropFilename) ;
            if (isDebugMode) Log.i(sTAG,"onActivityResult=B=SaveBitmap="+sPath+"/" + szImageCropFilename);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        //將Bitmap對像寫入本地路徑中，Unity在去相同的路徑來讀取這個文件
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try
        {
            fOut.flush();
            if (isDebugMode) Log.i(sTAG,"onActivityResult=C=done");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            fOut.close();
            if (isDebugMode) Log.i(sTAG,"onActivityResult=D=file close");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
