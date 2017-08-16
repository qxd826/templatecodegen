
import lombok.Data;
@Data
public class ${FMT.XyzAbc($table)} {

    #foreach($item in $values)
private ${item.type} ${FMT.xyzAbc(${item.name})};//${item.comment}
    #end
    }

