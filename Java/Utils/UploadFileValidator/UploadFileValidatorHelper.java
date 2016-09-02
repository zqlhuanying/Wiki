import com.vip.xfd.file.common.constant.FileConstants;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qianliao.zhuang on 2016/8/31.
 */
public class UploadFileValidatorHelper {

    public static class Builder{
        private static UploadFileValidatorHelper helper = new UploadFileValidatorHelper();

        public static UploadFileValidatorHelper build(List<UploadFileValidator> validators){
            helper.setCustomValidator(validators);
            return build();
        }

        public static UploadFileValidatorHelper build(){
            helper.setChainValidator();
            return helper;
        }
    }

    private static final List<String> SUPPORTED_FILE_TYPE = FileConstants.SUPPORTED_FILE_TYPE;
    private static final List<String> TEXTED_FILE_TYPE = FileConstants.TEXTED_FILE_TYPE;
    private static final List<String> SUPPORTED_FILE_MAGIC = FileConstants.SUPPORTED_FILE_MAGIC;

    private List<UploadFileValidator> chainValidator;
    private List<UploadFileValidator> customValidator;

    private UploadFileValidatorHelper(){}

    public boolean validate(String fileName, byte[] fileByte){
        for(UploadFileValidator validator : chainValidator){
            if(!validator.validate(fileName, fileByte)){
                return false;
            }
        }
        return true;
    }

    public void setChainValidator(){
        if(chainValidator == null){
            chainValidator = new ArrayList<UploadFileValidator>();
        }
        for(UploadFileValidator validator : getDefaultValidator()){
            addValidator(validator);
        }
    }

    public void setCustomValidator(List<UploadFileValidator> validators){
        this.customValidator = validators;
    }

    public List<UploadFileValidator> getCustomValidator(){
        return this.customValidator;
    }

    private void addValidator(UploadFileValidator validator){
        chainValidator.add(validator);
    }

    private List<UploadFileValidator> getDefaultValidator(){
        List<UploadFileValidator> validators = new ArrayList<UploadFileValidator>();

        validators.add(new FileNameValidator(SUPPORTED_FILE_TYPE));
        validators.add(new FileMagicValidator(TEXTED_FILE_TYPE, SUPPORTED_FILE_MAGIC));

        if(getCustomValidator() != null){
            validators.addAll(getCustomValidator());
        }

        return validators;
    }
}
