import java.util.ArrayList;
import java.util.List;

/**
 * Created by qianliao.zhuang on 2016/9/1.
 */
public class FileConstants {

    public static final List<String> SUPPORTED_FILE_TYPE = FileMagic.getSupportedFileType();
    public static final List<String> SUPPORTED_FILE_MAGIC = FileMagic.getSupportedFileMagic();
    public static final List<String> TEXTED_FILE_TYPE = FileMagic.getTextedFileType();

    public enum FileMagic {
        // image magic
        JPEG("JPEG", "FFD8FF"),
        JPG("JPG", "FFD8FF"),
        PNG("PNG", "89504E47"),
        GIF("GIF", "47494638"),
        TIFF("TIFF", "49492A00"),
        BMP("BMP", "424D"),

        // text magic
        // 文本类型的文件没有固定的魔数，所以统一用 @#*! 来代替
        JS("JS", "@#*!"),
        CSS("CSS", "@#*!");

        FileMagic(String fileName, String magic) {
            this.fileName = fileName;
            this.magic = magic;
        }

        private String fileName;
        private String magic;

        public static List<String> getSupportedFileType(){
            List<String> supported = new ArrayList<String>(FileMagic.values().length);
            for(FileMagic file : FileMagic.values()){
                supported.add(file.getFileName());
            }
            return supported;
        }

        public static List<String> getSupportedFileMagic(){
            List<String> supported = new ArrayList<String>(FileMagic.values().length);
            for(FileMagic file : FileMagic.values()){
                supported.add(file.getMagic());
            }
            return supported;
        }

        public static List<String> getTextedFileType(){
            List<String> textedFile = new ArrayList<String>();
            textedFile.add(JS.getFileName());
            textedFile.add(CSS.getFileName());
            return textedFile;
        }

        public String getMagic() {
            return magic;
        }

        public void setMagic(String magic) {
            this.magic = magic;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }
}
