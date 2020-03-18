package com.tyflow.controller.api;

import java.util.*;

import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.form.api.FormDefinition;
import org.flowable.form.api.FormDeployment;
import org.flowable.form.api.FormDeploymentBuilder;
import org.flowable.form.api.FormInfo;
import org.flowable.form.api.FormInstance;
import org.flowable.form.api.FormService;
import org.flowable.form.model.FormField;
import org.flowable.form.model.OptionFormField;
import org.flowable.form.model.SimpleFormModel;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.ui.modeler.model.form.FormRepresentation;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tyflow.common.FlowableHttpCall;
import com.tyflow.common.response.ApiResponse;
import com.tyflow.common.response.FlowableResponse;
import com.tyflow.controller.FlowableBaseApi;
import com.tyflow.entity.CustomFormField;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/tyapi/form")
@Api(tags = {"表单相关api"})
public class FormApi extends FlowableBaseApi {

    private static Logger logger = LoggerFactory.getLogger(FormApi.class);

    @Autowired
    private FormService formService;

    private List<HistoricVariableInstance> list;

    @ApiOperation(value = "通过 ui模型 获取表单信息", tags = {"表单相关api"}, notes = "通过 ui模型 获取表单信息")
    @GetMapping(value = "/model/{modelId}", produces = "application/json")
    public ApiResponse getFormByModelId(@ApiParam(name = "modelId", value = "UI模型ID") @PathVariable String modelId) {
        logger.info("通过模型ID获取表单：{}", modelId);
        FormRepresentation formRep = uiFormService.getForm(modelId);
        if (formRep != null) {
            return ApiResponse.success(formRep.getFormDefinition(), "获取成功");
        } else {
            return ApiResponse.fail("表单不存在");
        }
    }

    @ApiOperation(value = "通过任务和流程查询表单", tags = {"表单相关api"}, notes = "通过任务和流程查询表单")
    @GetMapping(value = "/task-process", produces = "application/json")
    public ApiResponse getFormInProcess(@RequestParam(value = "任务实例ID", required = false) String taskId,
                                        @RequestParam(value = "流程定义ID", required = false) String processDefinitionId) {
        logger.info("获取流程中的表单");
        Map<String, Object> params = new HashMap<>();
        params.put("taskId", taskId);
        params.put("processDefinitionId", processDefinitionId);

        FlowableResponse b = FlowableHttpCall.get("/form/form-data", params);
        if (b.getState()) {
            return ApiResponse.success(b.getData());
        } else {
            return ApiResponse.fail("查询失败：" + b.getMessage());
        }
    }

    @ApiOperation(value = "通过UI模型部署表单", tags = {"表单相关api"}, notes = "通过UI模型部署表单")
    @PostMapping(value = "/deploy/{modelId}", produces = "application/json")
    public ApiResponse deployForm(@ApiParam(name = "modelId", value = "UI模型ID") @PathVariable String modelId) {
        logger.info("通过模型ID部署表单：{}", modelId);
        // 从ui中的model中获取表单
        FormRepresentation formRep = uiFormService.getForm(modelId);
        // 获取json
        String formJson = "";
        try {
            formJson = objectMapper.writeValueAsString(formRep.getFormDefinition());
            logger.info("获取到表单:{}", formJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 获取流程部署对象
        FormDeploymentBuilder formBuilder = formRepositoryService.createDeployment()
                .addString(formRep.getName() + ".form", formJson).name(formRep.getName());
//		if (!StringUtils.isEmpty(processKey)) {
//			Deployment deployment = repositoryService.createDeploymentQuery().deploymentKey(processKey).singleResult();
//			formBuilder.parentDeploymentId(deployment.getId());
//		}
        // 根据json部署表单
        FormDeployment formDeployment = formBuilder.deploy();
        FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery()
                .deploymentId(formDeployment.getId()).singleResult();
        logger.info("表单部署成功:{}", formDefinition);
        return ApiResponse.success(formDefinition, "表单部署成功");
    }

    @ApiOperation(value = "删除表单部署", tags = {"表单相关api"}, notes = "删除表单部署")
    @DeleteMapping(value = "/deploy/{deploymentId}", produces = "application/json")
    public ApiResponse deleteDoployById(
            @ApiParam(name = "deploymentId", value = "表单部署ID") @PathVariable String deploymentId) {
        logger.info("正在删除表单部署 id：{}", deploymentId);
        try {
            formRepositoryService.deleteDeployment(deploymentId);
            logger.info("表单部署删除成功 id: {}", deploymentId);
            return ApiResponse.success(null, "表单部署删除成功");
        } catch (Exception e) {
            logger.error("表单部署删除失败", e);
            return ApiResponse.fail("表单部署删除失败." + e.getMessage());
        }
    }

    @ApiOperation(value = "删除表单实例", tags = {"表单相关api"}, notes = "删除表单实例")
    @DeleteMapping(value = "/instance/{formInstanceId}", produces = "application/json")
    public ApiResponse deleteInstance(
            @ApiParam(name = "formInstanceId", value = "表单实例ID") @PathVariable String formInstanceId) {
        logger.info("正在删除表单实例 id: {}", formInstanceId);
        try {
            formService.deleteFormInstance(formInstanceId);
            logger.info("表单实例删除成功 id: {}", formInstanceId);
            return ApiResponse.success(null, "表单实例删除成功");
        } catch (Exception e) {
            logger.error("表单实例删除失败", e);
            return ApiResponse.fail("表单实例删除失败." + e.getMessage());
        }
    }

    @ApiOperation(value = "通过任务获取表单字段", tags = {"表单相关api"}, notes = "通过任务获取表单字段")
    @GetMapping(value = "/field/{taskInstanceId}", produces = "application/json")
    public List<CustomFormField> getFormFields(
            @ApiParam(name = "taskInstanceId", value = "任务实例ID") @PathVariable String taskInstanceId) {
        logger.info("开始获取任务【{}】的表单字段", taskInstanceId);
        Task task = taskService.createTaskQuery().taskId(taskInstanceId).singleResult();
        String formKey;
        if (task == null) {
            HistoricTaskInstance historyTask = historyService.createHistoricTaskInstanceQuery().taskId(taskInstanceId)
                    .singleResult();
            if (historyTask == null) {
                throw new FlowableObjectNotFoundException("未找到id为" + taskInstanceId + "的任务实例");
            }
            formKey = historyTask.getFormKey();
        } else {
            formKey = task.getFormKey();
        }

        if (StringUtils.isEmpty(formKey)) {
            throw new FlowableObjectNotFoundException("id为" + taskInstanceId + "的任务没有定义表单");
        }
        //找最新版本的表单定义
        List<FormDefinition> formDefinitions = formRepositoryService.createFormDefinitionQuery().formDefinitionKey(formKey).list();
        if(formDefinitions==null||formDefinitions.size()==0){
            throw new FlowableObjectNotFoundException("表单key为" + formKey + "的表单没有部署", FormDefinition.class);
        }
        FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery().formDefinitionKey(formKey)
                .formVersion(formDefinitions.size()).singleResult();
        if (formDefinition == null) {
            throw new FlowableObjectNotFoundException("未找到formkey为" + formKey + "的部署的表单定义", FormDefinition.class);
        }
        FormInfo formInfo = formRepositoryService.getFormModelById(formDefinition.getId());
        if (formInfo == null) {
            throw new FlowableObjectNotFoundException("未找到formkey为" + formKey + "的部署的表单model", FormInfo.class);
        }
        FormInstance formInstance = formService.createFormInstanceQuery().taskId(taskInstanceId)
                .formDefinitionId(formDefinition.getId()).singleResult();
        if (formInstance == null)
            logger.info("该任务未提交过表单");

        SimpleFormModel simpleFormModel = (SimpleFormModel) formInfo.getFormModel();
        List<FormField> fields = simpleFormModel.listAllFields();

        List<CustomFormField> resultList = new ArrayList<CustomFormField>(fields.size());
        for (FormField ff : fields) {
            CustomFormField customField = new CustomFormField();
            customField.setId(ff.getId());
            customField.setName(ff.getName());
            customField.setParams(ff.getParams());
            customField.setPlaceholder(ff.getPlaceholder());
            customField.setType(ff.getType());
            customField.setReadOnly(ff.isReadOnly());
            //使hyperlink的渲染出来也是只读的
            if( customField.getType().equals("hyperlink")){
                customField.setReadOnly(true);
            }
            customField.setRequired(ff.isRequired());

            // 修改为先在execution范围拿，没找到再到流程实例范围中变量拿，还没拿到就用表单的
            HistoricVariableInstance histroyVar =
                    historyService.createHistoricVariableInstanceQuery()
                    .executionId(task.getExecutionId()).excludeTaskVariables().variableName(ff.getId()).singleResult();
            if (histroyVar == null) {
                histroyVar = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId()).executionId(task.getProcessInstanceId()).variableName(ff.getId()).singleResult();
            }
            if (histroyVar != null) {
                customField.setValue(histroyVar.getValue());
            } else {//流程变量中都没有，就设置为空
                customField.setValue("");
            }

            if (ff.getType().equals("radio-buttons") || ff.getType().equals("dropdown")) {
                OptionFormField optionFormField = (OptionFormField) ff;
                customField.setOptionList(optionFormField.getOptions());
            }
            resultList.add(customField);
        }
        //排序 需要填的放在最后
        sortFieldList(resultList);
        return resultList;
    }

    private void sortFieldList(List<CustomFormField> resultList) {
        //readonly的全部前置
        List<CustomFormField> appendList = new ArrayList<>();
        //readonly的全部前置
        for (int i = 0; i < resultList.size(); i++) {
            CustomFormField customFormField = resultList.get(i);
            if (!customFormField.isReadOnly()) {
                CustomFormField remove = resultList.remove(i--);
                appendList.add(remove);
            }
        }
        resultList.addAll(appendList);
    }

    @ApiOperation(value = "通过任务，提交表单。但是不完成任务", tags = {"表单相关api"}, notes = "通过任务，提交表单.但是不完成任务")
    @PostMapping(value = "/submit/{taskId}", produces = "application/json")
    public ApiResponse submit(@ApiParam(name = "taskId", value = "任务实例ID") @PathVariable String taskId,
                              @RequestParam(name = "variables", required = true) String variables,
                              @RequestParam(name = "formDefinitionId", required = true) String formDefinitionId) {
        logger.info("开始提交表单。任务id:{}", taskId);
        // 查找任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            logger.error("没有找到id为" + taskId + "的任务");
            return ApiResponse.fail("没有找到id为" + taskId + "的任务");
        }

        FormInfo formInfo = formRepositoryService.getFormModelById(formDefinitionId);
        if (formInfo == null) {
            logger.error("没有找到id为" + formDefinitionId + "的表单，可能没有部署");
            return ApiResponse.fail("没有找到id为" + formDefinitionId + "的表单，可能没有部署");
        }

        // 设置参数
        JSONArray array = JSONArray.parseArray(variables);
        int size = array.size();
        Map<String, Object> params = new HashMap<String, Object>(size);
        for (int i = 0; i < size; i++) {
            JSONObject o = array.getJSONObject(i);
            params.put(o.getString("name"), o.getString("value"));
        }

        FormInstance formInstance = null;
        try {
            Map<String, Object> formVariables = formService.getVariablesFromFormSubmission(formInfo, params, null);
            if (task.getProcessInstanceId() != null) {
                formInstance = formService.saveFormInstance(formVariables, formInfo, task.getId(),
                        task.getProcessInstanceId(), task.getProcessDefinitionId(), task.getTenantId());
            } else {
                formInstance = formService.saveFormInstanceWithScopeId(formVariables, formInfo, task.getId(),
                        task.getScopeId(), task.getScopeType(), task.getScopeDefinitionId(), task.getTenantId());
            }
        } catch (Exception e) {
            logger.error("表单提交失败，创建表单实例时失败", e);
            return ApiResponse.fail("表单提交失败，创建表单实例时失败" + e.getMessage());
        }

        try {
            //taskService.setVariablesLocal(task.getId(), params);
            runtimeService.setVariables(task.getProcessInstanceId(), params);
            runtimeService.setVariablesLocal(task.getExecutionId(), params);


        } catch (Exception e) {
            logger.error("表单提交失败，任务变量设置错误", e);
            return ApiResponse.fail("表单提交失败，任务变量设置错误" + e.getMessage());
        }

        return ApiResponse.success(formInstance.getId(), "表单提交成功");
    }

}
