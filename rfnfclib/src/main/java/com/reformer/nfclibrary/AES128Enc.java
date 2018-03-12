package com.reformer.nfclibrary;

import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by Administrator on 2015-07-02.
 */
public class AES128Enc {
    private byte[] sbox=
            {
                    (byte)0x63, (byte)0x7c, (byte)0x77, (byte)0x7b, (byte)0xf2, (byte)0x6b, (byte)0x6f, (byte)0xc5, (byte)0x30, (byte)0x01, (byte)0x67, (byte)0x2b, (byte)0xfe, (byte)0xd7, (byte)0xab, (byte)0x76,
                    (byte)0xca, (byte)0x82, (byte)0xc9, 0x7d, (byte)0xfa, 0x59, 0x47, (byte)0xf0, (byte)0xad, (byte)0xd4, (byte)0xa2, (byte)0xaf, (byte)0x9c, (byte)0xa4, 0x72, (byte)0xc0,
                    (byte)0xb7, (byte)0xfd, (byte)0x93, 0x26, 0x36, 0x3f, (byte)0xf7, (byte)0xcc, 0x34, (byte)0xa5, (byte)0xe5, (byte)0xf1, 0x71, (byte)0xd8, 0x31, 0x15,
                    0x04, (byte)0xc7, 0x23, (byte)0xc3, 0x18, (byte)0x96, 0x05, (byte)0x9a, 0x07, 0x12, (byte)0x80, (byte)0xe2, (byte)0xeb, 0x27, (byte)0xb2, 0x75,
                    0x09, (byte)0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, (byte)0xa0, 0x52, 0x3b, (byte)0xd6, (byte)0xb3, 0x29, (byte)0xe3, 0x2f, (byte)0x84,
                    0x53, (byte)0xd1, 0x00, (byte)0xed, 0x20, (byte)0xfc, (byte)0xb1, 0x5b, 0x6a, (byte)0xcb, (byte)0xbe, 0x39, 0x4a, 0x4c, 0x58, (byte)0xcf,
                    (byte)0xd0, (byte)0xef, (byte)0xaa, (byte)0xfb, 0x43, 0x4d, 0x33, (byte)0x85, 0x45, (byte)0xf9, 0x02, 0x7f, 0x50, 0x3c, (byte)0x9f, (byte)0xa8,
                    0x51, (byte)0xa3, 0x40, (byte)0x8f, (byte)0x92, (byte)0x9d, 0x38, (byte)0xf5, (byte)0xbc, (byte)0xb6, (byte)0xda, 0x21, 0x10, (byte)0xff, (byte)0xf3, (byte)0xd2,
                    (byte)0xcd, 0x0c, 0x13, (byte)0xec, 0x5f, (byte)0x97, 0x44, 0x17, (byte)0xc4, (byte)0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
                    0x60, (byte)0x81, 0x4f, (byte)0xdc, 0x22, 0x2a, (byte)0x90, (byte)0x88, 0x46, (byte)0xee, (byte)0xb8, 0x14, (byte)0xde, 0x5e, 0x0b, (byte)0xdb,
                    (byte)0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, (byte)0xc2, (byte)0xd3, (byte)0xac, 0x62, (byte)0x91, (byte)0x95, (byte)0xe4, 0x79,
                    (byte)0xe7, (byte)0xc8, 0x37, 0x6d, (byte)0x8d, (byte)0xd5, 0x4e, (byte)0xa9, 0x6c, 0x56, (byte)0xf4, (byte)0xea, 0x65, 0x7a, (byte)0xae, 0x08,
                    (byte)0xba, 0x78, 0x25, 0x2e, 0x1c, (byte)0xa6, (byte)0xb4, (byte)0xc6, (byte)0xe8, (byte)0xdd, 0x74, 0x1f, 0x4b, (byte)0xbd, (byte)0x8b, (byte)0x8a,
                    0x70, 0x3e, (byte)0xb5, 0x66, 0x48, 0x03, (byte)0xf6, 0x0e, 0x61, 0x35, 0x57, (byte)0xb9, (byte)0x86, (byte)0xc1, 0x1d, (byte)0x9e,
                    (byte)0xe1, (byte)0xf8, (byte)0x98, 0x11, 0x69, (byte)0xd9, (byte)0x8e, (byte)0x94, (byte)0x9b, 0x1e, (byte)0x87, (byte)0xe9, (byte)0xce, 0x55, 0x28, (byte)0xdf,
                    (byte)0x8c, (byte)0xa1, (byte)0x89, 0x0d, (byte)0xbf, (byte)0xe6, 0x42, 0x68, 0x41, (byte)0x99, 0x2d, 0x0f, (byte)0xb0, 0x54, (byte)0xbb, 0x16
            };
    private byte[] RC={0x01,0x02,0x04,0x08,0x10,0x20,0x40,(byte)0x80,0x1b,0x36};
    /*Used to invert mix columns*/
    private byte[][] MixC=
            {
                    {0x02,0x03,0x01,0x01},
                    {0x01,0x02,0x03,0x01},
                    {0x01,0x01,0x02,0x03},
                    {0x03,0x01,0x01,0x02}
            };

    protected byte[] aes128cbc_Pkcs7_Enc(byte[] input, byte[] key){
        byte[] output = new byte[16];
        int[] W = new int[11*4];
        int[] tempW = new int[4];
        if (input.length < 16){
            int start = input.length;
            byte pk = (byte) (16 - input.length);
            input = Arrays.copyOf(input,16);
            Arrays.fill(input,start,16,pk);
        }
        int i=0;
        for(i=0;i<16;i++) output[i]=(byte)(input[i]^key[i]);
        output = reverse4x(output);
        W = key_expansion(key,W);
        i=0;
        output = state_add_rou_key(output,W);
        i++;
        while(i<10){
            output = state_bvary_lshift(output);
            output = state_mix_columns(output);
            System.arraycopy(W,i*4,tempW,0,4);
            output = state_add_rou_key(output,tempW);
            i++;
        }
        output = state_bvary_lshift(output);
        System.arraycopy(W,i*4,tempW,0,4);
        output = state_add_rou_key(output,tempW);
        output = reverse4x(output);
        return output;
    }

    private byte[] reverse4x(byte[] input){
        byte[] output = new byte[16];
        System.arraycopy(input,0,output,0,16);
        int i,j;
        for(i=0;i<3;i++)
            for(j=i+1;j<4;j++){
                output[4*i+j] = input[4*j+i];
                output[4*j+i] = input[4*i+j];
            }
//                swap(&box[4*i+j],&box[4*j+i]);
        return output;
    }

    private int[] key_expansion(byte[]  key,int[]  w)
    {
        int  temp;
        int i;
        //int j;
        for(i=0;i<4;i++)
        {
            w[i]=key[4*i]<<24 |
                    ((key[4*i+1]<<16)&0xff0000) |
                    ((key[4*i+2]<<8)&0xff00)  |
                    ((key[4*i+3])&0xff);
        }
        for(i=4;i<44;i++)
        {
            temp=w[i-1];
            if(i%4==0)
            {
                temp = (temp<<8)|(temp>>>24); //ROTATE_LEFT(x, s, n)  ((x) << (n))|((x) >> ((s) - (n)))
                temp = subword(temp,sbox);
                temp = temp ^ (RC[i/4-1]<<24); //#define GF2sup8_add(a,b) ((a)^(b))
//                temp=ROTATE_LEFT(temp,32,8);//rotword
//                subword(temp,sbox);
//                temp=GF2sup8_add(temp,RC[i/4-1]<<24);
            }
            w[i] = w[i-4]^temp;
//            w[i]=GF2sup8_add(w[i-4],temp);
        }
        return  w;
    }

    private int subword(int wtemp, final byte[]  s)
    {
        byte[] newtemp = int2Byte(wtemp);
        for (int i = 0; i < 4; i++) {
            if (newtemp[i] < 0)
                newtemp[i] = s[newtemp[i]+256];
            else
                newtemp[i] = s[newtemp[i]];
        }
        return byte2Int(newtemp);
    }

    private byte[] int2Byte(int intValue) {
        byte[] b = new byte[4];
        if(ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < 4; i++) {
                b[i] = (byte) (intValue >> 8 * (3 - i) & 0xFF);
                //System.out.print(Integer.toBinaryString(b[i])+" ");
                //System.out.print((b[i] & 0xFF) + " ");
            }
        }else{
            for (int i = 0; i < 4; i++) {
                b[i] = (byte) (intValue >> 8 * (i) & 0xFF);
            }
        }
        return b;
    }

    /**
     * 4λ�ֽ�����ת��Ϊ����
     * @param b
     * @return
     */
    private int byte2Int(byte[] b) {
        int intValue = 0;
        if(ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < b.length; i++) {
                intValue += (b[i] & 0xFF) << (8 * (3 - i));
            }
        } else {
            for (int i = 0; i < b.length; i++) {
                intValue += (b[i] & 0xFF) << (8 * i);
            }
        }
        return intValue;
    }

    /*Add round key*/
    private byte[] state_add_rou_key(byte[] state,int[] key)
    {
        int i;
        for(i=0;i<4;i++)
        {
            state[i] = (byte)(state[i]^((((key[i]<<8)&0xff00)|(key[i]>>>24))&0xFF));
            state[4+i]= (byte)(state[4+i]^((((key[i]<<8)&0xff00)|(key[i]>>>16))&0xFF));
            state[2*4+i] = (byte)(state[2*4+i]^(((key[i]>>>8)|(key[i]<<24))&0xFF));
            state[3*4+i] = (byte)(state[3*4+i]^((key[i])&0xFF));
//            newState[i]=GF2sup8_add(state[i],ROTATE_LEFT(key[i],32,8)&0xff);
//            newState[4+i]=GF2sup8_add(state[4+i],ROTATE_LEFT(key[i],32,16)&0x00ff);
//            newState[2*4+i]=GF2sup8_add(state[2*4+i],ROTATE_RIGHT(key[i],32,8)&0xff); //#define ROTATE_RIGHT(x, s, n) ((x) >> (n))|((x) << ((s) - (n)))
//            newState[3*4+i]=GF2sup8_add(state[3*4+i],key[i]&0xff);
        }
        return state;
    }

    private byte[] state_bvary_lshift(byte[]  state)
    {
        int i,j,ret;
        byte[] temp = new byte[4];
        for(i=0;i<4;i++)
        {
            for(j=0;j<4;j++)
            {
//                subbyte(&state[i*4+j],sbox);//substitute bytes
                if(state[i*4+j] < 0)
                    state[i*4+j] = sbox[state[i*4+j]+256];
                else
                    state[i*4+j] = sbox[state[i*4+j]];
            }
            if (i<=3 && i>0) {
                System.arraycopy(state, i * 4, temp, 0, 4);
                temp = state_shift_row_left(temp, i);
                System.arraycopy(temp, 0, state, i * 4, 4);
            }
//            ret = state_shift_row_left(&state[i*4],i);//shift rows
//            if(ret < 0)
//                return ret;
        }
        return state;
    }

    /*Shift one row left*/
    private byte[] state_shift_row_left(byte[] state,int n)
    {
//        int * swt = (word_t*)state;
        int sw= byte2Int(state);
        if(ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN){
            sw=(sw<<(n*8))|(sw>>>(32-n*8));//ROTATE_LEFT(sw,32,n*8);
        }else{
            sw=(sw>>>(n*8))|(sw<<(32-n*8));//*swt=ROTATE_RIGHT(sw,32,n*8);
        }

        return int2Byte(sw);
    }

    /*Mix columns*/
    private byte[] state_mix_columns(byte[] state)
    {
        int i,j;
        byte[] tr = new byte[4];
        for(i=0;i<4;i++){
            for(j=0;j<4;j++){
                tr[j]=(byte)(GF2sup8_mul8(MixC[j][0],(state[0*4+i]&0xff))^
                        GF2sup8_mul8(MixC[j][1],(state[1*4+i]&0xff))^
                        GF2sup8_mul8(MixC[j][2],(state[2*4+i]&0xff))^
                        GF2sup8_mul8(MixC[j][3],(state[3*4+i]&0xff)));
            }
            state[0*4+i]=tr[0];
            state[1*4+i]=tr[1];
            state[2*4+i]=tr[2];
            state[3*4+i]=tr[3];
        }
        return state;
    }

    /*axb*/
    private int GF2sup8_mul8(final int a, final int b)
    {
        if (a ==0 || b==0) return 0;
        int[] BX = new int[8];
        BX[0]=a;
        int i;
        for(i=1;i<8;i++){
            if((BX[i-1] & 0x80) != 0){
                BX[i]=BX[i-1]<<1;
                BX[i]^=0x001b;
            }else
                BX[i]=BX[i-1]<<1;
        }
        i=0;
        int n=1;
        int ret=0;
        while(n!=0){
            if((n&b) != 0) ret^=BX[i];
            i++;
            n<<=1;
        }

        return ret;
    }
}
