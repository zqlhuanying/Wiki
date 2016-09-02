import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * Created by qianliao.zhuang on 2016/8/31.
 */
public class FileMagicValidator implements UploadFileValidator{

    private List<String> supportedFileMagic;
    private List<String> textedFileType;

    public FileMagicValidator(List<String> textedFileType, List<String> supportedFileMagic) {
        this.textedFileType = textedFileType;
        this.supportedFileMagic = supportedFileMagic;
    }

    @Override
    public boolean validate(String fileName, byte[] fileByte){
        String fileType = FilenameUtils.getExtension(fileName);
        if(fileType == null || fileType.length() <= 0){
            return false;
        }
        fileType = fileType.toUpperCase();
        if(getTextedFileType().contains(fileType)){
            return true;
        }

        String fileMagic = getFileMagic(fileByte);
        if(fileMagic == null || fileMagic.length() <= 0){
            return false;
        }

        fileMagic = fileMagic.toUpperCase();
        for(String supportedMagic : getSupportedFileMagic()){
            if(fileMagic.startsWith(supportedMagic)){
                return true;
            }
        }
        return false;
    }

    private String getFileMagic(byte[] fileByte){
        if(fileByte == null || fileByte.length <= 0){
            return null;
        }

        int magicLength = Math.min(28, fileByte.length);
        byte[] fileHeader = new byte[magicLength];
        for(int i = 0; i < fileHeader.length; i++){
            fileHeader[i] = fileByte[i];
        }
        return bytesToHexString(fileHeader);
    }

    /**
     * 将文件头转换成16进制字符串
     *
     */
    private String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public List<String> getSupportedFileMagic() {
        return this.supportedFileMagic;
    }

    public List<String> getTextedFileType() {
        return textedFileType;
    }
}
