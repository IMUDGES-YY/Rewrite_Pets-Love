package com.imudges.yy.Pets_Love;

import Configs.Config;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import beans.User;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import g.V;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yangyang on 2017/1/14.
 */
public class SignInAcitivity extends Activity implements TextWatcher{
    public EditText et_sign_in_email = null;
    public EditText et_sign_in_password = null;
    public EditText et_sign_in_nick_name;
    public RadioGroup sex;
    public RadioButton male;
    public RadioButton female;
    public String userSex;
    public HashMap<String ,String > userMessage = new HashMap<String,String>();
    public Button btn_submit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        initData();
        this.sex.setOnCheckedChangeListener(new OnCheckedChangeListenerImp());
        /**
         * 连接Bmob,上传数据
         * */
        Bmob.initialize(this,Config.BmobId);

        //第二：自v3.4.7版本开始,设置BmobConfig,允许设置请求超时时间、文件分片上传时每片的大小、文件的过期时间(单位为秒)，
        //BmobConfig config =new BmobConfig.Builder(this)
        ////设置appkey
        //.setApplicationId("Your Application ID")
        ////请求超时时间（单位为秒）：默认15s
        //.setConnectTimeout(30)
        ////文件分片上传时每片的大小（单位字节），默认512*1024
        //.setUploadBlockSize(1024*1024)
        ////文件的过期时间(单位为秒)：默认1800s
        //.setFileExpiration(2500)
        //.build();
        //Bmob.initialize(config);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_sign_in_email.addTextChangedListener(SignInAcitivity.this);
                /**
                 * 需要检查email和password是否为空，但是缺少确定密码项
                 * */
                if(TextUtils.isEmpty(et_sign_in_email.getText())||TextUtils.isEmpty(et_sign_in_password.getText())){
                    Toast.makeText(SignInAcitivity.this,"请填写您的邮箱或密码",Toast.LENGTH_SHORT).show();
                }
                else{
                    /**
                     * 检查邮箱是否合法
                     * */
                    if(CheckEmail(et_sign_in_email.getText().toString())){
                        CheckAndAddUserMessage();
                    }
                    else{
                        Toast.makeText(SignInAcitivity.this,"您的邮箱格式有误",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    public void initData(){
        btn_submit = (Button) findViewById(R.id.btn_submit_signin);
        et_sign_in_email = (EditText) findViewById(R.id.et_sign_in_email);
        et_sign_in_password = (EditText) findViewById(R.id.et_sign_in_password);
        et_sign_in_nick_name = (EditText) findViewById(R.id.et_sign_in_nick_name);
        sex = (RadioGroup) findViewById(R.id.sex);
        male = (RadioButton) findViewById(R.id.male);
        female =(RadioButton) findViewById(R.id.female);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private class OnCheckedChangeListenerImp implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if(male.getId()==i){
                userSex = "male";
            }
            else{
                userSex = "female";
            }
        }
    }

    private void CheckAndAddUserMessage(){
        Log.v("test",et_sign_in_email.getText().toString());
        BmobQuery<User> bmobQuery = new BmobQuery<User>();
        bmobQuery.addWhereEqualTo("userEmail",et_sign_in_email.getText().toString());
        bmobQuery.setLimit(1);
        bmobQuery.findObjects(this, new FindListener<User>() {
            @Override
            public void onSuccess(List<User> list) {
                /**
                 * 此处有BUG，未知BUG，如果去掉下一条语句，总会查找成功
                 * */
                Log.v("查找成功",list.get(0).getUserEmail());
                Toast.makeText(SignInAcitivity.this,"您的邮箱已被注册",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int i, String s) {
                Log.v("查找失败",s);
                User user = new User(et_sign_in_email.getText().toString(),et_sign_in_password.getText().toString(),et_sign_in_nick_name.getText().toString(),userSex);
                user.save(SignInAcitivity.this,new SaveListener(){
                    @Override
                    public void onSuccess() {
                        Log.v("1","添加成功");
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.v("2","添加失败");
                    }
                });
                userMessage.put("userEmail",et_sign_in_email.getText().toString());
                userMessage.put("password",et_sign_in_password.getText().toString());
                /**
                 * 返回数据至MainActivity，自动填写刚才注册的信息
                 * */
                Bundle bundle = new Bundle();
                bundle.putSerializable("user",userMessage);
                Intent data = new Intent();
                data.putExtras(bundle);//"key=>value"对
                setResult(2,data);
                finish();//关闭Activity
            }
        });
    }
    public static boolean CheckEmail(String email){
        boolean flag = false;
        try{
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }
}


