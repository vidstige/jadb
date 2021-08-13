package se.vidstige.jadb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FrameBuffer {
    public int version;
    public int bpp;
    public int colorSpace;
    public int size;
    public int width;
    public int height;
    public int red_offset;
    public int red_length;
    public int blue_offset;
    public int blue_length;
    public int green_offset;
    public int green_length;
    public int alpha_offset;
    public int alpha_length;
    public byte[] data;

    /**
     * get bitmap format data as byte array
     * <p>
     * this data can be saved as a BMP format file
     * <p>
     * if you want a JPEG format file ,you can convert it like this:
     * <p>
     * ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
     * <p>
     * BufferedImage image = ImageIO.read(byteArrayInputStream);
     * <p>
     * ImageIO.write(image, "jpeg", outputStream);
     */
    public byte[] getBitmapData() {
        if (data == null) {
            return null;
        }
        byte[] fileHeader = new byte[14];
        byte[] infoHeader = new byte[40];
        int imagePixelSize = width * height;
        int pixelByteCount = size / imagePixelSize;
        int bitmapFileSize = fileHeader.length + infoHeader.length + size;
        ByteBuffer buffer = ByteBuffer.wrap(fileHeader);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put("BM".getBytes());//bfType
        buffer.putInt(bitmapFileSize);//bfSize
        buffer.putInt(0);//bfReserved
        buffer.putInt(fileHeader.length + infoHeader.length);//bfOffBits
        buffer = ByteBuffer.wrap(infoHeader);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(infoHeader.length);//biSize
        buffer.putInt(width);//biWidth
        buffer.putInt(height);//biHeight
        buffer.putShort((short) 1);//biPlanes
        buffer.putShort((short) 32);//biBitCount
        buffer.putInt(0);//biCompression
        buffer.putInt(0);//biSizeImage
        buffer.putInt(0);//biXPelsPerMeter
        buffer.putInt(0);//biYPelsPerMeter
        buffer.putInt(0);//biClrUsed
        buffer.putInt(0);//biClrImportant
        //reverse row data in vertical direction and for each pixel convert RGBA to BGRA
        for (int i = 0; i < height / 2; i++) {
            for (int j = 0; j < width; j++) {
                int id1 = (i * width + j) * pixelByteCount;
                int id2 = ((height - i - 1) * width + j) * pixelByteCount;
                byte r = data[id1];
                byte g = data[id1 + 1];
                byte b = data[id1 + 2];
                data[id1] = data[id2 + 2];
                data[id1 + 1] = data[id2 + 1];
                data[id1 + 2] = data[id2];
                data[id2] = b;
                data[id2 + 1] = g;
                data[id2 + 2] = r;
            }
        }

        byte[] imageData = new byte[bitmapFileSize];
        System.arraycopy(fileHeader, 0, imageData, 0, fileHeader.length);
        System.arraycopy(infoHeader, 0, imageData, fileHeader.length, infoHeader.length);
        System.arraycopy(data, 0, imageData, fileHeader.length + infoHeader.length, data.length);
        return imageData;
    }

    @Override
    public String toString() {
        return "FrameBuffer{" +
                "version=" + version +
                ", bpp=" + bpp +
                ", colorSpace=" + colorSpace +
                ", size=" + size +
                ", width=" + width +
                ", height=" + height +
                ", red_offset=" + red_offset +
                ", red_length=" + red_length +
                ", blue_offset=" + blue_offset +
                ", blue_length=" + blue_length +
                ", green_offset=" + green_offset +
                ", green_length=" + green_length +
                ", alpha_offset=" + alpha_offset +
                ", alpha_length=" + alpha_length +
                '}';
    }


}
