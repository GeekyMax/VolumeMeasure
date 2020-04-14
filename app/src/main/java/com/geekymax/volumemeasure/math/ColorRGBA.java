package com.geekymax.volumemeasure.math;

/**
 * 颜色类。
 * 每种颜色有red、green、blue、alpha四个通道，每个通道使用1字节存储。
 * 
 * @author yanmaoyuan
 *
 */
public class ColorRGBA {

    public byte r;
    public byte g;
    public byte b;
    public byte a;

    public static final ColorRGBA WHITE = new ColorRGBA(0xFFFFFFFF);
    public static final ColorRGBA BLACK = new ColorRGBA(0x000000FF);
    public static final ColorRGBA RED = new ColorRGBA(0xFF0000FF);
    public static final ColorRGBA GREEN = new ColorRGBA(0x00FF00FF);
    public static final ColorRGBA BLUE = new ColorRGBA(0x0000FFFF);
    public static final ColorRGBA DARKGRAY = new ColorRGBA(0x666666FF);
    public static final ColorRGBA BLACK_NO_ALPHA = new ColorRGBA(0x00000000);

    public ColorRGBA() {
        r = g = b = a = (byte) 0xFF;
    }

    public ColorRGBA(Vector4f c) {
        r = (byte)(c.x * 0xFF);
        g = (byte)(c.y * 0xFF);
        b = (byte)(c.z * 0xFF);
        a = (byte)(c.w * 0xFF);
    }
    
    public ColorRGBA(int color) {
        r = (byte) ((color >> 24) & 0xFF);
        g = (byte) ((color >> 16) & 0xFF);
        b = (byte) ((color >> 8) & 0xFF);
        a = (byte) (color & 0xFF);
    }
}
