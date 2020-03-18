package com.tyflow.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tyflow.common.response.ApiResponse;

import io.swagger.annotations.ApiOperation;

@RestController
public class HistoryApi {
	@Autowired
	private HistoryService historyService;
	@ApiOperation(value = "查询历史办理任务，只查star和end之间的", tags = { "历史相关api" }, notes = "查询历史办理任务，只查star和end之间的")
	@GetMapping("/tyapi/history/tasks")
	public ApiResponse historyTask(@RequestParam(required = true,name = "processInstanceId")String procInsId,@RequestParam(name = "startId")String startTaskId,@RequestParam(name = "endId")String endTaskId){
	 List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(procInsId)
				.orderByHistoricTaskInstanceStartTime().asc().orderByHistoricTaskInstanceEndTime().asc().list();
		boolean start = false;
		Map<String, Integer> actMap =new HashMap();
		JSONArray hisTasks=new JSONArray();
		for (int i=0; i<list.size(); i++){
			
			HistoricTaskInstance histIns = list.get(i);
			
			// 过滤开始节点前的节点
			if (StringUtils.isNotBlank(startTaskId) && startTaskId.equals(histIns.getId())){
				start = true;
			}
			if (StringUtils.isNotBlank(startTaskId) && !start){
				continue;
			}
			
			// 只显示执行人不为空的任务
			if (StringUtils.isNotBlank(histIns.getAssignee())){
				// 给节点增加一个序号
				Integer actNum = actMap.get(histIns.getId());
				if (actNum == null){
					actMap.put(histIns.getId(), actMap.size());
				}
				
				JSONObject historyTaskJSONObj=new JSONObject();
//				e.setHistIns(histIns);
				historyTaskJSONObj.put("taskId", histIns.getId());
				historyTaskJSONObj.put("taskName", histIns.getName());				
				historyTaskJSONObj.put("startTime", histIns.getStartTime());
				historyTaskJSONObj.put("endTime", histIns.getEndTime());
				historyTaskJSONObj.put("duration", histIns.getDurationInMillis());
				// 获取任务执行人名称
				if (StringUtils.isNotEmpty(histIns.getAssignee())){
						historyTaskJSONObj.put("assignee", histIns.getAssignee());
				}
			
				hisTasks.add(historyTaskJSONObj);
			}
			
			// 过滤结束节点后的节点
			if (StringUtils.isNotBlank(endTaskId) && endTaskId.equals(histIns.getId())){
				boolean bl = false;
				Integer actNum = actMap.get(histIns.getId());
				// 该活动节点，后续节点是否在结束节点之前，在后续节点中是否存在
				for (int j=i+1; j<list.size(); j++){
					HistoricTaskInstance hi = list.get(j);
					Integer actNumA = actMap.get(hi.getId());
					if ((actNumA != null && actNumA < actNum) || StringUtils.equals(hi.getId(), histIns.getId())){
						bl = true;
					}
				}
				if (!bl){
					break;
				}
			}
		}
		return ApiResponse.success(hisTasks);
	}
}
