package com.example.protectyoureyes.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.protectyoureyes.MyActivity;
import com.example.protectyoureyes.R;
import com.example.protectyoureyes.My_View.TitleLayout;
import com.example.protectyoureyes.bean.GlobalData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;


public class Activity_inform extends MyActivity {
    private EditText et_inform_title;
    private EditText et_inform_content;
    private Button bt_inform_confirm;
    private SharedPreferences.Editor editor;

    //设置图片（拍照和从相册选取）
    public static final int CUT_PICTURE = 1;
    public static final int SET_PICTURE = 2;
    private Button takePhoto;
    private Button chooseFromAlbum;
    //判断是否是从相册选取图片
    private boolean IschooseFromAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inform_control);
        initToolbar(R.id.tb_inform);
        InitView();
    }

    @Override
    protected void initToolbar(int id) {
        super.initToolbar(id);
        toolbar.setTitle(R.string.tb_inform);
    }



    private void InitView() {
        et_inform_title = (EditText) findViewById(R.id.et_inform_title);
        et_inform_content = (EditText) findViewById(R.id.et_inform_content);
        bt_inform_confirm = (Button) findViewById(R.id.bt_inform_confirm);
        editor = getSharedPreferences("GlobalData", MODE_PRIVATE).edit();
        takePhoto = (Button) findViewById(R.id.takePhoto);
        chooseFromAlbum = (Button) findViewById(R.id.chooseFromAlbum);

        et_inform_title.setText(GlobalData.inform_title);
        et_inform_content.setText(GlobalData.inform_content);

        //点击确认按钮后，把提示标题和内容都用sharedpreference存储，并且把他们复制给全局变量
        bt_inform_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = et_inform_title.getText().toString();
                String content = et_inform_content.getText().toString();

                GlobalData.inform_title = title;
                GlobalData.inform_content = content;
                editor.putString("inform_title", title);
                editor.putString("inform_content", content);
                editor.commit();
                Toast.makeText(Activity_inform.this, "设置成功！", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //设置提示图片
        setInformImage();
    }

    private void setInformImage() {
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IschooseFromAlbum = false;
                //创建File对象，用于存储拍照后的图片
                //将此图片存储于SD卡的根目录下
                File outputImage = new File(Environment.getExternalStorageDirectory()+File.separator+"ProtectYourEyes",
                        "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //将File对象转换成Uri对象
                //Uri表标识着图片的地址
                GlobalData.imageUri = Uri.fromFile(outputImage);
                //隐式调用照相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                //拍下的照片会被输出到output_image.jpg中去
                intent.putExtra(MediaStore.EXTRA_OUTPUT, GlobalData.imageUri);
                //此处是使用的startActivityForResult（）
                //因此在拍照完后悔有结果返回到onActivityResult（）中
                //onActivityResult（）中主要是实现图片裁剪
                startActivityForResult(intent, CUT_PICTURE);
            }
        });

        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IschooseFromAlbum = true;
                File outputImage = new File(Environment.getExternalStorageDirectory()+File.separator+"ProtectYourEyes",
                        "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                GlobalData.imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                //此处调用了图片选择器
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, GlobalData.imageUri);
                Toast.makeText(Activity_inform.this, "000", Toast.LENGTH_SHORT).show();
                startActivityForResult(intent, CUT_PICTURE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CUT_PICTURE:
                if (resultCode == RESULT_OK) {

                    //此处启动裁剪程序(两者在setDataAndType中的data不同)
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    //如果是从相册选取的，那么在setDataAndType中就不能写绝对路径，详情可以看读者的相关博客
                    //相关博客地址：http://blog.csdn.net/double2hao/article/details/49515383
                    if (IschooseFromAlbum) {
                        intent.setDataAndType(data.getData(), "image/*");
                    } else {
                        intent.setDataAndType(GlobalData.imageUri, "image/*");
                    }
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, GlobalData.imageUri);
                    startActivityForResult(intent, SET_PICTURE);
                }
                break;

            case SET_PICTURE:
                //将图片设置到全局变量中
                try {
                    GlobalData.inform_bitmap = BitmapFactory.decodeStream(getContentResolver()
                            .openInputStream(GlobalData.imageUri));
                    Toast.makeText(Activity_inform.this, "图片设置成功", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

}
