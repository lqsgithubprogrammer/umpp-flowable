package com.tyflow.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.tyflow.common.response.ApiResponse;
import com.tyflow.controller.FlowableBaseApi;
import com.tyflow.entity.CustomTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.ui.modeler.domain.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tyapi/umpp")
@Api(tags = {"中车流程相关API"})
public class UmppRelateApi extends FlowableBaseApi {
    private static Logger logger = LoggerFactory.getLogger(UmppRelateApi.class);

    @ApiOperation(value = "完成任务并设置流程变量", tags = {"任务相关API"}, notes = "完成任务并设置流程变量")
    @PostMapping(value = "/task/complete/{taskId}", produces = "application/json")
    public ApiResponse complete(@ApiParam(name = "taskId", value = "任务实例ID") @PathVariable String taskId,
                                @RequestParam(name = "variables", required = false) String variables) {
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
        Map<String, Object> varMap = JSONObject.parseObject(variables, new TypeReference<Map<String, Object>>() {
        });
        try {
            taskService.complete(taskId, varMap);//TODO 这种方式的var存的scope是什么？
        } catch (Exception e) {
            logger.error("完成任务时异常，可能已经完成任务", e);
            return ApiResponse.fail("完成任务时异常，可能已经完成任务" + e.getMessage());
        }
        return ApiResponse.success(null, "任务完成成功");
    }


    @ApiOperation(value = "通过模型部署流程", tags = {"流程相关API"}, notes = "通过模型部署流程")
    @PostMapping(value = "/deployments/modelId/{modelId}", produces = "application/json")
    public ApiResponse deployByBpmnModel(@ApiParam(name = "modelId", value = "UI模型ID") @PathVariable String modelId) {
        logger.info("通过bpmn模型进行部署,模型ID为:{}", modelId);
        Model model = modelService.getModel(modelId);
        if (model == null)
            return ApiResponse.fail("模型不存在");
        Deployment deployment = null;
        try {
            deployment = repositoryService.createDeployment()
                    .addBpmnModel(model.getKey() + ".bpmn20.xml", modelService.getBpmnModel(model)).key(model.getKey())
                    .name(model.getName()).deploy();
        } catch (Exception e) {
            logger.error("部署出错", e);
            return ApiResponse.fail("部署出错，原因：" + e.getMessage());
        }
        logger.info("部署成功  部署对象：{}", deployment);
        JSONObject object = new JSONObject();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        object.put("processDefinitionId", processDefinition.getId());
        return ApiResponse.success(object, "部署成功");
    }


    @ApiOperation(value = " 以key方式启动最新版本的流程", tags = {"流程相关API"}, notes = " 以key方式启动最新版本的流程")
    @PostMapping(value = "/process/startByKey/{processDefinitionKey}", produces = "application/json")
    public ApiResponse startLatedByKey(
            @ApiParam(name = "modelId", value = "UI模型ID", required = true) @PathVariable String processDefinitionKey,
            @ApiParam(name = "businessId", value = "业务项目id", required = false) @RequestParam(required = false) String businessId,
            @ApiParam(name = "assignPersons", value = "人员分配", required = false) @RequestParam(required = false) String assignPersons,
            @ApiParam(name = "variables", value = "流程变量", required = false) @RequestParam(required = false) String variables
    ) {
        logger.info("-------启动流程 id:{}", processDefinitionKey);
        logger.info("-------传入的业务key:{}", businessId);
        logger.info("-------传入的业务key:{}", assignPersons);
        logger.info("-------传入的启动参数:{}", variables);
        //最新的流程定义启动
        //流程变量封装
        ProcessInstance processInstance;
        try {

            Map<String, Object> params = Maps.newHashMap();
            params.put("businessId", businessId);
            params.put("assignPersons", assignPersons);
            if (variables != null) {
                HashMap<String, Object> vars = JSONObject.parseObject(variables, new TypeReference<HashMap<String, Object>>() {
                });
                params.putAll(vars);
            }
            processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessId, params);

        } catch (Exception e) {
            return ApiResponse.fail(null, e.getMessage());
        }
        return ApiResponse.success(processInstance.getId(), "流程启动成功");
    }

    @ApiOperation(value = " 根据Id获取任务信息", tags = {"任务相关API"}, notes = " 根据Id获取任务信息")
    @GetMapping(value = "/task/taskId/{taskId}")
    public ApiResponse getTaskById(
            @ApiParam(name = "taskId", value = "任务ID", required = true) @PathVariable String taskId) {
        try {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            CustomTask customTask = new CustomTask(task, null);
            return ApiResponse.success(customTask);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
