import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * Created by qianliao.zhuang on 2016/8/31.
 */
public class FileNameValidator implements UploadFileValidator {

    private List<String> supportedFileType;

    public FileNameValidator(List<String> supportedFileType) {
        this.supportedFileType = supportedFileType;
    }

    @Override
    public boolean validate(String fileName, byte[] fileByte){
        String fileType = FilenameUtils.getExtension(fileName);
        if(fileType == null || fileType.length() <= 0){
            return false;
        }
        fileType = fileType.toUpperCase();
        if(getSupportedFileType().contains(fileType)){
            return true;
        }
        return false;
    }

    public List<String> getSupportedFileType() {
        return this.supportedFileType;
    }
}
