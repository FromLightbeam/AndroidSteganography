#include <jni.h>
#include <cstring>

#define LEN_PARTS 4


int decrypt_part_byte_from_color(int color){
    // var & mask => two last digit == one
    int mask = 3;

    int alpha = (color) & 0b11111111;
    int red = (color >> 8) & 0b11111111;
    int green = (color >> 16) & 0b11111111;
    int blue = (color >> 24) & 0b11111111;

    return  ((color) & mask);
}


char decrypt_byte_from_bitmap(JNIEnv *env, jobject bmp, int &x, int &y, int width, int height) {
    int parts[LEN_PARTS] = {0, 0, 0, 0};
    char res = 0;
    int color;
    for (int j = 0; j < LEN_PARTS; j++) {

        jclass bitmap = env->GetObjectClass(bmp);
        jmethodID getPixel = env->GetMethodID(bitmap, "getPixel", "(II)I");
        int color =  env->CallIntMethod(bmp, getPixel, x, y);

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
            break;
        }
    }

    return res;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_hard_light_eight5_MainActivity_decodeBit(JNIEnv *env, jobject instance, jobject bmp,
                                                  jint width, jint height) {

    int x = 0;
    int y = 0;

    char len_mess = decrypt_byte_from_bitmap(env, bmp, x, y, width, height);
    char *str = new char[len_mess];
    for (int i = 0; i < len_mess; i++){
        str[i] = decrypt_byte_from_bitmap(env, bmp, x, y, width, height);
    }

    return env->NewStringUTF(str);
}



int encrypt_part_byte_in_color(int color, char b){
    // var & mask => two last digit == zero
    int mask = ~3;

    int alpha = (color) & 0b11111111;
    int red = (color >> 8) & 0b11111111;
    int green = (color >> 16) & 0b11111111;
    int blue = (color >> 24) & 0b11111111;
    blue = blue & mask | b;

    return  ((color) & mask) | b;
}

void encrypt_byte_in_bitmap(char b, JNIEnv *env, jobject &bmp, int &x, int &y, int width, int height){
    jclass bitmap = env->GetObjectClass(bmp);
    jmethodID getPixel = env->GetMethodID(bitmap, "getPixel", "(II)I");
    jmethodID setPixel = env->GetMethodID(bitmap, "setPixel", "(III)V");
    char parts [LEN_PARTS] = {0, 0, 0, 0};
    parts[0] = (b & 0b00000011);
    parts[1] = ((b >> 2) & 0b00000011);
    parts[2] = ((b >> 4) & 0b00000011);
    parts[3] = ((b >> 6) & 0b00000011);
    int color;
    int newcolor;
    for (int j = 0; j < LEN_PARTS; j++){

        color =  env->CallIntMethod(bmp, getPixel, x, y);

        newcolor = encrypt_part_byte_in_color(color, parts[j]);

        env->CallVoidMethod(bmp, setPixel, x, y, newcolor);

        if (x < width){
            x++;
        }
        else if(y < height){
            x = 0;
            y++;
        }
        else{
            break;
        }
    }

}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_hard_light_eight5_MainActivity_encodeBit(JNIEnv *env, jobject instance, jobject bmp,
                                                  jstring msg_, jint width, jint height) {
    const char *msg = env->GetStringUTFChars(msg_, 0);
    int x = 0;
    int y = 0;
    char length = strlen(msg);
    encrypt_byte_in_bitmap(length, env, bmp, x, y, width, height);
    for (int i = 0; i < length; i++) {
        encrypt_byte_in_bitmap(msg[i], env, bmp, x, y, width, height);
    }

    return bmp;
}
