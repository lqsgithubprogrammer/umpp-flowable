package com.tyflow.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tyflow.common.response.ApiResponse;
import com.tyflow.controller.FlowableBaseApi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/tyapi/process")
@Api(tags = {"流程相关API"})
public class ProcessApi extends FlowableBaseApi {

    private static Logger logger = LoggerFactory.getLogger(ProcessApi.class);

    /**
     * 最基本的方式启动流程
     *
     * @param processDefinitionId 流程定义ID
     * @return
     */
    @ApiOperation(value = "最基本的方式启动流程", tags = {"流程相关API"}, notes = "最基本的方式启动流程")
    @PostMapping(value = "/start/{processDefinitionId}", produces = "application/json")
    public ApiResponse start(
            @ApiParam(name = "processDefinitionId", value = "流程定义ID") @PathVariable String processDefinitionId,
            @RequestParam(name = "variables", required = false) String variables,
            @RequestParam(name = "businessKey", required = false) String businessKey
    ) {
        logger.info("-------启动流程 id:{}", processDefinitionId);
        logger.info("-------传入的启动参数:{}", variables);
        logger.info("-------传入的业务key:{}", businessKey);
        //在业务系统里把参数转化为map，传入流程引擎时，要转化为流程引擎识别的格式
        //格式如下：
		/*
		 *  [
			    {
			      "name": "myVariable",
			      "type": "string",
			      "value": "test",
			      "valueUrl": "http://....",
			      "scope": "string"
			    }
			  ]
		 * */
        Map<String, Object> var = null;
        if (!StringUtils.isEmpty(variables)) {
            //设置流程参数
            JSONArray array = JSONArray.parseArray(variables);
            var = new HashMap<String, Object>(array.size());
            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.getJSONObject(i);
                var.put(o.getString("name"), o.getString("value"));
                logger.info("---设置流程参数  名称:{}  值:{}", o.getString("name"), o.getString("value"));
            }
        }
        ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionId, businessKey, var);
        if (instance != null) {
            logger.info("流程启动成功 {}", instance);
            return ApiResponse.success(instance.getId(), "流程启动成功");
        } else {
            logger.info("流程启动失败");
            return ApiResponse.fail("流程启动失败");
        }

    }

    /**
     * 以key方式启动最新版本的流程
     *
     * @param processDefinitionKey 流程定义Key
     * @return
     */
    @ApiOperation(value = " 以key方式启动最新版本的流程", tags = {"流程相关API"}, notes = " 以key方式启动最新版本的流程")
    @PostMapping(value = "/startByKey/{processDefinitionKey}", produces = "application/json")
    public ApiResponse startLatedByKey(
            @ApiParam(name = "processDefinitionKey", value = "流程定义Key") @PathVariable String processDefinitionKey,
            @RequestParam(name = "variables", required = false) String variables,
            @RequestParam(name = "businessKey", required = false) String businessKey
    ) {
        logger.info("-------启动流程 id:{}", processDefinitionKey);
        logger.info("-------传入的启动参数:{}", variables);
        logger.info("-------传入的业务key:{}", businessKey);
        //在业务系统里把参数转化为map，传入流程引擎时，要转化为流程引擎识别的格式
        //格式如下：
		/*
		 *  [
			    {
			      "name": "myVariable",
			      "type": "string",
			      "value": "test",
			      "valueUrl": "http://....",
			      "scope": "string"
			    }
			  ]
		 * */
        Map<String, Object> var = null;
        if (!StringUtils.isEmpty(variables)) {
            //设置流程参数
            JSONArray array = JSONArray.parseArray(variables);
            var = new HashMap<String, Object>(array.size());
            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.getJSONObject(i);
                var.put(o.getString("name"), o.getString("value"));
                logger.info("---设置流程参数  名称:{}  值:{}", o.getString("name"), o.getString("value"));
            }
        }
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, var);
        if (instance != null) {
            logger.info("流程启动成功 {}", instance);
            return ApiResponse.success(instance.getId(), "流程启动成功");
        } else {
            logger.info("流程启动失败");
            return ApiResponse.fail("流程启动失败");
        }

    }

    /**
     * 通过bpmn模型部署流程
     *
     * @param modelId 模型ID
     * @return
     */
    @ApiOperation(value = "通过bpmnUI模型部署流程", tags = {"流程相关API"}, notes = "通过bpmnUI模型部署流程")
    @PostMapping(value = "/deployments/bpmnModel/{modelId}", produces = "application/json")
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
        return ApiResponse.success(deployment.getKey(), "部署成功");
    }

    /**
     * 部署并启动（已经部署则不用再次部署）
     */
    @ApiOperation(value = "通过bpmnUI模型部署流程并启动流程", tags = {"流程相关API"}, notes = "通过bpmnUI模型部署流程并启动流程")
    @PostMapping(value = "/start/bpmnModel/{modelId}", produces = "application/json")
    public ApiResponse deployAndStartByBpmnModel(@ApiParam(name = "modelId", value = "UI模型ID", required = true) @PathVariable String modelId,
                                                 @ApiParam(name = "businessId", value = "业务项目id", required = false) @RequestParam(required = false) String businessId,
                                                 @ApiParam(name = "assignPersons", value = "人员分配", required = false) @RequestParam(required = false) String assignPersons,
                                                 @ApiParam(name = "variables", value = "流程变量", required = false) @RequestParam(required = false) String variables) {
        //部署，可能会重复部署
        Model model = modelService.getModel(modelId);
        if (model == null)
            return ApiResponse.fail("模型不存在");
        ApiResponse response = deployByBpmnModel(modelId);
        if (!response.getState()) {
            return ApiResponse.fail(response.getMessage());
        }

        //最新的流程定义启动

        //流程变量封装
        JSONArray paramArray = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("name", "businessId");
        object.put("value", businessId);
        paramArray.add(object);

        JSONObject assignees = new JSONObject();
        assignees.put("name", "assignPersons");
        assignees.put("value", assignPersons);
        paramArray.add(assignees);


        JSONObject variable = JSONObject.parseObject(variables);
        JSONObject varObj;
        if (variable != null) {
            for (String key : variable.keySet()
            ) {
                varObj = new JSONObject();
                varObj.put("name", key);
                varObj.put("value", variable.getString(key));
                paramArray.add(varObj);
            }
        }

        String param = JSON.toJSONString(paramArray);
        ApiResponse apiResponse = startLatedByKey(model.getKey(), param, model.getDescription() + ":" + businessId);
        if (!apiResponse.getState()) {
            return ApiResponse.fail(apiResponse.getMessage());
        }
        return ApiResponse.success(apiResponse.getData(), apiResponse.getMessage());
    }

    /**
     * 删除部署
     *
     * @param deploymentId 部署ID
     * @return
     */
    @ApiOperation(value = "通过部署id删除部署", tags = {"流程相关API"}, notes = "删除部署")
    @DeleteMapping(value = "/deployments/{deploymentId}", produces = "application/json")
    public ApiResponse deleteDeploymentById(
            @ApiParam(name = "deploymentId", value = "流程部署ID") @PathVariable String deploymentId) {
        logger.info("删除部署 ID为:{}", deploymentId);
        try {
            repositoryService.deleteDeployment(deploymentId, true);
            logger.info("流程部署删除成功 ID为{}", deploymentId);
            return ApiResponse.success("{}", "删除成功");
        } catch (Exception e) {
            logger.error("删除失败.", e);
            return ApiResponse.fail("删除失败." + e.getMessage());
        }
    }

    @ApiOperation(value = "通过流程key删除相关所有部署", tags = {"流程相关API"}, notes = "通过流程key删除相关所有部署")
    @DeleteMapping(value = "/deployments/processkey/{processKey}", produces = "application/json")
    public ApiResponse deleteDepoymentByKey(@ApiParam(name = "processKey", value = "流程key") @PathVariable String processKey) {
        logger.info("删除部署 key为：{}", processKey);
        List<Deployment> list = repositoryService.createDeploymentQuery().processDefinitionKey(processKey).list();
        try {
            for (Deployment d : list) {
                repositoryService.deleteDeployment(d.getId(), true);
            }
            return ApiResponse.success(null, "删除成功，删除了" + list.size() + "条数据");
        } catch (Exception e) {
            logger.error("删除失败", e);
            return ApiResponse.fail("删除失败，原因:" + e.getMessage());
        }
    }


    /**
     * 通过模型删除部署
     *
     * @param modelId
     * @return
     */
    @ApiOperation(value = "通过模型删除部署", tags = {"流程相关API"}, notes = "通过模型删除部署")
    @DeleteMapping(value = "/model/{modelId}", produces = "application/json")
    public ApiResponse deleteProcessWithModel(
            @ApiParam(name = "modelId", value = "流程模型ID") @PathVariable String modelId) {
        logger.info("删除流程通过模型 ID：{}", modelId);
        Model model = modelService.getModel(modelId);
        if (model == null) {
            return ApiResponse.fail("删除失败，模型不存在");
        }

        List<Deployment> list = repositoryService.createDeploymentQuery().deploymentKey(model.getKey()).list();
        try {
            for (Deployment deployment : list) {
                logger.info("正在删除 {}", deployment.getId());
                repositoryService.deleteDeployment(deployment.getId(), true);
            }

            return ApiResponse.success(null, "删除成功");
        } catch (Exception e) {
            logger.error("删除失败，系统异常" + e.getMessage());
            return ApiResponse.fail("删除失败，系统异常." + e.getMessage());
        }
    }

    @ApiOperation(value = "获取流程实例列表", tags = {"流程相关API"}, notes = "获取流程实例列表")
    @GetMapping(value = "/instance", produces = "application/json")
    public List<Map<String, Object>> instance(@RequestParam(name = "businessKey", required = false) String businessKey) {
        logger.info("获取流程实例列表 业务key: {}", businessKey);
        List<ProcessInstance> list = null;
        if (!StringUtils.isEmpty(businessKey)) {
            list = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
        } else {
            list = runtimeService.createProcessInstanceQuery().list();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(list.size());
        for (ProcessInstance o : list) {
            Map<String, Object> map = new HashMap<String, Object>(3);
            map.put("id", o.getId());
            map.put("key", o.getProcessDefinitionKey());
            map.put("name", o.getProcessDefinitionName());
            map.put("definitionId", o.getProcessDefinitionId());
            result.add(map);
        }
        return result;
    }

    @ApiOperation(value = "获取流程历史列表", tags = {"流程相关API"}, notes = "获取流程历史列表")
    @GetMapping(value = "/history", produces = "application/json")
    public List<Map<String, Object>> history(@RequestParam(name = "businessKey", required = false) String businessKey) {
        logger.info("获取流程历史列表 业务key: {}", businessKey);
        List<HistoricProcessInstance> list = null;
        if (!StringUtils.isEmpty(businessKey)) {
            list = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
        } else {
            list = historyService.createHistoricProcessInstanceQuery().list();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(list.size());
        for (HistoricProcessInstance o : list) {
            Map<String, Object> map = new HashMap<String, Object>(3);
            map.put("id", o.getId());
            map.put("key", o.getProcessDefinitionKey());
            map.put("name", o.getProcessDefinitionName());
            map.put("definitionId", o.getProcessDefinitionId());
            result.add(map);
        }
        return result;
    }

    @ApiOperation(value = "删除流程实例", tags = {"流程相关API"}, notes = "删除流程实例")
    @DeleteMapping(value = "/instance/{id}", produces = "application/json")
    public ApiResponse instanceDelete(@ApiParam(name = "id", value = "流程模型ID") @PathVariable String id) {
        logger.info("删除流程实例 : {}", id);
        try {
            runtimeService.deleteProcessInstance(id, "删除");
            logger.info("删除成功");
        } catch (Exception e) {
            return ApiResponse.fail("删除失败 系统异常" + e.getMessage());
        }
        return ApiResponse.success(null, "删除成功");
    }


    @ApiOperation(value = "获取模型信息", tags = {"流程相关API"}, notes = "获取模型信息")
    @GetMapping(value = "/model/{modelId}", produces = "application/json")
    public ApiResponse getModelInfo(@ApiParam(name = "modelId", value = "流程模型ID") @PathVariable String modelId) {
        try {
            AbstractModel model = modelService.getModel(modelId);
            return ApiResponse.success(model, "成功");
        } catch (Exception e) {
            return ApiResponse.fail("失败 系统异常" + e.getMessage());
        }
    }

}
