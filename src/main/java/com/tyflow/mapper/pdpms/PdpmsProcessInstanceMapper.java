package com.tyflow.mapper.pdpms;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.tyflow.entity.pdpms.PdpmsProcessInstance;

@Mapper
public interface PdpmsProcessInstanceMapper {
	
	
	@Select("select * from pdpms_process_instance t where t.proc_ins_id")
	List<PdpmsProcessInstance> find(String procInsId);
	
}
