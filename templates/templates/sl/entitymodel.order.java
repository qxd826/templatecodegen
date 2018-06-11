
import lombok.Data;
@Data
public class ${FMT.XyzAbc($table)} {

    #foreach($item in $values)
/**${item.comment}*/
private ${item.type} ${FMT.xyzAbc(${item.name})};
    #end
    }

