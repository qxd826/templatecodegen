package com.tqmall.legend.entity.order;

import lombok.Getter;
import lombok.Setter;

import com.tqmall.legend.entity.base.BaseEntity;

@Getter
@Setter
public class ${FMT.XyzAbc($table)} extends BaseEntity {

    #foreach($item in $values)#if(${FMT.xyzAbc(${item.name})} != 'id' && ${FMT.xyzAbc(${item.name})} != 'gmtCreate' && ${FMT.xyzAbc(${item.name})} != 'gmtModified' && ${FMT.xyzAbc(${item.name})} != 'creator' && ${FMT.xyzAbc(${item.name})} != 'modifier' && ${FMT.xyzAbc(${item.name})} != 'isDeleted')
private ${item.type} ${FMT.xyzAbc(${item.name})};//${item.comment}
    #end
    #end

    }

