package com.tyflow.controller.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tyflow.common.response.ApiResponse;
import com.tyflow.controller.FlowableBaseApi;
import com.tyflow.controller.ProcessDefinitionUtils;
import com.tyflow.entity.CustomTask;
import com.tyflow.service.BpmnCustomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.form.api.FormDefinition;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tyapi/task")
@Api(tags = {"任务相关API"})
public class TaskApi extends FlowableBaseApi {

    private static Logger logger = LoggerFactory.getLogger(TaskApi.class);

    @Autowired
    private BpmnCustomService bpmnCustomService;
    @Autowired
    private ProcessDefinitionUtils processDefinitionUtils;

    @ApiOperation(value = "任务完成", tags = {"任务相关API"}, notes = "任务完成")
    @PostMapping(value = "/complete/{taskId}", produces = "application/json")
    public ApiResponse complete(@ApiParam(name = "taskId", value = "任务实例ID") @PathVariable String taskId,
                                @RequestParam(name = "variables", required = false) String variables,
                                @RequestParam(name = "formDefinitionId", required = false) String formDefinitionId) {
        logger.info("进入 任务完成 :{}", taskId);
        // 在业务系统里把参数转化为map，传入流程引擎时，要转化为流程引擎识别的格式
        // 格式如下：
        /*
         * [ { "name": "myVariable", "type": "string", "value": "test", "valueUrl":
         * "http://....", "scope": "string" } ]
         */
        if (StringUtils.isEmpty(variables)) {
            try {
                taskService.complete(taskId);
                return ApiResponse.success(null, "任务完成成功");
            } catch (Exception e) {
                logger.error("任务完成失败", e);
                return ApiResponse.fail("任务完成失败：" + e.getMessage());
            }
        }
        // 设置参数
        JSONArray array = JSONArray.parseArray(variables);
        int size = array.size();
        Map<String, Object> params = new HashMap<String, Object>(size);
        for (int i = 0; i < size; i++) {
            JSONObject o = array.getJSONObject(i);
            params.put(o.getString("name"), o.getString("value"));
            logger.info("---设置任务完成参数  名称:{}  值:{}", o.getString("name"), o.getString("value"));
        }

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            logger.error("没有找到id为" + taskId + "的任务");
            throw new FlowableObjectNotFoundException("没有找到id为" + taskId + "的任务", Task.class);
        }

        if (StringUtils.isEmpty(formDefinitionId)) {
            logger.info("非表单方式完成");
            taskService.setVariablesLocal(task.getId(), params);
            runtimeService.setVariablesLocal(task.getExecutionId(), params);

            try {
                taskService.complete(taskId);
            } catch (Exception e) {
                logger.error("完成任务时异常，可能已经完成任务", e);
                return ApiResponse.fail("完成任务时异常，可能已经完成任务" + e.getMessage());
            }
        } else {
            logger.info("使用表单方式完成");
            try {
                taskService.completeTaskWithForm(taskId, formDefinitionId, null, params, true);
            } catch (Exception e) {
                logger.error("完成任务时异常，可能已经完成任务", e);
                return ApiResponse.fail("完成任务时异常，可能已经完成任务" + e.getMessage());
            }
        }
        return ApiResponse.success(null, "任务完成成功");
    }

    @ApiOperation(value = "根据流程定义获取用户任务", tags = {"任务相关API"}, notes = "根据流程定义获取用户任务")
    @GetMapping(value = "/process-definition/{processDefinitionId}", produces = "application/json")
    public ApiResponse findByprocessDefinition(
            @ApiParam(name = "processDefinitionId", value = "流程定义Id") @PathVariable String processDefinitionId) {
        logger.info("根据流程定义获取任务  流程定义id :{}", processDefinitionId);
        try {
            BpmnModel bpmdModel = repositoryService.getBpmnModel(processDefinitionId);
            JSONArray tasksJson;
            tasksJson = bpmnCustomService.findBpmnUserTaskByProcessDefinitionId(processDefinitionId);
            return ApiResponse.success(tasksJson, "获取流程定义的用户任务成功");
        } catch (Exception e) {
            return ApiResponse.fail("流程定义不存在");
        }
    }

    @ApiOperation(value = "根据流程模型Id获取用户任务", tags = {"任务相关API"}, notes = "根据流程模型Id获取用户任务")
    @GetMapping(value = "/model/{modelId}")
    public JSONArray findTaskByModelId(
            @ApiParam(name = "modelId", value = "流程模型Id", required = true) @PathVariable String modelId) {
        logger.info("根据流程模型Id获取用户任务， 流程模型ID:{}", modelId);
        return bpmnCustomService.findBpmnUserTaskByModelId(modelId);
    }

    @ApiOperation(value = "根据任务key 模糊查询 获取流程实例下的任务", tags = {"任务相关API"}, notes = "根据任务key 模糊查询 获取流程实例下的任务")
    @GetMapping(value = "/keylike")
    public ApiResponse findTaskByLikeKey(
            @ApiParam(name = "processInstanceId", value = "流程实例id", required = true) @RequestParam String processInstanceId,
            @ApiParam(name = "key", value = "任务keylike", required = true) @RequestParam String key) {
        logger.info("根据任务key {} 模糊查询 获取流程实例  {}  下的任务", key, processInstanceId);
        // 按照任务生成时间，升序排列，早生成的在前
        List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKeyLike("%" + key + "%")
                .orderByTaskCreateTime().asc()
                .list();
        if (taskList.size() > 0) {
            return ApiResponse.success(taskList);
        }
        return ApiResponse.fail("未查询到任务");
    }

    @ApiOperation(value = "根据任务key的Pattern 模糊查询 获取流程实例下的任务", tags = {"任务相关API"}, notes = "根据任务key的Pattern 模糊查询 获取流程实例下的任务")
    @GetMapping(value = "/keylikeByPattern")
    public ApiResponse findTaskByLikeKeyByPattern(
            @ApiParam(name = "processInstanceId", value = "流程实例id", required = true) @RequestParam String processInstanceId,
            @ApiParam(name = "keyPattern", value = "任务key的Pattern", required = true) @RequestParam String keyPattern) {
        logger.info("根据任务key {} 模糊查询 获取流程实例  {}  下的任务", keyPattern, processInstanceId);
        // 按照任务生成时间，升序排列，早生成的在前
        List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKeyLike(keyPattern)
                .orderByTaskCreateTime().asc()
                .list();
        if (taskList.size() > 0) {
            return ApiResponse.success(taskList);
        }
        return ApiResponse.fail("未查询到任务");
    }

    @ApiOperation(value = "根据任务id获取任务", tags = {"任务相关API"}, notes = "根据任务id获取任务")
    @GetMapping(value = "/{taskId}")
    public CustomTask findTaskById(@ApiParam(name = "taskId", value = "任务Id") @PathVariable String taskId) {
        logger.info("正在获取id：{}的任务", taskId);
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        HistoricTaskInstance historyTask = null;
        String formKey;
        if (task == null) {
            historyTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            if (historyTask == null) {
                throw new FlowableObjectNotFoundException("未找到id为" + taskId + "的任务实例");
            }
            formKey = historyTask.getFormKey();
        } else {
            formKey = task.getFormKey();
        }
        String formDefinitionId = "";
        if (!StringUtils.isEmpty(formKey)) {
            List<FormDefinition> list = formRepositoryService.createFormDefinitionQuery().formDefinitionKey(formKey).list();
            FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery().formDefinitionKey(formKey).formVersion(list.size()).singleResult();
            formDefinitionId = formDefinition == null ? "" : formDefinition.getId();
        }
        if (task == null) {
            return new CustomTask(historyTask, formDefinitionId);
        }
        return new CustomTask(task, formDefinitionId);
    }

    @ApiOperation(value = "指派任务执行人", tags = {"任务相关API"}, notes = "指派任务执行人")
    @PostMapping(value = "/assign/{taskId}", produces = "application/json")
    public ApiResponse assign(@ApiParam(name = "taskId", value = "任务实例ID") @PathVariable String taskId,
                              @RequestParam(name = "userId", required = true) String userId) {
        logger.info("正在指派id:{}的任务执行人:{}", taskId, userId);
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            logger.error("指派错误，任务不存在");
            return ApiResponse.fail("指派错误，任务不存在");
        }

        try {
            taskService.setAssignee(taskId, userId);
        } catch (Exception e) {
            logger.error("指派失败，系统异常。", e);
            return ApiResponse.fail("指派失败，系统异常。" + e.getMessage());
        }
        return ApiResponse.success(null, "指派成功");
    }

    /**
     * liqianshi
     *
     * @param taskId
     * @param variableName
     * @return
     */
    @ApiOperation(value = "获取任务变量或者流程变量", tags = {"任务相关API"}, notes = "获取任务变量或者流程变量")
    @GetMapping(value = "/variable", produces = "application/json")
    public ApiResponse variable(@RequestParam(name = "taskId", required = true) String taskId,
                                @RequestParam(name = "variableName", required = true) String variableName) {
        try {
            Object variable = taskService.getVariable(taskId, variableName);
            return ApiResponse.success(variable, "获取任务变量或者流程变量成功");
        } catch (Exception e) {
            logger.error("获取流程变量失败", e);
            return ApiResponse.fail("获取流程变量失败" + e.getMessage());
        }
    }

    /**
     * liqianshi
     *
     * @param taskId
     * @param variables
     * @return
     */
    @ApiOperation(value = "设置任务变量或者流程变量", tags = {"任务相关API"}, notes = "设置任务变量或者流程变量")
    @PostMapping(value = "/setVariable", produces = "application/json")
    public ApiResponse setVariable(@RequestParam(name = "taskId", required = true) String taskId,
                                   @RequestParam(required = true) String variables) {
		/*
		variables是map形式的JSONObject
		 */
        try {
            Map<String, Object> variableMap = JSONObject.parseObject(variables, new TypeReference<Map<String, Object>>() {
            });
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            runtimeService.setVariables(task.getProcessInstanceId(), variableMap);
            runtimeService.setVariablesLocal(task.getExecutionId(), variableMap);
            return ApiResponse.success(null, "设置任务变量或者流程变量成功");
        } catch (Exception e) {
            logger.error("设置流程变量失败", e);
            return ApiResponse.fail("设置流程变量失败" + e.getMessage());
        }
    }

    @ApiOperation(value = "设置任务变量或者流程变量", tags = {"任务相关API"}, notes = "获取任务变量或者流程变量")
    @PostMapping(value = "/setVariableByPayload", produces = "application/json")
    public ApiResponse setVariableByPayload(@RequestBody Map request) {
        try {
            String taskId = (String) request.get("taskId");
            String variables = (String) request.get("variables");
            logger.info("开始设置任务id {} 变量 {}", taskId, variables);
            Map<String, Object> variableMap = JSONObject.parseObject(variables, new TypeReference<Map<String, Object>>() {
            });
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                logger.error("设置任务变量出错，运行中的任务 {} 不存在", taskId);
                return ApiResponse.fail("设置任务变量出错，运行中的任务不存在");
            }
            runtimeService.setVariables(task.getProcessInstanceId(), variableMap);
            runtimeService.setVariablesLocal(task.getExecutionId(), variableMap);
            logger.info("设置变量成功：{}", variableMap);
            return ApiResponse.success(null, "设置任务变量或者流程变量成功");
        } catch (Exception e) {
            logger.error("设置流程变量失败", e);
            return ApiResponse.fail("设置流程变量失败" + e.getMessage());
        }
    }

    @ApiOperation(value = "获取运行中任务", tags = {"任务相关API"}, notes = "获取运行中任务")
    @GetMapping(value = "/runtime", produces = "application/json")
    public List<Map<String, Object>> runtime() {
        logger.info("获取运行中任务");
        List<Task> list = taskService.createTaskQuery().list();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(list.size());
        for (Task o : list) {
            Map<String, Object> map = new HashMap<String, Object>(5);
            map.put("id", o.getId());
            map.put("taskKey", o.getTaskDefinitionKey());
            map.put("formKey", o.getFormKey());
            map.put("name", o.getName());
            map.put("assignee", o.getAssignee());
            map.put("processInstanceId", o.getProcessInstanceId());
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(o.getProcessDefinitionId());
            map.put("processKey", processDefinition.getKey());
            map.put("processName", processDefinition.getName());
            result.add(map);
        }
        return result;
    }

    @PostMapping(value = "/backToStep", produces = "application/json")
    public void backToStep(String taskId, String distFlowElementId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        FlowElement distActivity = processDefinitionUtils.findFlowElementById(task.getProcessDefinitionId(), distFlowElementId);

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        List<String> currentActivityIds = new ArrayList<>();
        tasks.forEach(t -> currentActivityIds.add(t.getTaskDefinitionKey()));
        //3. 删除节点信息,不知道什么用
		/*if (!(distActivity instanceof EndEvent)) {
			this.deleteHisActivities((Activity) distActivity, processInstanceId);
		}*/
        //5.执行驳回操作
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstanceId)
                .moveActivityIdsToSingleActivityId(currentActivityIds, distFlowElementId)
                .changeState();
    }

    @ApiOperation(value = "通过任务key，驳回至任务节点", tags = {"任务相关API"}, notes = "通过任务key，驳回至任务节点")
    @PostMapping(value = "/reject/{taskId}/{destTaskKey}", produces = "application/json")
    public ApiResponse rejectToTask(@ApiParam(name = "taskId", value = "任务实例ID") @PathVariable String taskId,
                                    @ApiParam(name = "destTaskKey", value = "驳回目标的任务key") @PathVariable String destTaskKey) {
        logger.info("驳回任务:{} 至 {}", taskId, destTaskKey);
        // TODO 此方法需要目标任务key和activityid一致
        Task task = null;
        try {
            task = taskService.createTaskQuery().taskId(taskId).singleResult();
        } catch (Exception e) {
            return ApiResponse.fail("驳回失败，任务不存在");
        }
        FlowElement distActivity = processDefinitionUtils.findFlowElementById(task.getProcessDefinitionId(), destTaskKey);

        String processInstanceId = task.getProcessInstanceId();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        List<String> currentActivityIds = new ArrayList<>();
        tasks.forEach(t -> currentActivityIds.add(t.getTaskDefinitionKey()));
//		if (!(distActivity instanceof EndEvent)) {
//						this.deleteHisActivities((Activity) distActivity, processInstanceId);
//		}
        //5.执行驳回操作
        try {
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstanceId)
                    .moveActivityIdsToSingleActivityId(currentActivityIds, destTaskKey)
                    .changeState();
        } catch (Exception e) {
            logger.error("驳回失败", e);
            return ApiResponse.fail("驳回失败，原因:" + e.getMessage());
        }
        return ApiResponse.success(null, "驳回成功");
    }
    @ApiOperation(value = "通过任务key，驳回至任务节点", tags = {"任务相关API"}, notes = "通过任务key，驳回至任务节点")
    @PostMapping(value = "/rejectToTasks/{taskId}", produces = "application/json")
    public ApiResponse rejectToParallel(@ApiParam(name = "taskId", value = "任务实例ID") @PathVariable String taskId,
                                    @ApiParam(name = "destTaskKey", value = "驳回目标的分支任务key") @RequestParam List<String> destTaskKey) {
        Task task = null;
        try {
            task = taskService.createTaskQuery().taskId(taskId).singleResult();
        } catch (Exception e) {
            return ApiResponse.fail("驳回失败，任务不存在");
        }
        String processInstanceId = task.getProcessInstanceId();
        //5.执行驳回操作
        try {
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(processInstanceId)
                    .moveSingleActivityIdToActivityIds(task.getTaskDefinitionKey(), destTaskKey)
                    .changeState();
        } catch (Exception e) {
            logger.error("驳回失败", e);
            return ApiResponse.fail("驳回失败，原因:" + e.getMessage());
        }
        return ApiResponse.success(null, "驳回成功");
    }
}
