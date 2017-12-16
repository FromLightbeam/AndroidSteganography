package com.hard.light.eight5;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    ImageView imageView;
    TextView textView;
    String secret_message = "Vituh Petuh";

    Bitmap bmp;
    int x = 0;
    int y = 0;
    int width;
    int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_kek);
        textView = findViewById(R.id.text);
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//
//        tv.setText(stringFromJNI());


    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public Bitmap getBitmap(){
        int id = R.drawable.bird;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), id);
        Bitmap mutBmp=bmp.copy(Bitmap.Config.ARGB_8888, true);
        return mutBmp;
    }


    private int encrypt_part_byte_in_color(int color, byte b){
            // var & mask => two last digit == zero
        int mask = ~3;

        int alpha = (color) & 0b11111111;
        int red = (color >> 8) & 0b11111111;
        int green = (color >> 16) & 0b11111111;
        int blue = (color >> 24) & 0b11111111;
        blue = blue & mask | b;

        return  ((color) & mask) | b;
    }

    private void encrypt_byte_in_bitmap(byte b){
        byte[] parts = {0, 0, 0, 0};
        parts[0] = (byte) (b & 0b00000011);
        parts[1] = (byte) ((b >> 2) & 0b00000011);
        parts[2] = (byte) ((b >> 4) & 0b00000011);
        parts[3] = (byte) ((b >> 6) & 0b00000011);
        int color;
        int newcolor;
        for (int j = 0; j < parts.length; j++){
            color = bmp.getPixel(x, y);
            newcolor = encrypt_part_byte_in_color(color, parts[j]);
            bmp.setPixel(x, y, newcolor);

            if (x < width){
                x++;
            }
            else if(y < height){
                x = 0;
                y++;
            }
            else{
                throw new NullPointerException();
            }
        }
    }

    public void encrypt(View view) {
        bmp = getBitmap();
        byte[] bytes = secret_message.getBytes();

        width = bmp.getWidth();
        height = bmp.getHeight();
        encrypt_byte_in_bitmap((byte) bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            encrypt_byte_in_bitmap(bytes[i]);
        }

        imageView.setImageBitmap(bmp);
    }

    private int decrypt_part_byte_from_color(int color){
        // var & mask => two last digit == one
        int mask = 3;

        int alpha = (color) & 0b11111111;
        int red = (color >> 8) & 0b11111111;
        int green = (color >> 16) & 0b11111111;
        int blue = (color >> 24) & 0b11111111;

        return  ((color) & mask);
    }


    private byte decrypt_byte_from_bitmap() {
        int[] parts = {0, 0, 0, 0};
        byte res = 0;
        int color;
        for (int j = 0; j < parts.length; j++) {
            color = bmp.getPixel(x, y);
            parts[j] = decrypt_part_byte_from_color(color) << (j*2);
            res |= parts[j];
            if (x < width){
                x++;
            }
            else if(y < height){
                x = 0;
                y++;
            }
            else{
                throw new NullPointerException();
            }
        }

        return res;
    }

    public void decrypt(View view) {
        x = 0;
        y = 0;
        bmp = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        width = bmp.getWidth();
        height = bmp.getHeight();

        byte len_mess = decrypt_byte_from_bitmap();
        byte[] str = new byte[len_mess];
        for (int i = 0; i < len_mess; i++){
            str[i] = decrypt_byte_from_bitmap();
        }
        String s = new String(str);
        textView.setText(s);
    }


}
